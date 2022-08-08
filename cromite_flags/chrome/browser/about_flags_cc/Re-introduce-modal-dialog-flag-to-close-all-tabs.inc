#if BUILDFLAG(IS_ANDROID)

#ifdef FLAG_SECTION

    {"close-all-tabs-modal-dialog",
     "Close all tabs modal dialog",
     "Shows a confirmation modal dialog when clicking 'Close all tabs' in the"
     "app menu.", kOsAndroid,
     FEATURE_VALUE_TYPE(chrome::android::kCloseAllTabsModalDialog)},

#endif

#endif
