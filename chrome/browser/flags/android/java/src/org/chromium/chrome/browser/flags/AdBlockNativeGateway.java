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

    public static void setAdBlockEnabled(boolean enabled) {
        AdBlockNativeGatewayJni.get().setAdBlockEnabled(enabled);
    }

    public static boolean getAdBlockEnabled() {
        return AdBlockNativeGatewayJni.get().getAdBlockEnabled();
    }

    @NativeMethods
    interface Natives {
        void setAdBlockFiltersURL(String url);
        String getAdBlockFiltersURL();
        void setAdBlockEnabled(boolean enabled);
        boolean getAdBlockEnabled();
    }
}
