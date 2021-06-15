// Copyright 2012 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "chrome/browser/ui/webui/crashes_ui.h"

#include <stddef.h>

#include <memory>
#include <string>
#include <vector>

#include "base/functional/bind.h"
#include "base/functional/callback_helpers.h"
#include "base/memory/ref_counted_memory.h"
#include "base/strings/utf_string_conversions.h"
#include "base/system/sys_info.h"
#include "base/values.h"
#include "build/build_config.h"
#include "build/chromeos_buildflags.h"
#include "chrome/browser/crash_upload_list/crash_upload_list.h"
#include "chrome/browser/metrics/chrome_metrics_service_accessor.h"
#include "chrome/browser/metrics/metrics_reporting_state.h"
#include "chrome/browser/profiles/profile.h"
#include "chrome/browser/signin/identity_manager_factory.h"
#include "chrome/common/url_constants.h"
#include "chrome/grit/branded_strings.h"
#include "components/crash/core/browser/crashes_ui_util.h"
#include "components/grit/components_scaled_resources.h"
#include "components/grit/dev_ui_components_resources.h"
#include "components/signin/public/identity_manager/identity_manager.h"
#include "components/version_info/version_info.h"
#include "content/public/browser/web_contents.h"
#include "content/public/browser/web_ui.h"
#include "content/public/browser/web_ui_data_source.h"
#include "content/public/browser/web_ui_message_handler.h"
#include "google_apis/gaia/gaia_auth_util.h"
#include "ui/base/resource/resource_bundle.h"

#include "base/logging.h"
#include "base/debug/dump_without_crashing.h"
#include "base/files/file_util.h"
#include "base/files/file_enumerator.h"
#include "base/files/scoped_temp_dir.h"
#include "base/task/task_traits.h"
#include "base/task/thread_pool.h"
#if BUILDFLAG(IS_ANDROID)
#include "base/android/path_utils.h"
#endif
#include "net/base/filename_util.h"
#include "third_party/zlib/google/zip.h"

#if BUILDFLAG(IS_CHROMEOS_ASH)
#include "chromeos/ash/components/dbus/debug_daemon/debug_daemon_client.h"
#endif

#if BUILDFLAG(IS_LINUX) || BUILDFLAG(IS_CHROMEOS)
#include "components/crash/core/app/crashpad.h"
#endif

using content::WebContents;
using content::WebUIMessageHandler;

namespace {

void CreateAndAddCrashesUIHTMLSource(Profile* profile) {
  content::WebUIDataSource* source = content::WebUIDataSource::CreateAndAdd(
      profile, chrome::kChromeUICrashesHost);

  for (size_t i = 0; i < crash_reporter::kCrashesUILocalizedStringsCount; ++i) {
    source->AddLocalizedString(
        crash_reporter::kCrashesUILocalizedStrings[i].name,
        crash_reporter::kCrashesUILocalizedStrings[i].resource_id);
  }

  source->AddLocalizedString(crash_reporter::kCrashesUIShortProductName,
                             IDS_SHORT_PRODUCT_NAME);
  source->UseStringsJs();
  source->AddResourcePath(crash_reporter::kCrashesUICrashesJS,
                          IDR_CRASH_CRASHES_JS);
  source->AddResourcePath(crash_reporter::kCrashesUICrashesCSS,
                          IDR_CRASH_CRASHES_CSS);
  source->AddResourcePath(crash_reporter::kCrashesUISadTabSVG,
                          IDR_CRASH_SADTAB_SVG);
  source->SetDefaultResource(IDR_CRASH_CRASHES_HTML);
}

constexpr base::TaskTraits kLoadingTaskTraits = {
    base::MayBlock(), base::TaskPriority::USER_BLOCKING,
    base::TaskShutdownBehavior::CONTINUE_ON_SHUTDOWN};

////////////////////////////////////////////////////////////////////////////////
//
// CrashesDOMHandler
//
////////////////////////////////////////////////////////////////////////////////

// The handler for Javascript messages for the chrome://crashes/ page.
class CrashesDOMHandler : public WebUIMessageHandler {
 public:
  CrashesDOMHandler(content::WebContents* web_contents);

  CrashesDOMHandler(const CrashesDOMHandler&) = delete;
  CrashesDOMHandler& operator=(const CrashesDOMHandler&) = delete;

  ~CrashesDOMHandler() override;

  // WebUIMessageHandler implementation.
  void RegisterMessages() override;

 private:
  void OnUploadListAvailable();

  // Asynchronously fetches the list of crashes. Called from JS.
  void HandleRequestCrashes(const base::Value::List& args);

  void RequestCrashesList();

#if BUILDFLAG(IS_CHROMEOS_ASH)
  // Asynchronously triggers crash uploading. Called from JS.
  void HandleRequestUploads(const base::Value::List& args);
#endif

  // Sends the recent crashes list JS.
  void UpdateUI();

  // Asynchronously requests a user triggered upload. Called from JS.
  void HandleRequestSingleCrashUpload(const base::Value::List& args);

  std::string RequestSingleUpload(const std::string& local_id) const;
  void RequestSingleUploadCallback(const std::string& local_id, const std::string& filename);

  // Asynchronously requests a user log extraction. Called from JS.
  void HandleRequestNewExtraction(const base::Value::List& args);
  void RequestNewExtraction();

  // Requests remove all crash files. Called from JS.
  void HandleRequestClearAll(const base::Value::List& args);
  void ClearAll();

  scoped_refptr<UploadList> upload_list_;
  bool list_available_;
  bool first_load_;
  raw_ptr<content::WebContents> web_contents_;
};

CrashesDOMHandler::CrashesDOMHandler(content::WebContents* web_contents)
    : list_available_(false), first_load_(true),
      web_contents_(web_contents) {
  upload_list_ = CreateCrashUploadList();
#if !BUILDFLAG(IS_ANDROID)
  web_contents_ = nullptr;
#endif
}

CrashesDOMHandler::~CrashesDOMHandler() {
  upload_list_->CancelLoadCallback();
}

void CrashesDOMHandler::RegisterMessages() {
  upload_list_->Load(base::BindOnce(&CrashesDOMHandler::OnUploadListAvailable,
                                    base::Unretained(this)));
  web_ui()->RegisterMessageCallback(
      crash_reporter::kCrashesUIRequestCrashList,
      base::BindRepeating(&CrashesDOMHandler::HandleRequestCrashes,
                          base::Unretained(this)));

#if BUILDFLAG(IS_CHROMEOS_ASH)
  web_ui()->RegisterMessageCallback(
      crash_reporter::kCrashesUIRequestCrashUpload,
      base::BindRepeating(&CrashesDOMHandler::HandleRequestUploads,
                          base::Unretained(this)));
#endif

  web_ui()->RegisterMessageCallback(
      crash_reporter::kCrashesUIRequestSingleCrashUpload,
      base::BindRepeating(&CrashesDOMHandler::HandleRequestSingleCrashUpload,
                          base::Unretained(this)));

  web_ui()->RegisterMessageCallback(
      crash_reporter::kCrashesUIHandleClearAll,
      base::BindRepeating(&CrashesDOMHandler::HandleRequestClearAll,
                          base::Unretained(this)));

  web_ui()->RegisterMessageCallback(
      crash_reporter::kCrashesUIHandleRequestNewExtraction,
      base::BindRepeating(&CrashesDOMHandler::HandleRequestNewExtraction,
                          base::Unretained(this)));
}

void CrashesDOMHandler::HandleRequestCrashes(const base::Value::List& args) {
  AllowJavascript();
  RequestCrashesList();
}

void CrashesDOMHandler::RequestCrashesList() {
  if (first_load_) {
    first_load_ = false;
    if (list_available_)
      UpdateUI();
  } else {
    list_available_ = false;
    upload_list_->Load(base::BindOnce(&CrashesDOMHandler::OnUploadListAvailable,
                                      base::Unretained(this)));
  }
}

#if BUILDFLAG(IS_CHROMEOS_ASH)
void CrashesDOMHandler::HandleRequestUploads(const base::Value::List& args) {
  ash::DebugDaemonClient* debugd_client = ash::DebugDaemonClient::Get();
  DCHECK(debugd_client);

  debugd_client->UploadCrashes(base::BindOnce([](bool success) {
    if (!success) {
      LOG(WARNING) << "crash_sender failed or timed out";
    }
  }));
}
#endif

void CrashesDOMHandler::OnUploadListAvailable() {
  list_available_ = true;
  if (!first_load_)
    UpdateUI();
}

void CrashesDOMHandler::UpdateUI() {
  bool crash_reporting_enabled = true;

  bool system_crash_reporter = false;
#if BUILDFLAG(IS_CHROMEOS)
  // Chrome OS has a system crash reporter.
  system_crash_reporter = true;
#endif

  bool is_internal = false;
  auto* identity_manager =
      IdentityManagerFactory::GetForProfile(Profile::FromWebUI(web_ui()));
  if (identity_manager) {
    is_internal = gaia::IsGoogleInternalAccountEmail(
        identity_manager->GetPrimaryAccountInfo(signin::ConsentLevel::kSignin)
            .email);
  }

  bool manual_uploads_supported = false;
#if BUILDFLAG(IS_WIN) || BUILDFLAG(IS_MAC) || BUILDFLAG(IS_LINUX) || \
    BUILDFLAG(IS_ANDROID)
  manual_uploads_supported = true;
#endif
  bool allow_manual_uploads =
      manual_uploads_supported &&
      (crash_reporting_enabled || !IsMetricsReportingPolicyManaged());

  // Show crash reports regardless of |crash_reporting_enabled| when it is
  // possible to manually upload reports.
  bool upload_list = manual_uploads_supported || crash_reporting_enabled;

  base::Value::List crash_list;
  if (upload_list)
    crash_reporter::UploadListToValue(upload_list_.get(), &crash_list);

  base::Value::Dict result;
  result.Set("enabled", crash_reporting_enabled);
  result.Set("dynamicBackend", system_crash_reporter);
  result.Set("manualUploads", allow_manual_uploads);
  result.Set("crashes", std::move(crash_list));
  result.Set("version", version_info::GetVersionNumber());
  result.Set("os", base::SysInfo::OperatingSystemName() + " " +
                       base::SysInfo::OperatingSystemVersion());
  result.Set("isGoogleAccount", is_internal);
  FireWebUIListener(crash_reporter::kCrashesUIUpdateCrashList, result);
}

void CrashesDOMHandler::HandleRequestSingleCrashUpload(
    const base::Value::List& args) {
  std::string local_id = args[0].GetString();
  base::ThreadPool::PostTaskAndReplyWithResult(
      FROM_HERE, kLoadingTaskTraits,
      base::BindOnce(&CrashesDOMHandler::RequestSingleUpload, base::Unretained(this), local_id),
      base::BindOnce(&CrashesDOMHandler::RequestSingleUploadCallback, base::Unretained(this), local_id));
}

std::string CrashesDOMHandler::RequestSingleUpload(const std::string& local_id) const {
#if BUILDFLAG(IS_ANDROID)
  // get crash file path
  std::string info_file_path = upload_list_->GetFilePathByLocalId(local_id);
  if (info_file_path.empty()) {
    LOG(ERROR) << "Crash report: file path is not set for " << local_id;
    return std::string();
  }
  base::FilePath crash_file_path(info_file_path);

  // get android crash report dir
  base::FilePath cache_dir;
  base::android::GetCacheDirectory(&cache_dir);
  base::FilePath upload_log_path = cache_dir.Append("Crash Reports");

  // crash reports can have multiple extensions (e.g. foo.dmp, foo.dmp.try1,
  // foo.skipped.try0), remove it
  base::FilePath zip_file_name = crash_file_path;
  while (zip_file_name != zip_file_name.RemoveExtension())
    zip_file_name = zip_file_name.RemoveExtension();

  // make zip file name, like "ec708a7b-cb17-44e7-8dae-e32f6c45cb8c.zip"
  zip_file_name = upload_log_path.Append(zip_file_name.BaseName())
                            .AddExtensionASCII(".zip");
  // since the download is always allowed, the generation takes place only
  // at the first request, so if exists return it
  if (base::PathExists(zip_file_name))
    return zip_file_name.value();

  // original code remove the file immediately after upload.
  // we changed this behavior but it is still possible that the file no longer exists
  // because in uploads.log it could be indicated but the file was deleted by self-cleaning
  if (!base::PathExists(crash_file_path)) {
    LOG(ERROR) << "Crash report: file " << crash_file_path
               << " no more available";
    return std::string();
  }

  std::vector<base::FilePath> files_list;
  files_list.push_back(crash_file_path.BaseName());

  // open zip file
  base::File zip_f(zip_file_name,
                      base::File::FLAG_CREATE | base::File::FLAG_WRITE);
  auto result = zip::ZipFiles(crash_file_path.DirName(), files_list, zip_f.GetPlatformFile());
  zip_f.Close();
  if (result) {
    return zip_file_name.value();
  }
#endif
  LOG(ERROR) << "Crash report: cannot create zip content";
  return std::string();
}

void CrashesDOMHandler::RequestSingleUploadCallback(const std::string& local_id,
                                                    const std::string& file_name) {
#if BUILDFLAG(IS_ANDROID)
  if (!file_name.empty()) {
    upload_list_->RequestSingleUploadAsync(local_id);

    base::FilePath file_path(file_name);
    web_contents_->GetController().LoadURL(
        net::FilePathToFileURL(file_path), {}, {}, {});
  }
#endif
}

void CrashesDOMHandler::HandleRequestNewExtraction(
    const base::Value::List& args) {
  base::ThreadPool::PostTask(
      FROM_HERE, kLoadingTaskTraits,
      base::BindOnce(&CrashesDOMHandler::RequestNewExtraction, base::Unretained(this)));
}

void CrashesDOMHandler::RequestNewExtraction() {
  base::debug::DumpWithoutCrashing();
  // ask java to get file from crashpad and to add logcat
  upload_list_->RequestNewExtraction();
}

void CrashesDOMHandler::HandleRequestClearAll(
    const base::Value::List& args) {
  base::ThreadPool::PostTaskAndReply(
      FROM_HERE, kLoadingTaskTraits,
      base::BindOnce(&CrashesDOMHandler::ClearAll, base::Unretained(this)),
      base::BindOnce(&CrashesDOMHandler::RequestCrashesList, base::Unretained(this)));
}

void CrashesDOMHandler::ClearAll() {
#if BUILDFLAG(IS_ANDROID)
  // get android crash report dir
  base::FilePath cache_dir;
  base::android::GetCacheDirectory(&cache_dir);
  base::FilePath upload_log_path = cache_dir.Append("Crash Reports");

  base::FileEnumerator dir_enum(
    upload_log_path,
    /*recursive=*/false, base::FileEnumerator::FILES);
  base::FilePath full_name;
  while (full_name = dir_enum.Next(), !full_name.empty()) {
    // remove all files, don't care for result
    base::DeleteFile(full_name);
   }
#endif
}

}  // namespace

///////////////////////////////////////////////////////////////////////////////
//
// CrashesUI
//
///////////////////////////////////////////////////////////////////////////////

CrashesUI::CrashesUI(content::WebUI* web_ui) : WebUIController(web_ui) {
  web_ui->AddMessageHandler(std::make_unique<CrashesDOMHandler>(
      web_ui->GetWebContents()));

  // Set up the chrome://crashes/ source.
  CreateAndAddCrashesUIHTMLSource(Profile::FromWebUI(web_ui));
}

// static
base::RefCountedMemory* CrashesUI::GetFaviconResourceBytes(
    ui::ResourceScaleFactor scale_factor) {
  return ui::ResourceBundle::GetSharedInstance().LoadDataResourceBytesForScale(
      IDR_CRASH_SAD_FAVICON, scale_factor);
}
