package com.android.gallery3d.rgk.newui.multiselect;

import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.glrenderer.ResourceTexture;
import com.android.gallery3d.ui.AlbumSlotRenderer;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.SlotView;

import com.android.gallery3d.R;

public class MSelectSlotRender extends AlbumSlotRenderer {

    private final ResourceTexture mSelectedIcon;
    
	public MSelectSlotRender(AbstractGalleryActivity context,
			SlotView slotView, SelectionManager selectionManager,
			int placeholderColor) {
		super(context, slotView, selectionManager, placeholderColor);
	       mSelectedIcon = new ResourceTexture(context, R.drawable.ic_check_circle_raised_color_24dp);
	}
	
	@Override
    protected void drawSelectedFrame(GLCanvas canvas, int width, int height) {
		super.drawSelectedFrame(canvas, width, height);
        int iconSize = Math.min(width, height) / 4;
        mSelectedIcon.draw(canvas, (width - iconSize) / 8, (height - iconSize) / 8,
                iconSize, iconSize);
    }

}
