// Copyright 2020 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "android_webview/browser/aw_field_trials.h"

#include "base/base_paths_android.h"
#include "base/feature_list.h"
#include "base/metrics/persistent_histogram_allocator.h"
#include "base/path_service.h"
#include "components/metrics/persistent_histograms.h"

void AwFieldTrials::OnVariationsSetupComplete() {
}
