// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.sharing.shared_intent;

import org.chromium.base.Log;

import android.content.res.Resources;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.TextView;
import android.net.Uri;

import org.chromium.base.ContextUtils;
import org.chromium.base.IntentUtils;
import org.chromium.base.ThreadUtils;
import org.chromium.base.task.PostTask;
import org.chromium.base.task.TaskTraits;
import org.chromium.chrome.R;
import org.chromium.chrome.browser.AlwaysIncognitoLinkInterceptor;
import org.chromium.chrome.browser.flags.ChromeFeatureList;
import org.chromium.chrome.browser.init.AsyncInitializationActivity;
import org.chromium.chrome.browser.LaunchIntentDispatcher;
import org.chromium.chrome.browser.IntentHandler;
import org.chromium.ui.widget.ButtonCompat;

/**
 * Activity to display device targets to share text.
 */
public class SharedIntentShareActivity
        extends AsyncInitializationActivity {

    /**
     * Checks whether sending shared clipboard message is enabled for the user and enables/disables
     * the SharedIntentShareActivity appropriately. This call requires native to be loaded.
     */
    public static void updateComponentEnabledState() {
        boolean enabled = ChromeFeatureList.isEnabled(ChromeFeatureList.SHARED_INTENT_UI);
        PostTask.postTask(TaskTraits.USER_VISIBLE, () -> setComponentEnabled(enabled));
    }

    /**
     * Sets whether or not the SharedIntentShareActivity should be enabled. This may trigger a
     * StrictMode violation so shouldn't be called on the UI thread.
     */
    private static void setComponentEnabled(boolean enabled) {
        ThreadUtils.assertOnBackgroundThread();
        Context context = ContextUtils.getApplicationContext();
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName =
                new ComponentName(context, SharedIntentShareActivity.class);

        int newState = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                               : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        // This indicates that we don't want to kill Chrome when changing component enabled state.
        int flags = PackageManager.DONT_KILL_APP;

        if (packageManager.getComponentEnabledSetting(componentName) != newState) {
            packageManager.setComponentEnabledSetting(componentName, newState, flags);
        }
    }

    @Override
    protected void triggerLayoutInflation() {
        setContentView(R.layout.sharing_intent_content);

        String linkUrl = IntentUtils.safeGetStringExtra(getIntent(), Intent.EXTRA_TEXT);
        Resources resources = ContextUtils.getApplicationContext().getResources();
        TextView share_message_text = findViewById(R.id.share_message_text);
        share_message_text.setText(
            resources.getString(R.string.shared_intent_share_activity_text, linkUrl));

        View mask = findViewById(R.id.mask);
        mask.setOnClickListener(v -> finish());

        ButtonCompat open_url_button = findViewById(R.id.open_url_button);
        open_url_button.setVisibility(View.VISIBLE);
        open_url_button.setOnClickListener(view -> {
            Context applicationContext = ContextUtils.getApplicationContext();
            Intent chromeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl));
            chromeIntent.setPackage(applicationContext.getPackageName());
            chromeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            LaunchIntentDispatcher.dispatch(this, chromeIntent);
            finish();
        });

        ButtonCompat open_url_incognito_button = findViewById(R.id.open_url_incognito_button);
        open_url_incognito_button.setVisibility(View.VISIBLE);
        open_url_incognito_button.setOnClickListener(view -> {
            Context applicationContext = ContextUtils.getApplicationContext();
            Intent chromeIntent = IntentHandler.createTrustedOpenNewTabIntent(applicationContext,
                /*incognito*/true);

            chromeIntent.setData(Uri.parse(linkUrl));
            chromeIntent.setPackage(applicationContext.getPackageName());
            chromeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            LaunchIntentDispatcher.dispatch(this, chromeIntent);
            finish();
        });

       if (AlwaysIncognitoLinkInterceptor.isAlwaysIncognito())
            open_url_incognito_button.setVisibility(View.GONE);

        onInitialLayoutInflationComplete();
    }

    @Override
    public boolean shouldStartGpuProcess() {
        return false;
    }
}
