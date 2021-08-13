/*
    This file is part of Bromite.

    Bromite is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Bromite is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Bromite. If not, see <https://www.gnu.org/licenses/>.
*/

package org.chromium.components.user_scripts;

import java.util.ArrayList;
import java.util.List;
import java.lang.ref.WeakReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import android.app.AlertDialog;
import android.content.DialogInterface;

import org.chromium.base.annotations.CalledByNative;
import org.chromium.base.annotations.JNINamespace;
import org.chromium.base.annotations.NativeMethods;
import org.chromium.base.ContentUriUtils;
import org.chromium.base.Log;
import org.chromium.ui.base.WindowAndroid;

import org.chromium.components.user_scripts.ScriptListPreference;
import org.chromium.components.user_scripts.IUserScriptsUtils;

@JNINamespace("user_scripts")
public class UserScriptsBridge {
    private static final String TAG = "UserScript";

    static WeakReference<ScriptListPreference> observer;

    private static IUserScriptsUtils utilInstance;

    public static void registerUtils(IUserScriptsUtils instance) {
        utilInstance = instance;
    }

    public static IUserScriptsUtils getUtils() {
        return utilInstance;
    }

    public static boolean isEnabled() {
        return UserScriptsBridgeJni.get().isEnabled();
    }

    public static void setEnabled(boolean enabled) {
        UserScriptsBridgeJni.get().setEnabled(enabled);
    }

    public static void RemoveScript(String key) {
        UserScriptsBridgeJni.get().removeScript(key);
    }

    public static void SetScriptEnabled(String key,
                                        boolean enabled) {
        UserScriptsBridgeJni.get().setScriptEnabled(key, enabled);
    }

    public static void Reload() {
        UserScriptsBridgeJni.get().reload();
    }

    public static void SelectAndAddScriptFromFile(WindowAndroid window) {
        Context context = window.getContext().get();

        Intent fileSelector = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        fileSelector.addCategory(Intent.CATEGORY_OPENABLE);
        fileSelector.setType("*/*");
        fileSelector.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        window.showIntent(fileSelector,
            new WindowAndroid.IntentCallback() {
                @Override
                public void onIntentCompleted(int resultCode, Intent data) {
                    if (data == null) return;
                    Uri filePath = data.getData();
                    TryToInstall(context, filePath.toString());
                }
            },
            null);
    }

    public static void TryToInstall(Context context, String ScriptFullPath) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        UserScriptsBridgeJni.get().tryToInstall(ScriptFullPath);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        String scriptName = ScriptFullPath;
        if (ContentUriUtils.isContentUri(scriptName)) {
            scriptName = ContentUriUtils.getFilePathFromContentUri(Uri.parse(scriptName));
            if (scriptName == null) {
                // fallback to content uri name if fail
                scriptName = ContentUriUtils.getDisplayName(Uri.parse(ScriptFullPath), context,
                    MediaStore.MediaColumns.DISPLAY_NAME);
            }
        }

        String message = context.getString(R.string.ask_to_install, scriptName);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
            .setPositiveButton(context.getString(R.string.yes), dialogClickListener)
            .setNegativeButton(context.getString(R.string.no), dialogClickListener)
            .show();
    }

    public static List<ScriptInfo> getUserScriptItems() {
        List<ScriptInfo> list = new ArrayList<>();
        try {
            String json = UserScriptsBridgeJni.get().getScriptsInfo();

            JSONObject jsonObject = new JSONObject(json);

            JSONArray scripts = jsonObject.names();
            if (scripts != null) {
                Log.i(TAG, "User Scripts Loaded: %s", json);
                Log.i(TAG, "Totals scripts: %s", Integer.toString(scripts.length()));
                for (int i = 0; i < scripts.length(); i++) {
                    String key = (String) scripts.get(i);
                    JSONObject script = jsonObject.getJSONObject(key);

                    ScriptInfo si = new ScriptInfo();
                    si.Key = key;
                    list.add(si);

                    if(script.has("name")) si.Name = script.getString("name");
                    if(script.has("description")) si.Description = script.getString("description");
                    if(script.has("version")) si.Version = script.getString("version");
                    if(script.has("file_path")) si.FilePath = script.getString("file_path");
                    if(script.has("url_source")) si.UrlSource = script.getString("url_source");
                    if(script.has("parser_error")) si.ParserError = script.getString("parser_error");
                    if(script.has("force_disabled")) si.ForceDisabled = script.getBoolean("force_disabled");;
                    si.Enabled = script.getBoolean("enabled");
                }
            } else {
                Log.i(TAG, "User Scripts list empty");
            }
        } catch (Exception e) {
            Log.e(TAG, "User Scripts Load Error", e.toString());
        }
        return list;
    }

    public static void RegisterLoadCallback(ScriptListPreference caller) {
        UserScriptsBridgeJni.get().registerLoadCallback();
        observer = new WeakReference<ScriptListPreference>(caller);
    }

    @CalledByNative
    private static void shouldRefreshUserScriptList() {
        ScriptListPreference reference = observer.get();
        if (reference != null) {
            reference.NotifyScriptsChanged();
        }
    }

    @CalledByNative
    private static void onUserScriptLoaded(boolean result, String error) {
        ScriptListPreference reference = observer.get();
        if (reference != null) {
            reference.OnUserScriptLoaded(result, error);
        }
    }

    @NativeMethods
    interface Natives {
        boolean isEnabled();
        void setEnabled(boolean enabled);

        String getScriptsInfo();

        void removeScript(String scriptKey);
        void setScriptEnabled(String scriptKey, boolean enabled);

        void reload();
        void selectAndAddScriptFromFile(WindowAndroid window);
        void tryToInstall(String scriptFullPath);

        void registerLoadCallback();
    }

}
