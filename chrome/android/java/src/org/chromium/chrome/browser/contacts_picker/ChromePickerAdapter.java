// Copyright 2020 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.contacts_picker;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.chromium.chrome.R;
import org.chromium.chrome.browser.profiles.Profile;
import org.chromium.components.browser_ui.contacts_picker.ContactDetails;
import org.chromium.components.browser_ui.contacts_picker.PickerAdapter;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A {@link PickerAdapter} with special behavior tailored for Chrome.
 *
 * Owner email is looked up in the {@link ProfileDataCache}, or, failing that, via the {@link
 * AccountManagerFacade}.
 */
public class ChromePickerAdapter extends PickerAdapter {
    // Whether an observer for ProfileDataCache has been registered.
    private boolean mObserving;

    // Whether owner info is being fetched asynchronously.
    private boolean mWaitingOnOwnerInfo;

    public ChromePickerAdapter(Context context) {
    }

    // Adapter:

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        addProfileDataObserver();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        removeProfileDataObserver();
    }

    private void addProfileDataObserver() {
        if (!mObserving) {
            mObserving = true;
        }
    }

    private void removeProfileDataObserver() {
        if (mObserving) {
            mObserving = false;
        }
    }

    // PickerAdapter:

    /**
     * Returns the email for the currently signed-in user. If that is not available, return the
     * first Google account associated with this phone instead.
     */
    @Override
    protected String findOwnerEmail() {
        return null;
    }

    @Override
    protected void addOwnerInfoToContacts(ArrayList<ContactDetails> contacts) {
        // Processing was not complete, finish the rest asynchronously. Flow continues in
        // onProfileDataUpdated.
        mWaitingOnOwnerInfo = true;
        addProfileDataObserver();
        contacts.add(0, constructOwnerInfo(getOwnerEmail()));
    }

    /**
     * Constructs a {@link ContactDetails} record for the currently signed in user. Name is obtained
     * via the {@link DisplayableProfileData}, if available, or (alternatively) using the signed in
     * information.
     * @param ownerEmail The email for the currently signed in user.
     * @return The contact info for the currently signed in user.
     */
    @SuppressLint("HardwareIds")
    private ContactDetails constructOwnerInfo(String ownerEmail) {
        ContactDetails contact = new ContactDetails(ContactDetails.SELF_CONTACT_ID, ownerEmail,
                Collections.singletonList(ownerEmail), /*phoneNumbers=*/null, /*addresses=*/null);
        return null;
    }
}
