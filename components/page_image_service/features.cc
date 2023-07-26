// Copyright 2023 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "components/page_image_service/features.h"

namespace page_image_service {

// Enabled by default because we are only using this as a killswitch.
BASE_FEATURE(kImageService,
             "ImageService",                           // disabled by default
             base::FEATURE_DISABLED_BY_DEFAULT);       // in Brave

// Disabled by default because the usage of this is still not approved.
BASE_FEATURE(kImageServiceSuggestPoweredImages,
             "ImageServiceSuggestPoweredImages",       // disabled by default
             base::FEATURE_DISABLED_BY_DEFAULT);       // in Brave, too

// Enabled the capability by default, can be used as a killswitch.
BASE_FEATURE(kImageServiceOptimizationGuideSalientImages,
             "ImageServiceOptimizationGuideSalientImages",
             base::FEATURE_ENABLED_BY_DEFAULT);

}  // namespace page_image_service
