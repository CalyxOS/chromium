// Copyright 2015 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.bookmarks;

import android.content.Context;

/**
 * Class that manages all the logic and UI behind the signin promo header in the bookmark
 * content UI. The header is shown only on certain situations, (e.g., not signed in).
 */
public class BookmarkPromoHeader {
    // TODO(kkimlabs): Figure out the optimal number based on UMA data.
    private static final int MAX_SIGNIN_AND_SYNC_PROMO_SHOW_COUNT = 10;

    /**
     * Initializes the class. Note that this will start listening to signin related events and
     * update itself if needed.
     */
    BookmarkPromoHeader(Context context, Runnable promoHeaderChangeAction) {
    }
}
