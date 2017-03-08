package com.android.gallery3d.voice;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class VoicePlayer implements OnCompletionListener, OnPreparedListener {

	private volatile Looper mThreadLooper;
	private volatile ThreadHandler mThreadHandler;
	private static final int MSG_START_PLAYING_FILE = 1;
	private static final int MSG_START_PLAYING_BYTE = 2;
	private static final int MSG_STOP_PLAYING = 3;

	private MediaPlayer mPlayer = null;

	private VoiceListener mVoiceListener;

	private final class ThreadHandler extends Handler {
		public ThreadHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_START_PLAYING_FILE:
				startPlaying((AssetFileDescriptor) msg.obj);
				break;

			case MSG_START_PLAYING_BYTE:
				startPlaying((byte[]) msg.obj);
				break;

			case MSG_STOP_PLAYING:
				stopPlaying();
				break;
			}
		}
	}

	public VoicePlayer(Context context) {
		// mRecorder = new MediaRecorder();
		newHandlerThread();
	}

	public void destroyBackThread() {
		mThreadLooper.quit(); // Quit looper thread
	}

	public void start(AssetFileDescriptor afd) {
		Message msg = mThreadHandler.obtainMessage(MSG_START_PLAYING_FILE, afd);
		mThreadHandler.sendMessage(msg);
	}

	public void start(byte[] data) {
		Message msg = mThreadHandler
				.obtainMessage(MSG_START_PLAYING_BYTE, data);
		mThreadHandler.sendMessage(msg);
	}

	public void stop() {
		mThreadHandler.sendEmptyMessage(MSG_STOP_PLAYING);
	}

	public boolean isPlaying() {
		return mPlayer != null;
	}

	private void newHandlerThread() {
		HandlerThread thread = new HandlerThread("PlayerThread");
		thread.start();

		mThreadLooper = thread.getLooper();
		mThreadHandler = new ThreadHandler(mThreadLooper);
	}

	private void startPlaying(final AssetFileDescriptor afd) {
		mPlayer = new MediaPlayer();
		mPlayer.setOnCompletionListener(this);
		try {
			mPlayer.setDataSource(afd.getFileDescriptor(),
					afd.getStartOffset(), afd.getLength());
			afd.close();
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			Log.e("sqm", "VoicePlayer prepare() failed");
		}
	}

	private void startPlaying(byte[] data) {
		mPlayer = new MediaPlayer();
		mPlayer.setOnCompletionListener(this);
		mPlayer.setOnPreparedListener(this);
		try {
			mPlayer.setDataSource(new MyDataSource(data));
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			Log.e("sqm", "VoicePlayer prepare() failed");
		}
	}

	private void stopPlaying() {
		mPlayer.stop();
		mPlayer.release();
		mPlayer = null;
	}

	public void setVoiceListener(VoiceListener listener) {
		mVoiceListener = listener;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		if (mVoiceListener != null) {
			mVoiceListener.onGetDuration(mp.getDuration());
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		stopPlaying();
		if (mVoiceListener != null) {
			mVoiceListener.onComplete();
		}
	}

	class MyDataSource extends MediaDataSource {
		private byte[] mData;

		public MyDataSource(byte[] data) {
			mData = data;
		}

		@Override
		public void close() throws IOException {
			mData = null;
			Log.d("sqm", "VoicePlayer$MyDataSource[close]");
		}

		@Override
		public long getSize() throws IOException {
			Log.d("sqm", "VoicePlayer$MyDataSource[getSize]");
			return mData.length;
		}

		@Override
		public int readAt(long position, byte[] buffer, int offset, int size)
				throws IOException {
			Log.d("sqm", "VoicePlayer$MyDataSource[readAt]---position="
					+ position + ",buffer=" + buffer + ",offset=" + offset
					+ ",size=" + size);
			long totalSize = getSize();
			if (position >= totalSize) {
				return -1;
			} else if (totalSize <= position + size) {
				size = (int) (totalSize - position);
			}
			System.arraycopy(mData, (int) position, buffer, offset, size);

			return size;
		}

	}

	public interface VoiceListener {
		void onGetDuration(long mills);

		void onComplete();
	}

}
