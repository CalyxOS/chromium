// Copyright 2021 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.subscriptions;

import androidx.annotation.VisibleForTesting;

import org.chromium.chrome.browser.lifecycle.ActivityLifecycleDispatcher;
import org.chromium.chrome.browser.lifecycle.PauseResumeWithNativeObserver;
import org.chromium.chrome.browser.preferences.ChromePreferenceKeys;
import org.chromium.chrome.browser.preferences.SharedPreferencesManager;
import org.chromium.chrome.browser.subscriptions.CommerceSubscription.CommerceSubscriptionType;
import org.chromium.chrome.browser.tabmodel.TabModelSelector;

import java.util.concurrent.TimeUnit;

/**
 * Commerce Subscriptions Service.
 */
public class CommerceSubscriptionsService {
    @VisibleForTesting
    public static final String CHROME_MANAGED_SUBSCRIPTIONS_TIMESTAMP =
            ChromePreferenceKeys.COMMERCE_SUBSCRIPTIONS_CHROME_MANAGED_TIMESTAMP;

    private final SubscriptionsManagerImpl mSubscriptionManager;
    private final SharedPreferencesManager mSharedPreferencesManager;
    private final CommerceSubscriptionsMetrics mMetrics;
    private ActivityLifecycleDispatcher mActivityLifecycleDispatcher;
    private PauseResumeWithNativeObserver mPauseResumeWithNativeObserver;

    /** Creates a new instance. */
    CommerceSubscriptionsService(SubscriptionsManagerImpl subscriptionsManager) {
        mSubscriptionManager = subscriptionsManager;
        mSharedPreferencesManager = SharedPreferencesManager.getInstance();
        mMetrics = new CommerceSubscriptionsMetrics();
    }

    /** Performs any deferred startup tasks required by {@link Subscriptions}. */
    public void initDeferredStartupForActivity(TabModelSelector tabModelSelector,
            ActivityLifecycleDispatcher activityLifecycleDispatcher) {
        mActivityLifecycleDispatcher = activityLifecycleDispatcher;
        mPauseResumeWithNativeObserver = new PauseResumeWithNativeObserver() {
            @Override
            public void onResumeWithNative() {
                maybeRecordMetricsAndInitializeSubscriptions();
            }

            @Override
            public void onPauseWithNative() {}
        };
        mActivityLifecycleDispatcher.register(mPauseResumeWithNativeObserver);
    }

    /** Returns the subscriptionsManager. */
    public SubscriptionsManagerImpl getSubscriptionsManager() {
        return mSubscriptionManager;
    }

    /**
     * Cleans up internal resources. Currently this method calls SubscriptionsManagerImpl#destroy.
     */
    public void destroy() {
        if (mActivityLifecycleDispatcher != null) {
            mActivityLifecycleDispatcher.unregister(mPauseResumeWithNativeObserver);
        }
    }

    private void maybeRecordMetricsAndInitializeSubscriptions() {
        if (System.currentTimeMillis()
                        - mSharedPreferencesManager.readLong(
                                CHROME_MANAGED_SUBSCRIPTIONS_TIMESTAMP, -1)
                < TimeUnit.SECONDS.toMillis(
                        CommerceSubscriptionsServiceConfig.getStaleTabLowerBoundSeconds())) {
            return;
        }
        mSharedPreferencesManager.writeLong(
                CHROME_MANAGED_SUBSCRIPTIONS_TIMESTAMP, System.currentTimeMillis());
        mMetrics.recordAccountWaaStatus();
    }
}
