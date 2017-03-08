package com.android.gallery3d.voice;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.VectorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.android.gallery3d.R;

public class CircleProgressBar extends View {
	private RectF mColorWheelRectangle = new RectF();
	private RectF mHighlightWheelRect = new RectF();
	private Paint mHighlightProgressPaint;
	private Paint mNormalProgressPaint;
	private Paint mBallPaint;

	private float mProgressStrokeWidth;

	private int mNormalProgressColor;
	private int mHighlightColor;
	private VectorDrawable mMiddleDrawable;

	private int mIconWidth;
	private int mIconHeight;
	private int padding = 26;
	private int shaderRadius = 2;
	private int mHalfStroke;

	private int mProgress;
	private long mDuration;

	private ValueAnimator mValueAnimator;

	private boolean mInAnimator = false;

	public CircleProgressBar(Context context) {
		this(context, null);
	}

	public CircleProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CircleProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
				R.styleable.CircleProgressBar);
		mNormalProgressColor = mTypedArray
				.getColor(R.styleable.CircleProgressBar_normal_progress_color,
						0xFFFFFFFF);
		mHighlightColor = mTypedArray.getColor(
				R.styleable.CircleProgressBar_highlight_progress_color,
				0xFFFF7A20);
		mProgressStrokeWidth = mTypedArray.getDimensionPixelSize(
				R.styleable.CircleProgressBar_progress_width,
				dip2px(context, 5));
		mIconWidth = mTypedArray.getDimensionPixelSize(
				R.styleable.CircleProgressBar_middle_icon_width,
				dip2px(context, 50));
		mIconHeight = mTypedArray.getDimensionPixelSize(
				R.styleable.CircleProgressBar_middle_icon_height,
				dip2px(context, 50));
		mMiddleDrawable = (VectorDrawable) mTypedArray
				.getDrawable(R.styleable.CircleProgressBar_middle_icon);

		mTypedArray.recycle();

		init();
	}

	@SuppressLint("NewApi")
	private void init() {
		mHighlightProgressPaint = new Paint();
		mHighlightProgressPaint.setAntiAlias(true);
		mHighlightProgressPaint.setColor(mHighlightColor);
		mHighlightProgressPaint.setStyle(Paint.Style.STROKE);
		mHighlightProgressPaint.setStrokeWidth(mProgressStrokeWidth);

		mNormalProgressPaint = new Paint();
		mNormalProgressPaint.setAntiAlias(true);
		mNormalProgressPaint.setColor(mNormalProgressColor);
		mNormalProgressPaint.setStyle(Paint.Style.STROKE);
		mNormalProgressPaint.setStrokeWidth(mProgressStrokeWidth);
		mNormalProgressPaint.setShadowLayer(shaderRadius, 0, 0, 0xFFA3A3A3);

		mBallPaint = new Paint();
		mBallPaint.setAntiAlias(true);
		mBallPaint.setColor(mHighlightColor);
		mBallPaint.setStyle(Paint.Style.FILL);

		mValueAnimator = ValueAnimator.ofFloat(0, 1);
		// mValueAnimator.setDuration(300);
		mValueAnimator.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				mInAnimator = false;
				mProgress = 0;
				invalidate(); // to re draw ui
			}
		});
		mValueAnimator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator anim) {
				float fraction = (Float) anim.getAnimatedValue();
				mProgress = (int) (360 * fraction);
				invalidate(); // to re draw ui
			}
		});

		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int w = getWidth();
		int h = getHeight();

		drawMiddleIcon(canvas, w, h);
		drawProgress(canvas, w, h);
		drawBall(canvas, w, h);
	}

	private void drawBall(Canvas canvas, int w, int h) {
		if (mProgress == 0) {
			return;
		}

		canvas.save();
		canvas.translate(w >> 1, h >> 1);
		canvas.rotate(mProgress - 90);
		canvas.drawCircle((w >> 1) - mHalfStroke, 0, mHalfStroke, mBallPaint);
		canvas.restore();
	}

	private void drawProgress(Canvas canvas, int w, int h) {
		Log.d("sqm", "CircleProgressBar[drawProgress]:---------mProgress="
				+ mProgress);

		canvas.drawArc(mColorWheelRectangle, -90, 360, false,
				mNormalProgressPaint);
		canvas.drawArc(mHighlightWheelRect, -90, mProgress, false,
				mHighlightProgressPaint);
	}

	private void drawMiddleIcon(Canvas canvas, int w, int h) {
		mMiddleDrawable.draw(canvas);
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width;
		int height;

		int dw = mMiddleDrawable.getIntrinsicWidth();
		int dh = mMiddleDrawable.getIntrinsicHeight();
		int iconSize = dw < dh ? dh : dw;

		if (widthMode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else {
			width = iconSize + (int) (mProgressStrokeWidth * 2) + padding * 2
					+ shaderRadius;
		}

		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else {
			height = iconSize + (int) (mProgressStrokeWidth * 2) + padding * 2
					+ shaderRadius;
		}

		Rect dst = new Rect();
		dst.left = (width - dw) >> 1;
		dst.top = (height - dh) >> 1;
		dst.right = dst.left + dw;
		dst.bottom = dst.top + dh;
		mMiddleDrawable.setBounds(dst);

		int halfStroke = mHalfStroke = ((int) mProgressStrokeWidth >> 1)
				+ shaderRadius;
		mHighlightWheelRect.left = halfStroke;
		mHighlightWheelRect.right = width - halfStroke;
		mHighlightWheelRect.top = halfStroke;
		mHighlightWheelRect.bottom = height - halfStroke;

		halfStroke += 2;
		mColorWheelRectangle.left = halfStroke;
		mColorWheelRectangle.right = width - halfStroke;
		mColorWheelRectangle.top = halfStroke;
		mColorWheelRectangle.bottom = height - halfStroke;

		setMeasuredDimension(width, height);
	}

	public void startCustomAnimation() {
		mInAnimator = true;
		mValueAnimator.setDuration(mDuration);
		mValueAnimator.start();
	}

	public void setDuration(long duration) {
		mDuration = duration;
	}

	public void cancelAnimation() {
		if (mInAnimator) {
			mValueAnimator.cancel();
			mValueAnimator.end();
		}
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

}
