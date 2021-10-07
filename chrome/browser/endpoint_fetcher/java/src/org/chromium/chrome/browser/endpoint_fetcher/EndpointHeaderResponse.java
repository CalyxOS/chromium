// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.endpoint_fetcher;

import org.chromium.base.annotations.CalledByNative;

public class EndpointHeaderResponse {
    private final String mResponseString;
    private final String mRedirectUrl;

    public EndpointHeaderResponse(String responseString, String redirectUrl) {
        mResponseString = responseString;
        mRedirectUrl = redirectUrl;
    }

    public String getResponseString() {
        return mResponseString;
    }

    public String getRedirectUrl() {
        return mRedirectUrl;
    }

    @CalledByNative
    private static EndpointHeaderResponse createEndpointResponse(
            String response, String redirectUrl) {
        return new EndpointHeaderResponse(response, redirectUrl);
    }
}
