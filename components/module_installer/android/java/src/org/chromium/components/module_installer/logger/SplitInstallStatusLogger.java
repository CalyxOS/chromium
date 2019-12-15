// Copyright 2019 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.components.module_installer.logger;

import org.chromium.base.metrics.RecordHistogram;

class SplitInstallStatusLogger {
    // FeatureModuleInstallingStatus defined in //tools/metrics/histograms/enums.xml.
    // These values are persisted to logs. Entries should not be renumbered and numeric values
    // should never be reused.
    private static final int UNKNOWN_CODE = 0;
    private static final int REQUESTED = 1;
    private static final int PENDING = 2;
    private static final int DOWNLOADING = 3;
    private static final int DOWNLOADED = 4;
    private static final int INSTALLING = 5;
    private static final int INSTALLED = 6;
    private static final int FAILED = 7;
    private static final int CANCELING = 8;
    private static final int CANCELED = 9;
    private static final int REQUIRES_USER_CONFIRMATION = 10;
    private static final int REQUESTED_DEFERRED = 11;

    // Keep this one at the end and increment appropriately when adding new status.
    private static final int COUNT = 12;

    public void logStatusChange(String moduleName, int status) {
    }

    public void logRequestStart(String moduleName) {
        recordInstallStatus(moduleName, REQUESTED);
    }

    public void logRequestDeferredStart(String moduleName) {
        recordInstallStatus(moduleName, REQUESTED_DEFERRED);
    }

    private void recordInstallStatus(String moduleName, int status) {
        String name = "Android.FeatureModules.InstallingStatus." + moduleName;
        RecordHistogram.recordEnumeratedHistogram(name, status, COUNT);
    }
}
