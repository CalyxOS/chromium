// Copyright 2015 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.gcore;

import android.content.Context;

import org.chromium.base.Log;
import org.chromium.base.TraceEvent;
import org.chromium.components.externalauth.ExternalAuthUtils;

import java.util.concurrent.TimeUnit;

/**
 * Default implementation for {@link ChromeGoogleApiClient}.
 */
public class ChromeGoogleApiClientImpl implements ChromeGoogleApiClient {
    private static final String TAG = "Icing";

    private final Context mApplicationContext;
    private final ExternalAuthUtils mExternalAuthUtils;

    /**
     * @param context its application context will be exposed through
     *            {@link #getApplicationContext()}.
     * @param client will be exposed through {@link #getApiClient()}.
     * @param requireFirstPartyBuild true if the given client can only be used in a first-party
     *            build.
     */
    public ChromeGoogleApiClientImpl(Context context,
            boolean requireFirstPartyBuild) {
        mApplicationContext = context.getApplicationContext();
        mExternalAuthUtils = ExternalAuthUtils.getInstance();
        if (requireFirstPartyBuild && !mExternalAuthUtils.isChromeGoogleSigned()) {
            throw new IllegalStateException("GoogleApiClient requires first-party build");
        }
    }

    @Override
    public void disconnect() {
    }

    @Override
    public boolean isGooglePlayServicesAvailable() {
        TraceEvent.begin("ChromeGoogleApiClientImpl:isGooglePlayServicesAvailable");
        try {
            return mExternalAuthUtils.canUseGooglePlayServices();
        } finally {
            TraceEvent.end("ChromeGoogleApiClientImpl:isGooglePlayServicesAvailable");
        }
    }

    @Override
    public boolean connectWithTimeout(long timeout) {
        TraceEvent.begin("ChromeGoogleApiClientImpl:connectWithTimeout");
        TraceEvent.end("ChromeGoogleApiClientImpl:connectWithTimeout");
        return false;
    }

    public Context getApplicationContext() {
        return mApplicationContext;
    }
}
