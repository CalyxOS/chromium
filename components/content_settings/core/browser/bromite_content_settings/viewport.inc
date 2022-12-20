  Register(ContentSettingsType::VIEWPORT, "viewport", CONTENT_SETTING_ALLOW,
           WebsiteSettingsInfo::SYNCABLE,
           /*allowlisted_schemes=*/{},
           /*valid_settings=*/{CONTENT_SETTING_ALLOW,
                               CONTENT_SETTING_BLOCK},
           WebsiteSettingsInfo::TOP_ORIGIN_ONLY_SCOPE,
           WebsiteSettingsRegistry::ALL_PLATFORMS,
           ContentSettingsInfo::INHERIT_IN_INCOGNITO,
           ContentSettingsInfo::EXCEPTIONS_ON_SECURE_AND_INSECURE_ORIGINS);

  content_settings::WebsiteSettingsRegistry::GetInstance()
    ->GetMutable(ContentSettingsType::VIEWPORT)
    ->set_show_into_info_page()
    .set_desktop_ui()
    .set_is_renderer_content_setting()
    .set_title_ui(IDS_SITE_SETTINGS_TYPE_VIEWPORT)
    .set_description_ui(IDS_SETTINGS_SITE_SETTINGS_VIEWPORT_PROTECTION_DESCRIPTION)
    .set_allowed_ui(IDS_WEBSITE_SETTINGS_CATEGORY_VIEWPORT_ENABLED)
    .set_blocked_ui(IDS_WEBSITE_SETTINGS_CATEGORY_VIEWPORT_DISABLED)
    .set_allowed_exceptions_ui(IDS_SETTINGS_SITE_SETTINGS_VIEWPORT_PROTECTION_ALLOWED_EXCEPTIONS)
    .set_blocked_exceptions_ui(IDS_SETTINGS_SITE_SETTINGS_VIEWPORT_PROTECTION_BLOCKED_EXCEPTIONS)
    .set_mid_sentence_ui(IDS_SITE_SETTINGS_TYPE_VIEWPORT_MID_SENTENCE);
