  content_settings::WebsiteSettingsRegistry::GetInstance()
    ->GetMutable(ContentSettingsType::JAVASCRIPT)
    ->set_show_into_info_page()
    .set_is_renderer_content_setting();
