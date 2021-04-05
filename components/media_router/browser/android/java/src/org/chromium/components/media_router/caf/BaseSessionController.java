// Copyright 2018 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.components.media_router.caf;

import androidx.annotation.Nullable;

import org.chromium.base.Log;
import org.chromium.components.media_router.FlingingController;
import org.chromium.components.media_router.MediaSink;
import org.chromium.components.media_router.MediaSource;

import java.util.ArrayList;
import java.util.List;

/**
 * A base wrapper for {@link CastSession}, extending its functionality for Chrome MediaRouter.
 *
 * Has persistent lifecycle and always attaches itself to the current {@link CastSession}.
 */
public abstract class BaseSessionController {
    private static final String TAG = "BaseSessionCtrl";

    /** Callback class for listening to state changes. */
    public static interface Callback {
        /** Called when session started. */
        void onSessionStarted();

        /** Called when session ended. */
        void onSessionEnded();

        /** Called when status updated. */
        void onStatusUpdated();

        /** Called when metadata updated. */
        void onMetadataUpdated();
    }

    private final CafBaseMediaRouteProvider mProvider;
    private CreateRouteRequestInfo mRouteCreationInfo;
    private final List<Callback> mCallbacks = new ArrayList<>();

    public BaseSessionController(CafBaseMediaRouteProvider provider) {
        mProvider = provider;
    }

    public void addCallback(Callback callback) {
        mCallbacks.add(callback);
    }

    public void removeCallback(Callback callback) {
        mCallbacks.remove(callback);
    }

    public void requestSessionLaunch() {
        mRouteCreationInfo = mProvider.getPendingCreateRouteRequestInfo();
        // When the user clicks a route on the MediaRouteChooserDialog, we intercept the click event
        // and do not select the route. Instead the route selection is postponed to here. This will
        // trigger CAF to launch the session.
        mRouteCreationInfo.routeInfo.select();
    }

    public MediaSource getSource() {
        return (mRouteCreationInfo != null) ? mRouteCreationInfo.source : null;
    }

    public MediaSink getSink() {
        return (mRouteCreationInfo != null) ? mRouteCreationInfo.sink : null;
    }

    public CreateRouteRequestInfo getRouteCreationInfo() {
        return mRouteCreationInfo;
    }

    public abstract BaseNotificationController getNotificationController();

    public void endSession() {
    }

    public List<String> getCapabilities() {
        List<String> capabilities = new ArrayList<>();
        return capabilities;
    }

    public boolean isConnected() {
        return false;
    }

    private void updateRemoteMediaClient(String message) {
        if (!isConnected()) return;
    }

    /** Called when session started. */
    public void onSessionStarted() {
        notifyCallback((Callback callback) -> callback.onSessionStarted());
    }

    /** Called when session ended. */
    public void onSessionEnded() {
        notifyCallback((Callback callback) -> callback.onSessionEnded());
    }

    protected final CafBaseMediaRouteProvider getProvider() {
        return mProvider;
    }

    /**
     *  Helper message to get the session ID of the attached session. For stubbing in tests as
     * {@link CastSession#getSessionId()} is final.
     */
    public String getSessionId() {
        return null;
    }

    private void notifyCallback(NotifyCallbackAction action) {
        for (Callback callback : mCallbacks) {
            action.notify(callback);
        }
    }

    private interface NotifyCallbackAction {
        void notify(Callback callback);
    }
}
