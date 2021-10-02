/*
    This file is part of Bromite.

    Bromite is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Bromite is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Bromite. If not, see <https://www.gnu.org/licenses/>.
*/

package org.chromium.chrome.browser.privacy.settings;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.provider.Browser;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import org.chromium.chrome.R;
import org.chromium.chrome.browser.preferences.Pref;
import org.chromium.chrome.browser.profiles.Profile;
import org.chromium.chrome.browser.profiles.ProfileManager;
import org.chromium.chrome.browser.AlwaysIncognitoLinkInterceptor;
import org.chromium.chrome.browser.ui.messages.snackbar.SnackbarManager;
import org.chromium.chrome.browser.ui.messages.snackbar.INeedSnackbarManager;
import org.chromium.chrome.browser.ui.messages.snackbar.Snackbar;
import org.chromium.chrome.browser.ApplicationLifetime;
import org.chromium.components.browser_ui.settings.ChromeSwitchPreference;
import org.chromium.components.browser_ui.settings.SettingsUtils;
import org.chromium.components.prefs.PrefService;
import org.chromium.components.user_prefs.UserPrefs;

/**
 * Fragment to keep track of the all the always incognito related preferences.
 */
public class IncognitoSettings
        extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener,
                                                    INeedSnackbarManager {
    private SnackbarManager mSnackbarManager;
    private Snackbar mSnackbar;

    private static final String PREF_ALWAYS_INCOGNITO = "always_incognito";
    private static final String PREF_INCOGNITO_TAB_HISTORY = "incognito_history";
    private static final String PREF_INCOGNITO_SAVE_SITE_SETTING = "incognito_save_site_setting";

    private final PrefService prefService = UserPrefs.get(ProfileManager.getLastUsedRegularProfile());

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PrivacyPreferencesManagerImpl privacyPrefManager =
                PrivacyPreferencesManagerImpl.getInstance();
        SettingsUtils.addPreferencesFromResource(this, R.xml.incognito_preferences);
        getActivity().setTitle(R.string.incognito_settings_title);

        setHasOptionsMenu(true);

        updatePreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferences();
    }

    public void updatePreferences() {
        ChromeSwitchPreference alwaysIncognitoPref =
                (ChromeSwitchPreference) findPreference(PREF_ALWAYS_INCOGNITO);
        alwaysIncognitoPref.setChecked(
                prefService.getBoolean(Pref.ALWAYS_INCOGNITO_ENABLED));
        alwaysIncognitoPref.setOnPreferenceChangeListener(this);

        mSnackbar = Snackbar.make(getActivity().getString(R.string.ui_relaunch_notice),
                new SnackbarManager.SnackbarController() {
                        @Override
                        public void onDismissNoAction(Object actionData) { }

                        @Override
                        public void onAction(Object actionData) {
                                ApplicationLifetime.terminate(true);
                        }
                }, Snackbar.TYPE_NOTIFICATION, Snackbar.UMA_UNKNOWN)
                .setSingleLine(false)
                .setAction(getActivity().getString(R.string.relaunch),
                        /*actionData*/null)
                .setDuration(/*durationMs*/70000);

        ChromeSwitchPreference historyInIncognitoPref =
                (ChromeSwitchPreference) findPreference(PREF_INCOGNITO_TAB_HISTORY);
        historyInIncognitoPref.setChecked(
                prefService.getBoolean(Pref.INCOGNITO_TAB_HISTORY_ENABLED));
        historyInIncognitoPref.setOnPreferenceChangeListener(this);

        ChromeSwitchPreference saveSiteSettingsPref =
                (ChromeSwitchPreference) findPreference(PREF_INCOGNITO_SAVE_SITE_SETTING);
        saveSiteSettingsPref.setChecked(
                prefService.getBoolean(Pref.INCOGNITO_SAVE_SITE_SETTING_ENABLED));
        saveSiteSettingsPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (PREF_ALWAYS_INCOGNITO.equals(key)) {
            AlwaysIncognitoLinkInterceptor.setAlwaysIncognito((boolean) newValue);
        } else if (PREF_INCOGNITO_TAB_HISTORY.equals(key)) {
            prefService.setBoolean(Pref.INCOGNITO_TAB_HISTORY_ENABLED, (boolean) newValue);
        } else if (PREF_INCOGNITO_SAVE_SITE_SETTING.equals(key)) {
            prefService.setBoolean(Pref.INCOGNITO_SAVE_SITE_SETTING_ENABLED, (boolean) newValue);
        }
        if (!mSnackbarManager.isShowing()) {
            mSnackbarManager.showSnackbar(mSnackbar);
        }
        return true;
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
            Context context = getContext();

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bromite/bromite/wiki/AlwaysIncognito"));
            // Let Chromium know that this intent is from Chromium, so that it does not close the app when
            // the user presses 'back' button.
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
            intent.putExtra(Browser.EXTRA_CREATE_NEW_TAB, true);
            intent.setPackage(context.getPackageName());
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public void setSnackbarManager(SnackbarManager manager) {
        mSnackbarManager = manager;
    }
}
