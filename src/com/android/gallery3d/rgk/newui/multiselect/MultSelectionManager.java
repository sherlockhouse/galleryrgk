package com.android.gallery3d.rgk.newui.multiselect;


import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.SelectionManager;

public class MultSelectionManager extends SelectionManager {

	public MultSelectionManager(AbstractGalleryActivity activity,
			boolean isAlbumSet) {
		super(activity, isAlbumSet);
	}

    public void setPathSelected(Path path, boolean selected) {
        if (selected) {
            if (!mClickedSet.contains(path)) {
                 enterSelectionMode();
                mClickedSet.add(path);
            }
        } else if (mClickedSet.contains(path)) {
            mClickedSet.remove(path);
        }
        if (mListener != null) mListener.onSelectionChange(path, isItemSelected(path));

    }
}
