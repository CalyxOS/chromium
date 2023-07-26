// Copyright 2023 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "components/aggregation_service/features.h"

#include "base/feature_list.h"

namespace aggregation_service {

BASE_FEATURE(kAggregationServiceMultipleCloudProviders,
             "AggregationServiceMultipleCloudProviders", // disabled by default
             base::FEATURE_DISABLED_BY_DEFAULT);         // in Brave, too

}  // namespace aggregation_service
