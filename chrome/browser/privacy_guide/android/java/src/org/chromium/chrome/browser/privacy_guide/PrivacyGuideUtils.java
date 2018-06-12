// Copyright 2022 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.privacy_guide;

import org.chromium.chrome.browser.profiles.Profile;
import org.chromium.chrome.browser.safe_browsing.SafeBrowsingBridge;
import org.chromium.chrome.browser.safe_browsing.SafeBrowsingState;
import org.chromium.components.content_settings.CookieControlsMode;
import org.chromium.components.content_settings.PrefNames;
import org.chromium.components.sync.UserSelectableType;
import org.chromium.components.user_prefs.UserPrefs;

import java.util.Set;

/**
 * A utility class for Privacy Guide that fetches the current state of {@link
 * PrivacyGuideFragment.FragmentType}s.
 *
 * TODO(crbug.com/1393960): Utilize the methods on this class on the PG fragments
 */
class PrivacyGuideUtils {
    public static boolean isMsbbEnabled() {
        return false;
    }
    public static boolean isHistorySyncEnabled() {
        return false;
    }
    public static @SafeBrowsingState int getSafeBrowsingState() {
        return SafeBrowsingState.NO_SAFE_BROWSING;
    }
    public static @CookieControlsMode int getCookieControlsMode() {
        return UserPrefs.get(Profile.getLastUsedRegularProfile())
                .getInteger(PrefNames.COOKIE_CONTROLS_MODE);
    }
}
