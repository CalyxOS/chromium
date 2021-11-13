// Copyright 2015 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.privacy.settings;

import static org.chromium.components.content_settings.PrefNames.COOKIE_CONTROLS_MODE;

import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import org.chromium.base.metrics.RecordHistogram;
import org.chromium.base.metrics.RecordUserAction;
import org.chromium.chrome.R;
import org.chromium.chrome.browser.feedback.HelpAndFeedbackLauncherImpl;
import org.chromium.chrome.browser.flags.ChromeFeatureList;
import org.chromium.chrome.browser.incognito.reauth.IncognitoReauthSettingSwitchPreference;
import org.chromium.chrome.browser.preferences.Pref;
import org.chromium.chrome.browser.prefetch.settings.PreloadPagesSettingsFragment;
import org.chromium.chrome.browser.privacy.secure_dns.SecureDnsSettings;
import org.chromium.chrome.browser.privacy_guide.PrivacyGuideInteractions;
import org.chromium.chrome.browser.privacy_sandbox.PrivacySandboxBridge;
import org.chromium.chrome.browser.privacy_sandbox.PrivacySandboxReferrer;
import org.chromium.chrome.browser.privacy_sandbox.PrivacySandboxSettingsBaseFragment;
import org.chromium.chrome.browser.profiles.Profile;
import org.chromium.chrome.browser.settings.ChromeManagedPreferenceDelegate;
import org.chromium.chrome.browser.settings.SettingsLauncherImpl;
import org.chromium.components.browser_ui.settings.ChromeSwitchPreference;
import org.chromium.components.browser_ui.settings.ManagedPreferenceDelegate;
import org.chromium.components.browser_ui.settings.SettingsLauncher;
import org.chromium.components.browser_ui.settings.SettingsUtils;
import org.chromium.components.browser_ui.site_settings.ContentSettingsResources;
import org.chromium.components.browser_ui.site_settings.SingleCategorySettings;
import org.chromium.components.prefs.PrefService;
import org.chromium.components.signin.identitymanager.ConsentLevel;
import org.chromium.components.user_prefs.UserPrefs;
import org.chromium.ui.text.NoUnderlineClickableSpan;
import org.chromium.ui.text.SpanApplier;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceCategory;
import org.chromium.chrome.browser.preferences.SharedPreferencesManager;
import org.chromium.components.browser_ui.settings.ChromeSwitchPreference;

/**
 * Fragment to keep track of the all the privacy related preferences.
 */
public class PrivacySettings
        extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
    private static final String PREF_CAN_MAKE_PAYMENT = "can_make_payment";
    private static final String PREF_PRELOAD_PAGES = "preload_pages";
    private static final String PREF_HTTPS_FIRST_MODE = "https_first_mode";
    private static final String PREF_SECURE_DNS = "secure_dns";
    private static final String PREF_DO_NOT_TRACK = "do_not_track";
    private static final String PREF_CLEAR_BROWSING_DATA = "clear_browsing_data";
    private static final String PREF_PRIVACY_GUIDE = "privacy_guide";
    private static final String PREF_INCOGNITO_LOCK = "incognito_lock";
    private static final String PREF_THIRD_PARTY_COOKIES = "third_party_cookies";

    // moved from SyncAndServicesSettings.java
    private static final String PREF_SERVICES_CATEGORY = "services_category";
    private static final String PREF_SEARCH_SUGGESTIONS = "search_suggestions";
    private ChromeSwitchPreference mSearchSuggestions;
    private final SharedPreferencesManager mSharedPreferencesManager =
            SharedPreferencesManager.getInstance();
    private final PrefService prefService = UserPrefs.get(Profile.getLastUsedRegularProfile());

    private ManagedPreferenceDelegate mManagedPreferenceDelegate;
    private IncognitoLockSettings mIncognitoLockSettings;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PrivacyPreferencesManagerImpl privacyPrefManager =
                PrivacyPreferencesManagerImpl.getInstance();
        getActivity().setTitle(R.string.prefs_privacy_security);

        Preference privacyGuidePreference = findPreference(PREF_PRIVACY_GUIDE);
        // Record the launch of PG from the S&P link-row entry point
        privacyGuidePreference.setOnPreferenceClickListener(preference -> {
            RecordUserAction.record("Settings.PrivacyGuide.StartPrivacySettings");
            RecordHistogram.recordEnumeratedHistogram("Settings.PrivacyGuide.EntryExit",
                    PrivacyGuideInteractions.SETTINGS_LINK_ROW_ENTRY,
                    PrivacyGuideInteractions.MAX_VALUE);
            return false;
        });
        if (!ChromeFeatureList.isEnabled(ChromeFeatureList.PRIVACY_GUIDE)) {
            getPreferenceScreen().removePreference(privacyGuidePreference);
        }

        IncognitoReauthSettingSwitchPreference incognitoReauthPreference =
                (IncognitoReauthSettingSwitchPreference) findPreference(PREF_INCOGNITO_LOCK);
        mIncognitoLockSettings = new IncognitoLockSettings(incognitoReauthPreference);
        mIncognitoLockSettings.setUpIncognitoReauthPreference(getActivity());

        setHasOptionsMenu(true);

        mManagedPreferenceDelegate = createManagedPreferenceDelegate();

        mSearchSuggestions = (ChromeSwitchPreference) findPreference(PREF_SEARCH_SUGGESTIONS);
        mSearchSuggestions.setOnPreferenceChangeListener(this);
        mSearchSuggestions.setManagedPreferenceDelegate(mManagedPreferenceDelegate);

        ChromeSwitchPreference canMakePaymentPref =
                (ChromeSwitchPreference) findPreference(PREF_CAN_MAKE_PAYMENT);
        canMakePaymentPref.setOnPreferenceChangeListener(this);

        ChromeSwitchPreference httpsFirstModePref =
                (ChromeSwitchPreference) findPreference(PREF_HTTPS_FIRST_MODE);
        httpsFirstModePref.setVisible(
                ChromeFeatureList.isEnabled(ChromeFeatureList.HTTPS_FIRST_MODE));
        httpsFirstModePref.setOnPreferenceChangeListener(this);
        httpsFirstModePref.setManagedPreferenceDelegate(mManagedPreferenceDelegate);
        httpsFirstModePref.setChecked(UserPrefs.get(Profile.getLastUsedRegularProfile())
                                              .getBoolean(Pref.HTTPS_ONLY_MODE_ENABLED));

        Preference secureDnsPref = findPreference(PREF_SECURE_DNS);
        secureDnsPref.setVisible(SecureDnsSettings.isUiEnabled());

        Preference thirdPartyCookies = findPreference(PREF_THIRD_PARTY_COOKIES);
        if (thirdPartyCookies != null) {
            thirdPartyCookies.getExtras().putString(
                    SingleCategorySettings.EXTRA_CATEGORY, thirdPartyCookies.getKey());
            thirdPartyCookies.getExtras().putString(
                    SingleCategorySettings.EXTRA_TITLE, thirdPartyCookies.getTitle().toString());
        }

        updatePreferences();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (PREF_CAN_MAKE_PAYMENT.equals(key)) {
            UserPrefs.get(Profile.getLastUsedRegularProfile())
                    .setBoolean(Pref.CAN_MAKE_PAYMENT_ENABLED, (boolean) newValue);
        } else if (PREF_HTTPS_FIRST_MODE.equals(key)) {
            UserPrefs.get(Profile.getLastUsedRegularProfile())
                    .setBoolean(Pref.HTTPS_ONLY_MODE_ENABLED, (boolean) newValue);
        } else if (PREF_SEARCH_SUGGESTIONS.equals(key)) {
            UserPrefs.get(Profile.getLastUsedRegularProfile())
                    .setBoolean(Pref.SEARCH_SUGGEST_ENABLED, (boolean) newValue);
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferences();
    }

    /**
     * Updates the preferences.
     */
    public void updatePreferences() {
        mSearchSuggestions.setChecked(prefService.getBoolean(Pref.SEARCH_SUGGEST_ENABLED));

        ChromeSwitchPreference canMakePaymentPref =
                (ChromeSwitchPreference) findPreference(PREF_CAN_MAKE_PAYMENT);
        if (canMakePaymentPref != null) {
            canMakePaymentPref.setChecked(prefService.getBoolean(Pref.CAN_MAKE_PAYMENT_ENABLED));
        }

        Preference doNotTrackPref = findPreference(PREF_DO_NOT_TRACK);
        if (doNotTrackPref != null) {
            doNotTrackPref.setSummary(prefService.getBoolean(Pref.ENABLE_DO_NOT_TRACK)
                            ? R.string.text_on
                            : R.string.text_off);
        }

        Preference preloadPagesPreference = findPreference(PREF_PRELOAD_PAGES);
        if (preloadPagesPreference != null) {
            preloadPagesPreference.setSummary(
                    PreloadPagesSettingsFragment.getPreloadPagesSummaryString(getContext()));
        }

        Preference secureDnsPref = findPreference(PREF_SECURE_DNS);
        if (secureDnsPref != null && secureDnsPref.isVisible()) {
            secureDnsPref.setSummary(SecureDnsSettings.getSummary(getContext()));
        }

        mIncognitoLockSettings.updateIncognitoReauthPreferenceIfNeeded(getActivity());

        Preference thirdPartyCookies = findPreference(PREF_THIRD_PARTY_COOKIES);
        if (thirdPartyCookies != null) {
            thirdPartyCookies.setSummary(ContentSettingsResources.getThirdPartyCookieListSummary(
                    prefService.getInteger(COOKIE_CONTROLS_MODE)));
        }
    }

    private ChromeManagedPreferenceDelegate createManagedPreferenceDelegate() {
        return preference -> {
            String key = preference.getKey();
            if (PREF_HTTPS_FIRST_MODE.equals(key)) {
                return UserPrefs.get(Profile.getLastUsedRegularProfile())
                        .isManagedPreference(Pref.HTTPS_ONLY_MODE_ENABLED);
            }
            return false;
        };
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        MenuItem help =
                menu.add(Menu.NONE, R.id.menu_id_targeted_help, Menu.NONE, R.string.menu_help);
        help.setIcon(VectorDrawableCompat.create(
                getResources(), R.drawable.ic_help_and_feedback, getActivity().getTheme()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_id_targeted_help) {
            HelpAndFeedbackLauncherImpl.getInstance().show(getActivity(),
                    getString(R.string.help_context_privacy), Profile.getLastUsedRegularProfile(),
                    null);
            return true;
        }
        return false;
    }
}
