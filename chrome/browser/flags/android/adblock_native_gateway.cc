#include "chrome/browser/flags/jni_headers/AdBlockNativeGateway_jni.h"

#include "base/android/jni_string.h"
#include "chrome/browser/browser_process.h"
#include "chrome/common/pref_names.h"
#include "components/prefs/pref_service.h"

using base::android::ScopedJavaLocalRef;
using base::android::JavaParamRef;

static ScopedJavaLocalRef<jstring> JNI_AdBlockNativeGateway_GetAdBlockFiltersURL(
    JNIEnv* env) {
  return base::android::ConvertUTF8ToJavaString(env,
      g_browser_process->local_state()->GetString(prefs::kAdBlockFiltersURL));
}

static void JNI_AdBlockNativeGateway_SetAdBlockFiltersURL(
    JNIEnv* env, const JavaParamRef<jstring>& url) {
  g_browser_process->local_state()->SetString(prefs::kAdBlockFiltersURL,
      base::android::ConvertJavaStringToUTF8(env, url));
}
