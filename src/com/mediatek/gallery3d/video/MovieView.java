package com.mediatek.gallery3d.video;


import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.IBinder;
import android.os.Looper;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.media.SubtitleTrack.RenderingWidget;
import android.media.SubtitleController;


public interface MovieView extends SubtitleController.Anchor{

	
    /**
     * The SurfaceCallback will be invoked in SurfaceHolder.Callback, It's used
     * to interaction with MoviePlayerWrapper, and implemented in
     * MoviePlayerWrapper
     */
    public interface SurfaceCallback {
        public void onSurfaceCreated(SurfaceHolder holder);

        public void onSurfaceChanged(SurfaceHolder holder, int format,
                int width, int height);

        public void onSurfaceDestroyed(SurfaceHolder holder);
        

    }
    
    public interface TextureSurfaceCallback {
	    public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
				int width, int height);
	
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface);
	
		public void onSurfaceTextureAvailable(SurfaceTexture surface,
				int width, int height);
	
		void onSurfaceTextureUpdated(SurfaceTexture surface);
    }

	public abstract void setVideoLayout(int videoWidth, int videoHeight);

	public abstract void setSurfaceListener(SurfaceCallback surfaceListener);
	public abstract void setTextureSurfaceListener(TextureSurfaceCallback surfaceListener);
	
	/**
	 * for view
	 */
	void setVisibility(int v);
	
	boolean removeCallbacks(Runnable r);
	
	boolean postDelayed(Runnable r, long l);
	
	IBinder getWindowToken();
	
	//textureview
	Bitmap getBitmap();
	
	SurfaceTexture getSurfaceTexture();
	
	//surfaceview
	
	//subtitle
	void setSubtitleWidget(RenderingWidget subtitleWidget);
	
	Looper getSubtitleLooper() ;
}