// Copyright 2015 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.components.policy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;

/**
 * Concrete app restriction provider, that uses the default android mechanism to retrieve the
 * restrictions.
 */
public class AppRestrictionsProvider extends AbstractAppRestrictionsProvider {
    /**
     * Get the app restriction information from provided user manager, and record some timing
     * metrics on its runtime.
     * @param userManager UserManager service from Android System service
     * @param packageName package name for target application.
     * @return The restrictions for the provided package name
     */
    public static Bundle getApplicationRestrictionsFromUserManager(
            UserManager userManager, String packageName) {
        Bundle restrictions = new Bundle();
        try {
            restrictions = userManager.getApplicationRestrictions(packageName);
        } catch (SecurityException e) {
            // Android bug may throw SecurityException. See crbug.com/886814.
            // Do nothing, because we append our own policies below
        }
        // https://chromeenterprise.google/policies/#BrowserSignin
        restrictions.putInt("BrowserSignin", 0); // Disable browser sign-in
        // https://chromeenterprise.google/policies/#ContextualSearchEnabled
        restrictions.putBoolean("ContextualSearchEnabled", false);
        // https://chromeenterprise.google/policies/#DomainReliabilityAllowed
        restrictions.putBoolean("DomainReliabilityAllowed", false);
        // https://chromeenterprise.google/policies/#MetricsReportingEnabled
        restrictions.putBoolean("MetricsReportingEnabled", false);
        // https://chromeenterprise.google/policies/#NTPContentSuggestionsEnabled
        restrictions.putBoolean("NTPContentSuggestionsEnabled", false);
        // https://chromeenterprise.google/policies/#NetworkPredictionOptions
        restrictions.putInt("NetworkPredictionOptions", 2); // Do not predict network actions on any network connection
        // https://chromeenterprise.google/policies/#PrivacySandbox
        restrictions.putBoolean("PrivacySandboxAdMeasurementEnabled", false);
        restrictions.putBoolean("PrivacySandboxAdTopicsEnabled", false);
        restrictions.putBoolean("PrivacySandboxPromptEnabled", false);
        restrictions.putBoolean("PrivacySandboxSiteEnabledAdsEnabled", false);
        // https://chromeenterprise.google/policies/#SafeBrowsing
        restrictions.putBoolean("SafeBrowsingExtendedReportingEnabled", false);
        restrictions.putInt("SafeBrowsingProtectionLevel", 0); // Safe Browsing is never active.
        return restrictions;
    }

    private final UserManager mUserManager;

    public AppRestrictionsProvider(Context context) {
        super(context);

        mUserManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
    }

    @Override
    protected Bundle getApplicationRestrictions(String packageName) {
        return getApplicationRestrictionsFromUserManager(mUserManager, packageName);
    }

    @Override
    protected String getRestrictionChangeIntentAction() {
        return Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED;
    }
}
