package com.mediatek.galleryfeature.drm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;

import com.mediatek.galleryframework.util.MtkLog;

public class DeviceMonitor {
    private static final String TAG = "MtkGallery2/DeviceMonitor";

    private static final String ACTION_WFD = "com.mediatek.wfd.connection";
    private static final String ACTION_HDMI = Intent.ACTION_HDMI_PLUG;

    public static final int WFD_CONNECTED_FLAG = 1;
    public static final int WFD_DISCONNECTED_FLAG = 0;
    public static final String KEY_HDMI_ENABLE_STATUS = "hdmi_enable_status";

    public enum ConnectStatus {
        DISCONNECTED, WFD_CONNECTED, HDMI_CONNECTD
    }

    private Activity mActivity;
    private DevicePlugReceiver mReceiver;
    private ConnectStatus mConnectStatus = ConnectStatus.DISCONNECTED;
    private DeviceConnectListener mListener;

    public interface DeviceConnectListener {
        public void onDeviceConnected(ConnectStatus deviceConnected);
    }

    public DeviceMonitor(Activity activity) {
        assert (activity != null);
        mActivity = activity;
    }

    public void start() {
        registerReceiver();
    }

    public void setConnectListener(DeviceConnectListener listener) {
        mListener = listener;
    }

    public void stop() {
        unregisterReceiver();
    }

    public ConnectStatus getConnectedStatus() {
        return mConnectStatus;
    }

    private synchronized void registerReceiver() {
        if (mReceiver != null) {
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_WFD);
        filter.addAction(ACTION_HDMI);
        mReceiver = new DevicePlugReceiver();
        mActivity.registerReceiver(mReceiver, filter);
        MtkLog.i(TAG, "<registerReceiver> success");
    }

    private synchronized void unregisterReceiver() {
        if (mReceiver == null) {
            return;
        }
        mActivity.unregisterReceiver(mReceiver);
        mReceiver = null;
        MtkLog.i(TAG, "<unregisterReceiver> success");
    }

    private class DevicePlugReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectStatus status = ConnectStatus.DISCONNECTED;
            if (isWFDConnected(intent)) {
                status = ConnectStatus.WFD_CONNECTED;
            } else if (isHDMIConnected(intent)) {
                status = ConnectStatus.HDMI_CONNECTD;
            }
            MtkLog.i(TAG, "<onReceive> status = " + status + ", intent action = "
                    + intent.getAction());
            if (status != mConnectStatus) {
                mConnectStatus = status;
                if (mListener != null) {
                    mListener.onDeviceConnected(mConnectStatus);
                }
            }
        }

        private boolean isWFDConnected(Intent intent) {
            String action = intent.getAction();
            if (action == ACTION_WFD) {
                int extraResult = intent.getIntExtra("connected", 0);
                int secure = intent.getIntExtra("secure", 0);
                if (extraResult == WFD_CONNECTED_FLAG && secure == 0) {
                    return true;
                }
            }
            return false;
        }

        private boolean isHDMIConnected(Intent intent) {
            String action = intent.getAction();
            int hdmiCableState = intent.getIntExtra("state", 0);
            return ACTION_HDMI.equals(action)
                    && (hdmiCableState == 1)
                    && (Settings.System.getInt(mActivity.getContentResolver(),
                            KEY_HDMI_ENABLE_STATUS, 1) == 1);
        }
    }
}
