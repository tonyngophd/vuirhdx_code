package com.suas.uxdual;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import com.suas.uxdual.IVedeoInterface.Stub;
import com.taihang.insight.JniBridge;

import java.io.IOException;
import java.nio.ByteBuffer;

/*import com.taihang.insight.JniBridge;
import com.taihang.insight.JniBridge.networkInteface;*/

public class VideoServiceImpl extends Stub implements JniBridge.networkInteface {
    private final String TAG = VideoServiceImpl.class.getName();
    private final int TCPLINK = 1;
    private final int UDPLINK = 0;
    HandlerThread handlerThread;
    private ByteBuffer[] inputBuffers;
    private ByteBuffer[] outputBuffers;
    private Boolean mCodecIsOpen = new Boolean(false);
    private int mCodecStatus;
    private int mConnectType;
    int mConnection = 0;
    boolean mContinue = true;
    private Context mCtxt;
    private int mHeight;
    private boolean mInConnection = false;
    private JniBridge mJni = new JniBridge(this);
    private MediaCodec mMediaCodec;
    private String mServerIp;
    boolean mShowChannel34 = false;
    boolean mSurfaceDestroied = true;
    private String mToken = "";
    boolean mVisiuable = true;
    private int mWidth;
    MyHandler myHandler;
    private boolean printformat = true;
    private long frameNo = 0;

    class MyHandler extends Handler {
        public static final int message_pause = 0;
        public static final int message_resume = 1;
        public static final int message_sendpwm = 7;
        public static final int message_setresolution = 2;
        public static final int message_showlogo = 6;
        public static final int message_startlink = 4;
        public static final int message_startvideo = 3;
        public static final int message_stop = 5;

        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            String token;
            Bundle bd;
            Bundle bd1;
            switch (msg.what) {
                case 0:
                    token = msg.getData().getString("token");
                    if (token.contentEquals(VideoServiceImpl.this.mToken)) {
                        VideoServiceImpl.this._Pause(token);
                        VideoServiceImpl.this.mToken = "";
                        return;
                    }
                    return;
                case 1:
                    bd = msg.getData();
                    int w = bd.getInt("w");
                    int h = bd.getInt("h");
                    Surface sf = (Surface) bd.getParcelable("sf");
                    token = bd.getString("token");
                    VideoServiceImpl.this._Resume(w, h, bd.getInt("type"), token, sf);
                    return;
                case 4:
                    bd = msg.getData();
                    VideoServiceImpl.this._startLink(bd.getString("ip"), bd.getInt("type"));
                    return;
                case 5:
                    VideoServiceImpl.this._Stop();
                    break;
                case 6:
                    bd1 = msg.getData();
                    Surface sf1 = (Surface) bd1.getParcelable("sf");
                    //VideoServiceImpl.this._ShowLogo(bd1.getInt("w"), bd1.getInt("h"), bd1.getString("token"), sf1);
                    return;
                case 7:
                    bd1 = msg.getData();
                    VideoServiceImpl.this._sendpwm(bd1.getInt("pwmid"), bd1.getInt("pwmvalue"));
                    return;
                case 11:
                    bd1 = msg.getData();
                    VideoServiceImpl.this._SendG2AMessage1(bd1.getString(""));
                    return;
                case 12:
                    bd1 = msg.getData();
                    VideoServiceImpl.this._SendG2AMessage2(bd1.getInt("value"), bd1.getInt("channel"));
                    return;
                default:
                    return;
            }
        }
    }

    private void opencodec() {
        synchronized (this.mCodecIsOpen) {
            this.mMediaCodec.start();
            this.mCodecIsOpen = Boolean.valueOf(true);
        }
    }

    private void closecodec() {
        synchronized (this.mCodecIsOpen) {
            this.mCodecIsOpen = Boolean.valueOf(false);
            this.mMediaCodec.stop();
        }
    }

    public VideoServiceImpl(Context ctx) {
        this.mCtxt = ctx;
        InitMediaCodec();
        launchworkthread();

        this.mServerIp = ctx.getSharedPreferences(VideoService.SERVERIP_SAVEKEY, 0).getString(VideoService.SERVERIP_SAVEKEY, "192.168.2.220");
        this.handlerThread = new HandlerThread("handlerThread");
        this.handlerThread.start();
        this.myHandler = new MyHandler(this.handlerThread.getLooper());
    }

    public int onData(int idx, int len, long timestamp, int flag) {
        Log.d(this.TAG, "Java ondata is called. use buffer " + idx + " " + len + "bytes");
        try {
            synchronized (this.mCodecIsOpen) {
                if (this.mCodecIsOpen.booleanValue()) {
                    this.mMediaCodec.queueInputBuffer(idx, 0, len, timestamp, flag);
                }
            }
        } catch (Exception e) {
            Log.e(this.TAG, "onData got exception , but ignor " + e.toString());
        }
        return 0;
    }

    public int onMetaChanged(int width, int hight, int fps) {
        return 0;
    }

    public void setServerIp(String sip) {
        ConnectServer.getInstance().SetServerIp(sip);
        this.mServerIp = sip;
    }

    public void StartVideo(int type) {
        if (this.mInConnection) {
            if (this.mConnectType != type) {
                this.mJni.native_sink_stop();
                try {
                    Thread.sleep(100, 1);
                } catch (Exception e) {
                    Log.d(this.TAG, "Thread sleep got exception " + e.toString());
                }
            } else {
                return;
            }
        }
        this.mJni.native_sink_init(this.mServerIp, type);
        this.mConnectType = type;
        this.mInConnection = true;
    }

    private void InitMediaCodec() {
        try {
            this.mMediaCodec = MediaCodec.createDecoderByType("video/avc");
        } catch (IOException ioe) {
            Log.e(this.TAG, "mediaCodec got exception " + ioe.toString());
        }
    }

    public void Pause(String token) {
        Message msg = this.myHandler.obtainMessage();
        msg.what = 0;
        msg.getData().putString("token", token);
        this.myHandler.sendMessage(msg);
    }

    private void _Pause(String token) {
        Log.d(this.TAG, "_Pause");
        if (this.mToken.length() < 1) {
            Log.d(this.TAG, "not started");
        } else if (this.mToken.contentEquals(token)) {
            if (this.mCodecStatus == 2) {
                this.mJni.native_sink_enqueDecoderBuffer(-1);
            }
            if (this.mCodecStatus != 0) {
                closecodec();
                this.mToken = "";
                this.mCodecStatus = 0;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void Resume(int w, int h, int type, String token, Surface sf) {
        Log.d(TAG, "Resume: in VideoServiceImpl");
        Message msg = this.myHandler.obtainMessage();
        msg.what = 1;
        Bundle bd = msg.getData();
        bd.putInt("w", w);
        bd.putInt("h", h);
        bd.putParcelable("sf", sf);
        bd.putString("token", token);
        bd.putInt("type", type);
        this.myHandler.sendMessage(msg);
    }

    private void _Resume(int w, int h, int type, String token, Surface sf) {
        Log.d(TAG, "_Resume: in VideoServiceImpl");
        if (token.contentEquals(this.mToken) && type == this.mCodecStatus) {
            Log.d(this.TAG, "this view has in working " + this.mToken + " " + token + " type : " + type);
        }
        if (this.mToken.length() > 0) {
            _Pause(this.mToken);
        }
        this.mToken = token;
        this.mMediaCodec.configure(MediaFormat.createVideoFormat("video/avc", w, h), sf, null, 0);
        Log.d(this.TAG, "codec config success");
        opencodec();
        Log.e(this.TAG, "MediaCodec started\n");
        this.inputBuffers = this.mMediaCodec.getInputBuffers();
        //this.outputBuffers = this.mMediaCodec.getOutputBuffers();
        Log.e(this.TAG, "MediaCodec getInputBuffers " + this.inputBuffers.length);
        if (type == 2) {
            for (int i = 0; i < this.inputBuffers.length; i++) {
                Log.e(this.TAG, "native_sink_registerInputBuffers " + i);
                this.inputBuffers[i].clear();
                this.mJni.native_sink_registerInputBuffers(this.inputBuffers[i], i);
            }
            this.mJni.native_sink_enqueDecoderBuffer(-2);
        }
        this.mCodecStatus = type;
        synchronized (this.mMediaCodec) {
            this.mMediaCodec.notifyAll();
            Log.d(this.TAG, "codec notify all");
        }
    }

    public void SetResolution(int w, int h) {
        ConnectServer.getInstance().setResolution(w, h);
    }
    public void Stop() {
        Message msg = this.myHandler.obtainMessage();
        msg.what = 5;
        this.myHandler.sendMessage(msg);
    }

    private void _Stop() {
        if (this.mCodecStatus != 1) {
            this.mJni.native_sink_stop();
        }
    }

    public void startLink(String ip, int type) {
        Message msg = this.myHandler.obtainMessage();
        msg.what = 4;
        Bundle bd = msg.getData();
        bd.putString("ip", ip);
        bd.putInt("type", type);
        this.myHandler.sendMessage(msg);
    }

    private void _startLink(String ip, int type) {
        this.mServerIp = ip;
        Log.d(this.TAG, "in startLink. " + ip + " type: " + (type == 0 ? "tcp" : "udp"));
        if (this.mInConnection && this.mConnectType == type) {
            Log.d(this.TAG, " connection " + type + " has setup. return immediatly");
            return;
        }
        this.mConnectType = type;
        if (this.mInConnection) {
            Log.d(this.TAG, "will stop native first");
            _Pause(this.mToken);
            this.mInConnection = true;
            this.mJni.native_sink_stop();
            this.mCodecStatus = 0;
        }
        this.mInConnection = true;
        this.mJni.native_sink_init(this.mServerIp, this.mConnectType);
    }

    private void launchworkthread() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    new Thread(new Runnable() {
                        public void run() {
                            VideoServiceImpl.this.netthread();
                        }
                    }).start();
                    new Thread(new Runnable() {
                        public void run() {
                            VideoServiceImpl.this.renderthread();
                        }
                    }).start();
                } catch (Exception e) {
                    Log.d(VideoServiceImpl.this.TAG, "surfaceChanged got exception " + e.toString());
                }
            }
        }).start();
    }

    private void netthread() {
        Log.e(this.TAG, "input thread is working\n");
        boolean codecnotexecuting = true;
        while (this.mContinue) {
            if (codecnotexecuting) {
                synchronized (this.mMediaCodec) {
                    try {
                        this.mMediaCodec.wait();
                        codecnotexecuting = false;
                    } catch (Exception e) {
                        return;
                    }
                }
            }
            try {
                int inputBufferId = this.mMediaCodec.dequeueInputBuffer(-1);
                //Log.d("netthread", " format " + this.mMediaCodec.getInputFormat());
                Log.d(this.TAG, "get a buffer id " + inputBufferId);
                if (!this.mContinue) {
                    break;
                } else if (inputBufferId < 0) {
                    Log.e(this.TAG, "deque an Inputbuffer failure");
                } else if (this.mCodecStatus == 1) {
                    /*byte[] logo = getLogo();
                    if (logo != null) {
                        this.inputBuffers[inputBufferId].clear();
                        this.inputBuffers[inputBufferId].put(logo);
                        this.mMediaCodec.queueInputBuffer(inputBufferId, 0, logo.length, 0, 0);
                    }*/
                } else {
                    Log.e(this.TAG, "deque an Inputbuffer " + inputBufferId);
                    this.mJni.native_sink_enqueDecoderBuffer(inputBufferId);
                    //this.mMediaCodec.queueInputBuffer(inputBufferId, 0, inputBuffers[inputBufferId].limit(), 0, 0);
                }
            } catch (IllegalStateException ls) {
                Log.e(this.TAG, "input codec is not in executing status" + ls.toString());
                codecnotexecuting = true;
            } catch (Exception e2) {
                Log.e(TAG, "netthread: exception e2");
            }
        }
    }

    private void renderthread() {
        Log.e(this.TAG, "renderthread thread is working\n");
        BufferInfo info = new BufferInfo();
        boolean codecnotexecuting = true;
        while (this.mContinue) {
            if (codecnotexecuting) {
                synchronized (this.mMediaCodec) {
                    try {
                        this.mMediaCodec.wait();
                        codecnotexecuting = false;
                    } catch (InterruptedException ie) {
                        Log.d(this.TAG, "renderthread GOT interrupt exception " + ie.toString());
                        return;
                    }
                }
            }
            try {
                int outIndex = this.mMediaCodec.dequeueOutputBuffer(info, 1000000);
                if (this.mContinue) {
                    switch (outIndex) {
                        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                            Log.d("renderthread", "INFO_OUTPUT_BUFFERS_CHANGED");
                            //this.outputBuffers = this.mMediaCodec.getOutputBuffers();
                            break;
                        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                            Log.d("renderthread", "New format " + this.mMediaCodec.getOutputFormat());
                            break;
                        case MediaCodec.INFO_TRY_AGAIN_LATER:
                            Log.d(TAG, "renderthread: index = -1");
                            break;
                        default:
                            //Log.d("renderthread", "got a frame");
                            this.mMediaCodec.releaseOutputBuffer(outIndex, true);

                            /* //This is trying to grab a video frame as an image, but it's not possible: video stream is being rendered to a surface, so
                            // it doesn't allow to get image!!!! https://stackoverflow.com/questions/18776776/outputbuffers-returns-null-pointer-although-the-frame-is-rendered-on-the-screen
                            frameNo++;
                            if(frameNo == 150) {
                                printformat = true;
                                Log.i(TAG, "renderthread: printformat = " + printformat);
                            }
                            else printformat = false;
                            if(printformat) {
                                Image image;
                                try {
                                    image = this.mMediaCodec.getOutputImage(outIndex);
                                    if(image != null) {
                                        Log.i(TAG, String.format("renderthread: imageformat = %s, width = %d, height = %d", image.getFormat(), image.getWidth(), image.getHeight()));
                                    } else {
                                        this.mMediaCodec.releaseOutputBuffer(outIndex, false);
                                        image = this.mMediaCodec.getOutputImage(outIndex);
                                        Log.i(TAG, "renderthread: image = " + image);
                                    }
                                } catch (IllegalStateException e){
                                    Log.e(TAG, "renderthread: ", e);
                                }
                                printformat = false;
                            } else{
                                this.mMediaCodec.releaseOutputBuffer(outIndex, true);
                            }*/
                            if (this.mCodecStatus != 1) {
                                //Log.d(TAG, "renderthread: mCodecStatus = " + mCodecStatus);
                                break;
                            }
                            codecnotexecuting = true;
                            break;
                    }
                }
            } catch (IllegalStateException ls) {
                Log.e(this.TAG, "renderthread decode thread: is not in executing status" + ls.toString());
                codecnotexecuting = true;
            } catch (Exception e) {
                Log.d(TAG, "renderthread: exception e");
            }
        }
    }

    public void ShowLogo(int w, int h, String token, Surface sf) {
        /*Message msg = this.myHandler.obtainMessage();
        msg.what = 6;
        Bundle bd = msg.getData();
        bd.putInt("w", w);
        bd.putInt("h", h);
        bd.putParcelable("sf", sf);
        bd.putString("token", token);
        this.myHandler.sendMessage(msg);*/
    }

    private void _ShowLogo(int w, int h, String token, Surface sf) {
        /*Log.d(this.TAG, "_showLogo");
        if (this.mInConnection) {
            _Resume(w, h, 2, token, sf);
        } else {
            _Resume(w, h, 1, token, sf);
        }*/
    }

    private byte[] getLogo() {
        return null;
        /*try {
            InputStream is = this.mCtxt.getAssets().open("iframe.bin");
            byte[] logo = new byte[is.available()];
            is.read(logo);
            is.close();
            return logo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }*/
    }
    public void SendG2AMessage(String content) {
        ConnectServer.getInstance().sendG2AMessage(content);
    }

    public void sendpwm(int pwmid, int pwmvalue) {
        Message msg = this.myHandler.obtainMessage();
        msg.what = 7;
        Bundle bd = msg.getData();
        bd.putInt("pwmid", pwmid);
        bd.putInt("pwmvalue", pwmvalue);
        this.myHandler.sendMessage(msg);
    }

    private void _sendpwm(int pwmid, int pwmvalue) {
        try {
            ConnectServer.getInstance().set_pwm1(this.mServerIp, pwmid, pwmvalue);
            Log.d(this.TAG, "in service, set camera " + pwmid + " " + pwmvalue);
        } catch (Exception e) {
            Log.d(this.TAG, "_sendpwm got excetpion " + e.toString());
        }
    }

    public void SendG2AMessage2(int value, int channel) {
        Message msg = this.myHandler.obtainMessage();
        msg.what = 12;
        Bundle bd = msg.getData();
        bd.putInt("value", value);
        bd.putInt("channel", channel);
        this.myHandler.sendMessage(msg);
    }

    private void _SendG2AMessage2(int value, int channel) {
        try {
            ConnectServer.getInstance().sendG2Amessage2(this.mServerIp, value, channel);
            Log.d(this.TAG, "in service, send message " + value + " " + channel);
        } catch (Exception e) {
            Log.d(this.TAG, "_SendG2AMessage2 got excetpion " + e.toString());
        }
    }

    public void SendG2AMessage1(String content) {
        Message msg = this.myHandler.obtainMessage();
        msg.what = 11;
        Bundle bd = msg.getData();
        bd.putString("", content);
        this.myHandler.sendMessage(msg);
    }

    private void _SendG2AMessage1(String content) {
        try {
            ConnectServer.getInstance().sendG2AMessage(content);
            Log.d(this.TAG, "in service, send G2A message " + content);
        } catch (Exception e) {
            Log.d(this.TAG, "_sendpwm got excetpion " + e.toString());
        }
    }

    public static void StartVideo(String ip, int type, int mSurface_w, int mSurface_h, Surface mSf) {
        try {
            ServiceBase.getServiceBase().getVideoService().Pause("");
            ServiceBase.getServiceBase().getVideoService().startLink(ip, type);
            ServiceBase.getServiceBase().getVideoService().Resume(mSurface_w, mSurface_h, 2, "", mSf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
