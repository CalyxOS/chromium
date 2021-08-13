// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.components.user_scripts;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.view.View;

import androidx.fragment.app.Fragment;

import org.chromium.ui.base.ActivityKeyboardVisibilityDelegate;
import org.chromium.ui.base.ImmutableWeakReference;
import org.chromium.ui.base.IntentRequestTracker;
import org.chromium.ui.base.IntentRequestTracker.Delegate;
import org.chromium.ui.base.WindowAndroid;

import java.lang.ref.WeakReference;

import org.chromium.ui.permissions.ActivityAndroidPermissionDelegate;

/**
 * Implements intent sending for a fragment based window. This should be created when
 * onAttach() is called on the fragment, and destroyed when onDetach() is called.
 */
public class FragmentWindowAndroid extends WindowAndroid {
    private Fragment mFragment;

    private static class TrackerDelegateImpl implements Delegate {
        private final Fragment mFragment;
        // This WeakReference is purely to avoid gc churn of creating a new WeakReference in
        // every getActivity call. It is not needed for correctness.
        private ImmutableWeakReference<Activity> mActivityWeakRefHolder;

        /**
         * Create an instance of delegate for the given fragment that will own the
         * IntentRequestTracker.
         * @param fragment The fragment that owns the IntentRequestTracker.
         */
        private TrackerDelegateImpl(Fragment fragment) {
            mFragment = fragment;
        }

        @Override
        public boolean startActivityForResult(Intent intent, int requestCode) {
            mFragment.startActivityForResult(intent, requestCode, null);
            return true;
        }

        @Override
        public boolean startIntentSenderForResult(IntentSender intentSender, int requestCode) {
            try {
                mFragment.startIntentSenderForResult(
                        intentSender, requestCode, new Intent(), 0, 0, 0, null);
            } catch (IntentSender.SendIntentException e) {
                return false;
            }
            return true;
        }

        @Override
        public void finishActivity(int requestCode) {
            Activity activity = getActivity().get();
            if (activity == null) return;
            activity.finishActivity(requestCode);
        }

        @Override
        public final WeakReference<Activity> getActivity() {
            if (mActivityWeakRefHolder == null
                    || mActivityWeakRefHolder.get() != mFragment.getActivity()) {
                mActivityWeakRefHolder = new ImmutableWeakReference<>(mFragment.getActivity());
            }
            return mActivityWeakRefHolder;
        }
    }

    FragmentWindowAndroid(Context context, Fragment fragment) {
        super(context, IntentRequestTracker.createFromDelegate(new TrackerDelegateImpl(fragment)));
        mFragment = fragment;

        setKeyboardDelegate(new ActivityKeyboardVisibilityDelegate(getActivity()));
        setAndroidPermissionDelegate(new ActivityAndroidPermissionDelegate(getActivity()));
    }
}
