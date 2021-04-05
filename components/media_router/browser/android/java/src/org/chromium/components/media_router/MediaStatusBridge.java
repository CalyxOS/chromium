// Copyright 2018 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.components.media_router;

import org.chromium.base.annotations.CalledByNative;
import org.chromium.base.annotations.JNINamespace;

/**
 * Wrapper layer that exposes a gms.cast.MediaStatus to native code.
 * See also media/base/media_status.h.
 */
@JNINamespace("media_router")
public class MediaStatusBridge {

    /**
     * Gets the play state of the stream. Return values are defined as such:
     * - PLAYER_STATE_UNKOWN = 0
     * - PLAYER_STATE_IDLE = 1
     * - PLAYER_STATE_PLAYING = 2
     * - PLAYER_STATE_PAUSED = 3
     * - PLAYER_STATE_BUFFERING = 4
     * See https://developers.google.com/android/reference/com/google/android/gms/cast/MediaStatus
     */
    @CalledByNative
    public int playerState() {
        return 0;
    }

    /**
     * Gets the idle reason. Only meaningful if we are in PLAYER_STATE_IDLE.
     * - IDLE_REASON_NONE = 0
     * - IDLE_REASON_FINISHED = 1
     * - IDLE_REASON_CANCELED = 2
     * - IDLE_REASON_INTERRUPTED = 3
     * - IDLE_REASON_ERROR = 4
     * See https://developers.google.com/android/reference/com/google/android/gms/cast/MediaStatus
     */
    @CalledByNative
    public int idleReason() {
        return 0;
    }

    /**
     * The main title of the media. For example, in a MediaStatus representing
     * a YouTube Cast session, this could be the title of the video.
     */
    @CalledByNative
    public String title() {
        return "";
    }

    /**
     * If this is true, the media can be played and paused.
     */
    @CalledByNative
    public boolean canPlayPause() {
        return false;
    }

    /**
     * If this is true, the media can be muted and unmuted.
     */
    @CalledByNative
    public boolean canMute() {
        return false;
    }

    /**
     * If this is true, the media's volume can be changed.
     */
    @CalledByNative
    public boolean canSetVolume() {
        return false;
    }

    /**
     * If this is true, the media's current playback position can be chaxnged.
     */
    @CalledByNative
    public boolean canSeek() {
        return false;
    }

    /**
     * Returns the stream's mute state.
     */
    @CalledByNative
    public boolean isMuted() {
        return false;
    }

    /**
     * Current volume of the media, with 1 being the highest and 0 being the
     * lowest/no sound. When |is_muted| is true, there should be no sound
     * regardless of |volume|.
     */
    @CalledByNative
    public double volume() {
        return 0.0;
    }

    /**
     * The length of the media, in ms. A value of zero indicates that this is a media
     * with no set duration (e.g. a live stream).
     */
    @CalledByNative
    public long duration() {
        return 0;
    }

    /**
     * Current playback position, in ms. Must be less than or equal to |duration|.
     */
    @CalledByNative
    public long currentTime() {
        return 0;
    }
}
