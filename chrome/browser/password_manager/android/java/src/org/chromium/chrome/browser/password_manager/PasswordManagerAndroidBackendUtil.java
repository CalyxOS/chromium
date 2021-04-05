// Copyright 2022 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.
package org.chromium.chrome.browser.password_manager;

import static org.chromium.chrome.browser.password_manager.PasswordManagerHelper.usesUnifiedPasswordManagerUI;

import android.app.PendingIntent;

import androidx.annotation.Nullable;

import org.chromium.base.Log;
import org.chromium.chrome.browser.password_manager.CredentialManagerLauncher.CredentialManagerError;

/**
 * Collection of utilities used by classes interacting with the password manager backend
 * in Google Mobile Services.
 */
class PasswordManagerAndroidBackendUtil {
    private static final String TAG = "PwdManagerBackend";

    private PasswordManagerAndroidBackendUtil() {}

    static @AndroidBackendErrorType int getBackendError(Exception exception) {
        if (exception instanceof PasswordStoreAndroidBackend.BackendException) {
            return ((PasswordStoreAndroidBackend.BackendException) exception).errorCode;
        }
        return AndroidBackendErrorType.UNCATEGORIZED;
    }

    static @CredentialManagerError int getPasswordCheckupBackendError(Exception exception) {
        if (exception instanceof PasswordCheckupClientHelper.PasswordCheckBackendException) {
            return ((PasswordCheckupClientHelper.PasswordCheckBackendException) exception)
                    .errorCode;
        }
        return CredentialManagerError.UNCATEGORIZED;
    }

    static int getApiErrorCode(Exception exception) {
        return 13; // '13' means ERROR
    }
}
