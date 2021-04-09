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

package org.chromium.chrome.browser.settings;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceViewHolder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.widget.RadioGroup;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import androidx.recyclerview.widget.RecyclerView;

import org.chromium.components.browser_ui.settings.ChromeSwitchPreference;
import org.chromium.components.browser_ui.widget.RadioButtonWithDescription;
import org.chromium.components.browser_ui.widget.RadioButtonWithEditText;
import org.chromium.components.browser_ui.settings.SettingsUtils;

import org.chromium.chrome.browser.preferences.ChromePreferenceKeys;
import org.chromium.chrome.browser.preferences.SharedPreferencesManager;

import org.chromium.chrome.browser.app.tabmodel.TabWindowManagerSingleton;
import org.chromium.chrome.browser.tabmodel.TabWindowManager;
import org.chromium.chrome.browser.privacy.settings.PrivacyPreferencesManagerImpl;
import org.chromium.chrome.R;

/**
 * Fragment that allows the user to configure User Agent related preferences.
 */
public class UserAgentPreferences
        extends PreferenceFragmentCompat implements RadioGroup.OnCheckedChangeListener {

    private static final String PREF_STICK_DESKTOP_MODE_SWITCH = "desktop_mode_switch";
    private RadioButtonWithDescription useDefaultAgentSwitch;
    private RadioButtonWithEditText useCustomAgentSwitch;
    private RadioButtonWithDescription useDefaultAgentSwitchDesktopMode;
    private RadioButtonWithEditText useCustomAgentSwitchDesktopMode;
    private RadioGroup mRadioGroup;
    private RadioGroup mRadioGroupDesktopMode;
    private CheckBox mDesktopModeViewportmeta;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getActivity().setTitle(R.string.useragent_settings_title);
        SettingsUtils.addPreferencesFromResource(this, R.xml.useragent_preferences);

        ChromeSwitchPreference alwaysDesktopModeSwitch =
                (ChromeSwitchPreference) findPreference(PREF_STICK_DESKTOP_MODE_SWITCH);
        boolean enabled = SharedPreferencesManager.getInstance().readBoolean(
            ChromePreferenceKeys.USERAGENT_STICKY_DESKTOP_MODE, false);
        alwaysDesktopModeSwitch.setChecked(enabled);
        alwaysDesktopModeSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
            SharedPreferencesManager.getInstance().writeBoolean(
                ChromePreferenceKeys.USERAGENT_STICKY_DESKTOP_MODE, (boolean) newValue);
            UpdateAllTabs();
            return true;
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        LinearLayout viewGroup = (LinearLayout) super.onCreateView(inflater, container, savedInstanceState);
        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        viewGroup.setLayoutParams(params);
        ScrollView view = (ScrollView) inflater.inflate(R.layout.custom_useragent_preferences, viewGroup, false);
        viewGroup.addView(view);

        boolean enabledCustomUA = PrivacyPreferencesManagerImpl.getInstance().isOverrideUserAgentEnabled(false);
        boolean enabledCustomUADesktopMode = PrivacyPreferencesManagerImpl.getInstance().isOverrideUserAgentEnabled(true);
        boolean enabledDesktopModeViewportmeta = PrivacyPreferencesManagerImpl.getInstance().isDesktopModeViewportMetaEnabled();

        useDefaultAgentSwitch =
                (RadioButtonWithDescription) view.findViewById(R.id.default_ua_switch);
        useCustomAgentSwitch =
                (RadioButtonWithEditText) view.findViewById(R.id.custom_ua_switch);
        useDefaultAgentSwitchDesktopMode =
                (RadioButtonWithDescription) view.findViewById(R.id.default_ua_switch_dm);
        useCustomAgentSwitchDesktopMode =
                (RadioButtonWithEditText) view.findViewById(R.id.custom_ua_switch_dm);

        mRadioGroup = (RadioGroup) view.findViewById(R.id.ua_radio_button_layout);
        mRadioGroup.setOnCheckedChangeListener(this);

        mRadioGroupDesktopMode = (RadioGroup) view.findViewById(R.id.ua_radio_button_layout_dm);
        mRadioGroupDesktopMode.setOnCheckedChangeListener(this);

        mDesktopModeViewportmeta = (CheckBox) view.findViewById(R.id.desktop_mode_viewportmeta);
        mDesktopModeViewportmeta.setChecked(enabledDesktopModeViewportmeta);
        mDesktopModeViewportmeta.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrivacyPreferencesManagerImpl.getInstance().setDesktopModeViewportMetaEnabled(
                    mDesktopModeViewportmeta.isChecked());
            }
        });

        useDefaultAgentSwitch.setChecked(!enabledCustomUA);
        useCustomAgentSwitch.setChecked(enabledCustomUA);

        useDefaultAgentSwitchDesktopMode.setChecked(!enabledCustomUADesktopMode);
        useCustomAgentSwitchDesktopMode.setChecked(enabledCustomUADesktopMode);

        useCustomAgentSwitch.setPrimaryText(
            PrivacyPreferencesManagerImpl.getInstance().getOverrideUserAgentValue(false));
        useCustomAgentSwitch.addTextChangeListener(new RadioButtonWithEditText.OnTextChangeListener() {
            @Override
            public void onTextChanged(CharSequence newText) {
                PrivacyPreferencesManagerImpl.getInstance().setOverrideUserAgentValue(
                    newText.toString(), false);
            }
        });
        useCustomAgentSwitch.setFocusChangeListener( hasFocus -> {
            if( hasFocus )
                PrivacyPreferencesManagerImpl.getInstance().setOverrideUserAgentEnabled(true, false);
        });

        useCustomAgentSwitchDesktopMode.setPrimaryText(
            PrivacyPreferencesManagerImpl.getInstance().getOverrideUserAgentValue(true));
        useCustomAgentSwitchDesktopMode.addTextChangeListener(new RadioButtonWithEditText.OnTextChangeListener() {
            @Override
            public void onTextChanged(CharSequence newText) {
                PrivacyPreferencesManagerImpl.getInstance().setOverrideUserAgentValue(
                    newText.toString(), true);
            }
        });
        useCustomAgentSwitchDesktopMode.setFocusChangeListener( hasFocus -> {
            if( hasFocus )
                PrivacyPreferencesManagerImpl.getInstance().setOverrideUserAgentEnabled(true, true);
        });

        return viewGroup;
    }

    private void UpdateAllTabs() {
        final boolean alwaysDesktopModeEnabled = SharedPreferencesManager.getInstance().readBoolean(
            ChromePreferenceKeys.USERAGENT_ALWAYS_DESKTOP_MODE, false);
        TabWindowManagerSingleton.getInstance().SetOverrideUserAgentForAllTabs(alwaysDesktopModeEnabled);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (useDefaultAgentSwitch.isChecked()) {
            PrivacyPreferencesManagerImpl.getInstance().setOverrideUserAgentEnabled(false, false);
        } else if (useCustomAgentSwitch.isChecked()) {
            PrivacyPreferencesManagerImpl.getInstance().setOverrideUserAgentEnabled(true, false);
        }

        if (useDefaultAgentSwitchDesktopMode.isChecked()) {
            PrivacyPreferencesManagerImpl.getInstance().setOverrideUserAgentEnabled(false, true);
        } else if (useCustomAgentSwitchDesktopMode.isChecked()) {
            PrivacyPreferencesManagerImpl.getInstance().setOverrideUserAgentEnabled(true, true);
        }

        UpdateAllTabs();
    }

    @Override
    public void onStop() {
        super.onStop();
        UpdateAllTabs();
    }
}
