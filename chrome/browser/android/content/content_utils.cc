// Copyright 2017 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "base/android/jni_string.h"
#include "chrome/android/chrome_jni_headers/ContentUtils_jni.h"
#include "components/embedder_support/user_agent_utils.h"
#include "components/version_info/version_info.h"
#include "content/public/browser/web_contents.h"

#include "base/android/jni_android.h"
#include "base/android/scoped_java_ref.h"
#include "chrome/browser/browser_process.h"
#include "components/prefs/pref_service.h"
#include "chrome/common/pref_names.h"

using base::android::ConvertJavaStringToUTF8;
using base::android::ConvertUTF16ToJavaString;
using base::android::ConvertUTF8ToJavaString;
using base::android::JavaParamRef;
using base::android::JavaRef;
using base::android::ScopedJavaGlobalRef;
using base::android::ScopedJavaLocalRef;

static base::android::ScopedJavaLocalRef<jstring>
JNI_ContentUtils_GetBrowserUserAgent(JNIEnv* env) {
  return base::android::ConvertUTF8ToJavaString(
      env, embedder_support::GetUserAgent());
}

static void JNI_ContentUtils_SetUserAgentOverride(
    JNIEnv* env,
    const base::android::JavaParamRef<jobject>& jweb_contents,
    jboolean j_override_in_new_tabs) {
  bool enabled =
    g_browser_process->local_state()->GetBoolean(prefs::kOverrideUserAgentDesktopModeEnabled);

  if (enabled == true) {
    std::string ua = g_browser_process->local_state()->GetString(prefs::kOverrideUserAgentDesktopMode);
    blink::UserAgentOverride spoofed_ua;
    spoofed_ua.ua_string_override = ua;

    content::WebContents* web_contents =
        content::WebContents::FromJavaWebContents(jweb_contents);
    web_contents->SetUserAgentOverride(spoofed_ua, false);
    return;
  }

  content::WebContents* web_contents =
      content::WebContents::FromJavaWebContents(jweb_contents);
  embedder_support::SetDesktopUserAgentOverride(
      web_contents, embedder_support::GetUserAgentMetadata(),
      j_override_in_new_tabs);
}
