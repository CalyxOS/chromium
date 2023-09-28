#if !BUILDFLAG(IS_ANDROID)

  content_settings::WebsiteSettingsRegistry::GetInstance()
    ->GetMutable(ContentSettingsType::MEDIASTREAM_CAMERA)
    ->set_show_into_info_page()
    .set_desktop_ui();

#endif
