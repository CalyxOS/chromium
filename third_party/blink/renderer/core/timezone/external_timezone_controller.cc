// Copyright 2019 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "third_party/blink/renderer/core/timezone/external_timezone_controller.h"
#include "third_party/blink/renderer/core/timezone/timezone_controller.h"
#include "third_party/blink/renderer/platform/wtf/wtf.h"

namespace blink {

ExternalTimeZoneController::ExternalTimeZoneController() {
  DCHECK(IsMainThread());
}

// static
std::unique_ptr<ExternalTimeZoneController::TimeZoneOverride>
ExternalTimeZoneController::SetTimeZoneOverride(const std::string& timezone_id) {
  auto timezone = String(timezone_id.c_str());

  auto timezone_override = TimeZoneController::SetTimeZoneOverride(timezone);
  if (!timezone_override) return nullptr;

  timezone_override->clear_at_destruction_ = false;
  return std::unique_ptr<TimeZoneOverride>(new TimeZoneOverride());
}

// static
bool ExternalTimeZoneController::HasTimeZoneOverride() {
  return TimeZoneController::HasTimeZoneOverride();
}

// static
void ExternalTimeZoneController::ClearTimeZoneOverride() {
  TimeZoneController::ClearTimeZoneOverride();
}

}  // namespace blink
