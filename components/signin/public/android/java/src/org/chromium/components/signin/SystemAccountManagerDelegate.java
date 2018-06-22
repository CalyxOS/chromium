// Copyright 2012 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.components.signin;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AuthenticatorDescription;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.os.Process;
import android.os.SystemClock;

import androidx.annotation.Nullable;

import org.chromium.base.ApiCompatibilityUtils;
import org.chromium.base.Callback;
import org.chromium.base.ContextUtils;
import org.chromium.base.Log;
import org.chromium.base.ThreadUtils;
import org.chromium.base.metrics.RecordHistogram;
import org.chromium.components.externalauth.ExternalAuthUtils;
import org.chromium.components.signin.metrics.FetchAccountCapabilitiesFromSystemLibraryResult;

import java.io.IOException;

/**
 * Default implementation of {@link AccountManagerDelegate} which delegates all calls to the
 * Android account manager.
 */
public class SystemAccountManagerDelegate implements AccountManagerDelegate {
    private final AccountManager mAccountManager;
    private AccountsChangeObserver mObserver;

    private static final String TAG = "Auth";

    public SystemAccountManagerDelegate() {
        Context context = ContextUtils.getApplicationContext();
        mAccountManager = AccountManager.get(context);
        mObserver = null;
    }

    @Override
    public void attachAccountsChangeObserver(AccountsChangeObserver observer) {
        assert mObserver == null : "Another AccountsChangeObserver is already attached!";

        mObserver = observer;
        Context context = ContextUtils.getApplicationContext();
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                mObserver.onAccountsChanged();
            }
        };
        IntentFilter accountsChangedIntentFilter = new IntentFilter();
        accountsChangedIntentFilter.addAction(AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION);
        context.registerReceiver(receiver, accountsChangedIntentFilter);

        IntentFilter gmsPackageReplacedFilter = new IntentFilter();
        gmsPackageReplacedFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        gmsPackageReplacedFilter.addDataScheme("package");
        gmsPackageReplacedFilter.addDataPath(
                "com.google.android.gms", PatternMatcher.PATTERN_PREFIX);

        context.registerReceiver(receiver, gmsPackageReplacedFilter);
    }

    @Override
    public Account[] getAccounts() {
        // Account seeding relies on GoogleAuthUtil.getAccountId to get GAIA ids,
        // so don't report any accounts if Google Play Services are out of date.
        return new Account[] {};
    }

    @Override
    public AccessTokenData getAuthToken(Account account, String authTokenScope)
            throws AuthException {
        ThreadUtils.assertOnBackgroundThread();
        assert AccountUtils.GOOGLE_ACCOUNT_TYPE.equals(account.type);
        throw new AuthException(AuthException.NONTRANSIENT,
              "Error while getting token for scope '" + authTokenScope + "'");
    }

    @Override
    public void invalidateAuthToken(String authToken) throws AuthException {
    }

    @Override
    public AuthenticatorDescription[] getAuthenticatorTypes() {
        return mAccountManager.getAuthenticatorTypes();
    }

    protected boolean hasFeatures(Account account, String[] features) {
        return false;
    }

    @Override
    public boolean hasFeature(Account account, String feature) {
        return hasFeatures(account, new String[] {feature});
    }

    @Override
    public @CapabilityResponse int hasCapability(Account account, String capability) {
        RecordHistogram.recordEnumeratedHistogram(
                "Signin.AccountCapabilities.GetFromSystemLibraryResult",
                FetchAccountCapabilitiesFromSystemLibraryResult.API_NOT_AVAILABLE,
                FetchAccountCapabilitiesFromSystemLibraryResult.MAX_VALUE + 1);
        return CapabilityResponse.EXCEPTION;
    }

    // No permission is needed on 23+ and Chrome always has MANAGE_ACCOUNTS permission on lower APIs
    @SuppressLint("MissingPermission")
    @Override
    public void createAddAccountIntent(Callback<Intent> callback) {
    }

    // No permission is needed on 23+ and Chrome always has MANAGE_ACCOUNTS permission on lower APIs
    @SuppressLint("MissingPermission")
    @Override
    public void updateCredentials(
            Account account, Activity activity, final Callback<Boolean> callback) {
        ThreadUtils.assertOnUiThread();
            if (callback != null) {
                callback.onResult(false);
            }
    }

    @Nullable
    @Override
    public String getAccountGaiaId(String accountEmail) {
        return null;
    }

    protected boolean isGooglePlayServicesAvailable() {
        return ExternalAuthUtils.getInstance().canUseGooglePlayServices();
    }

    protected boolean hasGetAccountsPermission() {
        return ApiCompatibilityUtils.checkPermission(ContextUtils.getApplicationContext(),
                       Manifest.permission.GET_ACCOUNTS, Process.myPid(), Process.myUid())
                == PackageManager.PERMISSION_GRANTED;
    }
}
