  Register(ContentSettingsType::TIMEZONE_OVERRIDE, "timezone-override", CONTENT_SETTING_ALLOW,
           WebsiteSettingsInfo::SYNCABLE,
           /*allowlisted_schemes=*/{},
           /*valid_settings=*/{CONTENT_SETTING_ALLOW,   // use system time
                               CONTENT_SETTING_ASK,     // custom timezone, default UTC
                               CONTENT_SETTING_BLOCK},  // random
           WebsiteSettingsInfo::TOP_ORIGIN_ONLY_SCOPE,
           WebsiteSettingsRegistry::ALL_PLATFORMS,
           ContentSettingsInfo::INHERIT_IN_INCOGNITO,
           ContentSettingsInfo::EXCEPTIONS_ON_SECURE_AND_INSECURE_ORIGINS);

  content_settings::WebsiteSettingsRegistry::GetInstance()
    ->GetMutable(ContentSettingsType::TIMEZONE_OVERRIDE)
    ->set_show_into_info_page()
    .set_desktop_ui()
    .set_is_renderer_content_setting()
    .set_title_ui(IDS_SITE_SETTINGS_TIMEZONE_OVERRIDE_TITLE)
    .set_description_ui(IDS_WEBSITE_SETTINGS_TIMEOVERRIDE_INFO)
    .set_allowed_ui(IDS_WEBSITE_SETTINGS_CATEGORY_TIMEZONE_OVERRIDE_ALLOWED)
    .set_blocked_ui(IDS_WEBSITE_SETTINGS_CATEGORY_TIMEZONE_OVERRIDE_RANDOM)
    .set_ask_ui(IDS_WEBSITE_SETTINGS_CATEGORY_TIMEZONE_OVERRIDE_CUSTOM)
    .set_allowed_exceptions_ui(IDS_WEBSITE_SETTINGS_CATEGORY_TIMEZONE_OVERRIDE_ALLOWED)
    .set_blocked_exceptions_ui(IDS_WEBSITE_SETTINGS_CATEGORY_TIMEZONE_OVERRIDE_RANDOM)
    .set_mid_sentence_ui(IDS_SITE_SETTINGS_TIMEZONE_OVERRIDE_TITLE);
