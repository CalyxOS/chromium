/*
    This file is part of Bromite.

    Bromite is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Bromite is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Bromite. If not, see <https://www.gnu.org/licenses/>.
*/

#include "chrome/browser/ui/webui/proxy_config_ui.h"

#include <stdint.h>

#include <memory>
#include <string>
#include <vector>

#include "base/bind.h"
#include "base/command_line.h"
#include "base/lazy_instance.h"
#include "base/memory/ref_counted.h"
#include "base/strings/string_util.h"
#include "base/strings/utf_string_conversions.h"
#include "base/values.h"
#include "chrome/browser/browser_process.h"
#include "chrome/browser/net/proxy_service_factory.h"
#include "chrome/browser/platform_util.h"
#include "chrome/browser/profiles/profile.h"
#include "chrome/common/url_constants.h"
#include "chrome/grit/browser_resources.h"
#include "components/prefs/pref_service.h"
#include "components/proxy_config/pref_proxy_config_tracker_impl.h"
#include "components/proxy_config/proxy_config_pref_names.h"
#include "components/grit/components_resources.h"
#include "content/public/browser/browser_thread.h"
#include "content/public/browser/url_data_source.h"
#include "content/public/browser/web_contents.h"
#include "content/public/browser/web_ui.h"
#include "content/public/browser/web_ui_data_source.h"
#include "content/public/browser/web_ui_message_handler.h"

#include "url/gurl.h"

using content::BrowserThread;
using content::WebContents;
using content::WebUIMessageHandler;

namespace {

content::WebUIDataSource* CreateProxyConfigHTMLSource() {
  content::WebUIDataSource* source =
      content::WebUIDataSource::Create(chrome::kChromeUIProxyConfigHost);

  source->UseStringsJs();
  source->AddResourcePath("proxy_config.js", IDR_PROXY_CONFIG_JS);
  source->SetDefaultResource(IDR_PROXY_CONFIG_HTML);
  return source;
}

// This class receives javascript messages from the renderer.
// Note that the WebUI infrastructure runs on the UI thread, therefore all of
// this class's public methods are expected to run on the UI thread.
class ProxyConfigMessageHandler
    : public WebUIMessageHandler,
      public base::SupportsWeakPtr<ProxyConfigMessageHandler>,
      public net::ProxyConfigService::Observer {
 public:
  ProxyConfigMessageHandler(const ProxyConfigMessageHandler&) = delete;
  ProxyConfigMessageHandler& operator=(const ProxyConfigMessageHandler&) = delete;
  // Creates a ProxyConfigMessageHandler that handles message exchanges with the Javascript
  // side of the UI and gets proxy settings from the Web UI associated profile to watch for changes.
  // The created ProxyConfigMessageHandler must be destroyed before |profile|.
  ProxyConfigMessageHandler(Profile *profile);
  ~ProxyConfigMessageHandler() override;

  // WebUIMessageHandler implementation.
  void RegisterMessages() override;

  // Messages
  void OnEnableNotifyUIWithState(const base::Value::List& args);
  void OnApply(const base::Value::List& args);
  void OnClear(const base::Value::List& args);

  // net::ProxyConfigService::Observer implementation:
  // Calls ProxyConfigView.onProxyConfigChanged JavaScript function in the
  // renderer.
  void OnProxyConfigChanged(
    const net::ProxyConfigWithAnnotation& config,
    net::ProxyConfigService::ConfigAvailability availability) override;

 private:
  // Not owned.
  PrefService *pref_service_;
  std::unique_ptr<net::ProxyConfigService> proxy_config_service_;
  // Monitors global and Profile prefs related to proxy configuration.
  std::unique_ptr<PrefProxyConfigTracker> pref_proxy_config_tracker_;
  bool is_observing_;

  void encodeConfig(const net::ProxyConfig& config, base::DictionaryValue& state);

  void apply(const net::ProxyConfig& config);

  base::WeakPtrFactory<ProxyConfigMessageHandler> weak_ptr_factory_;
};

ProxyConfigMessageHandler::ProxyConfigMessageHandler(Profile *profile)
    :
      weak_ptr_factory_(this) {

  // used to set new configuration preferences
  pref_service_ = g_browser_process->local_state();
  // observer is explicitly added only later in enableNotifyUIWithState
  is_observing_ = false;

// If this is the ChromeOS sign-in profile, just create the tracker from global
// state.
#if defined(OS_CHROMEOS)
  if (chromeos::ProfileHelper::IsSigninProfile(profile)) {
    pref_proxy_config_tracker_.reset(
        ProxyServiceFactory::CreatePrefProxyConfigTrackerOfLocalState(
            g_browser_process->local_state()));
  }
#endif  // defined(OS_CHROMEOS)

  if (!pref_proxy_config_tracker_) {
    pref_proxy_config_tracker_ =
        ProxyServiceFactory::CreatePrefProxyConfigTrackerOfProfile(
            profile->GetPrefs(), g_browser_process->local_state());
  }

  proxy_config_service_ = ProxyServiceFactory::CreateProxyConfigService(
      pref_proxy_config_tracker_.get(), nullptr);
}

void ProxyConfigMessageHandler::OnProxyConfigChanged(
    const net::ProxyConfigWithAnnotation& config,
    net::ProxyConfigService::ConfigAvailability availability) {
  DCHECK(BrowserThread::CurrentlyOn(BrowserThread::UI) ||
         !BrowserThread::IsThreadInitialized(BrowserThread::UI));

  base::DictionaryValue state;
  bool pending = false;
  switch (availability) {
    case net::ProxyConfigService::CONFIG_VALID:
      encodeConfig(config.value(), state);
      break;
    case net::ProxyConfigService::CONFIG_UNSET:
      state.SetPath({"config", "rules", "type"}, base::Value("none"));
      break;
    case net::ProxyConfigService::CONFIG_PENDING:
      //NOTE: this can only happen when triggered manually first time
      pending = true;
      break;
  }
  state.SetKey("pending", base::Value(pending));

  // call Javascript function
  web_ui()->CallJavascriptFunctionUnsafe("ProxyConfigView.getInstance().onProxyConfigChanged",
                                         *state.CreateDeepCopy());
}

const std::string omitDirect(const std::string pacString) {
  if (pacString == "DIRECT") {
    return "";
  }
  return pacString;
}

void ProxyConfigMessageHandler::encodeConfig(const net::ProxyConfig& config, base::DictionaryValue& state) {
  // when automatic settings are enabled they take precedence over manual settings
  // automatic settings are either the "auto-detect" flag or the existance of a PAC URL

  state.SetPath({"config", "auto_detect"}, base::Value(config.auto_detect()));

  auto rules = config.proxy_rules();
  if (config.has_pac_url()) {
    state.SetPath({"config", "pac_url"}, base::Value(config.pac_url().spec()));
    state.SetPath({"config", "pac_mandatory"}, base::Value(config.pac_mandatory()));
    state.SetPath({"config", "rules", "type"}, base::Value("none"));
    state.SetPath({"config", "rules", "bypass_rules"}, base::Value(rules.bypass_rules.ToString()));
    state.SetPath({"config", "rules", "reverse_bypass"}, base::Value(rules.reverse_bypass));
    return;
  }

  const char *type;
  switch (rules.type) {
    case net::ProxyConfig::ProxyRules::Type::EMPTY:
      type = "direct";
      break;
    case net::ProxyConfig::ProxyRules::Type::PROXY_LIST:
      type = "list";

      state.SetPath({"config", "rules", "single_proxies"}, base::Value(omitDirect(rules.single_proxies.ToPacString())));
      break;
    case net::ProxyConfig::ProxyRules::Type::PROXY_LIST_PER_SCHEME:
      type = "list_per_scheme";

      state.SetPath({"config", "rules", "proxies_for_http"}, base::Value(omitDirect(rules.proxies_for_http.ToPacString())));
      state.SetPath({"config", "rules", "proxies_for_https"}, base::Value(omitDirect(rules.proxies_for_https.ToPacString())));
      state.SetPath({"config", "rules", "proxies_for_ftp"}, base::Value(omitDirect(rules.proxies_for_ftp.ToPacString())));
      state.SetPath({"config", "rules", "fallback_proxies"}, base::Value(omitDirect(rules.fallback_proxies.ToPacString())));
      break;
    default:
     NOTREACHED();
     break;
  }
  state.SetPath({"config", "rules", "type"}, base::Value(type));
  state.SetPath({"config", "rules", "bypass_rules"}, base::Value(rules.bypass_rules.ToString()));
  state.SetPath({"config", "rules", "reverse_bypass"}, base::Value(rules.reverse_bypass));
}

ProxyConfigMessageHandler::~ProxyConfigMessageHandler() {
  DCHECK(BrowserThread::CurrentlyOn(BrowserThread::UI) ||
         !BrowserThread::IsThreadInitialized(BrowserThread::UI));
  if (is_observing_) {
    proxy_config_service_->RemoveObserver(this);
  }
  pref_proxy_config_tracker_->DetachFromPrefService();
}

void ProxyConfigMessageHandler::RegisterMessages() {
  DCHECK_CURRENTLY_ON(BrowserThread::UI);

  web_ui()->RegisterMessageCallback(
      "enableNotifyUIWithState",
      base::BindRepeating(&ProxyConfigMessageHandler::OnEnableNotifyUIWithState,
                          base::Unretained(this)));
  web_ui()->RegisterMessageCallback(
      "apply",
      base::BindRepeating(&ProxyConfigMessageHandler::OnApply,
                          base::Unretained(this)));
  web_ui()->RegisterMessageCallback(
      "clear",
      base::BindRepeating(&ProxyConfigMessageHandler::OnClear,
                          base::Unretained(this)));
}

// The proxy configuration UI is not notified of state changes until this function runs.
// After this function, OnProxyConfigChanged() will be called on all proxy state changes.
void ProxyConfigMessageHandler::OnEnableNotifyUIWithState(
    const base::Value::List& list) {
  DCHECK_CURRENTLY_ON(BrowserThread::UI);

  if (!is_observing_) {
    is_observing_ = true;
    proxy_config_service_->AddObserver(this);
  }

  net::ProxyConfigWithAnnotation config;
  auto availability = proxy_config_service_->GetLatestProxyConfig(&config);

  const PrefService::Preference* const pref =
      pref_service_->FindPreference(proxy_config::prefs::kProxy);
  ProxyConfigDictionary proxy_dict(pref->GetValue()->GetDict().Clone());
  ProxyPrefs::ProxyMode mode;
  if (!proxy_dict.GetMode(&mode) || mode == ProxyPrefs::MODE_SYSTEM) {
    availability = net::ProxyConfigService::CONFIG_UNSET;
  }

  OnProxyConfigChanged(config, availability);
}

void ProxyConfigMessageHandler::OnClear(const base::Value::List& list) {
  DCHECK_CURRENTLY_ON(BrowserThread::UI);

  const base::Value::Dict cfg = ProxyConfigDictionary::CreateSystem();
  pref_service_->SetDict(proxy_config::prefs::kProxy, cfg.Clone());
  pref_service_->CommitPendingWrite();
  OnEnableNotifyUIWithState(list);
}

void ProxyConfigMessageHandler::OnApply(const base::Value::List& list) {
  DCHECK_CURRENTLY_ON(BrowserThread::UI);

  if ((list.size() != 1) || !list[0].is_dict()) {
    return;
  }

  const base::DictionaryValue* config = nullptr;
  if (!list[0].GetAsDictionary(&config))
    return;

  const base::Value *autoDetect = config->FindKeyOfType("auto_detect", base::Value::Type::BOOLEAN);
  if (autoDetect == nullptr)
    return;

  if (autoDetect->GetBool()) {
    apply(net::ProxyConfig::CreateAutoDetect());
    return;
  }

  const base::Value *pacURL = config->FindKeyOfType("pac_url", base::Value::Type::STRING);
  if (pacURL != nullptr) {
    const base::Value *pacMandatory = config->FindKeyOfType("pac_mandatory", base::Value::Type::BOOLEAN);
    if (pacMandatory == nullptr)
      return;
    auto proxyConfig = net::ProxyConfig::CreateFromCustomPacURL(GURL(pacURL->GetString()));
    proxyConfig.set_pac_mandatory(pacMandatory->GetBool());

    apply(proxyConfig);
    return;
  }

  const base::Value *rules = config->FindKeyOfType("rules", base::Value::Type::DICTIONARY);
  if (rules == nullptr)
    return;

  const base::Value *type = rules->FindKeyOfType("type", base::Value::Type::STRING);
  if (type == nullptr)
    return;

  net::ProxyConfig proxyConfig;

  bool readBypass = false;

  auto t = type->GetString();
  if (t == "list") {
      const base::Value *single_proxies = rules->FindKeyOfType("single_proxies", base::Value::Type::STRING);
      if (single_proxies == nullptr)
        return;
      proxyConfig.proxy_rules().type = net::ProxyConfig::ProxyRules::Type::PROXY_LIST;
      proxyConfig.proxy_rules().single_proxies.SetFromPacString(single_proxies->GetString());
      readBypass = true;
  } else if (t == "list_per_scheme") {
      const base::Value *http = rules->FindKeyOfType("proxies_for_http", base::Value::Type::STRING);
      if (http == nullptr)
        return;

      const base::Value *https = rules->FindKeyOfType("proxies_for_https", base::Value::Type::STRING);
      if (https == nullptr)
        return;

      const base::Value *ftp = rules->FindKeyOfType("proxies_for_ftp", base::Value::Type::STRING);
      if (ftp == nullptr)
        return;

      const base::Value *fallback = rules->FindKeyOfType("fallback_proxies", base::Value::Type::STRING);
      if (fallback == nullptr)
        return;

      proxyConfig.proxy_rules().type = net::ProxyConfig::ProxyRules::Type::PROXY_LIST_PER_SCHEME;
      proxyConfig.proxy_rules().proxies_for_http.SetFromPacString(http->GetString());
      proxyConfig.proxy_rules().proxies_for_https.SetFromPacString(https->GetString());
      proxyConfig.proxy_rules().proxies_for_ftp.SetFromPacString(ftp->GetString());
      proxyConfig.proxy_rules().fallback_proxies.SetFromPacString(fallback->GetString());
      readBypass = true;
  } else if (t == "direct") {
      proxyConfig.proxy_rules().type = net::ProxyConfig::ProxyRules::Type::EMPTY;
  } else if (t == "none") {
      base::Value::List empty;
      OnClear(empty);
      return;
  } else {
     // invalid type
     LOG(WARNING) << "invalid proxy configuration type";
     return;
  }

  // bypass rules and reverse flag are common to both list types of proxy rules
  if (readBypass) {
    const base::Value *bypass_rules = rules->FindKeyOfType("bypass_rules", base::Value::Type::STRING);
    if (bypass_rules == nullptr)
      return;

    const base::Value *reverse_bypass = rules->FindKeyOfType("reverse_bypass", base::Value::Type::BOOLEAN);
    if (reverse_bypass == nullptr)
      return;

    proxyConfig.proxy_rules().bypass_rules.ParseFromString(bypass_rules->GetString());
    proxyConfig.proxy_rules().reverse_bypass = reverse_bypass->GetBool();
  }

  apply(proxyConfig);
}

void ProxyConfigMessageHandler::apply(const net::ProxyConfig& proxyConfig) {
  if (proxyConfig.auto_detect()) {
    const base::Value::Dict cfg = ProxyConfigDictionary::CreateAutoDetect();
    pref_service_->SetDict(proxy_config::prefs::kProxy, cfg.Clone());
  } else if (proxyConfig.has_pac_url()) {
    const base::Value::Dict cfg = ProxyConfigDictionary::CreatePacScript(proxyConfig.pac_url().spec(), proxyConfig.pac_mandatory());
    pref_service_->SetDict(proxy_config::prefs::kProxy, cfg.Clone());
  } else if (proxyConfig.proxy_rules().type == net::ProxyConfig::ProxyRules::Type::EMPTY) {
    const base::Value::Dict cfg = ProxyConfigDictionary::CreateDirect();
    pref_service_->SetDict(proxy_config::prefs::kProxy, cfg.Clone());
  } else {
    auto proxyRulesAsString = proxyConfig.proxy_rules().ToString();
    auto bypassRulesAsString = proxyConfig.proxy_rules().bypass_rules.ToString();

    // fixed servers
    const base::Value::Dict cfg = ProxyConfigDictionary::CreateFixedServers(proxyRulesAsString,
                            bypassRulesAsString, proxyConfig.proxy_rules().reverse_bypass);
    pref_service_->SetDict(proxy_config::prefs::kProxy, cfg.Clone());
  }
  pref_service_->CommitPendingWrite();

  base::Value::List empty;
  OnEnableNotifyUIWithState(empty);
}

}  // namespace

ProxyConfigUI::ProxyConfigUI(content::WebUI* web_ui) : WebUIController(web_ui) {
  Profile* profile = Profile::FromWebUI(web_ui);

  web_ui->AddMessageHandler(std::make_unique<ProxyConfigMessageHandler>(profile));

  // Set up the chrome://proxy/ source.
  content::WebUIDataSource::Add(profile, CreateProxyConfigHTMLSource());
}
