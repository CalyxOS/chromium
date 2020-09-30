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

package org.chromium.components.browser_ui.site_settings.impl;

import org.chromium.components.browser_ui.site_settings.R;

import org.chromium.components.browser_ui.site_settings.BromiteCustomContentSetting;
import org.chromium.components.browser_ui.site_settings.ContentSettingsResources;
import org.chromium.components.browser_ui.site_settings.SingleCategorySettings;
import org.chromium.components.browser_ui.site_settings.SiteSettingsCategory;
import org.chromium.components.browser_ui.site_settings.TimezoneOverrideSiteSettingsPreference;
import org.chromium.components.browser_ui.site_settings.WebsitePreferenceBridge;
import org.chromium.components.content_settings.ContentSettingValues;
import org.chromium.components.content_settings.ContentSettingsType;
import org.chromium.content_public.browser.BrowserContextHandle;

import android.view.View;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import java.util.ArrayList;

public class BromiteTimezoneOverrideContentSetting extends BromiteCustomContentSetting {
    private static final String TIMEOVERRIDE_STATE_TOGGLE_KEY = "timeoverride_state_toggle";

    public BromiteTimezoneOverrideContentSetting() {
        super(/*contentSettingsType*/ ContentSettingsType.TIMEZONE_OVERRIDE,
              /*defaultEnabledValue*/ ContentSettingValues.ALLOW,
              /*defaultDisabledValue*/ ContentSettingValues.BLOCK,
              /*allowException*/ true,
              /*preferenceKey*/ "timezone_override",
              /*profilePrefKey*/ "timezone_override_permission_list");
    }

    @Override
    public ContentSettingsResources.ResourceItem getResourceItem() {
        return new ContentSettingsResources.ResourceItem(
            /*icon*/ R.drawable.web_asset,
            /*title*/ R.string.timezone_override_permission_title,
            /*defaultEnabledValue*/ getDefaultEnabledValue(),
            /*defaultDisabledValue*/ getDefaultDisabledValue(),
            /*enabledSummary*/ R.string.website_settings_category_timezone_override_custom,
            /*disabledSummary*/ R.string.website_settings_category_timezone_override_random,
            /*summaryOverrideForScreenReader*/ R.string.website_settings_category_timezone_override_a11y);
    }

    @Override
    public int getCategorySummary(@Nullable @ContentSettingValues int value) {
        switch (value) {
            case ContentSettingValues.ALLOW:
                return R.string.website_settings_category_timezone_override_allowed;
            case ContentSettingValues.ASK:
                return R.string.website_settings_category_timezone_override_custom;
            case ContentSettingValues.BLOCK:
                return R.string.website_settings_category_timezone_override_random;
            default:
                return 0;
        }
    }

    @Override
    public int[] getTriStateSettingDescriptionIDs() {
          int[] descriptionIDs = {
                  R.string.website_settings_category_timezone_override_allowed, // ALLOWED
                  R.string.website_settings_category_timezone_override_custom,  // ASK
                  R.string.website_settings_category_timezone_override_random}; // BLOCKED
          return descriptionIDs;
    }

    @Override
    public int getCategoryDescription() {
        return R.string.website_settings_timeoverride_info;
    }

    @Override
    public boolean requiresTriStateContentSetting() {
        return true;
    }

    @Override
    public boolean showOnlyDescriptions() {
        return true;
    }

    @Override
    public int getAddExceptionDialogMessage() {
        return R.string.website_settings_category_timezone_override_allowed;
    }

    @Override
    public @Nullable Boolean considerException(SiteSettingsCategory category, @ContentSettingValues int value) {
        return value != ContentSettingValues.ALLOW;
    }

    @Override
    public boolean isOnBlockList(@ContentSettingValues Integer contentSetting) {
        return ContentSettingValues.ALLOW != contentSetting;
    }

    @Override
    public boolean isHelpAndFeedbackEnabled() {
        return true;
    }

    @Override
    public String getHelpAndFeedbackActivityUrl() {
        return "https://github.com/bromite/bromite/wiki/TimezoneOverride";
    }

    @Override
    public void configureGlobalToggles(SiteSettingsCategory category, SingleCategorySettings setting) {
        BrowserContextHandle browserContext = setting.getSiteSettingsDelegate().getBrowserContextHandle();
        PreferenceScreen screen = setting.getPreferenceScreen();

        Preference triStateToggle = screen.findPreference(
                                            SingleCategorySettings.TRI_STATE_TOGGLE_KEY);
        int order = triStateToggle.getOrder();
        screen.removePreference(triStateToggle);

        TimezoneOverrideSiteSettingsPreference timeOverrideStatePreference =
            new TimezoneOverrideSiteSettingsPreference(setting.getContext(), null);
        timeOverrideStatePreference.setKey(SingleCategorySettings.TRI_STATE_TOGGLE_KEY);
        screen.addPreference(timeOverrideStatePreference);
        timeOverrideStatePreference.setOrder(order);

        timeOverrideStatePreference.setOnPreferenceChangeListener(setting);
        @ContentSettingValues
        int value = WebsitePreferenceBridge.getDefaultContentSetting(
                browserContext, ContentSettingsType.TIMEZONE_OVERRIDE);
        timeOverrideStatePreference.initialize(value, browserContext);
    }
}
