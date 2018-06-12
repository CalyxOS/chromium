// Copyright 2021 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.commerce;

/** Self-documenting feature class for shopping.  */
public class ShoppingFeatures {
    /** Returns whether shopping is enabled. */
    public static boolean isShoppingListEnabled() {
        return false;
    }

    private static boolean isSignedIn() {
        return false;
    }

    private static boolean isAnonymizedUrlDataCollectionEnabled() {
        return false;
    }

    private static boolean isWebAndAppActivityEnabled() {
        return false;
    }
}
