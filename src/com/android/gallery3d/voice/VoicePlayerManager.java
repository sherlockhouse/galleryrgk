package com.android.gallery3d.voice;

import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryActivity;
import com.android.gallery3d.voice.VoicePlayer.VoiceListener;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManagerGlobal;

public class VoicePlayerManager {
	private GalleryActivity mActivity;

	private View mView;

	private boolean mStartPlaying = false;

	private VoicePlayer mVoicePlayer;

	private CircleProgressBar mVoiceProgress;

	private static final int MSG_PLAY_PREPARED = 1;
	private static final int MSG_PLAY_COMPLETE = 2;

	private byte[] currentVoiceBytes;

	private boolean mShow;
	
	private boolean hasBottomVisiblePending = false;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_PLAY_PREPARED:
				mVoiceProgress.startCustomAnimation();
				break;
			case MSG_PLAY_COMPLETE:
				mStartPlaying = false;
				break;
			}
		}

	};

	private OnClickListener clicker = new OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.player_progress_btn:
				onPlay(!mStartPlaying);
				break;
			}

		}
	};

	private VoiceListener mVoiceListener = new VoiceListener() {

		@Override
		public void onGetDuration(long mills) {
			Log.d("sqm", "total time = " + (mills / 1000));
			mVoiceProgress.setDuration(mills);
			mHandler.sendEmptyMessage(MSG_PLAY_PREPARED);
		}

		@Override
		public void onComplete() {
			Log.d("sqm", "onComplete:");
			mHandler.sendEmptyMessage(MSG_PLAY_COMPLETE);
		}
	};

	public VoicePlayerManager(GalleryActivity activity) {
		mActivity = activity;
	}

	public View getView() {
		LayoutInflater inflater = LayoutInflater.from(mActivity);
		View view = inflater.inflate(R.layout.voice_player_layout, null);

		mVoiceProgress = (CircleProgressBar) view
				.findViewById(R.id.player_progress_btn);
		mVoiceProgress.setOnClickListener(clicker);

		mVoicePlayer = new VoicePlayer(mActivity.getApplicationContext());
		mVoicePlayer.setVoiceListener(mVoiceListener);

		return view;
	}

	public void show() {
		if (mView == null) {
			mView = getView();
			mActivity.addView(mView);
		}
		if (mView != null) {
			mView.setVisibility(View.VISIBLE);
			mShow = true;
			if (hasBottomVisiblePending) {
				hasBottomVisiblePending = false;
				playTranslationAnimation(true);
			}
		}
	}

	public void hide() {
		if (mView != null) {
			mView.setVisibility(View.GONE);
			mShow = false;
		}
	}

	public void pause() {
		stopPlaying();
	}

	public void destroy() {
		if (mShow) {
			hide();
		}
		reset();
		if (mVoicePlayer != null) {
			mVoicePlayer.destroyBackThread();
			mVoicePlayer = null;
		}
	}

	private void onPlay(boolean start) {
		if (start) {
			startPlaying();
		} else {
			stopPlaying();
		}
	}

	private void startPlaying() {
		mStartPlaying = true;
		mVoicePlayer.start(currentVoiceBytes);
	}

	private void stopPlaying() {
		if (mVoicePlayer != null && mVoicePlayer.isPlaying()) {
			mStartPlaying = false;
			mVoiceProgress.cancelAnimation();
			mVoicePlayer.stop();
		}
	}

	public void setVoiceBytes(byte[] voiceBytes) {
		currentVoiceBytes = voiceBytes;
	}

	public byte[] getVoiceBytes() {
		return currentVoiceBytes;
	}

	public void reset() {
		currentVoiceBytes = null;
		stopPlaying();
	}
	
	public void destroyView() {
		if (mView != null) {
			mActivity.removeView(mView);
			mView = null;
			mVoicePlayer.setVoiceListener(null);
			mVoicePlayer.destroyBackThread();
			mVoicePlayer = null;
		}
	}

	public boolean isVisible() {
		return mShow;
	}

	public void onBottomVisible(boolean visible) {
		Log.d("sqm", "onBottomVisible---" + visible);
		if (mVoiceProgress == null) {
			hasBottomVisiblePending = true;
			return;
		}
		hasBottomVisiblePending = false;
		playTranslationAnimation(visible);
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	private void playTranslationAnimation(boolean visible) {
		final ObjectAnimator oa;
		if (visible && mVoiceProgress.getTranslationY() == 0) {
			int transY = -80 - getNavigationBarSize();
			oa = ObjectAnimator.ofFloat(mVoiceProgress, "translationY", transY);
		} else if (!visible && mVoiceProgress.getTranslationY() != 0) {
			oa = ObjectAnimator.ofFloat(mVoiceProgress, "translationY", 0);
		} else {
			oa = null;
		}
		if (oa == null) {
			return;
		}
		oa.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationEnd(Animator animation) {
				Log.d("sqm", "VoicePlayerManager: onAnimationEnd------");
				// mVoiceProgress.setTranslationY(0);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				Log.d("sqm", "VoicePlayerManager: onAnimationRepeat------");
			}

		});
		// oa.setRepeatMode(ObjectAnimator.REVERSE);
		// oa.setRepeatCount(7);
		oa.setDuration(80);
		oa.start();
	}

	private boolean hasNavigationBar() {
		try {
			return WindowManagerGlobal.getWindowManagerService().hasNavigationBar();
		} catch(RemoteException ex) {
			return false;
		}
	}

	private int getNavigationBarSize() {
		int naviHeight = 0;
		boolean isPortrait = mActivity.getResources().getConfiguration().orientation 
			== Configuration.ORIENTATION_PORTRAIT;
		if (hasNavigationBar() && isPortrait) {
			naviHeight = mActivity.getResources().getDimensionPixelSize(
				R.dimen.navigation_bar_height);
		}
		return naviHeight;
	}

}
