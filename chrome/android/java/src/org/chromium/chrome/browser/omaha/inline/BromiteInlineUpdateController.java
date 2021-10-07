// Copyright 2021 The Ungoogled Chromium Authors. All rights reserved.
//
// This file is part of Ungoogled Chromium Android.
//
// Ungoogled Chromium Android is free software: you can redistribute it
// and/or modify it under the terms of the GNU General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or any later version.
//
// Ungoogled Chromium Android is distributed in the hope that it will be
// useful, but WITHOUT ANY WARRANTY; without even the implied warranty
// of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Ungoogled Chromium Android.  If not,
// see <https://www.gnu.org/licenses/>.

package org.chromium.chrome.browser.omaha.inline;

import static org.chromium.chrome.browser.omaha.UpdateConfigs.getUpdateNotificationInterval;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.format.DateUtils;
import org.chromium.build.BuildConfig;

import androidx.annotation.Nullable;

import org.chromium.base.Callback;
import org.chromium.base.Log;
import org.chromium.base.task.AsyncTask;
import org.chromium.base.task.PostTask;
import org.chromium.chrome.browser.app.ChromeActivity;
import org.chromium.chrome.browser.omaha.OmahaBase;
import org.chromium.chrome.browser.omaha.UpdateConfigs;
import org.chromium.chrome.browser.omaha.UpdateStatusProvider;
import org.chromium.chrome.browser.profiles.Profile;
import org.chromium.chrome.browser.tab.TabLaunchType;
import org.chromium.chrome.browser.tabmodel.TabCreator;
import org.chromium.content_public.browser.LoadUrlParams;
import org.chromium.content_public.browser.UiThreadTaskTraits;
import org.chromium.ui.base.PageTransition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.regex.Pattern;

import org.chromium.chrome.browser.endpoint_fetcher.EndpointFetcher;
import org.chromium.chrome.browser.endpoint_fetcher.EndpointResponse;
import org.chromium.chrome.browser.omaha.VersionNumber;

public class BromiteInlineUpdateController implements InlineUpdateController {

    private static final String TAG = "BromiteInlineUpdateController";
    private final String REDIRECT_URL_PREFIX = "https://github.com/bromite/bromite/releases/download/";
    private static final String UPDATE_VERSION_URL = "https://github.com/bromite/bromite/releases/latest/download/";
    private final String UPSTREAM_VERSION_URL = "https://www.bromite.org/upstream.txt";
    public static final String VULNERABLE_VERSION_DOC_URL = "https://www.bromite.org/vulnerable-version";

    public static String getDownloadUrl() {
        return UPDATE_VERSION_URL + BuildConfig.BUILD_TARGET_CPU + "_ChromePublic.apk";
    }

    private boolean mEnabled = true;
    private Runnable mCallback;
    private @Nullable @UpdateStatusProvider.UpdateState Integer mUpdateState =
                                                    UpdateStatusProvider.UpdateState.NONE;
    private String mUpdateUrl = "";

    BromiteInlineUpdateController(Runnable callback) {
        mCallback = callback;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mEnabled == enabled) return;

        mEnabled = enabled;
        // check for an update when state changes
        if (mEnabled) pullCurrentState();
    }

    @Override
    public @Nullable @UpdateStatusProvider.UpdateState Integer getStatus() {
        if (mEnabled) pullCurrentState();
        return mUpdateState;
    }

    @Override
    public String getUpdateUrl() {
        // relies on a prior call to getStatus() to have state and URL correctly pulled
        return mUpdateUrl;
    }

    @Override
    public void startUpdate(Activity activity) {
        assert ChromeActivity.class.isInstance(activity);
        ChromeActivity thisActivity = (ChromeActivity) activity;
        // Always open in new incognito tab
        TabCreator tabCreator = thisActivity.getTabCreator(true);
        tabCreator.createNewTab(new LoadUrlParams(mUpdateUrl, PageTransition.AUTO_BOOKMARK),
                TabLaunchType.FROM_LINK, thisActivity.getActivityTab());
    }

    @Override
    public void completeUpdate() {
    }

    private void pullCurrentState() {
        if (OmahaBase.getSharedPreferences()
                .getBoolean(OmahaBase.PREF_ALLOW_INLINE_UPDATE, false) == false) {
            Log.i(TAG, "BromiteUpdater: disabled by user");
            return;
        }

        // do not pull state if there is already a state set
        if (mUpdateState != UpdateStatusProvider.UpdateState.NONE)
            return;

        if (shallUpdate() == false)
            return;

        switch (mUpdateState) {
            case UpdateStatusProvider.UpdateState.INLINE_UPDATE_AVAILABLE:
                break;
            case UpdateStatusProvider.UpdateState.NONE:
                OmahaBase.resetUpdatePrefs();
                checkLatestVersion((latestVersion) -> {
                    if (latestVersion == null) return;

                    if (OmahaBase.isNewVersionAvailableByVersion(latestVersion)) {
                        postStatus(UpdateStatusProvider.UpdateState.INLINE_UPDATE_AVAILABLE, getDownloadUrl());
                    } else {
                        checkLatestUpstreamVersion((latestUpstreamVersion) -> {
                           if (latestUpstreamVersion == null) return;
                           if (OmahaBase.isNewVersionAvailableByVersion(latestUpstreamVersion)) {
                               postStatus(UpdateStatusProvider.UpdateState.VULNERABLE_VERSION, VULNERABLE_VERSION_DOC_URL);
                           }
                        });
                    }
                });
                break;
            case UpdateStatusProvider.UpdateState.INLINE_UPDATE_READY:
                // Intentional fall through.
            case UpdateStatusProvider.UpdateState.INLINE_UPDATE_FAILED:
                // Intentional fall through.
            case UpdateStatusProvider.UpdateState.INLINE_UPDATE_DOWNLOADING:
                // Intentional fall through.
            case UpdateStatusProvider.UpdateState.UNSUPPORTED_OS_VERSION:
                // Intentional fall through.
            case UpdateStatusProvider.UpdateState.VULNERABLE_VERSION:
                // Intentional fall through.
            default:
                return;
        }
    }

    private boolean shallUpdate() {
        long currentTime = System.currentTimeMillis();
        SharedPreferences preferences = OmahaBase.getSharedPreferences();
        long lastPushedTimeStamp = preferences.getLong(OmahaBase.PREF_TIMESTAMP_OF_REQUEST, 0);
        return currentTime - lastPushedTimeStamp >= getUpdateNotificationInterval();
    }

    private void checkLatestVersion(final Callback<VersionNumber> callback) {
        assert UPDATE_VERSION_URL != null;

        String urlToCheck = getDownloadUrl();
        Log.i(TAG, "BromiteUpdater: fetching with HEAD '%s'", urlToCheck);

        EndpointFetcher.nativeHeadWithNoAuth(
                (endpointResponse) -> {
                    boolean versionFound = false;
                    String redirectURL = endpointResponse.getRedirectUrl();
                    if (redirectURL != null) {
                        Log.i(TAG, "BromiteUpdater: obtained response '%s' and redirect URL '%s'", endpointResponse.getResponseString(), redirectURL);
                        if (redirectURL.indexOf(REDIRECT_URL_PREFIX) == 0) {
                            redirectURL = redirectURL.substring(REDIRECT_URL_PREFIX.length());
                            String[] parts = redirectURL.split(Pattern.quote("/"));
                            if (parts.length > 0) {
                                VersionNumber version = VersionNumber.fromString(parts[0]);
                                if (version != null) {
                                    versionFound = true;
                                    OmahaBase.setLatestModifiedVersion(parts[0]);
                                    callback.onResult(version);
                                    return;
                                }
                            }
                        }
                    }
                    if (!versionFound) {
                        // retry after 1 hour
                        OmahaBase.updateLastPushedTimeStamp(
                            System.currentTimeMillis() - getUpdateNotificationInterval() -
                            DateUtils.HOUR_IN_MILLIS);
                        Log.e(TAG, "BromiteUpdater: failed, will retry in 1 hour");
                    }

                    callback.onResult(null);
                },
                Profile.getLastUsedRegularProfile(),
                urlToCheck, /*timeout*/5000, /*follow_redirect*/true);
    }

    private void checkLatestUpstreamVersion(final Callback<VersionNumber> callback) {
        Log.i(TAG, "BromiteUpdater: fetching with GET '%s'", UPSTREAM_VERSION_URL);

        EndpointFetcher.nativeFetchWithNoAuth(
                (endpointResponse) -> {
                    String response = endpointResponse.getResponseString().trim();
                    Log.i(TAG, "BromiteUpdater: obtained upstream version update response '%s'", response);
                    VersionNumber version = VersionNumber.fromString(response);
                    if (version != null) {
                        OmahaBase.updateLastPushedTimeStamp(System.currentTimeMillis());
                        OmahaBase.setLatestUpstreamVersion(response);
                        callback.onResult(version);
                        return;
                    }
                    // retry after 1 hour
                    OmahaBase.updateLastPushedTimeStamp(
                        System.currentTimeMillis() - getUpdateNotificationInterval() -
                        DateUtils.HOUR_IN_MILLIS);
                    Log.e(TAG, "BromiteUpdater: failed to fetch upstream version, will retry in 1 hour");

                    callback.onResult(null);
                },
                Profile.getLastUsedRegularProfile(),
                UPSTREAM_VERSION_URL, /*timeout*/5000, /*follow_redirect*/false);
    }

    private void postStatus(@UpdateStatusProvider.UpdateState int status, String updateUrl) {
        mUpdateState = status;
        mUpdateUrl = updateUrl;
        PostTask.postTask(UiThreadTaskTraits.DEFAULT, mCallback);
    }
}
