#ifdef FLAG_SECTION

#if BUILDFLAG(IS_ANDROID)

    {"offline-pages-auto-save",
     "Enables autosave of offline page",
     "Enables autosave of offline page, as automatic switching in case "
     "the device goes offline.", kOsAndroid,
     FEATURE_VALUE_TYPE(offline_pages::kOfflinePagesAutoSaveFeature)},

#endif

#endif
