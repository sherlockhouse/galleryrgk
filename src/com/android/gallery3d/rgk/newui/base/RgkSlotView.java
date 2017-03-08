package com.android.gallery3d.rgk.newui.base;

import android.util.Log;
import android.view.MotionEvent;

import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.ui.SlotView;

public class RgkSlotView extends SlotView {

	public RgkSlotView(AbstractGalleryActivity activity, Spec spec) {
		super(activity, spec);
	}

	public RgkSlotView(AbstractGalleryActivity activity, Spec spec, boolean b) {
		super(activity, spec, b);
	}

	private int mLastDraggedIndex;

	private int mInitialSeletion;
	private boolean mDragSelectActive;

	private int mMinReached;

	private int mMaxReached;

	public boolean setmDragSelectActive(boolean active, int initialSelection) {
		if (active && mDragSelectActive) {
			Log.i("jzbg", "already active  " + "index:" + initialSelection);
			return false;
		}

		mLastDraggedIndex = -1;
		mMinReached = -1;
		mMaxReached = -1;
		this.mDragSelectActive = active;
		mInitialSeletion = initialSelection;
		mLastDraggedIndex = initialSelection;
		return true;

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent e) {

		if (mDragSelectActive) {

			final int itemPosition = mLayout.getSlotIndexByPosition(e.getX(),
					e.getY());
			if (e.getAction() == MotionEvent.ACTION_UP) {
				mDragSelectActive = false;

				return true;
			} else if (e.getAction() == MotionEvent.ACTION_MOVE) {

				if (itemPosition != -1 && mLastDraggedIndex != itemPosition) {
					mLastDraggedIndex = itemPosition;
					if (mMinReached == -1)
						mMinReached = mLastDraggedIndex;
					if (mMaxReached == -1)
						mMaxReached = mLastDraggedIndex;
					if (mLastDraggedIndex > mMaxReached)
						mMaxReached = mLastDraggedIndex;
					if (mLastDraggedIndex < mMinReached)
						mMinReached = mLastDraggedIndex;

					if (mListener != null) {
						Log.i("jzbg", "rangerange");
						((RgkListener)mListener).selectRange(mInitialSeletion,
								mLastDraggedIndex, mMinReached, mMaxReached);
					}
					if (mInitialSeletion == mLastDraggedIndex) {
						mMinReached = mLastDraggedIndex;
						mMaxReached = mLastDraggedIndex;
					}
				}
				return true;
			}
		}

		return super.dispatchTouchEvent(e);
	}
	
	
	public interface RgkListener extends Listener {
		void selectRange(int initialSelection, int lastDraggedIndex,
				int minReached, int maxReached);
	}
	
	public static class RgkSimpleListener extends SimpleListener implements RgkListener {
		@Override
		public void selectRange(int initialSelection, int lastDraggedIndex,
				int minReached, int maxReached) {};
		
	}

}
