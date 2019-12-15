// Copyright 2019 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.components.module_installer.engine;

import android.app.Activity;

import org.chromium.base.ContextUtils;
import org.chromium.build.annotations.NullMarked;

/**
 * PlayCore SplitCompatEngine Context. Class used to segregate external dependencies that cannot be
 * easily mocked and simplify the engine's design.
 */
@NullMarked
class SplitCompatEngineFacade {
    public SplitCompatEngineFacade() {
    }


    public void installActivity(Activity activity) {
    }

    public void notifyObservers() {
    }

    public void updateCrashKeys() {
    }
}
