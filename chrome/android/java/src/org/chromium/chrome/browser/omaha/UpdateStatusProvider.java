// Copyright 2019 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.omaha;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.chromium.base.ActivityState;
import org.chromium.base.ApplicationStatus;
import org.chromium.base.ApplicationStatus.ActivityStateListener;
import org.chromium.base.BuildInfo;
import org.chromium.base.Callback;
import org.chromium.base.ContextUtils;
import org.chromium.base.ObserverList;
import org.chromium.base.ThreadUtils;
import org.chromium.base.metrics.RecordHistogram;
import org.chromium.base.task.AsyncTask;
import org.chromium.base.task.AsyncTask.Status;
import org.chromium.base.task.PostTask;
import org.chromium.chrome.browser.app.ChromeActivity;
import org.chromium.chrome.browser.omaha.inline.BromiteInlineUpdateController;
import org.chromium.chrome.browser.omaha.inline.InlineUpdateController;
import org.chromium.chrome.browser.omaha.inline.InlineUpdateControllerFactory;
import org.chromium.chrome.browser.omaha.metrics.UpdateSuccessMetrics;
import org.chromium.chrome.browser.preferences.ChromePreferenceKeys;
import org.chromium.chrome.browser.preferences.SharedPreferencesManager;
import org.chromium.components.browser_ui.util.ConversionUtils;
import org.chromium.content_public.browser.UiThreadTaskTraits;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.chromium.base.Log;
import android.content.SharedPreferences;
import android.os.Build;
import org.chromium.build.BuildConfig;

/**
 * Provides the current update state for Chrome.  This update state is asynchronously determined and
 * can change as Chrome runs.
 *
 * For manually testing this functionality, see {@link UpdateConfigs}.
 */
public class UpdateStatusProvider implements ActivityStateListener {
    /**
     * Possible update states.
     * Treat this as append only as it is used by UMA.
     */
    @IntDef({UpdateState.NONE, UpdateState.UPDATE_AVAILABLE, UpdateState.UNSUPPORTED_OS_VERSION,
            UpdateState.INLINE_UPDATE_AVAILABLE, UpdateState.INLINE_UPDATE_DOWNLOADING,
            UpdateState.INLINE_UPDATE_READY, UpdateState.INLINE_UPDATE_FAILED, UpdateState.VULNERABLE_VERSION})
    @Retention(RetentionPolicy.SOURCE)
    public @interface UpdateState {
        int NONE = 0;
        int UPDATE_AVAILABLE = 1;
        int UNSUPPORTED_OS_VERSION = 2;
        int INLINE_UPDATE_AVAILABLE = 3;
        int INLINE_UPDATE_DOWNLOADING = 4;
        int INLINE_UPDATE_READY = 5;
        int INLINE_UPDATE_FAILED = 6;
        int VULNERABLE_VERSION = 7;

        int NUM_ENTRIES = 8;
    }

    /** A set of properties that represent the current update state for Chrome. */
    public static final class UpdateStatus {
        /**
         * The current state of whether an update is available or whether it ever will be
         * (unsupported OS).
         */
        public @UpdateState int updateState;

        /** URL to direct the user to when Omaha detects a newer version available. */
        public String updateUrl;

        /**
         * The latest Chrome version available if OmahaClient.isNewerVersionAvailable() returns
         * true.
         */
        public String latestVersion;

        /**
         * If the current OS version is unsupported, and we show the menu badge, and then the user
         * clicks the badge and sees the unsupported message, we store the current version to a
         * preference and cache it here. This preference is read on startup to ensure we only show
         * the unsupported message once per version.
         */
        public String latestUnsupportedVersion;

        /**
         * Whether or not we are currently trying to simulate the update.  Used to ignore other
         * update signals.
         */
        private boolean mIsSimulated;

        /**
         * Whether or not we are currently trying to simulate an inline flow.  Used to allow
         * overriding Omaha update state, which usually supersedes inline update states.
         */
        private boolean mIsInlineSimulated;

        public UpdateStatus() {}

        UpdateStatus(UpdateStatus other) {
            updateState = other.updateState;
            updateUrl = other.updateUrl;
            latestVersion = other.latestVersion;
            latestUnsupportedVersion = other.latestUnsupportedVersion;
            mIsSimulated = other.mIsSimulated;
            mIsInlineSimulated = other.mIsInlineSimulated;
        }
    }

    private final ObserverList<Callback<UpdateStatus>> mObservers = new ObserverList<>();

    private final InlineUpdateController mInlineController;
    private final UpdateQuery mOmahaQuery;
    private final UpdateSuccessMetrics mMetrics;
    private @Nullable UpdateStatus mStatus;

    /** Whether or not we've recorded the initial update status yet. */
    private boolean mRecordedInitialStatus;

    /** @return Returns a singleton of {@link UpdateStatusProvider}. */
    public static UpdateStatusProvider getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * Adds {@code observer} to notify about update state changes.  It is safe to call this multiple
     * times with the same {@code observer}.  This method will always notify {@code observer} of the
     * current status.  If that status has not been calculated yet this method call will trigger the
     * async work to calculate it.
     * @param observer The observer to notify about update state changes.
     * @return {@code true} if {@code observer} is newly registered.  {@code false} if it was
     *         already registered.
     */
    public boolean addObserver(Callback<UpdateStatus> observer) {
        if (mObservers.hasObserver(observer)) return false;
        mObservers.addObserver(observer);

        if (mStatus != null) {
            PostTask.postTask(UiThreadTaskTraits.DEFAULT, observer.bind(mStatus));
        } else {
            if (mOmahaQuery.getStatus() == Status.PENDING) {
                mOmahaQuery.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

        return true;
    }

    /**
     * No longer notifies {@code observer} about update state changes.  It is safe to call this
     * multiple times with the same {@code observer}.
     * @param observer To no longer notify about update state changes.
     */
    public void removeObserver(Callback<UpdateStatus> observer) {
        if (!mObservers.hasObserver(observer)) return;
        mObservers.removeObserver(observer);
    }

    /**
     * Notes that the user is aware that this version of Chrome is no longer supported and
     * potentially updates the update state accordingly.
     */
    public void updateLatestUnsupportedVersion() {
        if (mStatus == null) return;

        // If we have already stored the current version to a preference, no need to store it again,
        // unless their Chrome version has changed.
        String currentlyUsedVersion = BuildInfo.getInstance().versionName;
        if (mStatus.latestUnsupportedVersion != null
                && mStatus.latestUnsupportedVersion.equals(currentlyUsedVersion)) {
            return;
        }

        SharedPreferencesManager.getInstance().writeString(
                ChromePreferenceKeys.LATEST_UNSUPPORTED_VERSION, currentlyUsedVersion);
        mStatus.latestUnsupportedVersion = currentlyUsedVersion;
        pingObservers();
    }

    /**
     * Starts the inline update process, if possible.
     * @param activity An {@link Activity} that will be used to interact with Play.
     */
    public void startInlineUpdate(Activity activity) {
        if (mStatus == null || (mStatus.updateState != UpdateState.INLINE_UPDATE_AVAILABLE && mStatus.updateState != UpdateState.VULNERABLE_VERSION)) return;
        mInlineController.startUpdate(activity);
    }

    /**
     * Retries the inline update process, if possible.
     * @param activity An {@link Activity} that will be used to interact with Play.
     */
    public void retryInlineUpdate(Activity activity) {
        if (mStatus == null || (mStatus.updateState != UpdateState.INLINE_UPDATE_AVAILABLE && mStatus.updateState != UpdateState.VULNERABLE_VERSION)) return;
        mInlineController.startUpdate(activity);
    }

    /** Finishes the inline update process, which may involve restarting the app. */
    public void finishInlineUpdate() {
        if (mStatus == null || mStatus.updateState != UpdateState.INLINE_UPDATE_READY) return;
        mInlineController.completeUpdate();
    }

    /**
     * Starts the intent update process, if possible
     * @param context An {@link Context} that will be used to fire off the update intent.
     * @param newTask Whether or not to make the intent a new task.
     * @return        Whether or not the update intent was sent and had a valid handler.
     */
    public boolean startIntentUpdate(Context context, boolean newTask) {
        // currently not used in Bromite
        if (mStatus == null || mStatus.updateState != UpdateState.UPDATE_AVAILABLE) return false;
        if (TextUtils.isEmpty(mStatus.updateUrl)) return false;

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mStatus.updateUrl));
            if (newTask) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            return false;
        }

        return true;
    }

    // ApplicationStateListener implementation.
    @Override
    public void onActivityStateChange(Activity changedActivity, @ActivityState int newState) {
        boolean hasActiveActivity = false;

        for (Activity activity : ApplicationStatus.getRunningActivities()) {
            if (activity == null || !(activity instanceof ChromeActivity)) continue;

            hasActiveActivity |=
                    ApplicationStatus.getStateForActivity(activity) == ActivityState.RESUMED;
            if (hasActiveActivity) break;
        }

        mInlineController.setEnabled(hasActiveActivity);
    }

    private UpdateStatusProvider() {
        mInlineController = InlineUpdateControllerFactory.create(this::resolveStatus);
        mOmahaQuery = new UpdateQuery(this::resolveStatus);
        mMetrics = new UpdateSuccessMetrics();

        // Note that as a singleton this class never unregisters.
        ApplicationStatus.registerStateListenerForAllActivities(this);
    }

    private void pingObservers() {
        for (Callback<UpdateStatus> observer : mObservers) observer.onResult(mStatus);
    }

    private void resolveStatus() {
        if (mOmahaQuery.getStatus() != Status.FINISHED || mInlineController.getStatus() == null) {
            return;
        }

        // We pull the Omaha result once as it will never change.
        if (mStatus == null) mStatus = new UpdateStatus(mOmahaQuery.getResult());

        if (mStatus.mIsSimulated) { // used only during tests
            if (mStatus.mIsInlineSimulated) {
                @UpdateState
                int inlineState = mInlineController.getStatus();
                String updateUrl = mInlineController.getUpdateUrl();

                if (inlineState == UpdateState.NONE) {
                    mStatus.updateState = mOmahaQuery.getResult().updateState;
                } else {
                    mStatus.updateState = inlineState;
                    mStatus.updateUrl = updateUrl;
                }
            }
        } else {
            // used by Bromite to resolve update status
            // ignores Omaha status
            @UpdateState
            int inlineState = mInlineController.getStatus();
            mStatus.updateState = inlineState;
            mStatus.updateUrl = mInlineController.getUpdateUrl();
        }

        if (!mRecordedInitialStatus) {
            RecordHistogram.recordEnumeratedHistogram(
                    "GoogleUpdate.StartUp.State", mStatus.updateState, UpdateState.NUM_ENTRIES);
            mRecordedInitialStatus = true;
        }

        pingObservers();
    }

    private static final class LazyHolder {
        private static final UpdateStatusProvider INSTANCE = new UpdateStatusProvider();
    }

    private static final class UpdateQuery extends AsyncTask<UpdateStatus> {
        static final String TAG = "UpdateStatusProvider";
        private final Context mContext = ContextUtils.getApplicationContext();
        private final Runnable mCallback;

        private @Nullable UpdateStatus mStatus;

        public UpdateQuery(@NonNull Runnable resultReceiver) {
            mCallback = resultReceiver;
        }

        public UpdateStatus getResult() {
            return mStatus;
        }

        @Override
        protected UpdateStatus doInBackground() {
            UpdateStatus testStatus = getTestStatus();
            if (testStatus != null) return testStatus;
            return getActualStatus(mContext);
        }

        @Override
        protected void onPostExecute(UpdateStatus result) {
            mStatus = result;
            PostTask.postTask(UiThreadTaskTraits.DEFAULT, mCallback);
        }

        private UpdateStatus getTestStatus() {
            @UpdateState
            Integer forcedUpdateState = UpdateConfigs.getMockUpdateState();
            if (forcedUpdateState == null) return null;

            UpdateStatus status = new UpdateStatus();

            status.mIsSimulated = true;
            status.updateState = forcedUpdateState;

            status.mIsInlineSimulated = forcedUpdateState == UpdateState.INLINE_UPDATE_AVAILABLE;

            // Push custom configurations for certain update states.
            switch (forcedUpdateState) {
                case UpdateState.UPDATE_AVAILABLE:
                    String updateUrl = UpdateConfigs.getMockMarketUrl();
                    if (!TextUtils.isEmpty(updateUrl)) status.updateUrl = updateUrl;
                    break;
                case UpdateState.UNSUPPORTED_OS_VERSION:
                    status.latestUnsupportedVersion =
                            SharedPreferencesManager.getInstance().readString(
                                    ChromePreferenceKeys.LATEST_UNSUPPORTED_VERSION, null);
                    break;
            }

            return status;
        }

        private UpdateStatus getActualStatus(Context context) {
            UpdateStatus status = new UpdateStatus();

            SharedPreferences preferences = OmahaBase.getSharedPreferences();
            status.latestVersion = preferences.getString(OmahaBase.PREF_LATEST_MODIFIED_VERSION, "");

            status.updateState = UpdateState.NONE;
            if (status.latestVersion != null && status.latestVersion.length() != 0) {
                VersionNumber latestVersion = VersionNumber.fromString(status.latestVersion);
                if (latestVersion == null) {
                   Log.e(TAG, "BromiteUpdater: stored latest version '%s' is invalid", status.latestVersion);
                } else if (OmahaBase.isNewVersionAvailableByVersion(latestVersion)) {
                   status.updateState = UpdateState.INLINE_UPDATE_AVAILABLE;
                   status.updateUrl = BromiteInlineUpdateController.getDownloadUrl();
                   return status;
                }
                String latestUpstreamVersion = preferences.getString(OmahaBase.PREF_LATEST_UPSTREAM_VERSION, "");
                if (latestUpstreamVersion != null && latestUpstreamVersion.length() != 0) {
                   VersionNumber upstreamVersion = VersionNumber.fromString(latestUpstreamVersion);
                   if (upstreamVersion == null) {
                       Log.e(TAG, "BromiteUpdater: stored latest upstream version '%s' is invalid", latestUpstreamVersion);
                   } else if (OmahaBase.isNewVersionAvailableByVersion(upstreamVersion)) {
                       status.updateUrl = BromiteInlineUpdateController.VULNERABLE_VERSION_DOC_URL;
                       status.updateState = UpdateState.VULNERABLE_VERSION;
                       return status;
                   }
                }
            }

            return status;
        }

        private boolean checkForSufficientStorage() {
            assert !ThreadUtils.runningOnUiThread();

            File path = Environment.getDataDirectory();
            StatFs statFs = new StatFs(path.getAbsolutePath());
            long size = getSize(statFs);
            RecordHistogram.recordLinearCountHistogram(
                    "GoogleUpdate.InfoBar.DeviceFreeSpace", (int) size, 1, 1000, 50);

            int minRequiredStorage = UpdateConfigs.getMinRequiredStorage();
            if (minRequiredStorage == -1) return true;

            return size >= minRequiredStorage;
        }

        private boolean isGooglePlayStoreAvailable(Context context) {
            return false;
        }

        private long getSize(StatFs statFs) {
            return ConversionUtils.bytesToMegabytes(statFs.getAvailableBytes());
        }
    }
}
