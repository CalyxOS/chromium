package org.chromium.chrome.browser.flags;

import org.chromium.base.annotations.CalledByNative;
import org.chromium.base.annotations.NativeMethods;

public class AdBlockNativeGateway {
    public static void setAdBlockFiltersURL(String url) {
        AdBlockNativeGatewayJni.get().setAdBlockFiltersURL(url);
    }

    public static String getAdBlockFiltersURL() {
        return AdBlockNativeGatewayJni.get().getAdBlockFiltersURL();
    }

    @NativeMethods
    interface Natives {
        void setAdBlockFiltersURL(String url);
        String getAdBlockFiltersURL();
    }
}
