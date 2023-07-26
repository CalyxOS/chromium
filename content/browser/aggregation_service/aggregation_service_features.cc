// Copyright 2022 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "content/browser/aggregation_service/aggregation_service_features.h"

namespace content {

// Enables the Aggregation Service. See crbug.com/1207974.
BASE_FEATURE(kPrivacySandboxAggregationService,
             "PrivacySandboxAggregationService",       // disabled by default
             base::FEATURE_DISABLED_BY_DEFAULT);       // in Brave

const base::FeatureParam<std::string>
    kPrivacySandboxAggregationServiceTrustedServerUrlAwsParam{
        &kPrivacySandboxAggregationService, "trusted_server_url",
        "https://publickeyservice.aws.privacysandboxservices.com/v1alpha/"
        "publicKeys"};

}  // namespace content
