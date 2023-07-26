// Copyright 2023 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "chrome/browser/preloading/preloading_features.h"

namespace features {

BASE_FEATURE(kPerformanceSettingsPreloadingSubpage,
             "PerformanceSettingsPreloadingSubpage",   // disabled by default
             base::FEATURE_DISABLED_BY_DEFAULT);       // in Brave, too

}  // namespace features
