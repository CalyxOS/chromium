// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "script_injection.h"

#include <map>
#include <utility>

#include "base/bind.h"
#include "base/feature_list.h"
#include "base/lazy_instance.h"
#include "base/metrics/histogram_macros.h"
#include "base/timer/elapsed_timer.h"
#include "base/values.h"
#include "base/logging.h"
#include "content/public/renderer/render_frame.h"
#include "content/public/renderer/render_frame_observer.h"
#include "content/public/renderer/v8_value_converter.h"
#include "../common/host_id.h"
#include "scripts_run_info.h"
#include "third_party/blink/public/platform/web_isolated_world_info.h"
#include "third_party/blink/public/platform/web_security_origin.h"
#include "third_party/blink/public/platform/web_string.h"
#include "third_party/blink/public/web/web_document.h"
#include "third_party/blink/public/web/web_local_frame.h"
#include "third_party/blink/public/web/web_script_source.h"
#include "url/gurl.h"

namespace user_scripts {

namespace {

using IsolatedWorldMap = std::map<std::string, int>;
base::LazyInstance<IsolatedWorldMap>::DestructorAtExit g_isolated_worlds =
    LAZY_INSTANCE_INITIALIZER;

const int64_t kInvalidRequestId = -1;

// Gets the isolated world ID to use for the given |injection_host|. If no
// isolated world has been created for that |injection_host| one will be created
// and initialized.
int GetIsolatedWorldIdForInstance(const InjectionHost* injection_host) {
  static int g_next_isolated_world_id = 1; // Embedder isolated worlds can use IDs in [1, 1<<29).

  IsolatedWorldMap& isolated_worlds = g_isolated_worlds.Get();

  int id = 0;
  const std::string& key = injection_host->id().id();
  auto iter = isolated_worlds.find(key);
  if (iter != isolated_worlds.end()) {
    id = iter->second;
  } else {
    id = g_next_isolated_world_id++;
    // This map will tend to pile up over time, but realistically, you're never
    // going to have enough injection hosts for it to matter.
    isolated_worlds[key] = id;
  }

  blink::WebIsolatedWorldInfo info;
  info.security_origin =
      blink::WebSecurityOrigin::Create(injection_host->url());
  info.human_readable_name = blink::WebString::FromUTF8(injection_host->name());
  info.stable_id = blink::WebString::FromUTF8(key);

  const std::string* csp = injection_host->GetContentSecurityPolicy();
  if (csp)
    info.content_security_policy = blink::WebString::FromUTF8(*csp);

  // Even though there may be an existing world for this |injection_host|'s key,
  // the properties may have changed (e.g. due to an extension update).
  // Overwrite any existing entries.
  blink::SetIsolatedWorldInfo(id, info);

  return id;
}

}  // namespace

// Watches for the deletion of a RenderFrame, after which is_valid will return
// false.
class ScriptInjection::FrameWatcher : public content::RenderFrameObserver {
 public:
  FrameWatcher(const FrameWatcher&) = delete;
  FrameWatcher& operator=(const FrameWatcher&) = delete;
  FrameWatcher(content::RenderFrame* render_frame,
               ScriptInjection* injection)
      : content::RenderFrameObserver(render_frame),
        injection_(injection) {}
  ~FrameWatcher() override {}

 private:
  void WillDetach() override { injection_->invalidate_render_frame(); }
  void OnDestruct() override { injection_->invalidate_render_frame(); }

  ScriptInjection* injection_;
};

// static
std::string ScriptInjection::GetHostIdForIsolatedWorld(int isolated_world_id) {
  const IsolatedWorldMap& isolated_worlds = g_isolated_worlds.Get();

  for (const auto& iter : isolated_worlds) {
    if (iter.second == isolated_world_id)
      return iter.first;
  }
  return std::string();
}

// static
void ScriptInjection::RemoveIsolatedWorld(const std::string& host_id) {
  g_isolated_worlds.Get().erase(host_id);
}

ScriptInjection::ScriptInjection(
    std::unique_ptr<ScriptInjector> injector,
    content::RenderFrame* render_frame,
    std::unique_ptr<const InjectionHost> injection_host,
    UserScript::RunLocation run_location,
    bool log_activity)
    : injector_(std::move(injector)),
      render_frame_(render_frame),
      injection_host_(std::move(injection_host)),
      run_location_(run_location),
      request_id_(kInvalidRequestId),
      complete_(false),
      did_inject_js_(false),
      log_activity_(log_activity),
      frame_watcher_(new FrameWatcher(render_frame, this)) {
  CHECK(injection_host_.get());
}

ScriptInjection::~ScriptInjection() {
  if (!complete_)
    NotifyWillNotInject(ScriptInjector::WONT_INJECT);
}

ScriptInjection::InjectionResult ScriptInjection::TryToInject(
    UserScript::RunLocation current_location,
    ScriptsRunInfo* scripts_run_info,
    CompletionCallback async_completion_callback) {
  if (current_location < run_location_)
    return INJECTION_WAITING;  // Wait for the right location.

  if (request_id_ != kInvalidRequestId) {
    // We're waiting for permission right now, try again later.
    return INJECTION_WAITING;
  }

  if (!injection_host_) {
    NotifyWillNotInject(ScriptInjector::EXTENSION_REMOVED);
    return INJECTION_FINISHED;  // We're done.
  }

  InjectionResult result = Inject(scripts_run_info);
  // If the injection is blocked, we need to set the manager so we can
  // notify it upon completion.
  if (result == INJECTION_BLOCKED)
    async_completion_callback_ = std::move(async_completion_callback);
  return result;
}

ScriptInjection::InjectionResult ScriptInjection::OnPermissionGranted(
    ScriptsRunInfo* scripts_run_info) {
  if (!injection_host_) {
    NotifyWillNotInject(ScriptInjector::EXTENSION_REMOVED);
    return INJECTION_FINISHED;
  }

  return Inject(scripts_run_info);
}

void ScriptInjection::OnHostRemoved() {
  injection_host_.reset(nullptr);
}

void ScriptInjection::NotifyWillNotInject(
    ScriptInjector::InjectFailureReason reason) {
  complete_ = true;
  injector_->OnWillNotInject(reason, render_frame_);
}

ScriptInjection::InjectionResult ScriptInjection::Inject(
    ScriptsRunInfo* scripts_run_info) {
  DCHECK(injection_host_);
  //DCHECK(scripts_run_info);
  DCHECK(!complete_);
  bool should_inject_js = injector_->ShouldInjectJs(
      run_location_, scripts_run_info->executing_scripts[host_id().id()]);
  bool should_inject_css = injector_->ShouldInjectCss(
      run_location_, scripts_run_info->injected_stylesheets[host_id().id()]);

  // This can happen if the extension specified a script to
  // be run in multiple rules, and the script has already run.
  // See crbug.com/631247.
  if (!should_inject_js && !should_inject_css) {
    return INJECTION_FINISHED;
  }

  if (should_inject_js)
    InjectJs(&(scripts_run_info->executing_scripts[host_id().id()]),
             &(scripts_run_info->num_js));
  if (should_inject_css)
    InjectCss(&(scripts_run_info->injected_stylesheets[host_id().id()]),
              &(scripts_run_info->num_css));

  complete_ = did_inject_js_ || !should_inject_js;

  if (complete_) {
    injector_->OnInjectionComplete(std::move(execution_result_), run_location_,
                                   render_frame_);
  } else {
    ++scripts_run_info->num_blocking_js;
  }

  return complete_ ? INJECTION_FINISHED : INJECTION_BLOCKED;
}

void ScriptInjection::InjectJs(std::set<std::string>* executing_scripts,
                               size_t* num_injected_js_scripts) {
  DCHECK(!did_inject_js_);
  std::vector<blink::WebScriptSource> sources = injector_->GetJsSources(
      run_location_, executing_scripts, num_injected_js_scripts);
  DCHECK(!sources.empty());
  int world_id = GetIsolatedWorldIdForInstance(injection_host_.get());

  base::ElapsedTimer exec_timer;

  // For content scripts executing during page load, we run them asynchronously
  // in order to reduce UI jank experienced by the user. (We don't do this for
  // DOCUMENT_START scripts, because there's no UI to jank until after those
  // run, so we run them as soon as we can.)
  // Note: We could potentially also run deferred and browser-driven scripts
  // asynchronously; however, these are rare enough that there probably isn't
  // UI jank. If this changes, we can update this.
  bool should_execute_asynchronously =
      injector_->script_type() == UserScript::CONTENT_SCRIPT &&
      (run_location_ == UserScript::DOCUMENT_END ||
       run_location_ == UserScript::DOCUMENT_IDLE);
  blink::mojom::EvaluationTiming execution_option =
      should_execute_asynchronously
          ? blink::mojom::EvaluationTiming::kAsynchronous
          : blink::mojom::EvaluationTiming::kSynchronous;

  render_frame_->GetWebFrame()->RequestExecuteScript(
      world_id, sources, blink::mojom::UserActivationOption::kDoNotActivate,
      execution_option,
      blink::mojom::LoadEventBlockingOption::kBlock,
      base::BindOnce(&ScriptInjection::OnJsInjectionCompleted,
                     weak_ptr_factory_.GetWeakPtr()),
      blink::BackForwardCacheAware::kPossiblyDisallow,
      blink::mojom::WantResultOption::kNoResult, blink::mojom::PromiseResultOption::kDoNotWait);
  }

void ScriptInjection::OnJsInjectionCompleted(
    absl::optional<base::Value> results,
    base::TimeTicks start_time) {
  DCHECK(!did_inject_js_);

  execution_result_ = std::move(results);
  did_inject_js_ = true;

  // If |async_completion_callback_| is set, it means the script finished
  // asynchronously, and we should run it.
  if (!async_completion_callback_.is_null()) {
    complete_ = true;
    injector_->OnInjectionComplete(std::move(execution_result_), run_location_,
                                   render_frame_);
    // Warning: this object can be destroyed after this line!
    std::move(async_completion_callback_).Run(this);
  }
}

void ScriptInjection::InjectCss(std::set<std::string>* injected_stylesheets,
                                size_t* num_injected_stylesheets) {
  std::vector<blink::WebString> css_sources = injector_->GetCssSources(
      run_location_, injected_stylesheets, num_injected_stylesheets);
  blink::WebLocalFrame* web_frame = render_frame_->GetWebFrame();
  // Default CSS origin is "author", but can be overridden to "user" by scripts.
  absl::optional<CSSOrigin> css_origin = injector_->GetCssOrigin();
  blink::WebCssOrigin blink_css_origin =
      css_origin && *css_origin == CSS_ORIGIN_USER
          ? blink::WebCssOrigin::kUser
          : blink::WebCssOrigin::kAuthor;
  blink::WebStyleSheetKey style_sheet_key;
  if (const absl::optional<std::string>& injection_key =
          injector_->GetInjectionKey())
    style_sheet_key = blink::WebString::FromASCII(*injection_key);
  for (const blink::WebString& css : css_sources)
    web_frame->GetDocument().InsertStyleSheet(css, &style_sheet_key,
                                              blink_css_origin);
}

}  // namespace extensions
