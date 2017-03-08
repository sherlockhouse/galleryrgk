/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.app;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManagerGlobal;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.android.gallery3d.R;

import java.util.HashMap;
import java.util.Map;

public class PhotoPageBottomControls implements OnClickListener {
    public interface Delegate {
        public boolean canDisplayBottomControls();
        public boolean canDisplayBottomControl(int control);
        public void onBottomControlClicked(int control);
        public void refreshBottomControlsWhenReady();
    }

    private Delegate mDelegate;
    private ViewGroup mParentLayout;
    private ViewGroup mContainer;

    private AbstractGalleryActivity mActivity; //add by songqingming for voice photo

    private boolean mContainerVisible = false;
    private Map<View, Boolean> mControlsVisible = new HashMap<View, Boolean>();

    private Animation mContainerAnimIn = new AlphaAnimation(0f, 1f);
    private Animation mContainerAnimOut = new AlphaAnimation(1f, 0f);
    private static final int CONTAINER_ANIM_DURATION_MS = 200;

    private static final int CONTROL_ANIM_DURATION_MS = 150;
    private static Animation getControlAnimForVisibility(boolean visible) {
        Animation anim = visible ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);
        anim.setDuration(CONTROL_ANIM_DURATION_MS);
        return anim;
    }

    /// M: [BUG.MODIFY] @{
    /*public PhotoPageBottomControls(Delegate delegate, Context context, RelativeLayout layout) {*/
    public PhotoPageBottomControls(Delegate delegate, Context context, ViewGroup layout) {
        /// @}
        mDelegate = delegate;
        mParentLayout = layout;
        mActivity = (AbstractGalleryActivity) context; //add by songqingming for voice photo

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContainer = (ViewGroup) inflater
                .inflate(R.layout.photopage_bottom_controls, mParentLayout, false);
        mParentLayout.addView(mContainer);

        for (int i = mContainer.getChildCount() - 1; i >= 0; i--) {
            View child = mContainer.getChildAt(i);
            child.setOnClickListener(this);
            mControlsVisible.put(child, false);
        }

        mContainerAnimIn.setDuration(CONTAINER_ANIM_DURATION_MS);
        mContainerAnimOut.setDuration(CONTAINER_ANIM_DURATION_MS);

        mDelegate.refreshBottomControlsWhenReady();
    }

    private void hide() {
        mContainer.clearAnimation();
        mContainerAnimOut.reset();
        mContainer.startAnimation(mContainerAnimOut);
        mContainer.setVisibility(View.INVISIBLE);
    }

    private void show() {
        mContainer.clearAnimation();
        mContainerAnimIn.reset();
        mContainer.startAnimation(mContainerAnimIn);
        mContainer.setVisibility(View.VISIBLE);
    }

    public void refresh() {
        boolean visible = mDelegate.canDisplayBottomControls();
        boolean containerVisibilityChanged = (visible != mContainerVisible);
        if (containerVisibilityChanged) {
            if (visible) {
                show();
            } else {
                hide();
            }
            mContainerVisible = visible;
        }
        if (!mContainerVisible) {
            return;
        }
        for (View control : mControlsVisible.keySet()) {
            Boolean prevVisibility = mControlsVisible.get(control);
            boolean curVisibility = mDelegate.canDisplayBottomControl(control.getId());
            if (prevVisibility.booleanValue() != curVisibility) {
                if (!containerVisibilityChanged) {
                    control.clearAnimation();
                    control.startAnimation(getControlAnimForVisibility(curVisibility));
                }
                control.setVisibility(curVisibility ? View.VISIBLE : View.INVISIBLE);
                mControlsVisible.put(control, curVisibility);
            }
        }

        //add by songqingming for voice photo (start)
        if (android.os.SystemProperties.getInt("ro.voice_capture_support", 0) == 1) {
            if (mControlsVisible.containsValue(true)) {
                setVoiceViewBottomMargin(true);
            } else {
                setVoiceViewBottomMargin(false);
            }
        }
        //(end)

        // Force a layout change
        mContainer.requestLayout(); // Kick framework to draw the control.
    }

    public void cleanup() {
        mParentLayout.removeView(mContainer);
        mControlsVisible.clear();
    }

    @Override
    public void onClick(View view) {
        Boolean controlVisible = mControlsVisible.get(view);
        if (mContainerVisible && controlVisible != null && controlVisible.booleanValue()) {
            mDelegate.onBottomControlClicked(view.getId());
        }
    }

    //A:JLLBLS-401 guoshuai 20151020(start)
    public void adjustLayoutForNavi(Context context, boolean hasNavi) {
        if(!hasNavi || mContainer == null) return;
        boolean isPortrait = context.getResources().getConfiguration().orientation 
                == Configuration.ORIENTATION_PORTRAIT;
        View view = mContainer.findViewById(R.id.photopage_bottom_controls);
        String contextClsName = context.getClass().getName();
        int NaviHeight = context.getResources().
                getDimensionPixelSize(R.dimen.navigation_bar_height);
        if(contextClsName.equals("com.android.camera.CameraActivity")){
            FrameLayout.LayoutParams lp = (android.widget.FrameLayout.LayoutParams) view.getLayoutParams();
            lp.bottomMargin = isPortrait ? NaviHeight : 0;
            view.setLayoutParams(lp);
        }else{
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
            lp.bottomMargin = isPortrait ? NaviHeight : 0;
            view.setLayoutParams(lp);
        }
    }
    
    
    //A:JLLBLS-401 guoshuai 20151020(end)
    /// M: [FEATURE.ADD] get photopage_bottom_control_edit visibility @{
    /**
     * Get view visibility, which controller other layer view visibility.
     * @return true if this view is visible.
     */
    public boolean getContainerVisibility() {
        boolean visiable = mContainerVisible;
        for (View control : mControlsVisible.keySet()) {
            if (control.getId() == R.id.photopage_bottom_control_edit) {
                Boolean editViewVisibility = mControlsVisible.get(control);
                visiable &= editViewVisibility;
            }
        }
        return visiable;
    }
    // In case of hide container without animation.
    public void hideContainer() {
        mContainer.clearAnimation();
        mContainer.setVisibility(View.INVISIBLE);
        mContainerVisible = false;
    }
    /// @}

    //add by songqingming for voice photo (start)
    private void setVoiceViewBottomMargin(boolean bottomViewVisible) {
        mActivity.getVoicePlayerManager().onBottomVisible(bottomViewVisible);
    }
    //(end)
}
