// Copyright 2022 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.components.autofill_assistant.guided_browsing.qr_code.image_picker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.SparseArray;

import org.chromium.base.task.AsyncTask;
import org.chromium.base.task.PostTask;
import org.chromium.components.autofill_assistant.guided_browsing.qr_code.AssistantQrCodeDelegate;
import org.chromium.content_public.browser.UiThreadTaskTraits;
import org.chromium.ui.base.WindowAndroid.IntentCallback;

import java.io.IOException;

/**
 * AssistantQrCodeImagePickerCallbacks provides the callbacks needed for QR scanning via image
 * picker.
 */
public class AssistantQrCodeImagePickerCallbacks implements IntentCallback {
    private final Context mContext;
    private final AssistantQrCodeImagePickerModel mImagePickerModel;
    private final AssistantQrCodeImagePickerCoordinator.DialogCallbacks mDialogCallbacks;

    /**
     * The AssistantQrCodeImagePickerCallbacks constructor.
     */
    AssistantQrCodeImagePickerCallbacks(Context context,
            AssistantQrCodeImagePickerModel imagePickerModel,
            AssistantQrCodeImagePickerCoordinator.DialogCallbacks dialogCallbacks) {
        mContext = context;
        mImagePickerModel = imagePickerModel;
        mDialogCallbacks = dialogCallbacks;
    }

    /**
     * Callback when the image picker intent finishes. Inspects the image for any QR Code. In case
     * of successful QR Code detection, sends the output value using the |AssistantQrCodeDelegate|
     * and dismisses the QR Code Image Picker dialog UI.
     */
    @Override
    public void onIntentCompleted(int resultCode, Intent data) {
    }

    /**
     * Sends back the CANCEL response using the |AssistantQrCodeDelegate| and dismisses the QR Code
     * Image Picker dialog UI.
     */
    private void onQrCodeScanCancel() {
    }

    /**
     * Sends back the FAILURE response using the |AssistantQrCodeDelegate| and dismisses the QR
     * Code Image Picker dialog UI.
     */
    private void onQrCodeScanFailure() {
    }
}
