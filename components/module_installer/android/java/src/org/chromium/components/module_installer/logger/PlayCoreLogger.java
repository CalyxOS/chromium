// Copyright 2019 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.components.module_installer.logger;

/** Concrete Logger for SplitCompat Installers (proxy to specific loggers). */
public class PlayCoreLogger implements Logger {
    public PlayCoreLogger() {
        this(
                null,
                null,
                null);
    }

    public PlayCoreLogger(
            Object failureLogger,
            SplitInstallStatusLogger statusLogger,
            SplitAvailabilityLogger availabilityLogger) {
    }

    @Override
    public void logRequestFailure(String moduleName, int errorCode) {
    }

    @Override
    public void logStatusFailure(String moduleName, int errorCode) {
    }

    @Override
    public void logStatus(String moduleName, int status) {
    }

    @Override
    public void logRequestStart(String moduleName) {
    }

    @Override
    public void logRequestDeferredStart(String moduleName) {
    }
}
