// Copyright 2022 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.components.autofill_assistant.guided_browsing.qr_code.camera_scan;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.SparseArray;

import org.chromium.base.task.AsyncTask;
import org.chromium.base.task.PostTask;
import org.chromium.components.autofill_assistant.guided_browsing.qr_code.AssistantQrCodeDelegate;
import org.chromium.content_public.browser.UiThreadTaskTraits;

import java.nio.ByteBuffer;

/**
 * AssistantQrCodeCameraCallbacks provides the callbacks needed for camera preview.
 */
public class AssistantQrCodeCameraCallbacks
        implements Camera.PreviewCallback, Camera.ErrorCallback {
    private final Context mContext;
    private final AssistantQrCodeCameraScanModel mCameraScanModel;
    private final AssistantQrCodeCameraScanCoordinator.DialogCallbacks mDialogCallbacks;

    /**
     * The AssistantQrCodeCameraCallbacks constructor.
     */
    AssistantQrCodeCameraCallbacks(Context context, AssistantQrCodeCameraScanModel cameraScanModel,
            AssistantQrCodeCameraScanCoordinator.DialogCallbacks dialogCallbacks) {
        mContext = context;
        mCameraScanModel = cameraScanModel;
        mDialogCallbacks = dialogCallbacks;
    }

    /**
     * Callback on successful camera preview. Inspects the frame for any QR Code. In case of no
     * QR code detection, the camera preview continues. In case of successful QR Code detection,
     * sends the output value using the |AssistantQrCodeDelegate| and dismisses the QR Code Camera
     * Scan dialog UI.
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
    }

    /**
     * Callback on camera error. Sends back the error using the |AssistantQrCodeDelegate| and
     * dismisses the QR Code Camera Scan dialog UI.
     */
    @Override
    public void onError(int error, Camera camera) {
    }
}
