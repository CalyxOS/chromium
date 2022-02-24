# Content Settings and Site Settings in Bromite

[TOC]

## Overview

To simplify the addition and management of content settings ui in Bromite, you can use this method: automatically the new menus in the site settings and the item in page info management will be added.

## How to use

* Add a class derived from [BromiteCustomContentSettingImpl](BromiteCustomContentSettingImpl.java)

```
public class NewContentSetting extends BromiteCustomContentSetting {
    public BromiteWebGLContentSetting() {
        super(/*contentSettingsType*/ ContentSettingsType.NEW_CONTENT_SETTING,
              /*siteSettingsCategory*/ SiteSettingsCategory.Type.NEW_CONTENT_SETTING,
              /*defaultEnabledValue*/ ContentSettingValues.ALLOW,
              /*defaultDisabledValue*/ ContentSettingValues.BLOCK,
              /*allowException*/ true,
              /*preferenceKey*/ "new_content_setting",
              /*profilePrefKey*/ "new_content_setting");
    }

    @Override
    public ContentSettingsResources.ResourceItem getResourceItem() {
        return new ContentSettingsResources.ResourceItem(
            /*icon*/ R.drawable.web_asset,
            /*title*/ R.string.new_content_setting_permission_title,
            /*defaultEnabledValue*/ getDefaultEnabledValue(),
            /*defaultDisabledValue*/ getDefaultDisabledValue(),
            /*enabledSummary*/ R.string.new_content_setting_enabled,
            /*disabledSummary*/ R.string.new_content_setting_disabled);
    }

    @Override
    public int getCategorySummary(@Nullable @ContentSettingValues int value) {
        switch (value) {
            case ContentSettingValues.ALLOW:
                return R.string.new_content_setting_allow;
            case ContentSettingValues.ASK:
                return R.string.new_content_setting_ask;
            case ContentSettingValues.BLOCK:
                return R.string.new_content_setting_disabled;
            default:
                return 0;
        }
    }

    @Override
    public boolean requiresTriStateContentSetting() {
        return true; // or false if is a on/off content setting
    }

    @Override
    public int[] getTriStateSettingDescriptionIDs() {
        // only needed if is a tristate setting
        int[] descriptionIDs = {
                R.string.website_settings_category_webgl_enabled_antifingerprint,  // ALLOWED
                R.string.website_settings_category_webgl_enabled,                  // ASK
                R.string.website_settings_category_webgl_disabled };               // BLOCKED
        return descriptionIDs;
    }

    @Override
    public boolean showOnlyDescriptions() {
        // true will remove ALLOWED/ASK/BLOCKED from UI
        // leaving only the descriptions
        return true;
    }

    @Override
    public int getAddExceptionDialogMessage() {
        return R.string.new_content_setting_exception_dialog_message;
    }

    @Override
    public @Nullable Boolean considerException(SiteSettingsCategory category, @ContentSettingValues int value) {
        // indicate when the value should be considered an exception
        return value != ContentSettingValues.BLOCK;
    }
}
```

* Add the new class to `BromiteCustomContentSettingImpl` `cctor`

```
    static {
        mItemList = new ArrayList<BromiteCustomContentSetting>();
        mItemList.add(new NewContentSetting());
    }
```

* Register the new content setting as usual in `ContentSettingsRegistry::Init()`

```
 Register(ContentSettingsType::NEW_CONTENT_SETTING, "new_content_setting", CONTENT_SETTING_BLOCK,
           WebsiteSettingsInfo::SYNCABLE,
           AllowlistedSchemes(),
           ValidSettings(CONTENT_SETTING_ALLOW,   // allow
                         CONTENT_SETTING_ASK,     // ask
                         CONTENT_SETTING_BLOCK),  // block
           WebsiteSettingsInfo::SINGLE_ORIGIN_WITH_EMBEDDED_EXCEPTIONS_SCOPE,
           WebsiteSettingsRegistry::PLATFORM_ANDROID,
           ContentSettingsInfo::INHERIT_IN_INCOGNITO,
           ContentSettingsInfo::PERSISTENT,
           ContentSettingsInfo::EXCEPTIONS_ON_SECURE_AND_INSECURE_ORIGINS,
           /*show_into_info_page*/ true,
           /*permission_type_ui*/ IDS_NEW_CONTENT_SETTING,
           /*permission_type_ui_mid_sentence*/ IDS_NEW_CONTENT_SETTING_MID_SENTENCE);
```

* Add your content setting in `@IntDef()` and `Type` in `SiteSettingsCategory` as usual

* Add your strings in a new file in `components/browser_ui/strings/android/`

```
<?xml version="1.0" encoding="utf-8"?>
<grit-part>
  <message name="IDS_NEW_CONTENT_SETTING" desc="The label used for your content type site settings controls.">
    your content type
  </message>
  <message name="IDS_NEW_CONTENT_SETTING_MID_SENTENCE" desc="The label used for your content type site settings controls when used mid-sentence.">
    your content type
  </message>
  <message name="IDS_NEW_CONTENT_SETTING_PERMISSION_TITLE" desc="Title of the permission to use your content type [CHAR-LIMIT=32]">
    your content type
  </message>
  <message name="IDS_NEW_CONTENT_SETTING_ENABLED" desc="Summary text explaining that your content type is enabled with fingerprinting protection.">
    your content type is enabled
  </message>
  <message name="IDS_NEW_CONTENT_SETTING_ASK" desc="Summary text explaining that your content type is full enabled.">
    ask before activate your content type
  </message>
  <message name="IDS_NEW_CONTENT_SETTING_DISABLED" desc="Summary text explaining that your content type is full disabled.">
    your content type is disabled
  </message>
</grit-part>
```

* Reference it in `components/components_strings.grd` to have strings in native

```
<part file="browser_ui/strings/android/new_content_setting_file.grdp" />
```

* Reference it in `components/browser_ui/strings/android/browser_ui_strings.grd` to have the strings in java

```
<part file="new_content_setting_file.grdp" />
```
