// Copyright 2019 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.components.module_installer.logger;

/**
 * Concrete Logger for SplitCompat Installers (proxy to specific loggers).
 */
public class PlayCoreLogger implements Logger {
    private final SplitInstallStatusLogger mStatusLogger;
    private final SplitAvailabilityLogger mAvailabilityLogger;

    public PlayCoreLogger() {
        this(new SplitInstallStatusLogger(),
                new SplitAvailabilityLogger());
    }

    public PlayCoreLogger(
            SplitInstallStatusLogger statusLogger, SplitAvailabilityLogger availabilityLogger) {
        mStatusLogger = statusLogger;
        mAvailabilityLogger = availabilityLogger;
    }

    @Override
    public void logRequestFailure(String moduleName, int errorCode) {
    }

    @Override
    public void logStatusFailure(String moduleName, int errorCode) {
    }

    @Override
    public void logStatus(String moduleName, int status) {
        mStatusLogger.logStatusChange(moduleName, status);
    }

    @Override
    public void logRequestStart(String moduleName) {
        mStatusLogger.logRequestStart(moduleName);
        mAvailabilityLogger.storeRequestStart(moduleName);
    }

    @Override
    public void logRequestDeferredStart(String moduleName) {
        mStatusLogger.logRequestDeferredStart(moduleName);
        mAvailabilityLogger.storeRequestDeferredStart(moduleName);
    }
}
