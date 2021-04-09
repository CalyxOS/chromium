// Copyright 2019 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include <jni.h>

#include "base/feature_list.h"
#include "chrome/android/chrome_jni_headers/PrivacyPreferencesManagerImpl_jni.h"
#include "chrome/browser/browser_process.h"
#include "chrome/browser/prefetch/prefetch_prefs.h"
#include "chrome/browser/profiles/profile_manager.h"
#include "chrome/common/pref_names.h"
#include "components/metrics/metrics_pref_names.h"
#include "components/policy/core/common/features.h"
#include "components/prefs/pref_service.h"

#include "base/command_line.h"
#include "base/base_switches.h"
#include "chrome/common/chrome_switches.h"
#include "content/browser/renderer_host/render_process_host_impl.h"
#include "content/common/renderer.mojom.h"
#include "chrome/browser/chrome_content_browser_client.h"

#include "components/embedder_support/content_settings_utils.h"
#include "components/embedder_support/switches.h"
#include "components/embedder_support/user_agent_utils.h"

#include "base/android/jni_android.h"
#include "base/android/jni_array.h"
#include "base/android/jni_string.h"
#include "base/android/scoped_java_ref.h"

using base::android::ConvertJavaStringToUTF8;
using base::android::ConvertUTF16ToJavaString;
using base::android::ConvertUTF8ToJavaString;
using base::android::JavaParamRef;
using base::android::JavaRef;
using base::android::ScopedJavaGlobalRef;
using base::android::ScopedJavaLocalRef;

static jboolean JNI_PrivacyPreferencesManagerImpl_IsMetricsReportingEnabled(
    JNIEnv* env) {
  PrefService* local_state = g_browser_process->local_state();
  return local_state->GetBoolean(metrics::prefs::kMetricsReportingEnabled);
}

static void JNI_PrivacyPreferencesManagerImpl_SetMetricsReportingEnabled(
    JNIEnv* env,
    jboolean enabled) {
  PrefService* local_state = g_browser_process->local_state();
  local_state->SetBoolean(metrics::prefs::kMetricsReportingEnabled, enabled);
}

static jboolean
JNI_PrivacyPreferencesManagerImpl_IsMetricsReportingDisabledByPolicy(
    JNIEnv* env) {
  // Metrics reporting can only be disabled by policy if the policy is active.
  if (!base::FeatureList::IsEnabled(
          policy::features::kActivateMetricsReportingEnabledPolicyAndroid)) {
    return false;
  }

  const PrefService* local_state = g_browser_process->local_state();
  return local_state->IsManagedPreference(
             metrics::prefs::kMetricsReportingEnabled) &&
         !local_state->GetBoolean(metrics::prefs::kMetricsReportingEnabled);
}

static void UpdateOverrideUserAgent() {
  bool overrideUserAgentEnabled =
    g_browser_process->local_state()->GetBoolean(prefs::kOverrideUserAgentEnabled);
  std::string ua = g_browser_process->local_state()->GetString(prefs::kOverrideUserAgent);
  if (ua.empty()) {
    ua = ChromeContentBrowserClient().GetUserAgent();
  }

  base::CommandLine* parsed_command_line =
      base::CommandLine::ForCurrentProcess();
  parsed_command_line->RemoveSwitch(embedder_support::kUserAgent);
  if (!ua.empty()) {
    if (overrideUserAgentEnabled) {
      parsed_command_line->AppendSwitchASCII(embedder_support::kUserAgent, ua);
    }

    for (auto iter = content::RenderProcessHost::AllHostsIterator(); !iter.IsAtEnd();
         iter.Advance()) {
      if (iter.GetCurrentValue()->IsInitializedAndNotDead()) {
        std::vector<std::string> cors_exempt_header_list;
        iter.GetCurrentValue()->GetRendererInterface()->InitializeRenderer(
          /*user_agent*/ ua, /*full_user_agent*/ ua, /*reduced_user_agent*/ ua,
          /*metadata*/ blink::UserAgentMetadata(), cors_exempt_header_list);
      }
    }
  }

  parsed_command_line->RemoveSwitch(switches::kDesktopModeViewportMetaEnabled);
  if (g_browser_process->local_state()->GetBoolean(prefs::kDesktopModeViewportMetaEnabled))
    parsed_command_line->AppendSwitch(switches::kDesktopModeViewportMetaEnabled);
}

static void JNI_PrivacyPreferencesManagerImpl_UpdateOverrideUserAgent(
    JNIEnv* env) {
  UpdateOverrideUserAgent();
}

static jboolean JNI_PrivacyPreferencesManagerImpl_IsOverrideUserAgentEnabled(
    JNIEnv* env, jboolean desktopMode) {
  if (desktopMode == false)
    return g_browser_process->local_state()->GetBoolean(prefs::kOverrideUserAgentEnabled);
  else
    return g_browser_process->local_state()->GetBoolean(prefs::kOverrideUserAgentDesktopModeEnabled);
}

static void JNI_PrivacyPreferencesManagerImpl_SetOverrideUserAgentEnabled(
    JNIEnv* env,
    jboolean enabled, jboolean desktopMode) {
  if (desktopMode == false) {
    g_browser_process->local_state()->SetBoolean(prefs::kOverrideUserAgentEnabled,
                                                enabled);
    UpdateOverrideUserAgent();
  } else {
    g_browser_process->local_state()->SetBoolean(prefs::kOverrideUserAgentDesktopModeEnabled,
                                                enabled);
  }
}

static void JNI_PrivacyPreferencesManagerImpl_SetOverrideUserAgentValue(
    JNIEnv* env,
    const JavaParamRef<jstring>& ua, jboolean desktopMode) {
  std::string new_ua = ConvertJavaStringToUTF8(env, ua);
  if (desktopMode == false) {
    g_browser_process->local_state()->SetString(prefs::kOverrideUserAgent,
                                                new_ua);
    UpdateOverrideUserAgent();
  } else {
    g_browser_process->local_state()->SetString(prefs::kOverrideUserAgentDesktopMode,
                                                new_ua);
  }
}

static base::android::ScopedJavaLocalRef<jstring>
    JNI_PrivacyPreferencesManagerImpl_GetOverrideUserAgentValue(
      JNIEnv* env, jboolean desktopMode) {
  if (desktopMode == false) {
    std::string ua = g_browser_process->local_state()->GetString(prefs::kOverrideUserAgent);
    return ConvertUTF8ToJavaString(env, ua);
  } else {
    std::string ua = g_browser_process->local_state()->GetString(prefs::kOverrideUserAgentDesktopMode);
    return ConvertUTF8ToJavaString(env, ua);
  }
}

static jboolean JNI_PrivacyPreferencesManagerImpl_IsDesktopModeViewportMetaEnabled(
    JNIEnv* env) {
  return g_browser_process->local_state()->GetBoolean(prefs::kDesktopModeViewportMetaEnabled);
}

static void JNI_PrivacyPreferencesManagerImpl_SetDesktopModeViewportMetaEnabled(
    JNIEnv* env,
    jboolean enabled) {
  g_browser_process->local_state()->SetBoolean(prefs::kDesktopModeViewportMetaEnabled,
                                              enabled);
  UpdateOverrideUserAgent();
}
