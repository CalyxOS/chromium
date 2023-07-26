// Copyright 2021 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "components/shared_highlighting/core/common/shared_highlighting_features.h"

#include "base/feature_list.h"
#include "build/build_config.h"

namespace shared_highlighting {

BASE_FEATURE(kPreemptiveLinkToTextGeneration,
             "PreemptiveLinkToTextGeneration",
             base::FEATURE_ENABLED_BY_DEFAULT);
constexpr base::FeatureParam<int> kPreemptiveLinkGenTimeoutLengthMs{
    &kPreemptiveLinkToTextGeneration, "TimeoutLengthMs", 500};

BASE_FEATURE(kSharedHighlightingAmp,
             "SharedHighlightingAmp",                  // disabled by default
             base::FEATURE_DISABLED_BY_DEFAULT         // in Brave
);

BASE_FEATURE(kIOSSharedHighlightingV2,
             "IOSSharedHighlightingV2",
             base::FEATURE_ENABLED_BY_DEFAULT);

BASE_FEATURE(kSharedHighlightingRefinedBlocklist,
             "SharedHighlightingRefinedBlocklist",
             base::FEATURE_DISABLED_BY_DEFAULT);

BASE_FEATURE(kSharedHighlightingRefinedMaxContextWords,
             "SharedHighlightingRefinedMaxContextWords",
             base::FEATURE_DISABLED_BY_DEFAULT);

BASE_FEATURE(kSharedHighlightingManager,
             "SharedHighlightingManager",              // disabled by default
             base::FEATURE_DISABLED_BY_DEFAULT);       // in Brave, too

const char kSharedHighlightingRefinedMaxContextWordsName[] =
    "SharedHighlightingRefinedMaxContextWords";

const base::FeatureParam<int> kSharedHighlightingMaxContextWords{
    &kSharedHighlightingRefinedMaxContextWords,
    kSharedHighlightingRefinedMaxContextWordsName, 10};

int GetPreemptiveLinkGenTimeoutLengthMs() {
  return kPreemptiveLinkGenTimeoutLengthMs.Get();
}

}  // namespace shared_highlighting
