// Copyright 2021 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "chrome/browser/android/signin/fre_mobile_identity_consistency_field_trial.h"
#include "base/android/jni_android.h"
#include "base/android/jni_string.h"

namespace fre_mobile_identity_consistency_field_trial {

std::string GetFREFieldTrialGroup() {
  return std::string();
}

bool IsFREFieldTrialEnabled() {
  return false;
}

std::string GetFREVariationsFieldTrialGroup() {
  return std::string();
}

}  // namespace fre_mobile_identity_consistency_field_trial
