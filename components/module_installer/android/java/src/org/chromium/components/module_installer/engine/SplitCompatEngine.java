// Copyright 2019 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.components.module_installer.engine;

import static org.chromium.build.NullUtil.assumeNonNull;

import android.app.Activity;

import androidx.annotation.VisibleForTesting;

import org.chromium.base.ThreadUtils;
import org.chromium.build.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Install engine that uses Play Core and SplitCompat to install modules. */
@NullMarked
class SplitCompatEngine implements InstallEngine {
    private final SplitCompatEngineFacade mFacade;
    private static final Map<String, List<InstallListener>> sSessions = new HashMap<>();

    public SplitCompatEngine() {
        this(new SplitCompatEngineFacade());
    }

    public SplitCompatEngine(SplitCompatEngineFacade facade) {
        mFacade = facade;
    }

    @Override
    public void initActivity(Activity activity) {
        mFacade.installActivity(activity);
    }

    @Override
    public boolean isInstalled(String moduleName) {
       return false;
    }

    @Override
    public void installDeferred(String moduleName) {
    }

    @Override
    public void install(String moduleName, InstallListener listener) {
        ThreadUtils.assertOnUiThread();
    }

    private void notifyListeners(String moduleName, boolean success) {
        unregisterUpdateListener();
    }

    protected void notifyListener(InstallListener listener, Boolean success) {
        if (success) {
            mFacade.notifyObservers();
        }

        listener.onComplete(success);
    }

    private void registerUpdateListener() {
    }

    private void unregisterUpdateListener() {
    }

    @VisibleForTesting
    public void resetSessionQueue() {
        sSessions.clear();
    }
}
