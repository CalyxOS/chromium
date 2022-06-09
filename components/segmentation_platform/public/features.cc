// Copyright 2021 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "components/segmentation_platform/public/features.h"

#include "build/build_config.h"

namespace segmentation_platform::features {

BASE_FEATURE(kSegmentationPlatformFeature,
             "SegmentationPlatform",                // disabled by default
             base::FEATURE_DISABLED_BY_DEFAULT);    // in Bromite

BASE_FEATURE(kSegmentationStructuredMetricsFeature,
             "SegmentationStructuredMetrics",
             base::FEATURE_ENABLED_BY_DEFAULT);

BASE_FEATURE(kSegmentationPlatformUkmEngine,
             "SegmentationPlatformUkmEngine",
             base::FEATURE_DISABLED_BY_DEFAULT);

BASE_FEATURE(kSegmentationPlatformLowEngagementFeature,
             "SegmentationPlatformLowEngagementFeature",
             base::FEATURE_ENABLED_BY_DEFAULT);

BASE_FEATURE(kShoppingUserSegmentFeature,
             "ShoppingUserSegmentFeature",
             base::FEATURE_ENABLED_BY_DEFAULT);

BASE_FEATURE(kSegmentationPlatformSearchUser,
             "SegmentationPlatformSearchUser",
             base::FEATURE_ENABLED_BY_DEFAULT);

BASE_FEATURE(kSegmentationPlatformFeedSegmentFeature,
             "SegmentationPlatformFeedSegmentFeature",
             base::FEATURE_DISABLED_BY_DEFAULT);

BASE_FEATURE(kResumeHeavyUserSegmentFeature,
             "ResumeHeavyUserSegment",
             base::FEATURE_ENABLED_BY_DEFAULT);

BASE_FEATURE(kSegmentationPlatformPowerUserFeature,
             "SegmentationPlatformPowerUserFeature",
             base::FEATURE_ENABLED_BY_DEFAULT);

BASE_FEATURE(kFrequentFeatureUserSegmentFeature,
             "FrequentFeatureUserSegmentFeature",
             base::FEATURE_ENABLED_BY_DEFAULT);

BASE_FEATURE(kContextualPageActions,
             "ContextualPageActions",
             base::FEATURE_ENABLED_BY_DEFAULT);

BASE_FEATURE(kContextualPageActionPriceTracking,
             "ContextualPageActionPriceTracking",
             base::FEATURE_ENABLED_BY_DEFAULT);

BASE_FEATURE(kContextualPageActionReaderMode,
             "ContextualPageActionReaderMode",
             base::FEATURE_DISABLED_BY_DEFAULT);

BASE_FEATURE(kSegmentationPlatformSegmentInfoCache,
             "SegmentationPlatformSegmentInfoCache",
             base::FEATURE_DISABLED_BY_DEFAULT);

BASE_FEATURE(kSegmentationDefaultReportingSegments,
             "SegmentationDefaultReportingSegments",
             base::FEATURE_ENABLED_BY_DEFAULT);

}  // namespace segmentation_platform::features
