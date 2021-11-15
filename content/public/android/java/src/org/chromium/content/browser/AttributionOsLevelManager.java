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
import org.chromium.base.ResettersForTesting;
import org.chromium.base.ThreadUtils;
import org.chromium.base.annotations.CalledByNative;
import org.chromium.base.annotations.JNINamespace;
import org.chromium.base.annotations.NativeMethods;
import org.chromium.base.metrics.RecordHistogram;
import org.chromium.base.task.PostTask;
import org.chromium.base.task.TaskTraits;
import org.chromium.url.GURL;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
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

    // Used for testing
    private static MeasurementManagerFutures sManagerForTesting;

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
            RegistrationResult.ERROR_INTERNAL, RegistrationResult.ERROR_BACKGROUND_CALLER,
            RegistrationResult.COUNT})
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
        int ERROR_BACKGROUND_CALLER = 9;
        int COUNT = 10;
    }

    @CalledByNative
    private AttributionOsLevelManager(long nativePtr) {
        mNativePtr = nativePtr;
    }

    private static boolean supportsAttribution() {
        return false;
    }

    private MeasurementManagerFutures getManager() {
        return null;
    }

    private void onRegistrationCompleted(
            int requestId, @RegistrationType int type, @RegistrationResult int result) {
        switch (type) {
            case RegistrationType.SOURCE:
                RecordHistogram.recordEnumeratedHistogram(
                        "Conversions.AndroidRegistrationResult.Source2", result,
                        RegistrationResult.COUNT);
                break;
            case RegistrationType.TRIGGER:
                RecordHistogram.recordEnumeratedHistogram(
                        "Conversions.AndroidRegistrationResult.Trigger2", result,
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
        if (!supportsAttribution()) {
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
        if (!supportsAttribution()) {
            onRegistrationCompleted(
                    requestId, RegistrationType.SOURCE, RegistrationResult.ERROR_INTERNAL);
            return;
        }
    }

    /**
     * Registers a web attribution trigger with native, see `registerWebTriggerAsync()`:
     * https://developer.android.com/reference/androidx/privacysandbox/ads/adservices/java/measurement/MeasurementManagerFutures.
     */
    @CalledByNative
    private void registerWebAttributionTrigger(
            int requestId, GURL registrationUrl, GURL topLevelOrigin, boolean isDebugKeyAllowed) {
        if (!supportsAttribution()) {
            onRegistrationCompleted(
                    requestId, RegistrationType.TRIGGER, RegistrationResult.ERROR_INTERNAL);
            return;
        }
    }

    /**
     * Registers an attribution trigger with native, see `registerTriggerAsync()`:
     * https://developer.android.com/reference/androidx/privacysandbox/ads/adservices/java/measurement/MeasurementManagerFutures.
     */
    @CalledByNative
    private void registerAttributionTrigger(int requestId, GURL registrationUrl) {
        if (!supportsAttribution()) {
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
        if (!supportsAttribution()) {
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
    private static void getMeasurementApiStatus() {
        ThreadUtils.assertOnBackgroundThread();

        if ((true)) {
            AttributionOsLevelManagerJni.get().onMeasurementStateReturned(0);
            return;
        }
        if (sManagerForTesting != null) {
            AttributionOsLevelManagerJni.get().onMeasurementStateReturned(1);
            return;
        }

        if (!supportsAttribution()) {
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
    }

    @CalledByNative
    private void nativeDestroyed() {
        mNativePtr = 0;
    }

    public static void setManagerForTesting(MeasurementManagerFutures manager) {
        sManagerForTesting = manager;
        PostTask.postTask(
                TaskTraits.BEST_EFFORT, () -> AttributionOsLevelManager.getMeasurementApiStatus());
        ResettersForTesting.register(() -> {
            sManagerForTesting = null;
            PostTask.postTask(TaskTraits.BEST_EFFORT,
                    () -> AttributionOsLevelManager.getMeasurementApiStatus());
        });
    }

    @NativeMethods
    interface Natives {
        void onDataDeletionCompleted(long nativeAttributionOsLevelManagerAndroid, int requestId);
        void onRegistrationCompleted(
                long nativeAttributionOsLevelManagerAndroid, int requestId, boolean success);
        void onMeasurementStateReturned(int state);
    }
}
