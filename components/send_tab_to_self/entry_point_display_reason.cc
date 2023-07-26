// Copyright 2022 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "components/send_tab_to_self/entry_point_display_reason.h"

#include "build/chromeos_buildflags.h"
#include "components/prefs/pref_service.h"
#include "components/send_tab_to_self/send_tab_to_self_model.h"
#include "components/send_tab_to_self/send_tab_to_self_sync_service.h"
#include "components/signin/public/base/signin_pref_names.h"
#include "components/signin/public/identity_manager/account_info.h"
#include "components/sync/service/sync_service.h"
#include "components/sync/service/sync_user_settings.h"
#include "url/gurl.h"

namespace send_tab_to_self {

namespace {

bool ShouldOfferSignin(syncer::SyncService* sync_service,
                       PrefService* pref_service) {
#if BUILDFLAG(IS_CHROMEOS_LACROS)
  return false;
#else
  return pref_service->GetBoolean(prefs::kSigninAllowed) &&
         sync_service->GetAccountInfo().IsEmpty() &&
         !sync_service->HasDisableReason(
             syncer::SyncService::DISABLE_REASON_ENTERPRISE_POLICY) &&
         !sync_service->IsLocalSyncEnabled();
#endif  // BUILDFLAG(IS_CHROMEOS_LACROS)
}

}  // namespace

namespace internal {

absl::optional<EntryPointDisplayReason> GetEntryPointDisplayReason(
    const GURL& url_to_share,
    syncer::SyncService* sync_service,
    SendTabToSelfModel* send_tab_to_self_model,
    PrefService* pref_service) {
  // Modified to replicate logic in brave-core 111b3401484fc8f52352776ea12fa40fb9880899
  // since the kSendTabToSelfSigninPromo flag is removed.

  if (!url_to_share.SchemeIsHTTPOrHTTPS()) {
    return absl::nullopt;
  }

  if (!send_tab_to_self_model || !sync_service) {
    // Send-tab-to-self can't work properly, don't show the entry point.
    return absl::nullopt;
  }

  if (ShouldOfferSignin(sync_service, pref_service)) {
    return absl::nullopt;
  }

  if (!send_tab_to_self_model->IsReady()) {
    return absl::nullopt;
  }

  if (!send_tab_to_self_model->HasValidTargetDevice()) {
    return absl::nullopt;
  }

  return EntryPointDisplayReason::kOfferFeature;
}

}  // namespace internal

}  // namespace send_tab_to_self
