// Copyright 2019 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.components.module_installer.engine;

import android.app.Activity;

import androidx.annotation.VisibleForTesting;

import org.chromium.base.ThreadUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Install engine that uses Play Core and SplitCompat to install modules.
 */
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
        mFacade.getLogger().logRequestDeferredStart(moduleName);
    }

    @Override
    public void install(String moduleName, InstallListener listener) {
        ThreadUtils.assertOnUiThread();
    }

/*    private SplitInstallStateUpdatedListener getStatusUpdateListener() {
        return state -> {
            assert !state.moduleNames().isEmpty();

            int status = state.status();
            List<String> modules = state.moduleNames();

            if (status == SplitInstallSessionStatus.INSTALLED) {
                mFacade.updateCrashKeys();
            }

            for (String moduleName : modules) {
                switch (status) {
                    case SplitInstallSessionStatus.INSTALLED:
                        notifyListeners(moduleName, true);
                        break;
                    case SplitInstallSessionStatus.FAILED:
                        notifyListeners(moduleName, false);
                        mFacade.getLogger().logStatusFailure(moduleName, state.errorCode());
                        break;
                }
                mFacade.getLogger().logStatus(moduleName, status);
            }
        };
    }*/

    private void notifyListeners(String moduleName, Boolean success) {
/*        for (InstallListener listener : sSessions.get(moduleName)) {
            notifyListener(listener, success);
        }*/

        sSessions.remove(moduleName);

        unregisterUpdateListener();
    }

    protected void notifyListener(InstallListener listener, Boolean success) {
        if (success) {
            mFacade.notifyObservers();
        }

        listener.onComplete(success);
    }

    private void registerUpdateListener() {
/*        if (sSessions.size() == 0) {
            mFacade.getSplitManager().registerListener(mUpdateListener);
        }*/
    }

    private void unregisterUpdateListener() {
/*        if (sSessions.size() == 0) {
            mFacade.getSplitManager().unregisterListener(mUpdateListener);
        }*/
    }

    @VisibleForTesting
    public void resetSessionQueue() {
        sSessions.clear();
    }
}
