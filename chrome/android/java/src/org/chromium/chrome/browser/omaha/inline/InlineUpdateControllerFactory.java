// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.omaha.inline;

import org.chromium.base.ContextUtils;
import org.chromium.chrome.browser.flags.ChromeFeatureList;
import org.chromium.chrome.browser.omaha.UpdateConfigs;

/**
 * A factory that creates an {@link InlineUpdateController} instance.
 */
public class InlineUpdateControllerFactory {
    /**
     * @return A new {@link InlineUpdateController}.
     */
    public static InlineUpdateController create(Runnable callback) {
        return new BromiteInlineUpdateController(callback);
    }
}
