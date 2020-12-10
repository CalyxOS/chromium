SET_CROMITE_FEATURE_ENABLED(kSplitCodeCacheByNetworkIsolationKey);
SET_CROMITE_FEATURE_ENABLED(kPartitionConnectionsByNetworkIsolationKey);

SET_CROMITE_FEATURE_ENABLED(kSplitCacheByIncludeCredentials);

// enable http cache partition
SET_CROMITE_FEATURE_ENABLED(kSplitCacheByNetworkIsolationKey);

// most restricted option (ByNavigationInitiator) used
// it generates a cache key isolating all requests per initiator.
// requests with initiators with an opaque origin are not cached
SET_CROMITE_FEATURE_ENABLED(kSplitCacheByNavigationInitiator);
