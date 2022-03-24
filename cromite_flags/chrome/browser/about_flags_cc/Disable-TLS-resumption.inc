#ifdef FLAG_SECTION

    {"disable-tls-resumption",
     "Disable TLS Session Resumption",
     "Disable TLS session resumption.", kOsAll,
     FEATURE_VALUE_TYPE(net::features::kDisableTLSResumption)},
    {"log-tls-resumption",
     "Log TLS Session Resumption",
     "Log TLS Session Resumption", kOsAll,
     FEATURE_VALUE_TYPE(net::features::kLogTLSResumption)},

#endif
