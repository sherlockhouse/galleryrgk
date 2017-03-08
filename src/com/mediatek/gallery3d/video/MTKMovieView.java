
package com.mediatek.gallery3d.video;

import com.android.gallery3d.ui.Log;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.media.SubtitleController;
import android.media.SubtitleTrack.RenderingWidget;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MTKMovieView extends SurfaceView implements  MovieView {

    private static final String TAG = "Gallery2/VideoPlayer/MovieView";
    private static final boolean LOG = true;

    private int mVideoWidth;
    private int mVideoHeight;
    private SurfaceCallback mSurfaceListener;

    /** Subtitle rendering widget overlaid on top of the video. */
    private RenderingWidget mSubtitleWidget;
    /** Listener for changes to subtitle data, used to redraw when needed. */
    private RenderingWidget.OnChangedListener mSubtitlesChangedListener;

    private SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mSurfaceListener != null) {
                mSurfaceListener.onSurfaceDestroyed(holder);
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (mSurfaceListener != null) {
                mSurfaceListener.onSurfaceCreated(holder);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                int height) {
            if (mSurfaceListener != null) {
                mSurfaceListener
                        .onSurfaceChanged(holder, format, width, height);
            }
        }
    };

    public MTKMovieView(Context context) {
        this(context, null, 0);
    }

    public MTKMovieView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MTKMovieView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView();
    }

    private void initVideoView() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        getHolder().addCallback(mSHCallback);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    /* (non-Javadoc)
	 * @see com.mediatek.gallery3d.video.MovieView#setVideoLayout(int, int)
	 */
    @Override
	public void setVideoLayout(int videoWidth, int videoHeight) {
        Log.v(TAG, "setVideoLayout, videoWidth = " + videoWidth
                + ", videoHeight = " + videoHeight);
        this.mVideoWidth = videoWidth;
        this.mVideoHeight = videoHeight;
        requestLayout();
    }

    /* (non-Javadoc)
	 * @see com.mediatek.gallery3d.video.MovieView#setSurfaceListener(com.mediatek.gallery3d.video.MTKMovieView.SurfaceCallback)
	 */
    @Override
	public void setSurfaceListener(SurfaceCallback surfaceListener) {
        this.mSurfaceListener = surfaceListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);

        if (mVideoWidth > 0 && mVideoHeight > 0) {
            if (mVideoWidth * height > width * mVideoHeight) {
                // Log.i("@@@", "image too tall, correcting");
                height = width * mVideoHeight / mVideoWidth;
            } else if (mVideoWidth * height < width * mVideoHeight) {
                // Log.i("@@@", "image too wide, correcting");
                width = height * mVideoWidth / mVideoHeight;
            }
        }
        if (LOG) {
            Log.v(TAG, "onMeasure[video size = " + mVideoWidth + 'x' + mVideoHeight
                    + "] set view size = " + width + 'x' + height);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mSubtitleWidget != null) {
            measureAndLayoutSubtitleWidget();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (mSubtitleWidget != null) {
            final int saveCount = canvas.save();
            canvas.translate(getPaddingLeft(), getPaddingTop());
            mSubtitleWidget.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mSubtitleWidget != null) {
            mSubtitleWidget.onAttachedToWindow();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mSubtitleWidget != null) {
            mSubtitleWidget.onDetachedFromWindow();
        }
    }

    /**
     * Forces a measurement and layout pass for all overlaid views.
     *
     * @see #setSubtitleWidget(RenderingWidget)
     */
    private void measureAndLayoutSubtitleWidget() {
        final int width = getWidth() - getPaddingLeft() - getPaddingRight();
        final int height = getHeight() - getPaddingTop() - getPaddingBottom();

        mSubtitleWidget.setSize(width, height);
    }

	@Override
    public void setSubtitleWidget(RenderingWidget subtitleWidget) {
        if (mSubtitleWidget == subtitleWidget) {
            return;
        }

        final boolean attachedToWindow = isAttachedToWindow();
        if (mSubtitleWidget != null) {
            if (attachedToWindow) {
                mSubtitleWidget.onDetachedFromWindow();
            }

            mSubtitleWidget.setOnChangedListener(null);
        }

        mSubtitleWidget = subtitleWidget;

        if (subtitleWidget != null) {
            if (mSubtitlesChangedListener == null) {
                mSubtitlesChangedListener = new RenderingWidget.OnChangedListener() {
                    @Override
                    public void onChanged(RenderingWidget renderingWidget) {
                        invalidate();
                    }
                };
            }

            setWillNotDraw(false);
            subtitleWidget.setOnChangedListener(mSubtitlesChangedListener);

            if (attachedToWindow) {
                subtitleWidget.onAttachedToWindow();
                requestLayout();
            }
        } else {
            setWillNotDraw(true);
        }

        invalidate();
    }

    /* (non-Javadoc)
	 * @see com.mediatek.gallery3d.video.MovieView#getSubtitleLooper()
	 */
	@Override
    public Looper getSubtitleLooper() {
        return Looper.getMainLooper();
    }

	@Override
	public Bitmap getBitmap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SurfaceTexture getSurfaceTexture() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTextureSurfaceListener(TextureSurfaceCallback surfaceListener) {
		
	}


}
