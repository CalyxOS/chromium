// Copyright 2019 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.content.browser.sms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.chromium.base.ContextUtils;
import org.chromium.base.Log;
import org.chromium.ui.base.WindowAndroid;

/**
 * Encapsulates logic to retrieve OTP code via SMS User Consent API.
 */
public class SmsUserConsentReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsUserConsentRcvr";
    private static final boolean DEBUG = false;
    private final SmsProviderGms mProvider;
    private boolean mDestroyed;
    private Wrappers.WebOTPServiceContext mContext;

    public SmsUserConsentReceiver(SmsProviderGms provider, Wrappers.WebOTPServiceContext context) {
        mDestroyed = false;
        mProvider = provider;
        mContext = context;
    }

    public void destroy() {
        if (mDestroyed) return;
        if (DEBUG) Log.d(TAG, "Destroying SmsUserConsentReceiver.");
        mDestroyed = true;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG) Log.d(TAG, "Received something!");

        if (mDestroyed) {
            return;
        }

        /*if (!SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            return;
        }

        if (intent.getExtras() == null) {
            return;
        }

        final Status status;

        try {
            status = (Status) intent.getParcelableExtra(SmsRetriever.EXTRA_STATUS);
        } catch (Throwable e) {
            if (DEBUG) Log.d(TAG, "Error getting parceable.");
            return;
        }

        switch (status.getStatusCode()) {
            case CommonStatusCodes.SUCCESS:
                assert mProvider.getWindow() != null;

                Intent consentIntent =
                        intent.getExtras().getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
                try {
                    mProvider.getWindow().showIntent(consentIntent,
                            (resultCode, data) -> onConsentResult(resultCode, data), null);
                } catch (android.content.ActivityNotFoundException e) {
                    if (DEBUG) Log.d(TAG, "Error starting activity for result.");
                }
                break;
            case CommonStatusCodes.TIMEOUT:
                if (DEBUG) Log.d(TAG, "Timeout");
                mProvider.onTimeout();
                break;
        } */
    }

    void onConsentResult(int resultCode, Intent data) {
        if (DEBUG) Log.d(TAG, "Activity result discarded.");
    }

    public void listen(WindowAndroid windowAndroid) {
        if (DEBUG) Log.d(TAG, "Ignored task");
    }
}
