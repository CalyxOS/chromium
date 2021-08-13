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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.chromium.components.browser_ui.widget.dragreorder.DragReorderableListAdapter;
import org.chromium.components.browser_ui.widget.dragreorder.DragStateDelegate;
import org.chromium.components.browser_ui.widget.listmenu.ListMenuButton;
import org.chromium.components.browser_ui.widget.listmenu.ListMenuButtonDelegate;
import org.chromium.ui.widget.ChromeImageView;

import java.util.ArrayList;
import java.util.List;

public class ScriptListBaseAdapter extends DragReorderableListAdapter<ScriptInfo> {

    class ItemClickListener {
        void onScriptOnOff(boolean Enabled) {}
        void onScriptClicked() {}
    }

    static class ScriptInfoRowViewHolder extends ViewHolder {
        private TextView mTitle;
        private TextView mDescription;
        private TextView mVersion;
        private TextView mFile;
        private TextView mUrl;
        private TextView mError;
        private Switch mSwitch;
        private ChromeImageView mIcon;
        private LinearLayout mErrorLayout;
        private LinearLayout mUrlContainer;

        private ListMenuButton mMoreButton;

        private CompoundButton.OnCheckedChangeListener mOnOffListener;

        ScriptInfoRowViewHolder(View view) {
            super(view);

            mSwitch = view.findViewById(R.id.switch_widget);
            mTitle = view.findViewById(R.id.title);
            mDescription = view.findViewById(R.id.description);
            mVersion = view.findViewById(R.id.version);
            mFile = view.findViewById(R.id.file);
            mUrl = view.findViewById(R.id.url);
            mUrlContainer = view.findViewById(R.id.url_container);
            mError = view.findViewById(R.id.error);
            mIcon = view.findViewById(R.id.icon);
            mErrorLayout = view.findViewById(R.id.error_layout);

            mMoreButton = view.findViewById(R.id.more);
        }

        protected void updateScriptInfo(ScriptInfo item) {
            mSwitch.setOnCheckedChangeListener(null);
            mSwitch.setChecked(item.Enabled);
            mSwitch.setOnCheckedChangeListener(mOnOffListener);

            mSwitch.setEnabled(true);
            if (item.ForceDisabled) {
                mSwitch.setEnabled(false);
            }

            mTitle.setText(item.Name);
            mDescription.setText(item.Description);
            mVersion.setText(item.Version);
            mFile.setText(item.Key);
            mUrl.setText(item.UrlSource);
            mError.setText(item.ParserError);

            mUrl.setVisibility(View.VISIBLE);
            if (item.UrlSource == null || item.UrlSource.isEmpty()) {
                mUrlContainer.setVisibility(View.GONE);
            }
            mErrorLayout.setVisibility(View.VISIBLE);
            if (item.ParserError == null || item.ParserError.isEmpty()) {
                mErrorLayout.setVisibility(View.GONE);
            }
        }

        void setMenuButtonDelegate(@NonNull ListMenuButtonDelegate delegate) {
            mMoreButton.setVisibility(View.VISIBLE);
            mMoreButton.setDelegate(delegate);
            // Set item row end padding 0 when MenuButton is visible.
            ViewCompat.setPaddingRelative(itemView, ViewCompat.getPaddingStart(itemView),
                    itemView.getPaddingTop(), 0, itemView.getPaddingBottom());
        }

        void setItemListener(@NonNull ItemClickListener listener) {
            mOnOffListener = (buttonView, isChecked) -> listener.onScriptOnOff(isChecked);
            mSwitch.setOnCheckedChangeListener(mOnOffListener);
            itemView.setOnClickListener(view -> listener.onScriptClicked());
        }
    }

    ScriptListBaseAdapter(Context context) {
        super(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View row = LayoutInflater.from(viewGroup.getContext())
                           .inflate(R.layout.accept_script_item, viewGroup, false);
        return new ScriptInfoRowViewHolder(row);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        ((ScriptInfoRowViewHolder) viewHolder).updateScriptInfo(mElements.get(i));
    }

    void setDisplayedScriptInfo(List<ScriptInfo> values) {
        mElements = new ArrayList<>(values);
        notifyDataSetChanged();
    }

    @Override
    protected void setOrder(List<ScriptInfo> order) {
    }

    @Override
    protected boolean isActivelyDraggable(ViewHolder viewHolder) {
        return isPassivelyDraggable(viewHolder);
    }

    @Override
    protected boolean isPassivelyDraggable(ViewHolder viewHolder) {
        return viewHolder instanceof ScriptInfoRowViewHolder;
    }
}
