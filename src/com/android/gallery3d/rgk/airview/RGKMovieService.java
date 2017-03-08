package com.android.gallery3d.rgk.airview;

import java.io.IOException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.support.v4.view.GestureDetectorCompat;

import com.mediatek.gallery3d.video.MediaPlayerWrapper;
import com.mediatek.gallery3d.video.MovieView;
import com.android.gallery3d.R;
import com.android.gallery3d.app.MovieActivity;
import com.android.gallery3d.rgk.airview.layout.PopupLayout;


public class RGKMovieService extends Service implements OnClickListener ,GestureDetector.OnDoubleTapListener,
GestureDetector.OnGestureListener{
    
    
    private Uri uri;
    private int position;
    public Boolean isremove = true;
    private PopupLayout mRootView;
    private ImageView close_button,play_button,mExpandButton;
    
    private SeekBar seek_position;
    
    private Boolean isPlay = false;
    private final int MEDIA_READY = 0;
    
    private Boolean isReady = false;
    
    private ServiceConnection conn;
    

    private Boolean isshow = false;
    
    
    private Runnable positionListener = new Runnable() {
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            position = vv.getCurrentPosition();
            seek_position.setProgress(position);
            mPHandler.postDelayed(positionListener, 500);
        }
    };
    
    
    private Handler mPHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case MEDIA_READY:
                seek_position.setMax(vv.getDuration());
                mPHandler.removeCallbacks(positionListener);
                vv.seekTo(position);
                mPHandler.postDelayed(positionListener, 500);
                break;

            default:
                break;
            }
        };
    };
    
	public void dothingp() {

		isReady = true;
		mPHandler.sendEmptyMessage(MEDIA_READY);
	}

	public void dothinge() {
		remove();
		if (conn != null) {
			getApplicationContext().unbindService(conn);
			conn = null;
		}
		stopSelf();
	}

	public void dothingc() {
		remove();
		if (conn != null) {
			getApplicationContext().unbindService(conn);
			conn = null;
		}
		stopSelf();

	}
    
    private static final int FLING_STOP_VELOCITY = 3000;
    private static final int MSG_DELAY = 3000;
    private static final int SHOW_BUTTONS = 0;
    private static final int HIDE_BUTTONS = 1;
    protected static final String TAG = "RGKMovieService";
    
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_BUTTONS:
                    play_button.setVisibility(View.VISIBLE);
                    close_button.setVisibility(View.VISIBLE);
                    mExpandButton.setVisibility(View.VISIBLE);
                    seek_position.setVisibility(View.VISIBLE);
                    
                    break;
                case HIDE_BUTTONS:
                    play_button.setVisibility(View.GONE);
                    close_button.setVisibility(View.GONE);
                    mExpandButton.setVisibility(View.GONE);
                    seek_position.setVisibility(View.GONE);
                    break;
            }
        }
    };
    
      @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (play_button.getVisibility() == View.VISIBLE)
                return false;
            mHandler.sendEmptyMessage(SHOW_BUTTONS);
            mHandler.sendEmptyMessageDelayed(HIDE_BUTTONS, MSG_DELAY);
            return true;
        }
      
        @Override
        public boolean onDoubleTap(MotionEvent e) {
//          mService.removePopup();
//          mService.switchToVideo();
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }


        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {}

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {}

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//          if (Math.abs(velocityX) > FLING_STOP_VELOCITY || velocityY > FLING_STOP_VELOCITY) {
//              stopPlayback();
//              return true;
//          }
            return false;
        }
    


    @Override
    public IBinder onBind(Intent arg0) {
        return new MsgBinder(this);
    }
    
    public void setServiceConnection(ServiceConnection conn){
        this.conn = conn;
    }
    public void setUri(Uri uri){
        if(!isremove){
            isReady = false;
            vv.setVideoURI(uri);
        }
        this.uri = uri;

    }
    

    public Uri getUri(){
        return uri;
    }
    
    public void setPosition(int position){
        if(!isremove){
            if(isReady){
                vv.seekTo(position);
            }
        }
        this.position = position;
    }
    
    public int getPosition(){
        return position;    
    }
    
    
    public void play(){

        vv.start();
        play_button.setImageResource(R.drawable.ic_popup_pause);
        isPlay = true;
        changeAudioFocus(true);
    }
    
    
    
    public void pause(){
        vv.pause();
        play_button.setImageResource(R.drawable.ic_popup_play);
        isPlay = false;
        changeAudioFocus(false);
    }
    
    public void remove(){
        if(!isremove){
            mHandler.removeCallbacks(positionListener);
//          wm.removeView(mRootView);
            mRootView.close();
            isremove = true;
        }
        
    }
    
    public int getCurrent(){
        return vv.getCurrentPosition();
    }
    
    private Boolean isseek = false;
    private AirviewController mAirview;
    private boolean mHasAudioFocus = false;
    
    
    private int mSeekMovePosition;
    
    public void init(){
        Log.i("zhoulei_test", "init");
        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = (PopupLayout)inflater.inflate(R.layout.video_play, null);
        MovieView mv = (MovieView) mRootView.findViewById(R.id.video);
//        mMediaPlayerWrapper.mMovieView = mv;
//        mMediaPlayerWrapper.initialize();
//        mMediaPlayerWrapper.setVideoURI(uri2);
//        mMediaPlayerWrapper.setListener(this);
        vv = new RgkMediaPlayerWrapper(this, mv);
//        vv.setListener(this);
        seek_position = (SeekBar)mRootView.findViewById(R.id.seek);
     
        seek_position.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            
          

			@Override
            public void onStopTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub
                position = seek_position.getProgress();
                vv.seekTo(position);
                if(isPlay){
                    vv.start();
                }
                isseek = false;
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub
                if(isPlay){
                    vv.pause();
                }
                isseek = true;
            }
            
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                // TODO Auto-generated method stub
                if(isseek)
                vv.seekTo(arg1);
                
            }
        });
        
        GestureDetectorCompat gestureDetector = new GestureDetectorCompat(this, this);
        gestureDetector.setOnDoubleTapListener(this);
        mRootView.setGestureDetector(gestureDetector);
    
        
        
        close_button = (ImageView)mRootView.findViewById(R.id.popup_close);
        play_button = (ImageView)mRootView.findViewById(R.id.video_play_pause);
        mExpandButton = (ImageView)mRootView.findViewById(R.id.popup_expand);
        close_button.setOnClickListener(this);
        play_button.setOnClickListener(this);
        mExpandButton.setOnClickListener(this);
        
        mAirview = new AirviewController(this, mRootView, vv) ;
        vv.setAirviewController(mAirview);
       
        
        isremove = false;
    }
    
    
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        registerScreenOff();
    }
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        
        remove();
        changeAudioFocus(false);
        unregisterScreenOff();
        super.onDestroy();
    }
    public class MsgBinder extends Binder{
         private RGKMovieService rms;
         public MsgBinder(RGKMovieService rms){
             super();
             this.rms = rms;
         }
         
         public RGKMovieService getService(){
             return rms;
         }
         
         
    }
    
    private final OnAudioFocusChangeListener mAudioFocusListener 
                                                = createOnAudioFocusChangeListener();
            
    private OnAudioFocusChangeListener createOnAudioFocusChangeListener() {
        return new OnAudioFocusChangeListener() {
            private boolean mLossTransient = false;
            private int mLossTransientVolume = -1;
            private boolean wasPlaying = false;

            @Override
            public void onAudioFocusChange(int focusChange) {
                /*
                 * Pause playback during alerts and notifications
                 */
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_LOSS:
                        Log.i(TAG, "AUDIOFOCUS_LOSS");
                        // Pause playback
                        changeAudioFocus(false);
                        pause();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        Log.i(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                        // Pause playback
                        mLossTransient = true;
                        wasPlaying = vv.isPlaying();
                        if (wasPlaying) {
                            pause();
                            close();
                        }
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        Log.i(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                        // Lower the volume
                        if (vv.isPlaying()) {
                            pause();
                        }
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN:
                        Log.i(TAG, "AUDIOFOCUS_GAIN: " + mLossTransientVolume + ", " + mLossTransient);
                        // Resume playback
                        if (mLossTransientVolume != -1) {
//                            vv.setVolume(mLossTransientVolume);
                            mLossTransientVolume = -1;
                        } else if (mLossTransient) {
                            if (wasPlaying)
                                play();
                            mLossTransient = false;
                        }
                        break;
                }
            }
        };
    }

    private void changeAudioFocus(boolean acquire) {
        final AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        if (am == null)
            return;

        if (acquire) {
            if (!mHasAudioFocus ) {
                final int result = am.requestAudioFocus(mAudioFocusListener,
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    am.setParameters("bgm_state=true");
                    mHasAudioFocus = true;
                }
            }
        } else {
            if (mHasAudioFocus) {
                final int result = am.abandonAudioFocus(mAudioFocusListener);
                am.setParameters("bgm_state=false");
                mHasAudioFocus = false;
            }
        }
    }
    
    public void close(){
        remove();
        mPHandler.removeCallbacks(positionListener);
        if(conn != null){
            getApplicationContext().unbindService(conn);
            conn = null;
        }
        stopSelf();
    }
    private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            /// M:add receive shut down broadcast, stop video.
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())
                    || Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
                // Only stop video.
                pause();
            }
        }

    };
	private RgkMediaPlayerWrapper vv;

    private void registerScreenOff() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        /// M:add receive shut down broadcast
        filter.addAction(Intent.ACTION_SHUTDOWN);
        registerReceiver(mScreenOffReceiver, filter);
    }

    private void unregisterScreenOff() {
        unregisterReceiver(mScreenOffReceiver);
    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        switch(arg0.getId()){
            case R.id.popup_close:
                close();
                break;
            case R.id.video_play_pause:
                if(isPlay){
                    pause();
                }else{
                    play();
                }
                break;
            case R.id.popup_expand:
                Intent intent = new Intent(Intent.ACTION_VIEW);  
        Log.v("URI:::::::::", uri.toString());  
        intent.setDataAndType(uri, "video/*");
        intent.setClass(getApplicationContext(), MovieActivity.class);
        intent.putExtra("rgk_time_support", true);
                intent.putExtra("rgk_air_view_position", getPosition());
                intent.putExtra("rgk_air_view_duration", vv.getDuration());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
                break;
        }
    }


   
}
