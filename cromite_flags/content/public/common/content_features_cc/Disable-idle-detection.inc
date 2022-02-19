// This is intended as a kill switch for the Idle Detection feature. To enable
// this feature, the experimental web platform features flag should be set,
// or the site should obtain an Origin Trial token.
CROMITE_FEATURE(kIdleDetection, "IdleDetection", base::FEATURE_DISABLED_BY_DEFAULT);
