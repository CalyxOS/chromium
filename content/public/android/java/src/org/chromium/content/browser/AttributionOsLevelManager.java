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
import androidx.annotation.OptIn;

import org.jni_zero.CalledByNative;
import org.jni_zero.JNINamespace;
import org.jni_zero.JniType;
import org.jni_zero.NativeMethods;

import org.chromium.base.ContextUtils;
import org.chromium.base.Log;
import org.chromium.base.ResettersForTesting;
import org.chromium.base.ThreadUtils;
import org.chromium.base.metrics.RecordHistogram;
import org.chromium.base.task.PostTask;
import org.chromium.base.task.TaskTraits;
import org.chromium.url.GURL;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

/**
 * Handles passing registrations with Web Attribution Reporting API to the underlying native
 * library.
 */
@JNINamespace("content")
public class AttributionOsLevelManager {
    private class MeasurementManagerFutures {}
    private class WebTriggerParams {}
    private class WebSourceParams {}

    private static final String TAG = "AttributionManager";
    // TODO: replace with constant in android.Manifest.permission once it becomes available in U.
    private static final String PERMISSION_ACCESS_ADSERVICES_ATTRIBUTION =
            "android.permission.ACCESS_ADSERVICES_ATTRIBUTION";

    // Used for testing
    private static MeasurementManagerFutures sManagerForTesting;

    private long mNativePtr;
    private MeasurementManagerFutures mManager;

    @IntDef({
        OperationType.REGISTER_SOURCE,
        OperationType.REGISTER_WEB_SOURCE,
        OperationType.REGISTER_TRIGGER,
        OperationType.REGISTER_WEB_TRIGGER,
        OperationType.GET_MEASUREMENT_API_STATUS,
        OperationType.DELETE_REGISTRATIONS
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface OperationType {
        int REGISTER_SOURCE = 0;
        int REGISTER_WEB_SOURCE = 1;
        int REGISTER_TRIGGER = 2;
        int REGISTER_WEB_TRIGGER = 3;
        int GET_MEASUREMENT_API_STATUS = 4;
        int DELETE_REGISTRATIONS = 5;
    }

    // These values are persisted to logs. Entries should not be renumbered and
    // numeric values should never be reused.
    @IntDef({
        OperationResult.SUCCESS,
        OperationResult.ERROR_UNKNOWN,
        OperationResult.ERROR_ILLEGAL_ARGUMENT,
        OperationResult.ERROR_IO,
        OperationResult.ERROR_ILLEGAL_STATE,
        OperationResult.ERROR_SECURITY,
        OperationResult.ERROR_TIMEOUT,
        OperationResult.ERROR_LIMIT_EXCEEDED,
        OperationResult.ERROR_INTERNAL,
        OperationResult.ERROR_BACKGROUND_CALLER,
        OperationResult.ERROR_VERSION_UNSUPPORTED,
        OperationResult.ERROR_PERMISSION_UNGRANTED,
        OperationResult.COUNT
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface OperationResult {
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
        int ERROR_VERSION_UNSUPPORTED = 10;
        int ERROR_PERMISSION_UNGRANTED = 11;
        int COUNT = 12;
    }

    private static boolean supportsAttribution() {
        return false;
    }

    private static void recordOperationResult(
            @OperationType int type, @OperationResult int result) {
    }

    @CalledByNative
    private AttributionOsLevelManager(long nativePtr) {
        mNativePtr = nativePtr;
    }

    private MeasurementManagerFutures getManager() {
        return null;
    }

    private void onRegistrationCompleted(
            int requestId, @OperationType int type, @OperationResult int result) {
        recordOperationResult(type, result);

        if (mNativePtr != 0) {
            AttributionOsLevelManagerJni.get()
                    .onRegistrationCompleted(
                            mNativePtr, requestId, result == OperationResult.SUCCESS);
        }
    }

    @CalledByNative
    private static List<WebSourceParams> createWebSourceParamsList(int size) {
        if (!supportsAttribution()) {
            return null;
        }
        return new ArrayList<WebSourceParams>(size);
    }

    @CalledByNative
    private static void addWebSourceParams(
            List<WebSourceParams> list, GURL registrationUrl, boolean isDebugKeyAllowed) {
        if (!supportsAttribution()) {
            return;
        }
    }

    /**
     * Registers a web attribution source with native, see `registerWebSourceAsync()`:
     * https://developer.android.com/reference/androidx/privacysandbox/ads/adservices/java/measurement/MeasurementManagerFutures.
     */
    @CalledByNative
    private void registerWebAttributionSource(
            int requestId, List<WebSourceParams> sources, GURL topLevelOrigin, MotionEvent event) {
        if (!supportsAttribution()) {
            onRegistrationCompleted(
                    requestId,
                    OperationType.REGISTER_WEB_SOURCE,
                    OperationResult.ERROR_VERSION_UNSUPPORTED);
            return;
        }
    }

    /**
     * Registers an attribution source with native, see `registerSourceAsync()`:
     * https://developer.android.com/reference/androidx/privacysandbox/ads/adservices/java/measurement/MeasurementManagerFutures.
     */
    @CalledByNative
    private void registerAttributionSource(
            int requestId, @JniType("std::vector") GURL[] registrationUrls, MotionEvent event) {
        if (!supportsAttribution()) {
            onRegistrationCompleted(
                    requestId,
                    OperationType.REGISTER_SOURCE,
                    OperationResult.ERROR_VERSION_UNSUPPORTED);
            return;
        }
    }

    @CalledByNative
    private static List<WebTriggerParams> createWebTriggerParamsList(int size) {
        return null;
    }

    @CalledByNative
    private static void addWebTriggerParams(
            List<WebTriggerParams> list, GURL registrationUrl, boolean isDebugKeyAllowed) {
    }

    /**
     * Registers a web attribution trigger with native, see `registerWebTriggerAsync()`:
     * https://developer.android.com/reference/androidx/privacysandbox/ads/adservices/java/measurement/MeasurementManagerFutures.
     */
    @CalledByNative
    private void registerWebAttributionTrigger(
            int requestId, List<WebTriggerParams> triggers, GURL topLevelOrigin) {
        if (!supportsAttribution()) {
            onRegistrationCompleted(
                    requestId,
                    OperationType.REGISTER_WEB_TRIGGER,
                    OperationResult.ERROR_VERSION_UNSUPPORTED);
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
                    requestId,
                    OperationType.REGISTER_TRIGGER,
                    OperationResult.ERROR_VERSION_UNSUPPORTED);
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
    private void deleteRegistrations(
            int requestId,
            long startMs,
            long endMs,
            @JniType("std::vector") GURL[] origins,
            @JniType("std::vector<std::string>") String[] domains,
            int deletionMode,
            int matchBehavior) {
        if (!supportsAttribution()) {
            recordOperationResult(
                    OperationType.DELETE_REGISTRATIONS, OperationResult.ERROR_VERSION_UNSUPPORTED);
            onDataDeletionCompleted(requestId);
            return;
        }
        MeasurementManagerFutures mm = getManager();
        if (mm == null) {
            recordOperationResult(
                    OperationType.DELETE_REGISTRATIONS, OperationResult.ERROR_INTERNAL);
            onDataDeletionCompleted(requestId);
            return;
        }
        onDataDeletionCompleted(requestId);
    }

    private static void onMeasurementStateReturned(int status, @OperationResult int result) {
        recordOperationResult(OperationType.GET_MEASUREMENT_API_STATUS, result);
        AttributionOsLevelManagerJni.get().onMeasurementStateReturned(status);
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
            onMeasurementStateReturned(/* status= */ 0, OperationResult.ERROR_VERSION_UNSUPPORTED);
            return;
        }
        if (ContextUtils.getApplicationContext()
                        .checkPermission(
                                PERMISSION_ACCESS_ADSERVICES_ATTRIBUTION,
                                Process.myPid(),
                                Process.myUid())
                != PackageManager.PERMISSION_GRANTED) {
            // Permission may not be granted when embedded as WebView.
            onMeasurementStateReturned(/* status= */ 0, OperationResult.ERROR_PERMISSION_UNGRANTED);
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
        ResettersForTesting.register(
                () -> {
                    sManagerForTesting = null;
                    PostTask.postTask(
                            TaskTraits.BEST_EFFORT,
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
