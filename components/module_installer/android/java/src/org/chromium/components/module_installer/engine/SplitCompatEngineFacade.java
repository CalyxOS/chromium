// Copyright 2019 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.components.module_installer.engine;

import android.app.Activity;

/*import com.google.android.play.core.splitcompat.SplitCompat;
import com.google.android.play.core.splitinstall.SplitInstallManager;
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory;
import com.google.android.play.core.splitinstall.SplitInstallRequest;*/

import org.chromium.base.ContextUtils;
import org.chromium.components.module_installer.logger.Logger;
import org.chromium.components.module_installer.logger.PlayCoreLogger;
//import org.chromium.components.module_installer.util.ModuleUtil;

/**
 * PlayCore SplitCompatEngine Context. Class used to segregate external dependencies that
 * cannot be easily mocked and simplify the engine's design.
 */
class SplitCompatEngineFacade {
    private final Logger mLogger;

    public SplitCompatEngineFacade() {
        this(new PlayCoreLogger());
    }

    public SplitCompatEngineFacade(Logger umaLogger) {
        mLogger = umaLogger;
    }

    public Logger getLogger() {
        return mLogger;
    }

    public void installActivity(Activity activity) {
    }

    public void notifyObservers() {
        //ModuleUtil.notifyModuleInstalled();
    }

    public void updateCrashKeys() {
        //ModuleUtil.updateCrashKeys();
    }
}
