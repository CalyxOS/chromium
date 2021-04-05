// Copyright 2018 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.components.media_router.caf;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import org.chromium.base.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Wrapper for {@link CastSession} for Casting. */
public class CastSessionController extends BaseSessionController {
    private static final String TAG = "CafSessionCtrl";

    private List<String> mNamespaces = new ArrayList<String>();
    private CafNotificationController mNotificationController;

    public CastSessionController(CafBaseMediaRouteProvider provider) {
        super(provider);
        mNotificationController = new CafNotificationController(this);
    }

    public List<String> getNamespaces() {
        return mNamespaces;
    }

    @Override
    public void onSessionEnded() {
        getMessageHandler().onSessionEnded();
        super.onSessionEnded();
    }

    @Override
    public BaseNotificationController getNotificationController() {
        return mNotificationController;
    }

    private void onApplicationStatusChanged() {
        updateNamespaces();

        getMessageHandler().broadcastClientMessage(
                "update_session", getMessageHandler().buildSessionMessage());
    }

    @VisibleForTesting
    void updateNamespaces() {
        if (!isConnected()) return;
    }

    private void registerNamespace(String namespace) {
        assert !mNamespaces.contains(namespace);

        if (!isConnected()) return;
    }

    private void unregisterNamespace(String namespace) {
        assert mNamespaces.contains(namespace);

        if (!isConnected()) return;
    }

    @NonNull
    private CafMessageHandler getMessageHandler() {
        return ((CafMediaRouteProvider) getProvider()).getMessageHandler();
    }
}
