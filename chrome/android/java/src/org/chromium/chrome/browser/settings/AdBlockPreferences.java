// Copyright 2015 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.settings;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import org.chromium.components.browser_ui.settings.ChromeSwitchPreference;

import org.chromium.content_public.browser.BrowserContextHandle;
import org.chromium.components.browser_ui.site_settings.SiteSettingsPreferenceFragment;
import org.chromium.components.browser_ui.site_settings.WebsitePreferenceBridge;
import org.chromium.components.content_settings.ContentSettingsType;
import org.chromium.components.browser_ui.settings.SettingsUtils;
import org.chromium.chrome.browser.flags.CachedFeatureFlags;
import androidx.annotation.VisibleForTesting;
import org.chromium.chrome.R;

/**
 * Fragment that allows the user to configure AdBlock related preferences.
 */
public class AdBlockPreferences extends SiteSettingsPreferenceFragment {
    @VisibleForTesting
    public static final String PREF_ADBLOCK_SWITCH = "adblock_switch";
    private static final String PREF_ADBLOCK_EDIT = "adblock_edit";

    private Preference mAdBlockEdit;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getActivity().setTitle(R.string.options_adblock_title);
        SettingsUtils.addPreferencesFromResource(this, R.xml.adblock_preferences);

        BrowserContextHandle browserContextHandle =
                getSiteSettingsDelegate().getBrowserContextHandle();

        ChromeSwitchPreference mAdBlockSwitch =
                (ChromeSwitchPreference) findPreference(PREF_ADBLOCK_SWITCH);
        boolean isAdBlockEnabled = !WebsitePreferenceBridge.isCategoryEnabled(browserContextHandle, ContentSettingsType.ADS);
        mAdBlockSwitch.setChecked(isAdBlockEnabled);
        mAdBlockSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
            WebsitePreferenceBridge.setCategoryEnabled(browserContextHandle, ContentSettingsType.ADS, !(boolean) newValue);
            return true;
        });

        mAdBlockEdit = findPreference(PREF_ADBLOCK_EDIT);
        updateCurrentAdBlockUrl();
    }

    private void updateCurrentAdBlockUrl() {
        mAdBlockEdit.setSummary(CachedFeatureFlags.getAdBlockFiltersURL());
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCurrentAdBlockUrl();
    }
}
