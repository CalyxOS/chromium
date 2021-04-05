// Copyright 2018 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.components.media_router.caf.remoting;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.mediarouter.app.MediaRouteButton;

import org.chromium.components.browser_ui.media.MediaNotificationUma;
import org.chromium.components.media_router.MediaRouteUmaRecorder;
import org.chromium.components.media_router.R;
import org.chromium.components.media_router.caf.BaseSessionController;
import org.chromium.third_party.android.media.MediaController;

/**
 * The activity that's opened by clicking the video flinging (casting) notification.
 */
public class CafExpandedControllerActivity
        extends FragmentActivity implements BaseSessionController.Callback {
    private static final int PROGRESS_UPDATE_PERIOD_IN_MS = 1000;

    private Handler mHandler;
    // We don't use the standard android.media.MediaController, but a custom one.
    // See the class itself for details.
    private MediaController mMediaController;
    private RemotingSessionController mSessionController;
    private MediaRouteButton mMediaRouteButton;
    private TextView mTitleView;
    private Runnable mUpdateProgressRunnable;

    /**
     * Handle actions from on-screen media controls.
     */
    private MediaController.Delegate mControllerDelegate = new MediaController.Delegate() {
        @Override
        public void play() {
        }

        @Override
        public void pause() {
        }

        @Override
        public long getDuration() {
            return 0;
        }

        @Override
        public long getPosition() {
            return 0;
        }

        @Override
        public void seekTo(long pos) {
        }

        @Override
        public boolean isPlaying() {
            return false;
        }

        @Override
        public long getActionFlags() {
            long flags =
                    PlaybackStateCompat.ACTION_REWIND | PlaybackStateCompat.ACTION_FAST_FORWARD;
            return flags;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSessionController = RemotingSessionController.getInstance();

        MediaNotificationUma.recordClickSource(getIntent());

        if (mSessionController == null || !mSessionController.isConnected()) {
            finish();
            return;
        }

        mSessionController.addCallback(this);

        // Make the activity full screen.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // requestWindowFeature must be called before adding content.
        setContentView(R.layout.expanded_cast_controller);

        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
        rootView.setBackgroundColor(Color.BLACK);

        // Create and initialize the media control UI.
        mMediaController = (MediaController) findViewById(R.id.cast_media_controller);
        mMediaController.setDelegate(mControllerDelegate);

        View castButtonView = getLayoutInflater().inflate(
                R.layout.caf_controller_media_route_button, rootView, false);
        if (castButtonView instanceof MediaRouteButton) {
            mMediaRouteButton = (MediaRouteButton) castButtonView;
            rootView.addView(mMediaRouteButton);
            mMediaRouteButton.bringToFront();
            mMediaRouteButton.setRouteSelector(mSessionController.getSource().buildRouteSelector());
        }

        mTitleView = (TextView) findViewById(R.id.cast_screen_title);

        mHandler = new Handler();
        mUpdateProgressRunnable = this::updateProgress;

        updateUi();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mSessionController == null || !mSessionController.isConnected()) {
            finish();
            return;
        }
    }

    @Override
    protected void onDestroy() {
        mSessionController.removeCallback(this);
        super.onDestroy();
    }

    @Override
    public void onSessionStarted() {}

    @Override
    public void onSessionEnded() {
        finish();
    }

    @Override
    public void onStatusUpdated() {
        updateUi();
    }

    @Override
    public void onMetadataUpdated() {
        updateUi();
    }

    private void updateUi() {
        if (!mSessionController.isConnected()) return;

        mMediaController.refresh();
        mMediaController.updateProgress();

        cancelProgressUpdateTask();
    }

    private void scheduleProgressUpdateTask() {
        mHandler.postDelayed(mUpdateProgressRunnable, PROGRESS_UPDATE_PERIOD_IN_MS);
    }

    private void cancelProgressUpdateTask() {
        mHandler.removeCallbacks(mUpdateProgressRunnable);
    }

    private void updateProgress() {
        mMediaController.updateProgress();
        scheduleProgressUpdateTask();
    }
}
