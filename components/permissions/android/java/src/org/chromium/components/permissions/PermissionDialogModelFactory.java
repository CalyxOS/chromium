// Copyright 2018 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.components.permissions;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import org.chromium.ui.UiUtils;
import org.chromium.ui.modaldialog.ModalDialogProperties;
import org.chromium.ui.modelutil.PropertyModel;

import java.util.Arrays;
import java.util.List;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import org.chromium.base.ApiCompatibilityUtils;
import org.chromium.ui.base.ViewUtils;
import org.chromium.components.content_settings.ContentSettingsType;
import org.chromium.components.content_settings.LifetimeMode;

/** This class creates the model for the permission dialog. */
class PermissionDialogModelFactory {
    public static PropertyModel getModel(
            ModalDialogProperties.Controller controller,
            PermissionDialogDelegate delegate,
            View customView,
            Runnable touchFilteredCallback) {
        Context context = delegate.getWindow().getContext().get();
        assert context != null;

        String messageText = delegate.getMessageText();
        assert !TextUtils.isEmpty(messageText);

        PropertyModel.Builder builder =
                new PropertyModel.Builder(ModalDialogProperties.ALL_KEYS)
                        .with(ModalDialogProperties.CONTROLLER, controller)
                        .with(ModalDialogProperties.FOCUS_DIALOG, true)
                        .with(ModalDialogProperties.CUSTOM_VIEW, customView)
                        .with(ModalDialogProperties.CONTENT_DESCRIPTION, messageText)
                        .with(ModalDialogProperties.FILTER_TOUCH_FOR_SECURITY, true)
                        .with(ModalDialogProperties.TOUCH_FILTERED_CALLBACK, touchFilteredCallback)
                        .with(
                                ModalDialogProperties.BUTTON_TAP_PROTECTION_PERIOD_MS,
                                UiUtils.PROMPT_INPUT_PROTECTION_SHORT_DELAY_MS)
                        .with(
                                ModalDialogProperties.CANCEL_ON_TOUCH_OUTSIDE,
                                PermissionsAndroidFeatureMap.isEnabled(
                                        PermissionsAndroidFeatureList
                                                .ANDROID_CANCEL_PERMISSION_PROMPT_ON_TOUCH_OUTSIDE));
        if (delegate.canShowEphemeralOption()) {
            var positiveEphemeralButtonSpec =
                    new ModalDialogProperties.ModalDialogButtonSpec(
                            ModalDialogProperties.ButtonType.POSITIVE_EPHEMERAL,
                            delegate.getPositiveEphemeralButtonText());
            var positiveButtonSpec =
                    new ModalDialogProperties.ModalDialogButtonSpec(
                            ModalDialogProperties.ButtonType.POSITIVE,
                            delegate.getPositiveButtonText());
            var negativeButtonSpec =
                    new ModalDialogProperties.ModalDialogButtonSpec(
                            ModalDialogProperties.ButtonType.NEGATIVE,
                            delegate.getNegativeButtonText());
            var buttonSpecs =
                    delegate.shouldShowPositiveNonEphemeralAsFirstButton()
                            ? new ModalDialogProperties.ModalDialogButtonSpec[] {
                                positiveButtonSpec, positiveEphemeralButtonSpec, negativeButtonSpec
                            }
                            : new ModalDialogProperties.ModalDialogButtonSpec[] {
                                positiveEphemeralButtonSpec, positiveButtonSpec, negativeButtonSpec
                            };
            builder.with(ModalDialogProperties.WRAP_CUSTOM_VIEW_IN_SCROLLABLE, true)
                    .with(ModalDialogProperties.BUTTON_GROUP_BUTTON_SPEC_LIST, buttonSpecs);
        } else {
            builder.with(
                            ModalDialogProperties.POSITIVE_BUTTON_TEXT,
                            delegate.getPositiveButtonText())
                    .with(
                            ModalDialogProperties.NEGATIVE_BUTTON_TEXT,
                            delegate.getNegativeButtonText());
        }

        PropertyModel pm = builder.build();
        int[] types = delegate.getContentSettingsTypes();
        if (contains(types, ContentSettingsType.GEOLOCATION) ||
            contains(types, ContentSettingsType.MEDIASTREAM_MIC) ||
            contains(types, ContentSettingsType.MEDIASTREAM_CAMERA))
        {
            LinearLayout layout = (LinearLayout) customView;

            // Create a text label before the lifetime selector.
            TextView lifetimeOptionsText = new TextView(context);
            lifetimeOptionsText.setText(context.getString(
                        org.chromium.components.permissions.R.string.session_permissions_title));
            ApiCompatibilityUtils.setTextAppearance(
                    lifetimeOptionsText, R.style.TextAppearance_TextMedium_Primary);

            LinearLayout.LayoutParams lifetimeOptionsTextLayoutParams =
                    new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lifetimeOptionsTextLayoutParams.setMargins(0, 0, 0, ViewUtils.dpToPx(context, 8));
            lifetimeOptionsText.setLayoutParams(lifetimeOptionsTextLayoutParams);
            layout.addView(lifetimeOptionsText);

            // Create radio buttons with lifetime options.
            RadioGroup radioGroup = new RadioGroup(context);

            RadioButton radioButton = new RadioButton(context);
            radioButton.setText(context.getString(
                        org.chromium.components.permissions.R.string.session_permissions_only_this_this));
            radioButton.setId(LifetimeMode.ONLY_THIS_TIME);
            radioGroup.addView(radioButton);

            radioButton = new RadioButton(context);
            radioButton.setText(context.getString(
                        org.chromium.components.permissions.R.string.session_permissions_until_page_close));
            radioButton.setId(LifetimeMode.UNTIL_ORIGIN_CLOSED);
            radioGroup.addView(radioButton);

            radioButton = new RadioButton(context);
            radioButton.setText(context.getString(
                        org.chromium.components.permissions.R.string.session_permissions_until_browser_close));
            radioButton.setId(LifetimeMode.UNTIL_BROWSER_CLOSED);
            radioGroup.addView(radioButton);

            radioButton = new RadioButton(context);
            radioButton.setText(context.getString(
                        org.chromium.components.permissions.R.string.session_permissions_forever));
            radioButton.setId(LifetimeMode.ALWAYS);
            radioGroup.addView(radioButton);

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    delegate.setSelectedLifetimeOption(checkedId);
                }
            });
            radioGroup.check(1);
            layout.addView(radioGroup);
        }

        return pm;
    }

    private static boolean contains(final int[] array, final int key) {
        int length = array.length;
        for(int i = 0; i < length; i++) {
            if (array[i] == key)
                return true;
        }
        return false;
    }
}
