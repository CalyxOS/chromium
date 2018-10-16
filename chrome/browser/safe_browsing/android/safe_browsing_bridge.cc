// Copyright 2019 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "base/android/jni_string.h"
#include "base/files/file_path.h"
// NOTE: This target is transitively depended on by //chrome/browser and thus
// can't depend on it.
#include "chrome/browser/profiles/profile_manager.h"  // nogncheck
#include "chrome/browser/safe_browsing/android/jni_headers/SafeBrowsingBridge_jni.h"
// NOTE: This target is transitively depended on by //chrome/browser and thus
// can't depend on it.
#include "chrome/browser/signin/identity_manager_factory.h"  // nogncheck
#include "components/password_manager/core/browser/leak_detection/leak_detection_check_impl.h"
#include "components/password_manager/core/common/password_manager_features.h"
#include "components/password_manager/core/common/password_manager_pref_names.h"
#include "components/prefs/pref_service.h"
#include "components/safe_browsing/content/common/file_type_policies.h"
#include "components/safe_browsing/core/common/safe_browsing_prefs.h"
#include "components/signin/public/identity_manager/identity_manager.h"

using base::android::JavaParamRef;

namespace {

PrefService* GetPrefService() {
  return ProfileManager::GetActiveUserProfile()
      ->GetOriginalProfile()
      ->GetPrefs();
}

}  // namespace

namespace safe_browsing {

}  // namespace safe_browsing
