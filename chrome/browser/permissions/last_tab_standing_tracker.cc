// Copyright 2020 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "chrome/browser/permissions/last_tab_standing_tracker.h"

#include "base/observer_list.h"
#include "url/gurl.h"

#include "components/content_settings/core/browser/host_content_settings_map.h"
#include "components/content_settings/core/common/content_settings_utils.h"
#include "components/permissions/permissions_client.h"

namespace {
  // Remove all sessions content setting by origin and type
  void RemoveSessionSettings(HostContentSettingsMap* content_settings,
                             const url::Origin& origin,
                             ContentSettingsType type) {
    ContentSettingsForOneType session_settings =
      content_settings->GetSettingsForOneType(
          type, content_settings::mojom::SessionModel::USER_SESSION);

    GURL url = origin.GetURL();
    for (ContentSettingPatternSource& entry : session_settings) {
      if (content_settings::IsConstraintSessionExpiration(entry,
              content_settings::mojom::LifetimeMode::UNTIL_ORIGIN_CLOSED) &&
          entry.primary_pattern.Matches(url)) {
        content_settings->SetWebsiteSettingCustomScope(
            entry.primary_pattern, entry.secondary_pattern,
            type, base::Value());
      }
    }
  }
}

LastTabStandingTracker::LastTabStandingTracker(content::BrowserContext* context)
    : context_(context) {}

LastTabStandingTracker::~LastTabStandingTracker() = default;

void LastTabStandingTracker::Shutdown() {
  for (auto& observer : observer_list_) {
    observer.OnShutdown();
  }
  observer_list_.Clear();
}

void LastTabStandingTracker::AddObserver(
    LastTabStandingTrackerObserver* observer) {
  observer_list_.AddObserver(observer);
}

void LastTabStandingTracker::RemoveObserver(
    LastTabStandingTrackerObserver* observer) {
  observer_list_.RemoveObserver(observer);
}

void LastTabStandingTracker::WebContentsLoadedOrigin(
    const url::Origin& origin) {
  if (origin.opaque())
    return;
  // There are cases where chrome://newtab/ and chrome://new-tab-page/ are
  // used synonymously causing inconsistencies in the map. So we just ignore
  // them.
  if (origin == url::Origin::Create(GURL("chrome://newtab/")) ||
      origin == url::Origin::Create(GURL("chrome://new-tab-page/")))
    return;
  tab_counter_[origin]++;
}

void LastTabStandingTracker::WebContentsUnloadedOrigin(
    const url::Origin& origin) {
  if (origin.opaque())
    return;
  if (origin == url::Origin::Create(GURL("chrome://newtab/")) ||
      origin == url::Origin::Create(GURL("chrome://new-tab-page/")))
    return;
  DCHECK(tab_counter_.find(origin) != tab_counter_.end());
  tab_counter_[origin]--;
  if (tab_counter_[origin] <= 0) {
    tab_counter_.erase(origin);
    for (auto& observer : observer_list_) {
      observer.OnLastPageFromOriginClosed(origin);
    }
    HostContentSettingsMap* content_settings =
            permissions::PermissionsClient::Get()->GetSettingsMap(context_);
    RemoveSessionSettings(content_settings, origin, ContentSettingsType::GEOLOCATION);
    RemoveSessionSettings(content_settings, origin, ContentSettingsType::MEDIASTREAM_MIC);
    RemoveSessionSettings(content_settings, origin, ContentSettingsType::MEDIASTREAM_CAMERA);
  }
}
