package org.chromium.chrome.browser.privacy.settings;

import android.os.Bundle;

import org.chromium.chrome.R;
import org.chromium.chrome.browser.preferences.Pref;
import org.chromium.chrome.browser.settings.ChromeBaseSettingsFragment;
import org.chromium.components.browser_ui.settings.ChromeSwitchPreference;
import org.chromium.components.browser_ui.settings.SettingsUtils;
import org.chromium.components.prefs.PrefService;
import org.chromium.components.user_prefs.UserPrefs;

public class GpcSettings extends ChromeBaseSettingsFragment {
    private static final String PREF_SWITCH = "gpc_switch";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        SettingsUtils.addPreferencesFromResource(this, R.xml.gpc_preferences);
        getActivity().setTitle(R.string.gpc_title);

        ChromeSwitchPreference switchPreference =
                (ChromeSwitchPreference) findPreference(PREF_SWITCH);

        PrefService prefService = UserPrefs.get(getProfile());
        boolean isEnabled = prefService.getBoolean(Pref.ENABLE_GPC);
        switchPreference.setChecked(isEnabled);

        switchPreference.setOnPreferenceChangeListener(
                (preference, newValue) -> {
                    prefService.setBoolean(Pref.ENABLE_GPC, (boolean) newValue);
                    return true;
                });
    }
}
