  content_settings::WebsiteSettingsRegistry::GetInstance()
    ->GetMutable(ContentSettingsType::IMAGES)
    ->set_show_into_info_page()
    .set_is_renderer_content_setting()
    .set_title_ui(IDS_SITE_SETTINGS_TYPE_IMAGES)
    .set_mid_sentence_ui(IDS_SITE_SETTINGS_TYPE_IMAGES_MID_SENTENCE);
