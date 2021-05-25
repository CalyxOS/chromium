/*
    This file is part of Cromite.

    Cromite is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cromite is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cromite. If not, see <https://www.gnu.org/licenses/>.
*/

package org.chromium.chrome.browser.flags.cromite;

import org.chromium.chrome.browser.flags.ChromeFeatureMap;
import org.chromium.components.cached_flags.CachedFlag;

public class sAllowExternalIntentRequests {
    private static final CachedFlag sInstance =
        new CachedFlag(ChromeFeatureMap.getInstance(),
            "AllowExternalIntentRequests", false);

    private sAllowExternalIntentRequests() {}

    public static CachedFlag getInstance() {
        return sInstance;
    }
}
