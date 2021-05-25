#if BUILDFLAG(IS_ANDROID)

#ifdef FLAG_SECTION

    {"allow-external-intent-requests",
     "Allow forward URL requests to external intents",
     "If disabled, URL requests will never"
     "allow for redirecting to an external intent.", kOsAndroid,
     SINGLE_DISABLE_VALUE_TYPE("disable-external-intent-requests")},

#endif

#endif
