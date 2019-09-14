package org.chromium.chrome.browser.flags;

import org.jni_zero.CalledByNative;
import org.jni_zero.NativeMethods;

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
