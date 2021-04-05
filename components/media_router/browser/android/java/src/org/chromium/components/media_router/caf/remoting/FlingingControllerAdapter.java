// Copyright 2018 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.components.media_router.caf.remoting;

import org.chromium.base.Log;
import org.chromium.components.media_router.FlingingController;
import org.chromium.components.media_router.MediaController;
import org.chromium.components.media_router.MediaStatusBridge;
import org.chromium.components.media_router.MediaStatusObserver;

/** Adapter class for bridging {@link RemoteMediaClient} and {@link FlingController}. */
public class FlingingControllerAdapter implements FlingingController, MediaController {
    private static final String TAG = "FlingCtrlAdptr";

    private final StreamPositionExtrapolator mStreamPositionExtrapolator;
    private final RemotingSessionController mSessionController;
    private final String mMediaUrl;
    private MediaStatusObserver mMediaStatusObserver;
    private boolean mLoaded;
    private boolean mHasEverReceivedValidMediaSession;

    FlingingControllerAdapter(RemotingSessionController sessionController, String mediaUrl) {
        mSessionController = sessionController;
        mMediaUrl = mediaUrl;
        mStreamPositionExtrapolator = new StreamPositionExtrapolator();
    }

    ////////////////////////////////////////////
    // FlingingController implementation begin
    ////////////////////////////////////////////

    @Override
    public MediaController getMediaController() {
        return this;
    }

    @Override
    public void setMediaStatusObserver(MediaStatusObserver observer) {
        assert mMediaStatusObserver == null;
        mMediaStatusObserver = observer;
    }

    @Override
    public void clearMediaStatusObserver() {
        assert mMediaStatusObserver != null;
        mMediaStatusObserver = null;
    }

    @Override
    public long getApproximateCurrentTime() {
        return mStreamPositionExtrapolator.getPosition();
    }

    public long getDuration() {
        return mStreamPositionExtrapolator.getDuration();
    }

    ////////////////////////////////////////////
    // FlingingController implementation end
    ////////////////////////////////////////////

    /** Starts loading the media URL, from the given position. */
    public void load(long position) {
        if (!mSessionController.isConnected()) return;
    }

    ////////////////////////////////////////////
    // MediaController implementation begin
    ////////////////////////////////////////////

    @Override
    public void play() {
        if (!mSessionController.isConnected()) return;

        if (!mLoaded) {
            load(/* position= */ 0);
            return;
        }
    }

    @Override
    public void pause() {
        if (!mSessionController.isConnected()) return;
    }

    @Override
    public void setMute(boolean mute) {
        if (!mSessionController.isConnected()) return;
    }

    @Override
    public void setVolume(double volume) {
        if (!mSessionController.isConnected()) return;
    }

    @Override
    public void seek(long position) {
    }

    ////////////////////////////////////////////
    // MediaController implementation end
    ////////////////////////////////////////////
}
