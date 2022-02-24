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

import android.app.Activity;
import android.content.Context;

import org.chromium.components.browser_ui.site_settings.ContentSettingsResources;
import org.chromium.components.browser_ui.site_settings.SiteSettingsCategory;
import org.chromium.components.content_settings.ContentSettingValues;
import org.chromium.components.content_settings.ContentSettingsType;
import org.chromium.content_public.browser.BrowserContextHandle;
import org.chromium.components.browser_ui.settings.ChromeBasePreference;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BromiteCustomContentSettingImpl
                            extends BromiteCustomContentSettingImplBase {

    public static SiteSettingsCategory createFromType(
            BrowserContextHandle browserContextHandle, @SiteSettingsCategory.Type int type) {
        for (BromiteCustomContentSetting cs : mItemList) {
            if (type == cs.getSiteSettingsCategory()) {
                return new SiteSettingsCategory(browserContextHandle, type, "");
            }
        }
        return null;
    }

    public static int NUM_ENTRIES() {
        return BromiteCustomContentSettingImplBase.NUM_ENTRIES();
    }

    public static BromiteCustomContentSetting getContentSetting(@ContentSettingsType int type) {
        for (BromiteCustomContentSetting cs : mItemList) {
            if (type == cs.getContentSetting()) {
                return cs;
            }
        }
        return null;
    }

    public static @Nullable String getPreferenceKey(@SiteSettingsCategory.Type int type) {
        for (BromiteCustomContentSetting cs : mItemList) {
            if (type == cs.getSiteSettingsCategory()) {
                return cs.getPreferenceKey();
            }
        }
        return null;
    }

   public static String getProfilePrefKey(@ContentSettingsType int type) {
        BromiteCustomContentSetting cs = getContentSetting(type);
        if (cs != null) return cs.getProfilePrefKey();
        return null;
    }

    public static @ContentSettingsType int contentSettingsType(@SiteSettingsCategory.Type int type) {
        for (BromiteCustomContentSetting cs : mItemList) {
            if (type == cs.getSiteSettingsCategory()) {
                return cs.getContentSetting();
            }
        }
        assert false;
        return ContentSettingsType.DEFAULT; // Conversion unavailable.
    }

    public static WebsitePermissionsFetcher.WebsitePermissionsType getPermissionsType(
            @ContentSettingsType int type) {
        BromiteCustomContentSetting cs = getContentSetting(type);
        if (cs != null) return cs.getPermissionsType();
        return null;
    }

    public static ContentSettingsResources.ResourceItem getResourceItem(@ContentSettingsType int type) {
        BromiteCustomContentSetting cs = getContentSetting(type);
        if (cs != null) return cs.getResourceItem();
        return null;
    }

    public static int getCategorySummary(@ContentSettingsType int type, @Nullable @ContentSettingValues int value) {
        BromiteCustomContentSetting cs = getContentSetting(type);
        if (cs != null) return cs.getCategorySummary(value);
        return 0;
    }

    public static boolean requiresTriStateContentSetting(@ContentSettingsType int type) {
        BromiteCustomContentSetting cs = getContentSetting(type);
        if (cs != null) return cs.requiresTriStateContentSetting();
        return false;
    }

    public static int[] getTriStateSettingDescriptionIDs(@ContentSettingsType int type) {
        BromiteCustomContentSetting cs = getContentSetting(type);
        if (cs != null) return cs.getTriStateSettingDescriptionIDs();
        return null;
    }

    public static int getCategoryDescription(SiteSettingsCategory category) {
        for (BromiteCustomContentSetting cs : mItemList) {
            if (category.getType() == cs.getSiteSettingsCategory()) {
                return cs.getCategoryDescription();
            }
        }
        return 0;
    }

    public static boolean onPreferenceChange(SiteSettingsCategory category,
                                             BrowserContextHandle browserContextHandle,
                                             Preference preference, Object newValue) {
        for (BromiteCustomContentSetting cs : mItemList) {
            if (category.getType() != cs.getSiteSettingsCategory()) {
                continue;
            }

            if (SingleCategorySettings.BINARY_TOGGLE_KEY.equals(preference.getKey())) {
                int setting = ((boolean) newValue) == true ? cs.getDefaultEnabledValue() :
                                                             cs.getDefaultDisabledValue();

                WebsitePreferenceBridge.setDefaultContentSetting(browserContextHandle,
                        cs.getContentSetting(), setting);
                return true;
            }
        }

        return false;
    }

    public static boolean processOnBlockList(@ContentSettingsType int type, @ContentSettingValues Integer value) {
        BromiteCustomContentSetting cs = getContentSetting(type);
        if (cs != null) return cs.processOnBlockList(value);
        return false;
    }

    public static boolean isOnBlockList(@ContentSettingsType int type,
                                        WebsitePreference website,
                                        @ContentSettingValues Integer contentSetting) {
        BromiteCustomContentSetting cs = getContentSetting(type);
        if (cs != null) return cs.isOnBlockList(contentSetting);
        return false;
    }

    public static @Nullable Boolean considerException(SiteSettingsCategory category, @ContentSettingValues int value) {
        for (BromiteCustomContentSetting cs : mItemList) {
            if (category.getType() == cs.getSiteSettingsCategory()) {
                return cs.considerException(category, value);
            }
        }
        return null;
    }

    public static int getAddExceptionDialogMessage(SiteSettingsCategory category) {
        for (BromiteCustomContentSetting cs : mItemList) {
            if (category.getType() == cs.getSiteSettingsCategory()) {
                return cs.getAddExceptionDialogMessage();
            }
        }
        return 0;
    }

    public static @Nullable Boolean allowSpecifyingExceptions(SiteSettingsCategory category) {
        for (BromiteCustomContentSetting cs : mItemList) {
            if (category.getType() == cs.getSiteSettingsCategory()) {
                return cs.isExceptionAllowed();
            }
        }
        return null;
    }

    public static void configurePreferences(SiteSettings settings) {
        Activity activity = settings.getActivity();
        PreferenceScreen preferenceScreen = settings.getPreferenceScreen();

        Context styledContext = settings.getPreferenceManager().getContext();
        for (BromiteCustomContentSetting cs : mItemList) {
            ChromeBasePreference pref = new ChromeBasePreference(styledContext);
            pref.setKey(cs.getPreferenceKey());
            pref.setFragment(SingleCategorySettings.class.getCanonicalName());
            preferenceScreen.addPreference(pref);
        }
    }

    public static List<Integer> getSettingsOrder() {
        int[] settingOrder = SiteSettingsUtil.SETTINGS_ORDER;
        List<Integer> order = new ArrayList<Integer>();
        for (int i = 0; i < settingOrder.length && order.add(settingOrder[i]); i++);

        for (BromiteCustomContentSetting cs : mItemList) {
            if (cs.showIntoInfoPage() && !order.contains(cs.getContentSetting())) {
               order.add(cs.getContentSetting());
            }
        }
        return order;
    }

    public static void onActivityCreated(SingleCategorySettings singleCategorySettings) {
    }

    public static void configureGlobalToggles(SiteSettingsCategory category, SingleCategorySettings setting) {
        for (BromiteCustomContentSetting cs : mItemList) {
            if (category.getType() == cs.getSiteSettingsCategory()) {
                cs.configureGlobalToggles(category, setting);
            }
        }
    }

    public static boolean isHelpAndFeedbackEnabled(SiteSettingsCategory category) {
        for (BromiteCustomContentSetting cs : mItemList) {
            if (category.getType() == cs.getSiteSettingsCategory()) {
                return cs.isHelpAndFeedbackEnabled();
            }
        }
        return false;
    }

    public static String getHelpAndFeedbackActivityUrl(SiteSettingsCategory category) {
        for (BromiteCustomContentSetting cs : mItemList) {
            if (category.getType() == cs.getSiteSettingsCategory()) {
                return cs.getHelpAndFeedbackActivityUrl();
            }
        }
        return "";
    }

    public static ContentSettingException createCustomException(@ContentSettingsType int type,
                                                                @ContentSettingValues int value,
                                                                WebsiteAddress websiteAddress) {
        BromiteCustomContentSetting cs = getContentSetting(type);
        if (cs != null) {
            ContentSettingException exception = cs.createCustomException(type, value, websiteAddress);
            if (exception == null) {
                exception = new ContentSettingException(
                    cs.getContentSetting(), websiteAddress.getHost(), value, "",
                    /*isEmbargoed=*/false);
            }
            return exception;
        }
        return null;
    }

    public static AlertDialog.Builder buildPreferenceDialog(Website site, @ContentSettingsType int type,
                                                            BrowserContextHandle browserContextHandle,
                                                            Context context,
                                                            final DialogInterface.OnClickListener listener) {
        BromiteCustomContentSetting cs = getContentSetting(type);
        if (cs != null && cs.requiresTriStateContentSetting()) {
            int[] values = cs.getTriStateSettingDescriptionIDs();

            CharSequence[] descriptions = new String[3];
            descriptions[0] = context.getString(values[0]); // ALLOWED
            descriptions[1] = context.getString(values[1]); // ASK
            descriptions[2] = context.getString(values[2]); // BLOCKED

            @ContentSettingValues
            Integer value = site.getContentSetting(browserContextHandle, type);

            return new AlertDialog.Builder(context, R.style.ThemeOverlay_BrowserUI_AlertDialog)
                    .setPositiveButton(R.string.cancel, null)
                    .setNegativeButton(R.string.remove,
                            (dialog, which) -> {
                                site.setContentSetting(browserContextHandle, type,
                                        ContentSettingValues.DEFAULT);
                                listener.onClick(dialog, which);
                                dialog.dismiss();
                            })
                    .setSingleChoiceItems(descriptions,
                            value == ContentSettingValues.ALLOW ? 0 :
                            value == ContentSettingValues.ASK ? 1 :
                                     2,
                            (dialog, which) -> {
                                @ContentSettingValues
                                int permission = which == 0 ? ContentSettingValues.ALLOW :
                                                 which == 1 ? ContentSettingValues.ASK :
                                                              ContentSettingValues.BLOCK;
                                site.setContentSetting(
                                        browserContextHandle, type, permission);

                                listener.onClick(dialog, which);
                                dialog.dismiss();
                            });
        }
        return null;
    }
}
