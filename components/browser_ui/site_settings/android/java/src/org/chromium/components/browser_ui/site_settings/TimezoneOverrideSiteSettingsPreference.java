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

package org.chromium.components.browser_ui.site_settings;

import org.chromium.base.Log;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Button;
import android.widget.TextView;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.graphics.Color;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import androidx.appcompat.app.AlertDialog;

import org.chromium.content_public.browser.BrowserContextHandle;
import org.chromium.components.content_settings.ContentSettingValues;
import org.chromium.components.browser_ui.site_settings.WebsitePreferenceBridge;
import org.chromium.components.browser_ui.widget.RadioButtonWithDescription;
import org.chromium.components.browser_ui.widget.RadioButtonWithEditText;
import org.chromium.components.browser_ui.widget.TintedDrawable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimeZone;

/**
 * TimezoneOverride Preference for SiteSettings.
 */
public class TimezoneOverrideSiteSettingsPreference
        extends Preference implements RadioGroup.OnCheckedChangeListener,
                                      RadioButtonWithEditText.OnTextChangeListener {
    private @ContentSettingValues int mSetting = ContentSettingValues.DEFAULT;
    private RadioButtonWithDescription mAllowed;
    private RadioButtonWithEditText mAsk;
    private RadioButtonWithDescription mBlocked;
    private RadioGroup mRadioGroup;
    private TextView mSelectButton;

    private String currentSelected;

    private BrowserContextHandle mBrowserContextHandle;

    public TimezoneOverrideSiteSettingsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setLayoutResource(R.layout.timezoneoverride_site_settings_preference);
        setSelectable(false);
    }

    public void initialize(@ContentSettingValues int setting, BrowserContextHandle browserContextHandle) {
        mSetting = setting;
        mBrowserContextHandle = browserContextHandle;
    }

    public @ContentSettingValues int getCheckedSetting() {
        return mSetting;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (mAllowed.isChecked()) {
            mSetting = ContentSettingValues.ALLOW;
        } else if (mAsk.isChecked()) {
            mSetting = ContentSettingValues.ASK;
        } else if (mBlocked.isChecked()) {
            mSetting = ContentSettingValues.BLOCK;
        }

        callChangeListener(mSetting);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mAllowed = (RadioButtonWithDescription) holder.findViewById(R.id.allowed);
        mAsk = (RadioButtonWithEditText) holder.findViewById(R.id.ask);
        mBlocked = (RadioButtonWithDescription) holder.findViewById(R.id.blocked);
        mRadioGroup = (RadioGroup) holder.findViewById(R.id.radio_button_layout);
        mRadioGroup.setOnCheckedChangeListener(this);

        if (mBrowserContextHandle != null)
            mAsk.setPrimaryText(WebsitePreferenceBridge.getCustomTimezone(mBrowserContextHandle));
        mAsk.addTextChangeListener(this);

        ListView listView = (ListView)holder.findViewById(R.id.listView);

        mSelectButton = (TextView) holder.findViewById(R.id.select_button);
        mSelectButton.setOnClickListener(view -> {
          showSelectTimeZoneDialog();
        });

        RadioButtonWithDescription radioButton = findRadioButton(mSetting);
        if (radioButton != null) radioButton.setChecked(true);
    }

    private RadioButtonWithDescription findRadioButton(@ContentSettingValues int setting) {
        if (setting == ContentSettingValues.ALLOW) {
            return mAllowed;
        } else if (setting == ContentSettingValues.ASK) {
            return mAsk;
        } else if (setting == ContentSettingValues.BLOCK) {
            return mBlocked;
        } else {
            return null;
        }
    }

    public void onTextChanged(CharSequence newText) {
        WebsitePreferenceBridge.setCustomTimezone(mBrowserContextHandle, newText.toString());
    }

    private void showSelectTimeZoneDialog() {
        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.time_zone_select_dialog, null);

        ListView listView = view.findViewById(R.id.listView);
        ArrayList<String> timezones = new ArrayList<>(Arrays.asList(TimeZone.getAvailableIDs()));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, timezones);
        listView.setAdapter(adapter);

        currentSelected = String.valueOf(mAsk.getPrimaryText());
        listView.post(new Runnable()
        {
          public void run()
          {
            for (int j = 0; j < timezones.size(); j++) {
                if (currentSelected.equals(timezones.get(j))) {
                    listView.requestFocusFromTouch();
                    listView.setSelection(j);
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
          }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                currentSelected = timezones.get(i);
                listView.setSelected(true);
            }
        });

        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int button) {
                if (button == AlertDialog.BUTTON_POSITIVE) {
                    mAsk.setPrimaryText(currentSelected);
                } else {
                    dialog.dismiss();
                }
            }
        };

        AlertDialog.Builder alert =
                new AlertDialog.Builder(getContext(), R.style.ThemeOverlay_BrowserUI_AlertDialog);
        AlertDialog alertDialog =
                alert.setTitle(R.string.website_settings_select_dialog_title)
                        .setView(view)
                        .setPositiveButton(
                                R.string.website_settings_select_dialog_button, onClickListener)
                        .setNegativeButton(R.string.cancel, onClickListener)
                        .create();
        alertDialog.getDelegate().setHandleNativeActionModesEnabled(false);
        alertDialog.show();
    }
}
