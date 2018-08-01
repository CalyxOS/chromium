#ifdef FLAG_SECTION

#if BUILDFLAG(IS_ANDROID)
    {"export-bookmarks-use-saf",
     "Use saf for bookmarks export",
     "When enabled user can choose where save the exported bookmarks "
     "file.", kOsAndroid,
     FEATURE_VALUE_TYPE(
         chrome::android::kBookmarksExportUseSaf)},
#endif

#endif
