/*
 * Copyright (C) 2014 MediaTek Inc.
 * Modification based on code covered by the mentioned copyright
 * and/or permission notice(s).
 */
/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.gallery3d.rgk.airview;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.widget.Toast;
import android.net.Uri;
import android.os.Environment;

import com.android.gallery3d.R;
import com.android.gallery3d.app.Log;
import com.android.gallery3d.app.MovieActivity;
import com.android.gallery3d.app.MovieControllerOverlay;
import com.android.gallery3d.app.CommonControllerOverlay.State;
import com.android.gallery3d.util.RgkGalleryUtils;
import com.mediatek.gallery3d.video.DefaultMovieItem;
import com.mediatek.gallery3d.video.IMovieItem;
import com.mediatek.gallery3d.video.MediaPlayerWrapper;
import com.mediatek.gallery3d.video.MovieUtils;
import com.mediatek.gallery3d.video.ExtensionHelper;
import com.mediatek.gallery3d.video.IContrllerOverlayExt;
import com.mediatek.gallery3d.video.MtkVideoFeature;
import com.mediatek.storage.StorageManagerEx;
import com.rgk.config.RgkConfig;

/**
 * The playback controller for the Movie Player.
 */
public class RgkMovieControllerOverlay extends MovieControllerOverlay  {
    
    private boolean mHidden;
    private static final String TAG = "Gallery2/VideoPlayer/RgkMovieControllerOverlay";
    private ImageView mLockView;
    private ImageView mCaptureView;
    private ImageView mAirView;
    public boolean mScreenLocked = false;
	private boolean enable_airview = false;

    public RgkMovieControllerOverlay(Context context) {
        super(context);
        enableAirview();
        if (enable_airview) {
        	initVideoControlButton(context);
        }
    }
    
    private void enableAirview() {
    	RgkConfig.enable_airview = mContext.getResources().getBoolean(R.bool.enable_airview);
    	enable_airview = RgkConfig.enable_airview;		
	}

	public RgkMovieControllerOverlay(Context context,
            MediaPlayerWrapper playerWrapper, IMovieItem movieItem) {
        super(context, playerWrapper, movieItem);
        enableAirview();
        if (enable_airview) {
        	initVideoControlButton(context);
        }
    }

    private void initVideoControlButton(Context context) {

        LayoutParams wrapContent =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mLockView = new ImageView(context);
        mLockView.setImageResource(R.drawable.btn_video_unlock);
        mLockView.setScaleType(ScaleType.CENTER);
        mLockView.setFocusable(true);
        mLockView.setClickable(true);
        mLockView.setOnClickListener(this);
        addView(mLockView, wrapContent);
        mLockView.setVisibility(View.GONE);
        
        mCaptureView = new ImageView(context);
        mCaptureView.setImageResource(R.drawable.btn_capture_pic);
        mCaptureView.setScaleType(ScaleType.CENTER);
        mCaptureView.setFocusable(true);
        mCaptureView.setClickable(true);
        mCaptureView.setOnClickListener(this);
        addView(mCaptureView, wrapContent);
        mCaptureView.setVisibility(View.GONE); 
        
        mAirView = new ImageView(context);
        mAirView.setImageResource(R.drawable.ic_popup_dim_normal_w);
        mAirView.setScaleType(ScaleType.CENTER);
        mAirView.setFocusable(true);
        mAirView.setClickable(true);
        mAirView.setOnClickListener(this);
        addView(mAirView, wrapContent);
        mAirView.setVisibility(View.GONE); 
        
        requestLayout();
    
    }

    @Override
    public void hide() {
        super.hide();
        setVideoWidgetInvisible();
    }

    private void setVideoWidgetInvisible() {
        if (mLockView != null && mCaptureView != null && mAirView != null) {
            mLockView.setVisibility(View.INVISIBLE);
            mCaptureView.setVisibility(View.INVISIBLE);
            mAirView.setVisibility(View.INVISIBLE);
        }
    
    }
    
    @Override
    protected void startHiding() {
        super.startHiding();
        if (enable_airview) {
        	setVideoWigdetHideAnima();
        }
    }
    
    private void setVideoWigdetHideAnima() {
        startHideAnimation(mLockView);
        startHideAnimation(mCaptureView);
        startHideAnimation(mAirView);
    }

    @Override
    protected void cancelHiding() {
        super.cancelHiding();
        if (enable_airview) {
        	setVideoWidgetAnimationNull();
        }
    }
    
    private void setVideoWidgetAnimationNull() {
        mLockView.setAnimation(null);
        mCaptureView.setAnimation(null);
        mAirView.setAnimation(null);
    }
    
    private static final boolean LOG = true;

    @Override
    public void onClick(View view) {
        if (LOG) {
            Log.v(TAG, "onClick(" + view + ") listener=" + mListener
                    + ", state=" + mState + ", canReplay=" + mCanReplay);
        }
        super.onClick(view);

        if (mListener != null) {
            if (!mScreenLocked) {
                if (view == mLockView) {
                    setOrientation(mScreenLocked);
                    hide();
                    lock(!mScreenLocked);
                    show();
                } else if (view == mCaptureView) {
					snapshot();
                }  else if (view == mAirView) {
                    ((RgkMoviePlayer)((MovieActivity) mContext).mPlayer).startAirView();
                }
            } else {
                if (view == mLockView) {
                    setOrientation(mScreenLocked);
                    Log.i(TAG, "LOCKVIEW");
                    hide();
                    lock(!mScreenLocked);
                    show();
                }
            }
        }
    }
  
	// A: DWQBE-86 gulincheng 20151015{

	private File screenshotsDirectory;
	private String ScreenshotStorage;
	public static final String SNAP_SHOT_PATH = "/Player";

	public void snapshot() {

		boolean isPlaying = false;
		screenshotsDirectory = new File(StorageManagerEx.getDefaultPath() + "/"
				+ (Environment.DIRECTORY_PICTURES) + SNAP_SHOT_PATH);

		if (!screenshotsDirectory.exists()) {
			screenshotsDirectory.mkdirs();
		}
		Log.d("jzbg", screenshotsDirectory.toString());
		if (RgkGalleryUtils.isHaveExternalSDCard()) {
			// gulincheng 20160511 DWYSLM-921 start
			if (!screenshotsDirectory.toString().contains("emulated/0")) {
				// gulincheng 20160511 DWYSLM-921 end
				ScreenshotStorage = mContext.getResources().getString(
						R.string.picture_store_in)
						+ ": "
						+ mContext.getResources().getString(
								R.string.sdcard_storage)
						+ "/"
						+ Environment.DIRECTORY_PICTURES + SNAP_SHOT_PATH;
			} else {
				ScreenshotStorage = mContext.getResources().getString(
						R.string.picture_store_in)
						+ ": "
						+ mContext.getResources().getString(
								R.string.phone_storage)
						+ "/"
						+ Environment.DIRECTORY_PICTURES + SNAP_SHOT_PATH;
			}
		} else {
			ScreenshotStorage = mContext.getResources().getString(
					R.string.picture_store_in)
					+ ": "
					+ mContext.getResources().getString(R.string.phone_storage)
					+ "/" + Environment.DIRECTORY_PICTURES + SNAP_SHOT_PATH;
		}

		final IMovieItem mItem = mMovieItem;
		final File savePath = new File(screenshotsDirectory.getPath() + "/"
				+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
				+ ".jpg");

		final Uri imgUri = Uri.fromFile(savePath);

		final Activity mActivity = (Activity) mContext;

		if (mMediaPlayerWrapper.isPlaying()) {
			isPlaying = true;
		}
		if (isPlaying) {
			mMediaPlayerWrapper.pause();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {

				// A: DWQBE-186 gulincheng 20151015{
				final Bitmap bitmap = 
						((MovieActivity)mContext).mPlayer.mMovieView.getBitmap();

				if (bitmap != null) {
					if (!RgkGalleryUtils.saveBitmap(savePath.getPath(), bitmap)) {

						savePath.delete();
						if (bitmap.getByteCount() < screenshotsDirectory
								.getUsableSpace()) {
							((Activity) mContext).runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(
											mContext,
											R.string.storage_not_enough_picture,
											Toast.LENGTH_LONG).show();
								}
							});

						}
					}
				} else {
					((Activity) mContext).runOnUiThread(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(mContext,
									R.string.video_capture_fail,
									Toast.LENGTH_LONG).show();
						}
					});

				}
				// A: }
				if (imgUri != null) {
					mContext.sendBroadcast(new Intent(
							Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imgUri));
				}

			}

		}).start();
		Toast.makeText(mContext, ScreenshotStorage, Toast.LENGTH_SHORT)
				.show();
		if (isPlaying) {
			mMediaPlayerWrapper.start();
		}
	}

    private void lock(boolean toLock) {
        if (toLock) {
            mLockView.setImageResource(R.drawable.btn_video_lock);
        } else {
            mLockView.setImageResource(R.drawable.btn_video_unlock);
        }
        mScreenLocked = toLock;
    }
    
    //A: DWQBE-186 gulincheng 20151027{
    public void setOrientation(boolean locked) {
        Activity a = (Activity) mContext;
        if (!locked) {
            switch (a.getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case Configuration.ORIENTATION_UNDEFINED:
                break;
            case Configuration.ORIENTATION_SQUARE:
                break;
            }
        } else {
            a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }
    //A: DWQBE-186 gulincheng 20151027{
    //A: }

    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (enable_airview) {
	        int width = ((Activity) mContext).getWindowManager().getDefaultDisplay().getWidth();
	        Rect insets = mWindowInsets;
	        int pl = insets.left; // the left paddings
	        int pr = insets.right;
	        int pt = insets.top;
	        int pb = insets.bottom;
	
	        int h = bottom - top;
	        int w = right - left;
	        boolean error = mErrorView.getVisibility() == View.VISIBLE;
	
	        int y = h - pb;
	        int ht = y - mTimeBar.getPreferredHeight();
	        int lw = mLockView.getMeasuredWidth();
	        int lh = mLockView.getMeasuredHeight();
	        int cw = mCaptureView.getMeasuredWidth();
	        int ch = mCaptureView.getMeasuredHeight();
	        int aw = mAirView.getMeasuredWidth();
	        int ah = mAirView.getMeasuredHeight();
	        int tmp = 4;
	        if (!mScreenLocked) {
			//modify DWEQBSE-110 teng.guo modify the video function button position 20161215 start
				//mLockView.layout(w / tmp - lw / 2, ht - lh - lh / 4, w / tmp + lw/ 2, ht - lh / 4);
	            mLockView.layout(w / tmp - lw / 2, ht - lh - lh / 4+10, w / tmp + lw
	                    / 2, ht - lh / 4+10);
	        } else {
				//mLockView.layout(w / 2 - lw / 2, ht - lh - lh / 4, w / 2 + lw / 2,ht - lh / 4);
	            mLockView.layout(w / 2 - lw / 2, ht - lh - lh / 4+10, w / 2 + lw / 2,
	                    ht - lh / 4+10);
	        }
			//mCaptureView.layout(w / 2 - lw / 2, ht - ch - ch / 4, w / 2 + lw / 2,ht - ch / 4);
	        mCaptureView.layout(w / 2 - lw / 2, ht - ch - ch / 4+10, w / 2 + lw / 2,
	                ht - ch / 4+10);
			//mAirView.layout(w * 3 / tmp - aw / 2, ht - lh - lh / 4, w * 3 / 4 + aw/ 2, ht - lh / 4);
	        mAirView.layout(w * 3 / tmp - aw / 2, ht - lh - lh / 4+10, w * 3 / 4 + aw
	                / 2, ht - lh / 4+10);
			//modify DWEQBSE-110 teng.guo modify the video function button position 20161215 end
        }
    }

    @Override
    protected void updateViews() {
		if (enable_airview) {

			if (mHidden) {
				return;
			}

			if (mScreenLocked) {
				mLockView.setVisibility(View.VISIBLE);
				((MovieActivity) mContext).getActionBar().hide();
				requestLayout();
				return;
			}
			setVideoWidgetVisible();
		}
 
        super.updateViews();
        if (LOG) {
            Log.v(TAG, "updateViews() state=" + mState + ", canReplay="
                    + mCanReplay);
        }
    	
    }
    
    private void setVideoWidgetVisible() {
        mLockView.setVisibility(View.VISIBLE);
        mCaptureView.setVisibility(View.VISIBLE);
        mAirView.setVisibility(View.VISIBLE);
    }
}
