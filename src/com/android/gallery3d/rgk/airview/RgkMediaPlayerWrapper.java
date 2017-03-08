
package com.android.gallery3d.rgk.airview;


import java.io.IOException;

import com.android.gallery3d.app.Log;
import com.mediatek.gallery3d.video.MediaPlayerWrapper;
import com.mediatek.gallery3d.video.MovieView;
import com.mediatek.gallery3d.video.MtkVideoFeature;
import com.mediatek.gallery3d.video.MovieView.TextureSurfaceCallback;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.media.Cea708CaptionRenderer;
import android.media.ClosedCaptionRenderer;
import android.media.Metadata;
import android.media.SubtitleController;
import android.media.TtmlRenderer;
import android.media.WebVttRenderer;

public class RgkMediaPlayerWrapper extends MediaPlayerWrapper implements
        MovieView.TextureSurfaceCallback {
	private static final String TAG = "Gallery2/VideoPlayer/RgkMediaPlayerWrapper";
	
	protected Surface mSurface = null;


	public RgkMediaPlayerWrapper(Context context, MovieView movieView) {
		super(context, movieView);
	}
	
	@Override
	public void initialize() {
        if (mMovieView != null) {
            mMovieView.setTextureSurfaceListener(this);
        }
	}
	
	@Override
	protected void openVideo() {
		if (!mOnResumed || mUri == null || mSurface == null) {
            Log.v(TAG, "openVideo, not ready for playback just yet," +
                    " will try again later, mOnResumed = " + mOnResumed +
                    ", mUri = " + mUri + ", mSurfaceHolder = " + mSurface);
            return;
        }
        Log.v(TAG, "openVideo");
        // we shouldn't clear clear the target state, because somebody might
        // have called start() previously
        release(false);

        try {
            mMediaPlayer = new MediaPlayer();
            final SubtitleController controller = new SubtitleController(
                    mContext, mMediaPlayer.getMediaTimeProvider(), mMediaPlayer);
            controller.registerRenderer(new WebVttRenderer(mContext));
            controller.registerRenderer(new TtmlRenderer(mContext));
            controller.registerRenderer(new Cea708CaptionRenderer(mContext));
            controller.registerRenderer(new ClosedCaptionRenderer(mContext));
            mMediaPlayer.setSubtitleAnchor(controller, mMovieView);
            if (mAudioSession != 0) {
                mMediaPlayer.setAudioSessionId(mAudioSession);
            } else {
                mAudioSession = mMediaPlayer.getAudioSessionId();
            }
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnInfoListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
            mMediaPlayer.setDataSource(mContext, mUri, mHeaders);
            if (MtkVideoFeature.isMtkMediaPlayer()) {
                mMediaPlayer.setParameter(KEY_PLAYBACK_PARAMETER, MTK_PLAYBACK_VALUE);
            }
//            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();

            mCurrentBufferPercentage = 0;
            mCurrentState = STATE_PREPARING;
        } catch (IOException ex) {
            Log.e(TAG, "unable to open content: " + mUri, ex);
            onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "unable to open content: " + mUri, ex);
            onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
	}
    protected AirviewController mAirviewController;
    public void setAirviewController(AirviewController ct) {
        mAirviewController = ct;
    }
	
	 @Override
	    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
	        Log.v(TAG, "onVideoSizeChanged, width = " + width + ", height = "
	                + height);
	        mVideoWidth = mp.getVideoWidth();
	        mVideoHeight = mp.getVideoHeight();
	        if (mVideoWidth != 0 && mVideoHeight != 0 && mSurface != null) {
	        	mMovieView.getSurfaceTexture().setDefaultBufferSize(mVideoWidth, mVideoHeight);
	            // TODO mNeedWaitLayout
	        }
	        if (mListener != null) {
	            mListener.onVideoSizeChanged(mp, width, height);
	        }
	        if (mMovieView != null) {
	            mMovieView.setVideoLayout(mVideoWidth, mVideoHeight);
	        }
	        if (mAirviewController != null ) {
	            mAirviewController.setViewSize(mVideoWidth, mVideoHeight);
	        }
	    }
	 
	  @Override
	    public void onPrepared(MediaPlayer mp) {
	        Log.v(TAG, "onPrepared(" + mp + ")");
	        mCurrentState = STATE_PREPARED;

	        /* get the capabilities of the player of this stream */
	        final Metadata data = mp.getMetadata(MediaPlayer.METADATA_ALL,
	                MediaPlayer.BYPASS_METADATA_FILTER);
	        if (data != null) {
	            mCanPause = !data.has(Metadata.PAUSE_AVAILABLE)
	                    || data.getBoolean(Metadata.PAUSE_AVAILABLE);
	            mCanSeekBack = !data.has(Metadata.SEEK_BACKWARD_AVAILABLE)
	                    || data.getBoolean(Metadata.SEEK_BACKWARD_AVAILABLE);
	            mCanSeekForward = !data.has(Metadata.SEEK_FORWARD_AVAILABLE)
	                    || data.getBoolean(Metadata.SEEK_FORWARD_AVAILABLE);
	        } else {
	            mCanPause = true;
	            mCanSeekBack = true;
	            mCanSeekForward = true;
	            Log.v(TAG, "Metadata is null!");
	        }
	        Log.v(TAG, "onPrepared, mCanPause=" + mCanPause + ", mCanSeekBack = "
	                + mCanSeekBack + ", mCanSeekForward = " + mCanSeekForward);

	        if (mListener != null) {
	            mListener.onPrepared(mp);
	        }

	        Log.d(TAG, "onPrepared, mSeekWhenPrepared = " + mSeekWhenPrepared);
			if (mContext instanceof RGKMovieService) {
				((RGKMovieService) mContext).dothingp();
			} else {
				if (mSeekWhenPrepared != 0) {
					seekTo(mSeekWhenPrepared);
				}
			}

	        // TODO for video size changed before started issue

	        mVideoWidth = mp.getVideoWidth();
	        mVideoHeight = mp.getVideoHeight();
	        Log.v(TAG, "onPrepared, mVideoWidth = " + mVideoWidth
	                + ", mVideoHeight = " + mVideoHeight);

	        if (mVideoWidth != 0 && mVideoHeight != 0 && mSurface != null) {
	        	mMovieView.getSurfaceTexture().setDefaultBufferSize(mVideoWidth, mVideoHeight);
	        }

	        if (mTargetState == STATE_PLAYING) {
	            start();
	        }
	   
	    }
	  
	  @Override
	public void onCompletion(MediaPlayer mp) {
		super.onCompletion(mp);
        if (mContext instanceof RGKMovieService ) {
            ((RGKMovieService) mContext).dothingc();
        }
	}
	  @Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		if (mContext instanceof RGKMovieService) {
			((RGKMovieService) mContext).dothinge();
		}
		return super.onError(mp, what, extra);
	}
	  
		// modify by RGK
      @Override
      public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width, final int height) {
          Log.v(TAG, "surfaceChanged(" + mSurface + ", "  + width
                  + ", " + height + ")");
          Log.v(TAG, "surfaceChanged() mMediaPlayer=" + mMediaPlayer
                  + ", mTargetState=" + mTargetState + ", mVideoWidth="
                  + mVideoWidth + ", mVideoHeight=" + mVideoHeight);
          mSurfaceWidth = width;
          mSurfaceHeight = height;
          final boolean isValidState = (mTargetState == STATE_PLAYING);
          final boolean hasValidSize = (mVideoWidth == width && mVideoHeight == height);
          if (mMediaPlayer != null && isValidState && hasValidSize
                  && mCurrentState != STATE_PLAYING) {
              if (mSeekWhenPrepared != 0) {
                  seekTo(mSeekWhenPrepared);
              }
              Log.v(TAG, "surfaceChanged() start()");
              start();
          }
      }

      @Override
      public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
          mSurface = new Surface(surface);
          openVideo();
          
      }
      
      @Override
      public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
          // after we return from this we can't use the surface any more
          // after we return from this we can't use the surface any more
          Log.v(TAG, "surfaceDestroyed(" + mSurface + ")");
          if (mMultiWindowListener != null) {
              mMultiWindowListener.onSurfaceDestroyed();
          }
          if (mSurface != null) {
              mSurface.release();
              mSurface = null;
          }
          release(true);
          return true;
      }
      @Override
      public void onSurfaceTextureUpdated(final SurfaceTexture surface) {
          // do nothing
      }
	
}
