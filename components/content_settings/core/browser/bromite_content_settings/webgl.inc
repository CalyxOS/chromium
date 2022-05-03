  Register(ContentSettingsType::WEBGL, "webgl", CONTENT_SETTING_BLOCK,
           WebsiteSettingsInfo::SYNCABLE,
           /*allowlisted_schemes=*/{},
           /*valid_settings=*/{CONTENT_SETTING_ALLOW,
                               CONTENT_SETTING_BLOCK},
           WebsiteSettingsInfo::TOP_ORIGIN_ONLY_SCOPE,
           WebsiteSettingsRegistry::ALL_PLATFORMS,
           ContentSettingsInfo::INHERIT_IN_INCOGNITO,
           ContentSettingsInfo::EXCEPTIONS_ON_SECURE_AND_INSECURE_ORIGINS);

  content_settings::WebsiteSettingsRegistry::GetInstance()
    ->GetMutable(ContentSettingsType::WEBGL)
    ->set_show_into_info_page()
    .set_desktop_ui()
    .set_is_renderer_content_setting()
    .set_title_ui(IDS_SITE_SETTINGS_TYPE_WEBGL)
    .set_description_ui(IDS_SETTINGS_SITE_SETTINGS_WEBGL_DESCRIPTION)
    .set_allowed_ui(IDS_WEBSITE_SETTINGS_CATEGORY_WEBGL_ENABLED)
    .set_blocked_ui(IDS_WEBSITE_SETTINGS_CATEGORY_WEBGL_DISABLED)
    .set_allowed_exceptions_ui(IDS_SETTINGS_SITE_SETTINGS_WEBGL_ALLOWED_EXCEPTIONS)
    .set_blocked_exceptions_ui(IDS_SETTINGS_SITE_SETTINGS_WEBGL_BLOCKED_EXCEPTIONS)
    .set_mid_sentence_ui(IDS_SITE_SETTINGS_TYPE_WEBGL);
