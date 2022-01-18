// Copyright 2022 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.accessibility.settings;

import androidx.preference.PreferenceFragmentCompat;

import org.chromium.base.metrics.RecordHistogram;
import org.chromium.chrome.R;
import org.chromium.chrome.browser.image_descriptions.ImageDescriptionsController;
import org.chromium.chrome.browser.preferences.ChromePreferenceKeys;
import org.chromium.chrome.browser.preferences.Pref;
import org.chromium.chrome.browser.preferences.SharedPreferencesManager;
import org.chromium.chrome.browser.profiles.Profile;
import org.chromium.chrome.browser.util.ChromeAccessibilityUtil;
import org.chromium.components.browser_ui.accessibility.AccessibilitySettingsDelegate;
import org.chromium.components.browser_ui.accessibility.PageZoomUtils;
import org.chromium.components.user_prefs.UserPrefs;
import org.chromium.content_public.browser.BrowserContextHandle;

import android.app.Activity;
import org.chromium.chrome.browser.flags.ChromeFeatureList;
import org.chromium.chrome.browser.flags.CachedFeatureFlags;
import org.chromium.chrome.browser.ui.messages.snackbar.SnackbarManager;
import org.chromium.chrome.browser.ui.messages.snackbar.INeedSnackbarManager;
import org.chromium.chrome.browser.ui.messages.snackbar.Snackbar;
import org.chromium.chrome.browser.ApplicationLifetime;

/** The Chrome implementation of AccessibilitySettingsDelegate. */
public class ChromeAccessibilitySettingsDelegate implements AccessibilitySettingsDelegate {
    private static final String READER_MODE_SELECTED_HISTOGRAM =
            "DomDistiller.ReaderModeAccessibilitySettingSelected";

    private static class AccessibilityTabSwitcherDelegate implements BooleanPreferenceDelegate {
        @Override
        public boolean isEnabled() {
            return SharedPreferencesManager.getInstance().readBoolean(
                    ChromePreferenceKeys.ACCESSIBILITY_TAB_SWITCHER, true);
        }

        @Override
        public void setEnabled(boolean value) {}
    }

    private SnackbarManager mSnackbarManager;

    public void setSnackbarManager(SnackbarManager snackbarManager) {
        mSnackbarManager = snackbarManager;
    }

    private static class ReaderForAccessibilityDelegate implements BooleanPreferenceDelegate {
        @Override
        public boolean isEnabled() {
            return UserPrefs.get(Profile.getLastUsedRegularProfile())
                    .getBoolean(Pref.READER_FOR_ACCESSIBILITY);
        }

        @Override
        public void setEnabled(boolean value) {
            RecordHistogram.recordBooleanHistogram(READER_MODE_SELECTED_HISTOGRAM, (Boolean) value);
            UserPrefs.get(Profile.getLastUsedRegularProfile())
                    .setBoolean(Pref.READER_FOR_ACCESSIBILITY, (Boolean) value);
        }
    }

    @Override
    public BrowserContextHandle getBrowserContextHandle() {
        return Profile.getLastUsedRegularProfile();
    }

    @Override
    public BooleanPreferenceDelegate getAccessibilityTabSwitcherDelegate() {
        if (!ChromeAccessibilityUtil.get().isAccessibilityEnabled()) {
            return null;
        }
        return new AccessibilityTabSwitcherDelegate();
    }

    @Override
    public BooleanPreferenceDelegate getReaderForAccessibilityDelegate() {
        return new ReaderForAccessibilityDelegate();
    }

    private static class ForceTabletUIDelegate implements BooleanPreferenceDelegate {
        @Override
        public boolean isEnabled() {
            return SharedPreferencesManager.getInstance().readBoolean(
                      ChromePreferenceKeys.FLAGS_FORCE_TABLET_UI_ENABLED, false);
        }

        @Override
        public void setEnabled(boolean value) {
            SharedPreferencesManager.getInstance().writeBoolean(
                      ChromePreferenceKeys.FLAGS_FORCE_TABLET_UI_ENABLED, value);
        }
    }

    @Override
    public BooleanPreferenceDelegate getForceTabletUIDelegate() {
        return new ForceTabletUIDelegate();
    }

    private static class MoveTopToolbarToBottomDelegate implements BooleanPreferenceDelegate {
        @Override
        public boolean isEnabled() {
            return CachedFeatureFlags.isEnabled(ChromeFeatureList.MOVE_TOP_TOOLBAR_TO_BOTTOM);
        }

        @Override
        public void setEnabled(boolean value) {
            CachedFeatureFlags.setFlagEnabled(ChromeFeatureList.MOVE_TOP_TOOLBAR_TO_BOTTOM,
                    "move-top-toolbar-to-bottom", value);
        }
    }

    @Override
    public BooleanPreferenceDelegate getMoveTopToolbarToBottomDelegate() {
        return new MoveTopToolbarToBottomDelegate();
    }

    @Override
    public void requestRestart(Activity activity) {
        Snackbar mSnackbar = Snackbar.make(activity.getString(R.string.ui_relaunch_notice),
                new SnackbarManager.SnackbarController() {
                        @Override
                        public void onDismissNoAction(Object actionData) { }

                        @Override
                        public void onAction(Object actionData) {
                                ApplicationLifetime.terminate(true);
                        }
                }, Snackbar.TYPE_NOTIFICATION, Snackbar.UMA_UNKNOWN)
                .setSingleLine(false)
                .setAction(activity.getString(R.string.relaunch),
                        /*actionData*/null)
                .setDuration(/*durationMs*/70000);
        if (!mSnackbarManager.isShowing())
            mSnackbarManager.showSnackbar(mSnackbar);
    }

    @Override
    public void addExtraPreferences(PreferenceFragmentCompat fragment) {
        if (ImageDescriptionsController.getInstance().shouldShowImageDescriptionsMenuItem()) {
            fragment.addPreferencesFromResource(R.xml.image_descriptions_settings_preference);
        }
    }

    @Override
    public boolean showPageZoomSettingsUI() {
        return PageZoomUtils.shouldShowSettingsUI();
    }
}
