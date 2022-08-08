#if BUILDFLAG(IS_ANDROID)

CROMITE_FEATURE(kCloseAllTabsModalDialog,
                "CloseAllTabsModalDialog",
                base::FEATURE_ENABLED_BY_DEFAULT);

SET_CROMITE_FEATURE_ENABLED(kArchiveTabService);

#endif
