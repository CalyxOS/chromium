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

package org.chromium.components.browser_ui.site_settings;

import android.content.Context;

import org.chromium.components.browser_ui.site_settings.ContentSettingsResources;
import org.chromium.components.browser_ui.site_settings.SiteSettingsCategory;
import org.chromium.components.content_settings.ContentSettingValues;
import org.chromium.components.content_settings.ContentSettingsType;
import org.chromium.content_public.browser.BrowserContextHandle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import java.util.ArrayList;

public abstract class BromiteCustomContentSetting {

    private @ContentSettingsType.EnumType int mContentSettingsType;
    private @SiteSettingsCategory.Type int mSiteSettingsCategory;
    private @ContentSettingValues Integer mDefaultEnabledValue;
    private @ContentSettingValues Integer mDefaultDisabledValue;
    private boolean mAllowException;
    private String mPreferenceKey;
    private String mProfilePrefKey;

    public BromiteCustomContentSetting(@ContentSettingsType.EnumType int contentSettingsType,
                                       @ContentSettingValues Integer defaultEnabledValue,
                                       @ContentSettingValues Integer defaultDisabledValue,
                                       boolean allowException,
                                       String preferenceKey,
                                       String profilePrefKey) {
        mContentSettingsType = contentSettingsType;
        mDefaultEnabledValue = defaultEnabledValue;
        mDefaultDisabledValue = defaultDisabledValue;
        mAllowException = allowException;
        mPreferenceKey = preferenceKey;
        mProfilePrefKey = profilePrefKey;
    }

    public @ContentSettingsType.EnumType int getContentSetting() {
        return mContentSettingsType;
    }

    public void setSiteSettingsCategory(int value) {
        mSiteSettingsCategory = value;
    }

    public @SiteSettingsCategory.Type int getSiteSettingsCategory() {
        return mSiteSettingsCategory;
    }

    protected @ContentSettingValues Integer getDefaultEnabledValue() {
        return mDefaultEnabledValue;
    }

    public @ContentSettingValues Integer getDefaultDisabledValue() {
        return mDefaultDisabledValue;
    }

    public Preference createWebSitePreference(Context context,
                        @ContentSettingValues @Nullable Integer value) {
        return null;
    }

    public boolean setWebSitePreferenceValue(Preference preference,
                        @ContentSettingValues @Nullable Integer value) {
        return false;
    }

    public Integer getWebSitePreferenceValue(Object newValue) {
        return null;
    }

    public String getPreferenceKey() {
        return mPreferenceKey;
    }

    public String getProfilePrefKey() {
        return mProfilePrefKey;
    }

    public boolean isExceptionAllowed() {
        return mAllowException;
    }

    public WebsitePermissionsFetcher.WebsitePermissionsType getPermissionsType() {
        return WebsitePermissionsFetcher.WebsitePermissionsType.CONTENT_SETTING_EXCEPTION;
    }

    public abstract ContentSettingsResources.ResourceItem getResourceItem();
    public abstract int getCategorySummary(@Nullable @ContentSettingValues int value);
    public abstract int getAddExceptionDialogMessage();
    public abstract int getCategoryDescription();

    public boolean processOnBlockList(@ContentSettingValues Integer value) {
        return true;
    }

    public boolean isOnBlockList(@ContentSettingValues Integer contentSetting) {
        return mDefaultDisabledValue == contentSetting;
    }

    public abstract @Nullable Boolean considerException(SiteSettingsCategory category, @ContentSettingValues int value);

    public ContentSettingException createCustomException(@ContentSettingsType.EnumType int type,
                                                         @ContentSettingValues int value,
                                                         WebsiteAddress websiteAddress) {
        return null;
    }

    public void configureGlobalToggles(SiteSettingsCategory category, SingleCategorySettings setting) {
    }

    public boolean isHelpAndFeedbackEnabled() {
        return false;
    }

    public String getHelpAndFeedbackActivityUrl() {
        return "";
    }

    public boolean requiresTriStateContentSetting() {
        return false;
    }

    public int[] getTriStateSettingDescriptionIDs() {
        return null;
    }

    public boolean showOnlyDescriptions() {
        return false;
    }

    public boolean showIntoInfoPage() {
        return true;
    }
}
