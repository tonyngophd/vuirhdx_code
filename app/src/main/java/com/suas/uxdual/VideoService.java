package com.suas.uxdual;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class VideoService extends Service {
    private static final String ACTION_USB_PERMISSION = "com.suas.vuirinsight.USB_PERMISSION";
    public static final int CODEC_STATE_IDLE = 0;
    public static final int CODEC_STATE_SHOWLOG = 1;
    public static final int CODEC_STATE_SHOWVIDEO = 2;
    public static final int CREATE_MEDIA_SESSION = 2;
    public static final int RELEASE_MEDIA_SESSION = 3;
    public static final String SERVERIP_SAVEKEY = "serverip";
    private static final String TAG = ("wenjie traffic" + VideoService.class.getSimpleName());
    public static final int UNRECOVERABLE_ERROR = 1;
    private Context mContext;
    private VideoServiceImpl mVideoServiceImpl;
    private Handler mHandler;
    private IBinder mBinder = new Mybinder();

    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        mHandler = new Handler();
        this.mVideoServiceImpl = new VideoServiceImpl(getApplicationContext());
        Log.d(TAG, "traffic service Link Initialized");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        int f = super.onStartCommand(intent, flags, startId);
        return Service.MODE_PRIVATE;//0
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind " + intent.getAction());
        return this.mVideoServiceImpl.asBinder();
    }

    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind " + intent.getAction());
        return super.onUnbind(intent);
    }

    public Handler getHandler() {
        return null;
    }

    public class Mybinder extends Binder{
        VideoService getService(){
            return VideoService.this;
        }
    }
}

