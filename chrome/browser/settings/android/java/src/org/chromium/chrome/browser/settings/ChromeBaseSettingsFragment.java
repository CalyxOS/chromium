// Copyright 2023 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.settings;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;

import org.chromium.base.supplier.Supplier;
import org.chromium.chrome.browser.feedback.HelpAndFeedbackLauncher;
import org.chromium.chrome.browser.feedback.HelpAndFeedbackLauncherFactory;
import org.chromium.chrome.browser.profiles.Profile;
import org.chromium.components.browser_ui.settings.EmbeddableSettingsPage;
import org.chromium.components.browser_ui.settings.SettingsCustomTabLauncher;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import org.chromium.components.browser_ui.settings.ChromeSwitchPreference;
import org.chromium.chrome.browser.flags.CromiteNativeUtils;

/**
 * Base class for settings in Chrome.
 *
 * <p>Common dependencies needed by the vast majority of settings screens can be added here for
 * convenience.
 */
public abstract class ChromeBaseSettingsFragment extends PreferenceFragmentCompat
        implements EmbeddableSettingsPage,
                ProfileDependentSetting,
                SettingsCustomTabLauncher.SettingsCustomTabLauncherClient {
    private Profile mProfile;
    private SettingsCustomTabLauncher mCustomTabLauncher;

    private Supplier<ChromeBaseSettingsFragment.RequireRestartDelegate> mRequireRestartDelegateSupplier;

    public interface RequireRestartDelegate {
        void RequireRestart();
    }

    public void setRequestRestartDelegateSupplier(
                    Supplier<ChromeBaseSettingsFragment.RequireRestartDelegate> delegate) {
        mRequireRestartDelegateSupplier = delegate;
    }

    public void onCreatePreferencesCromite(Bundle savedInstanceState, String rootKey) {
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        onCreatePreferencesCromite(savedInstanceState, rootKey);

        PreferenceScreen prefScreen = getPreferenceScreen();
        int prefCount = prefScreen.getPreferenceCount();

        for(int i=0; i < prefCount; i++) {
            Preference pref = prefScreen.getPreference(i);
            if (pref instanceof ChromeSwitchPreference) {
                ChromeSwitchPreference switchPref = (ChromeSwitchPreference)pref;
                String featureName = switchPref.getFeatureName();
                if (featureName == null)
                    continue;

                boolean enabled = CromiteNativeUtils.isFlagEnabled(featureName);
                switchPref.setChecked(enabled);

                switchPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    CromiteNativeUtils.setFlagEnabled(featureName, (boolean)newValue);
                    if (switchPref.needRestart()) {
                        mRequireRestartDelegateSupplier.get().RequireRestart();
                    }
                    return true;
                });
            }
        }
    }

    /**
     * @return The profile associated with the current Settings screen.
     */
    public Profile getProfile() {
        assert mProfile != null : "Attempting to use the profile before initialization.";
        return mProfile;
    }

    @Override
    public void setProfile(@NonNull Profile profile) {
        mProfile = profile;
    }

    @Override
    public void setCustomTabLauncher(SettingsCustomTabLauncher customTabLauncher) {
        mCustomTabLauncher = customTabLauncher;
    }

    /**
     * @return The launcher for help and feedback actions.
     */
    public HelpAndFeedbackLauncher getHelpAndFeedbackLauncher() {
        return HelpAndFeedbackLauncherFactory.getForProfile(mProfile);
    }

    /**
     * @return The launcher for CCT.
     */
    public SettingsCustomTabLauncher getCustomTabLauncher() {
        return mCustomTabLauncher;
    }
}
