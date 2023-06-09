#include "chrome/browser/flags/android/cromite_native_utils.h"

#include "chrome/browser/browser_process.h"
#include "chrome/browser/about_flags.h"
#include "chrome/browser/flags/jni_headers/CromiteNativeUtils_jni.h"
#include "components/webui/flags/pref_service_flags_storage.h"

#include "base/android/jni_string.h"

using base::android::JavaParamRef;

static void JNI_CromiteNativeUtils_SetEnabled(JNIEnv* env,
    const JavaParamRef<jstring>& featureName, jboolean isEnabled) {
  flags_ui::PrefServiceFlagsStorage flags_storage(
      g_browser_process->local_state());
  std::set<std::string> entries = flags_storage.GetFlags();

  auto internal_name = base::android::ConvertJavaStringToUTF8(env, featureName);
  entries.erase(internal_name);
  entries.erase(internal_name + "@1"); // enabled
  entries.erase(internal_name + "@2"); // disabled

  bool is_switch = false;
  if (const flags_ui::FeatureEntry* entry =
        about_flags::GetCurrentFlagsState()->FindFeatureEntryByName(
          internal_name)) {
    if (entry->type == flags_ui::FeatureEntry::SINGLE_DISABLE_VALUE) {
      entries.erase(entry->switches.command_line_switch);
      if (isEnabled) {
        entries.insert(entry->switches.command_line_switch);
      }
      is_switch = true;
    } else if (entry->type == flags_ui::FeatureEntry::ENABLE_DISABLE_VALUE) {
      entries.erase(entry->switches.command_line_switch);
      entries.erase(entry->switches.disable_command_line_switch);
      if (isEnabled) {
        entries.insert(entry->switches.disable_command_line_switch);
      }
      is_switch = true;
    }
  }

  if (!is_switch) {
    if (isEnabled) {
      entries.insert(internal_name + "@1");
    } else {
      entries.insert(internal_name + "@2");
    }
  }

  flags_storage.SetFlags(entries);
  flags_storage.CommitPendingWrites();
}

static jboolean JNI_CromiteNativeUtils_IsFlagEnabled(JNIEnv* env,
    const JavaParamRef<jstring>& featureName) {
  auto internal_name = base::android::ConvertJavaStringToUTF8(env, featureName);

  flags_ui::PrefServiceFlagsStorage flags_storage(
      g_browser_process->local_state());
  std::set<std::string> entries = flags_storage.GetFlags();

  if (const flags_ui::FeatureEntry* entry =
        about_flags::GetCurrentFlagsState()->FindFeatureEntryByName(
          internal_name)) {
    if (entry->type == flags_ui::FeatureEntry::SINGLE_DISABLE_VALUE &&
        entries.count(entry->switches.command_line_switch)) {
      return true;
    } else if (entry->type == flags_ui::FeatureEntry::ENABLE_DISABLE_VALUE &&
               entries.count(entry->switches.disable_command_line_switch)) {
      return true;
    }
  }

  const std::string enabled_entry = internal_name + "@1";
  if (entries.count(enabled_entry))
    return true;

  const std::string disabled_entry = internal_name + "@2";
  if (entries.count(disabled_entry))
    return false;

  if (const flags_ui::FeatureEntry* entry =
        about_flags::GetCurrentFlagsState()->FindFeatureEntryByName(
          internal_name)) {
    if (const base::Feature* feature = entry->feature.feature) {
      return feature->default_state == base::FEATURE_ENABLED_BY_DEFAULT;
    }
  }

  return false;
}
