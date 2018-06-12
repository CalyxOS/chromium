// Copyright 2020 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.share.send_tab_to_self;

import android.accounts.Account;
import android.content.Context;

import org.chromium.base.Callback;
import org.chromium.chrome.browser.flags.ChromeFeatureList;
import org.chromium.chrome.browser.profiles.Profile;
import org.chromium.components.browser_ui.bottomsheet.BottomSheetController;
import org.chromium.components.browser_ui.bottomsheet.EmptyBottomSheetObserver;
import org.chromium.ui.base.WindowAndroid;

import java.util.List;
import java.util.Optional;

/**
 * Coordinator for displaying the send tab to self feature.
 */
public class SendTabToSelfCoordinator {
    private final Context mContext;
    private final WindowAndroid mWindowAndroid;
    private final String mUrl;
    private final String mTitle;
    private final BottomSheetController mController;
    private final Profile mProfile;

    public SendTabToSelfCoordinator(Context context, WindowAndroid windowAndroid, String url,
            String title, BottomSheetController controller, Profile profile) {
        mContext = context;
        mWindowAndroid = windowAndroid;
        mUrl = url;
        mTitle = title;
        mController = controller;
        mProfile = profile;
    }

    public void show() {
    }

    private void onSignInComplete() {
    }
}
