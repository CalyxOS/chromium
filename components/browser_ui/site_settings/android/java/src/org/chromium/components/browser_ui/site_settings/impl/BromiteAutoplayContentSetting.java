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
import org.chromium.components.browser_ui.site_settings.SiteSettingsCategory;
import org.chromium.components.content_settings.ContentSettingValues;
import org.chromium.components.content_settings.ContentSettingsType;
import org.chromium.content_public.browser.BrowserContextHandle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import java.util.ArrayList;

public class BromiteAutoplayContentSetting extends BromiteCustomContentSetting {
    public BromiteAutoplayContentSetting() {
        super(/*contentSettingsType*/ ContentSettingsType.AUTOPLAY,
              /*defaultEnabledValue*/ ContentSettingValues.ALLOW,
              /*defaultDisabledValue*/ ContentSettingValues.BLOCK,
              /*allowException*/ true,
              /*preferenceKey*/ "autoplay",
              /*profilePrefKey*/ "autoplay");
    }

    @Override
    public ContentSettingsResources.ResourceItem getResourceItem() {
        return new ContentSettingsResources.ResourceItem(
            /*icon*/ R.drawable.settings_autoplay,
            /*title*/ R.string.autoplay_permission_title,
            /*defaultEnabledValue*/ getDefaultEnabledValue(),
            /*defaultDisabledValue*/ getDefaultDisabledValue(),
            /*enabledSummary*/ R.string.website_settings_category_autoplay_enabled,
            /*disabledSummary*/ R.string.website_settings_category_autoplay_disabled,
            /*summaryOverrideForScreenReader*/ R.string.website_settings_category_autoplay_a11y);
    }

    @Override
    public int getCategorySummary(@Nullable @ContentSettingValues int value) {
        switch (value) {
            case ContentSettingValues.ALLOW:
                return R.string.website_settings_category_autoplay_enabled;
            case ContentSettingValues.BLOCK:
                return R.string.website_settings_category_autoplay_disabled;
            default:
                return 0;
        }
    }

    @Override
    public int getCategoryDescription() {
        return R.string.website_settings_add_site_description_autoplay;
    }

    @Override
    public boolean requiresTriStateContentSetting() {
        return false;
    }

    @Override
    public boolean showOnlyDescriptions() {
        return true;
    }

    @Override
    public int getAddExceptionDialogMessage() {
        return R.string.website_settings_category_autoplay_enabled;
    }

    @Override
    public @Nullable Boolean considerException(SiteSettingsCategory category, @ContentSettingValues int value) {
        return value != ContentSettingValues.BLOCK;
    }
}
