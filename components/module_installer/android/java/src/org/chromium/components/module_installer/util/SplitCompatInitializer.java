// Copyright 2019 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.components.module_installer.util;

import org.chromium.base.ContextUtils;
import org.chromium.base.StrictModeContext;
import org.chromium.base.ThreadUtils;
import org.chromium.build.annotations.NullMarked;

/** PlayCore SplitCompat initializer for installing modules in the application context. */
@NullMarked
class SplitCompatInitializer {
    private static volatile boolean sIsInitialized;

    public static void initApplication() {
        ThreadUtils.assertOnUiThread();

        if (sIsInitialized) {
            return;
        }

        sIsInitialized = true;
    }

    public static boolean isInitialized() {
        return sIsInitialized;
    }
}
