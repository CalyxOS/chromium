// Copyright 2014 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "chrome/browser/domain_reliability/service_factory.h"

#include "base/command_line.h"
#include "base/metrics/field_trial.h"
#include "chrome/browser/browser_process.h"
#include "chrome/browser/metrics/chrome_metrics_service_accessor.h"
#include "chrome/common/chrome_switches.h"
#include "components/domain_reliability/domain_reliability_prefs.h"
#include "components/prefs/pref_service.h"

namespace domain_reliability {

// Identifies Chrome as the source of Domain Reliability uploads it sends.
const char kUploadReporterString[] = "chrome";

bool ShouldCreateService() {
  return false;
}

}  // namespace domain_reliability
