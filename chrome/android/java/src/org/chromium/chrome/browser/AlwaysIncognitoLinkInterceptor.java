/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.chromium.chrome.browser;

import android.content.SharedPreferences;
import org.chromium.base.ContextUtils;

import org.chromium.components.user_prefs.UserPrefs;
import org.chromium.components.prefs.PrefService;
import org.chromium.chrome.browser.profiles.Profile;
import org.chromium.chrome.browser.profiles.ProfileManager;
import org.chromium.chrome.browser.preferences.Pref;
import org.chromium.base.Log;

import androidx.annotation.Nullable;

/**
 * A {@link TabObserver} that implements the always-incognito preference behavior for links.
 */
public class AlwaysIncognitoLinkInterceptor {

    private static final String TAG = "AlwaysIncognito";
    public static final String PREF_ALWAYS_INCOGNITO = "always_incognito";

    private static @Nullable Boolean cachedIsAlwaysIncognito = null;

    public static boolean isAlwaysIncognito() {
        if (cachedIsAlwaysIncognito != null) return cachedIsAlwaysIncognito;
        cachedIsAlwaysIncognito = ContextUtils.getAppSharedPreferences()
                                    .getBoolean(PREF_ALWAYS_INCOGNITO, false);
        return cachedIsAlwaysIncognito;
    }

    public static void setAlwaysIncognito(boolean enabled) {
        UserPrefs.get(ProfileManager.getLastUsedRegularProfile())
            .setBoolean(Pref.ALWAYS_INCOGNITO_ENABLED, enabled);

        SharedPreferences.Editor sharedPreferenceEditor = ContextUtils.getAppSharedPreferences().edit();
        sharedPreferenceEditor.putBoolean("always_incognito", enabled);
        sharedPreferenceEditor.apply();
    }

    public static void migrateSettingToNative() {
        if (isAlwaysIncognito()) {
            PrefService prefService = UserPrefs.get(ProfileManager.getLastUsedRegularProfile());
            if (!prefService.getBoolean(Pref.ALWAYS_INCOGNITO_ENABLED)) {
                Log.i(TAG, "Pref migration from java to native");
                prefService.setBoolean(Pref.ALWAYS_INCOGNITO_ENABLED, true);
            }
        }
    }
}
