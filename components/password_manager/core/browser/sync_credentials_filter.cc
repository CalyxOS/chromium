// Copyright 2015 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "components/password_manager/core/browser/sync_credentials_filter.h"

#include <algorithm>

#include "base/feature_list.h"
#include "base/metrics/user_metrics.h"
#include "components/password_manager/core/browser/password_form_manager.h"
#include "components/password_manager/core/browser/password_manager_util.h"
#include "components/password_manager/core/common/password_manager_features.h"

namespace password_manager {

SyncCredentialsFilter::SyncCredentialsFilter(
    PasswordManagerClient* client,
    SyncServiceFactoryFunction sync_service_factory_function)
    : client_(client),
      sync_service_factory_function_(std::move(sync_service_factory_function)) {
}

SyncCredentialsFilter::~SyncCredentialsFilter() = default;

bool SyncCredentialsFilter::ShouldSave(const PasswordForm& form) const {
  if (client_->IsIncognito())
    return false;

  if (form.form_data.is_gaia_with_skip_save_password_form)
    return false;

  return true;
}

bool SyncCredentialsFilter::ShouldSaveGaiaPasswordHash(
    const PasswordForm& form) const {
  return false;
}

bool SyncCredentialsFilter::ShouldSaveEnterprisePasswordHash(
    const PasswordForm& form) const {
  return false;
}

bool SyncCredentialsFilter::IsSyncAccountEmail(
    const std::string& username) const {
  return false;
}

void SyncCredentialsFilter::ReportFormLoginSuccess(
    const PasswordFormManager& form_manager) const {
}

}  // namespace password_manager
