// Copyright 2023 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.content.browser;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.LimitExceededException;
import android.os.Process;
import android.view.MotionEvent;

import androidx.annotation.IntDef;

import org.chromium.base.ContextUtils;
import org.chromium.base.Log;
import org.chromium.base.annotations.CalledByNative;
import org.chromium.base.annotations.JNINamespace;
import org.chromium.base.annotations.NativeMethods;
import org.chromium.base.metrics.RecordHistogram;
import org.chromium.url.GURL;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

/**
 * Handles passing registrations with Web Attribution Reporting API to the underlying native
 * library.
 */
@JNINamespace("content")
public class AttributionOsLevelManager {
    private class MeasurementManagerFutures {}

    private static final String TAG = "AttributionManager";
    // TODO: replace with constant in android.Manifest.permission once it becomes available in U.
    private static final String PERMISSION_ACCESS_ADSERVICES_ATTRIBUTION =
            "android.permission.ACCESS_ADSERVICES_ATTRIBUTION";
    private long mNativePtr;
    private MeasurementManagerFutures mManager;

    @IntDef({RegistrationType.SOURCE, RegistrationType.TRIGGER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RegistrationType {
        int SOURCE = 0;
        int TRIGGER = 1;
    }

    // These values are persisted to logs. Entries should not be renumbered and
    // numeric values should never be reused.
    @IntDef({RegistrationResult.SUCCESS, RegistrationResult.ERROR_UNKNOWN,
            RegistrationResult.ERROR_ILLEGAL_ARGUMENT, RegistrationResult.ERROR_IO,
            RegistrationResult.ERROR_ILLEGAL_STATE, RegistrationResult.ERROR_SECURITY,
            RegistrationResult.ERROR_TIMEOUT, RegistrationResult.ERROR_LIMIT_EXCEEDED,
            RegistrationResult.ERROR_INTERNAL, RegistrationResult.COUNT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RegistrationResult {
        int SUCCESS = 0;
        int ERROR_UNKNOWN = 1;
        int ERROR_ILLEGAL_ARGUMENT = 2;
        int ERROR_IO = 3;
        int ERROR_ILLEGAL_STATE = 4;
        int ERROR_SECURITY = 5;
        int ERROR_TIMEOUT = 6;
        int ERROR_LIMIT_EXCEEDED = 7;
        int ERROR_INTERNAL = 8;
        int COUNT = 9;
    }

    @CalledByNative
    private AttributionOsLevelManager(long nativePtr) {
        mNativePtr = nativePtr;
    }

    private MeasurementManagerFutures getManager() {
        return null;
    }

    private void onRegistrationCompleted(
            int requestId, @RegistrationType int type, @RegistrationResult int result) {
        switch (type) {
            case RegistrationType.SOURCE:
                RecordHistogram.recordEnumeratedHistogram(
                        "Conversions.AndroidRegistrationResult.Source", result,
                        RegistrationResult.COUNT);
                break;
            case RegistrationType.TRIGGER:
                RecordHistogram.recordEnumeratedHistogram(
                        "Conversions.AndroidRegistrationResult.Trigger", result,
                        RegistrationResult.COUNT);

                break;
        }

        if (mNativePtr != 0) {
            AttributionOsLevelManagerJni.get().onRegistrationCompleted(
                    mNativePtr, requestId, result == RegistrationResult.SUCCESS);
        }
    }

    /**
     * Registers a web attribution source with native, see `registerWebSourceAsync()`:
     * https://developer.android.com/reference/androidx/privacysandbox/ads/adservices/java/measurement/MeasurementManagerFutures.
     */
    @CalledByNative
    private void registerWebAttributionSource(int requestId, GURL registrationUrl,
            GURL topLevelOrigin, boolean isDebugKeyAllowed, MotionEvent event) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onRegistrationCompleted(
                    requestId, RegistrationType.SOURCE, RegistrationResult.ERROR_INTERNAL);
            return;
        }
        MeasurementManagerFutures mm = getManager();
        if (mm == null) {
            onRegistrationCompleted(
                    requestId, RegistrationType.SOURCE, RegistrationResult.ERROR_INTERNAL);
            return;
        }
    }

    /**
     * Registers an attribution source with native, see `registerSourceAsync()`:
     * https://developer.android.com/reference/androidx/privacysandbox/ads/adservices/java/measurement/MeasurementManagerFutures.
     */
    @CalledByNative
    private void registerAttributionSource(int requestId, GURL registrationUrl, MotionEvent event) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onRegistrationCompleted(
                    requestId, RegistrationType.SOURCE, RegistrationResult.ERROR_INTERNAL);
            return;
        }
        MeasurementManagerFutures mm = getManager();
        if (mm == null) {
            onRegistrationCompleted(
                    requestId, RegistrationType.SOURCE, RegistrationResult.ERROR_INTERNAL);
            return;
        }
        onRegistrationCompleted(requestId, RegistrationType.SOURCE, RegistrationResult.SUCCESS);
    }

    /**
     * Registers a web attribution trigger with native, see `registerWebTriggerAsync()`:
     * https://developer.android.com/reference/androidx/privacysandbox/ads/adservices/java/measurement/MeasurementManagerFutures.
     */
    @CalledByNative
    private void registerWebAttributionTrigger(
            int requestId, GURL registrationUrl, GURL topLevelOrigin, boolean isDebugKeyAllowed) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onRegistrationCompleted(
                    requestId, RegistrationType.TRIGGER, RegistrationResult.ERROR_INTERNAL);
            return;
        }

        MeasurementManagerFutures mm = getManager();
        if (mm == null) {
            onRegistrationCompleted(
                    requestId, RegistrationType.TRIGGER, RegistrationResult.ERROR_INTERNAL);
            return;
        }
    }

    private void onDataDeletionCompleted(int requestId) {
        if (mNativePtr != 0) {
            AttributionOsLevelManagerJni.get().onDataDeletionCompleted(mNativePtr, requestId);
        }
    }

    /**
     * Deletes attribution data with native, see `deleteRegistrationsAsync()`:
     * https://developer.android.com/reference/androidx/privacysandbox/ads/adservices/java/measurement/MeasurementManagerFutures.
     */
    @CalledByNative
    private void deleteRegistrations(int requestId, long startMs, long endMs, GURL[] origins,
            String[] domains, int deletionMode, int matchBehavior) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onDataDeletionCompleted(requestId);
            return;
        }
        MeasurementManagerFutures mm = getManager();
        if (mm == null) {
            onDataDeletionCompleted(requestId);
            return;
        }
        onDataDeletionCompleted(requestId);
    }

    /**
     * Gets Measurement API status with native, see `getMeasurementApiStatusAsync()`:
     * https://developer.android.com/reference/androidx/privacysandbox/ads/adservices/java/measurement/MeasurementManagerFutures.
     */
    @CalledByNative
    private void getMeasurementApiStatus() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            AttributionOsLevelManagerJni.get().onMeasurementStateReturned(0);
            return;
        }
        if (ContextUtils.getApplicationContext().checkPermission(
                    PERMISSION_ACCESS_ADSERVICES_ATTRIBUTION, Process.myPid(), Process.myUid())
                != PackageManager.PERMISSION_GRANTED) {
            // Permission may not be granted when embedded as WebView.
            AttributionOsLevelManagerJni.get().onMeasurementStateReturned(0);
            return;
        }
        MeasurementManagerFutures mm = getManager();
        if (mm == null) {
            AttributionOsLevelManagerJni.get().onMeasurementStateReturned(0);
            return;
        }
    }

    @CalledByNative
    private void nativeDestroyed() {
        mNativePtr = 0;
    }

    @NativeMethods
    interface Natives {
        void onDataDeletionCompleted(long nativeAttributionOsLevelManagerAndroid, int requestId);
        void onRegistrationCompleted(
                long nativeAttributionOsLevelManagerAndroid, int requestId, boolean success);
        void onMeasurementStateReturned(int state);
    }
}
