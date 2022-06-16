#if BUILDFLAG(IS_ANDROID)

#ifdef FLAG_SECTION

    {"block-external-form-redirects-no-gesture",
     "Block intents from form submissions without user gesture",
     "Require a user gesture that triggered a form submission in order to "
     "allow for redirecting to an external intent.",
     kOsAndroid,
     FEATURE_VALUE_TYPE(
         external_intents::kIntentBlockExternalFormRedirectsNoGesture)},

#endif

#endif
