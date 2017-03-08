

package com.android.gallery3d.rgk.airview.layout;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;


public class PopupLayout extends RelativeLayout implements ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener {
    private static final String TAG = "VLC/PopupView";

    private WindowManager mWindowManager;
    private GestureDetectorCompat mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private double mScaleFactor = 1.d;
    private int mPopupWidth, mPopupHeight;
    private int mScreenWidth, mScreenHeight;

    private int initialX, initialY;
    private float initialTouchX, initialTouchY;
    WindowManager.LayoutParams mLayoutParams;

    public PopupLayout(Context context) {
        super(context);
        init(context);
    }

    public PopupLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PopupLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /*
     * Remove layout from window manager
     */
    public void close() {
        setKeepScreenOn(false);
        if (mWindowManager != null)
        mWindowManager.removeView(this);
        mWindowManager = null;
    }

    public void setGestureDetector(GestureDetectorCompat gdc) {
        mGestureDetector = gdc;
    }

    /*
     * Update layout dimensions and apply layout params to window manager
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void setViewSize(int width, int height) {
        if (width > mScreenWidth) {
            height = height * mScreenWidth / width;
            width = mScreenWidth;
        }
        if (height > mScreenHeight){
            width = width * mScreenHeight / height;
            height = mScreenHeight;
        }
        containInScreen(width, height);
        mLayoutParams.width = width;
        mLayoutParams.height = height;
        mWindowManager.updateViewLayout(this, mLayoutParams);
    }

    @SuppressWarnings("deprecation")
    private void init(Context context) {
        mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
               400,
                240,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.OPAQUE);

        params.gravity = Gravity.BOTTOM | Gravity.START;
        params.x = 50;
        params.y = 50;
            mScaleGestureDetector = new ScaleGestureDetector(context, this);
        setOnTouchListener(this);
        mWindowManager.addView(this, params);
        mLayoutParams = (WindowManager.LayoutParams)getLayoutParams();

        updateWindowSize();
    }

    private void updateWindowSize() {
            Point size = new Point();
            mWindowManager.getDefaultDisplay().getSize(size);
            mScreenWidth = size.x;
            mScreenHeight = size.y;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mWindowManager == null)
            return false;
        if (mScaleGestureDetector != null)
            mScaleGestureDetector.onTouchEvent(event);
        if (mGestureDetector != null && mGestureDetector.onTouchEvent(event))
            return true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = mLayoutParams.x;
                initialY = mLayoutParams.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                updateWindowSize();
                return true;
            case MotionEvent.ACTION_UP:
                return true;
            case MotionEvent.ACTION_MOVE:
                if (mScaleGestureDetector == null || !mScaleGestureDetector.isInProgress()) {
                    mLayoutParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                    mLayoutParams.y = initialY - (int) (event.getRawY() - initialTouchY);
                    containInScreen(mLayoutParams.width, mLayoutParams.height);
                    mWindowManager.updateViewLayout(PopupLayout.this, mLayoutParams);
                    return true;
                }
        }
        return false;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        mScaleFactor *= detector.getScaleFactor();

        mScaleFactor = Math.max(0.1d, Math.min(mScaleFactor, 5.0d));
        mPopupWidth = (int) (getWidth()*mScaleFactor);
        mPopupHeight = (int) (getHeight()*mScaleFactor);
        return true;
    }
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) getLayoutParams();
        lp.width *= mScaleFactor;
        lp.height *= mScaleFactor;
        setViewSize(mPopupWidth, mPopupHeight);
        mScaleFactor = 1.0d;
    }

    private void containInScreen(int width, int height) {
        mLayoutParams.x = Math.max(mLayoutParams.x, 0);
        mLayoutParams.y = Math.max(mLayoutParams.y, 0);
        if (mLayoutParams.x + width > mScreenWidth)
            mLayoutParams.x = mScreenWidth - width;
        if (mLayoutParams.y + height > mScreenHeight)
            mLayoutParams.y = mScreenHeight - height;
    }
}
