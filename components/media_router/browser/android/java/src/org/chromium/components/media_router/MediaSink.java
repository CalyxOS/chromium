// Copyright 2015 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.components.media_router;

import androidx.annotation.Nullable;
import androidx.mediarouter.media.MediaRouter;
import androidx.mediarouter.media.MediaRouter.RouteInfo;

/**
 * A common descriptor of a device that can present some URI.
 */
public class MediaSink {
    private static final String CAST_SINK_URN_PREFIX = "urn:x-org.chromium:media:sink:cast-";

    /**
     * @return The unique identifier of the sink.
     */
    public String getId() {
        return "";
    }

    /**
     * @return The user friendly name of the sink.
     */
    public String getName() {
        return "";
    }

    /**
     * @return The valid URN representing the sink.
     */
    public String getUrn() {
        return CAST_SINK_URN_PREFIX + getId();
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        return result;
    }

    /**
     * @param route The route information provided by Android.
     * @return A new MediaSink instance corresponding to the specified {@link RouteInfo}.
     */
    public static MediaSink fromRoute(MediaRouter.RouteInfo route) {
        return null;
    }

    /**
     * @param sinkId The id of the sink to find among known media routes.
     * @param router The instance of {@link MediaRouter} to enumerate the routes with.
     * @return A {@link MediaSink} corresponding to the {@link RouteInfo} with the specified id if
     * found, null otherwise.
     */
    @Nullable
    public static MediaSink fromSinkId(String sinkId, MediaRouter router) {
        return null;
    }
}
