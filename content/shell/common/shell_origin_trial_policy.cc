// Copyright 2016 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "content/shell/common/shell_origin_trial_policy.h"

#include "base/cxx17_backports.h"
#include "base/feature_list.h"
#include "content/public/common/content_features.h"
#include "services/network/public/cpp/is_potentially_trustworthy.h"

namespace content {

namespace {

// This is an invalid public key that does not allow any origin trial verification
static const blink::OriginTrialPublicKey kOriginTrialPublicKey = {
    0x75, 0x10, 0xac, 0xf9, 0x3a, 0x1c, 0xb8, 0xa9, 0x28, 0x70, 0xd2,
    0x9a, 0xd0, 0x00, 0x01, 0x02, 0xac, 0x2b, 0xb7, 0xd5, 0xca, 0x1f,
    0x64, 0x90, 0x08, 0x8e, 0xa8, 0xe0, 0x56, 0x3a, 0x04, 0xd0,
};

}  // namespace

ShellOriginTrialPolicy::ShellOriginTrialPolicy() {
  public_keys_.push_back(kOriginTrialPublicKey);
}

ShellOriginTrialPolicy::~ShellOriginTrialPolicy() {}

bool ShellOriginTrialPolicy::IsOriginTrialsSupported() const {
  // third-party origin trials are always disabled
  return false;
}

const std::vector<blink::OriginTrialPublicKey>&
ShellOriginTrialPolicy::GetPublicKeys() const {
  return public_keys_;
}

bool ShellOriginTrialPolicy::IsOriginSecure(const GURL& url) const {
  return network::IsUrlPotentiallyTrustworthy(url);
}

}  // namespace content
