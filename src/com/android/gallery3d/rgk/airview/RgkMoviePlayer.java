/*
 * Copyright (C) 2014 MediaTek Inc.
 * Modification based on code covered by the mentioned copyright
 * and/or permission notice(s).
 */
/*
 * Copyright (C) 2009 The Android Open Source Project
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

import com.android.gallery3d.R;
import com.android.gallery3d.app.Log;
import com.android.gallery3d.app.MovieActivity;
import com.android.gallery3d.app.MoviePlayer;
import com.android.gallery3d.app.MoviePlayer.MoviePlayerExtension;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mediatek.gallery3d.video.ExtensionHelper;
import com.mediatek.gallery3d.video.IMovieItem;
import com.mediatek.gallery3d.video.MediaPlayerWrapper;
import com.mediatek.gallery3d.video.MovieView;
import com.mediatek.gallery3d.video.VideoGestureController;

public class RgkMoviePlayer extends MoviePlayer {

	public RgkMoviePlayer(View rootView, MovieActivity movieActivity,
			IMovieItem movieItem, Bundle savedInstance, boolean canReplay,
			String cookie) {
		super(rootView, movieActivity, movieItem, savedInstance, canReplay, cookie);
		
		rgkMovieController = (RgkMovieControllerOverlay) mController;
		// TODO Auto-generated constructor stub
	}

	protected RgkMovieControllerOverlay rgkMovieController ;


	

	private Boolean airview_support = false;
	private int airview_position = 0;
	private int airview_duration = 0;
	private RGKMovieService rms;

	private Boolean isResume = false;
	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			// TODO Auto-generated method stub
			// cm.getPackageName()
			Log.i("zhoulei_test",
					"contect position = "
							+ mMediaPlayerWrapper.getCurrentPosition()
							+ ",position 2 = " + mVideoPosition
							+ ",position 3 =" + mSeekMovePosition);

			rms = ((RGKMovieService.MsgBinder) arg1).getService();
			if (isResume) {
				// activity first start or has close
				if (rms.isremove) {
					mContext.unbindService(conn);
					// activity first start
				} else {
					Log.i("zhoulei_test", "isremove = false");
					Log.i("zhoulei_test", "rms.getUri() = " + rms.getUri());
					if (mMovieItem.getUri().equals(rms.getUri())) {
						mVideoPosition = rms.getPosition();
					}
					rms.setServiceConnection(conn);
					rms.close();
				}
				rms = null;
			} else {
				rms.init();
				rms.setUri(mMovieItem.getUri());
				rms.setPosition(mMediaPlayerWrapper.getCurrentPosition() > 0 ? mMediaPlayerWrapper
						.getCurrentPosition() : mVideoPosition);
				rms.play();
				rms.setServiceConnection(conn);
				// rms.showDialogue();
				mActivityContext.finish();
			}

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.i("zhoulei_test", "discontect");
			rms = null;
		}

	};

	public void startAirView() {
		isResume = false;
		if (!(rgkMovieController).mScreenLocked && !(mTState == TState.COMPELTED)) {
			if (mIsOnlyAudio) {
				Toast.makeText(
						mContext,
						mContext.getResources().getString(
								R.string.air_view_support_error), 1000).show();
			} else {
				Intent intent = new Intent(mContext, RGKMovieService.class);
				mContext.bindService(intent, conn, Context.BIND_AUTO_CREATE);
			}
		}
	}

	public void onPause() {
		isResume = false;
		super.onPause();

	}

	public void onResume() {

		isResume = true;
		// new start or has close
		if (!(rgkMovieController).mScreenLocked) {
			if (rms == null) {
				Intent intent = new Intent(mContext, RGKMovieService.class);
				mContext.bindService(intent, conn, Context.BIND_AUTO_CREATE);
				// when airview on
			} else {
				mVideoPosition = rms.getPosition();
				rms.close();
			}
		}
		super.onResume();

		if (airview_support) {
			mMediaPlayerWrapper.seekTo(airview_position);
			airview_support = false;
		}
	}

	public MovieView getVideoSurface() {
		return mMovieView;
	}

	public class RgkMoviePlayerExtension extends MoviePlayerExtension {

	public MovieView getVideoSurface() {
			return getVideoSurface();
		}
	}

}
