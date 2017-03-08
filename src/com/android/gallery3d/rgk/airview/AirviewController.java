package com.android.gallery3d.rgk.airview;

import android.content.Context;
import android.view.View;

import com.android.gallery3d.rgk.airview.layout.PopupLayout;
import com.android.gallery3d.ui.GestureRecognizer;

public class AirviewController  {
    
    
    
    private Context mContext;
    private PopupLayout mVideoRoot;
	private RgkMediaPlayerWrapper mMediaPlayer;

    public AirviewController (Context context, PopupLayout videoroot, RgkMediaPlayerWrapper mediaPlayer) {
        mContext = context;
        mMediaPlayer = mediaPlayer;
        mVideoRoot = videoroot;
        mMediaPlayer.setAirviewController(this);
    }
    
    public void setViewSize(int w , int h) {
        mVideoRoot.setViewSize(w, h);
    }

}
