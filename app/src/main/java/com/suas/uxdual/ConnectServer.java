package com.suas.uxdual;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ConnectServer {
    private static final String TAG = "ConnectServer";
    private static int mResulution_height;
    private static int mResulution_width;
    private static String mServerIp;
    Handler mHandler;
    public static int ServerPort = 15380;
    static SocketChannel mSocketChannel2 = null;

    private static class SingletonHolder {
        static final ConnectServer INSTANCE = new ConnectServer();

        private SingletonHolder() {
        }
    }

    private abstract class adjustcamera implements Runnable {
        int angle;
        int cameraid;

        public adjustcamera(int cid, int a) {
            this.cameraid = cid;
            this.angle = a;
        }
    }

    public static ConnectServer getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private ConnectServer() {
        Log.d(TAG, "ConnectServer is called.");
        try {
            HandlerThread aHandlerThread = new HandlerThread(TAG);
            aHandlerThread.start();
            this.mHandler = new Handler(aHandlerThread.getLooper(), new Handler.Callback() {
                public boolean handleMessage(Message msg) {
                    Log.d(ConnectServer.TAG, "callback is called.");
                    return true;
                }
            }) {
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "mHandler creat failure.");
        }
    }

    public void SetServerIp(String serverip) {
        mServerIp = serverip;
    }

    private boolean Sendtoserver(String content) {
        boolean ret = false;
        try {
            Log.e(TAG, "trying " + mServerIp);
            SocketChannel mSocketChannel = SocketChannel.open();
            mSocketChannel.connect(new InetSocketAddress(InetAddress.getByName(mServerIp), ServerPort));//15380));
            mSocketChannel.socket().setTcpNoDelay(true);
            try {
                ByteBuffer bf = ByteBuffer.wrap(content.getBytes());
                while (bf.remaining() > 0) {
                    mSocketChannel.write(bf);
                }
                byte[] response = new byte[2048];
                int n = mSocketChannel.read(ByteBuffer.wrap(response));
                if (new String(response).contains("200")) {
                    ret = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "can't connect server " + content);
            }
            try {
                mSocketChannel.close();
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.e(TAG, "close socket failure");
            }
            Log.d(TAG, "send server " + ret);
            return ret;
        } catch (Exception e22) {
            e22.printStackTrace();
            return false;
        }
    }
    public static boolean Sendtoserver(String content, int port) {
        boolean ret = false;
        try {
            //Log.i(TAG, "trying IP = " + mServerIp + " port = " + port + " content = " + content);
            if(mSocketChannel2 == null) {
                mSocketChannel2 = SocketChannel.open();
            }
            if(!mSocketChannel2.isConnected()) {
                mSocketChannel2.connect(new InetSocketAddress(InetAddress.getByName(mServerIp), port));
                mSocketChannel2.socket().setTcpNoDelay(true);
            }
            //Log.i(TAG, "Sendtoserver: here after mSocketChannel.socket().setTcpNoDelay(true);");
            try {
                ByteBuffer bf = ByteBuffer.wrap(content.getBytes());
                //Log.i(TAG, "Sendtoserver: bf = " + bf);
                while (bf.remaining() > 0) {
                    mSocketChannel2.write(bf);
                }
                //byte[] response = new byte[2048];
                /*int n = mSocketChannel.read(ByteBuffer.wrap(response));
                Log.i(TAG, "Sendtoserver: response = " + response);
                if (new String(response).contains("200")) {
                    ret = true;
                }*/
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Sendtoserver can't connect server " + content);
            }
            try {
                //mSocketChannel.close();
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.e(TAG, "Sendtoserver close socket failure");
            }
            Log.d(TAG, "Sendtoserver send server " + ret);
            return ret;
        } catch (Exception e22) {
            e22.printStackTrace();
            return false;
        }
    }
    private boolean setResolution_thread(int width, int height) {
        String request = new String("");
        String content = ("CODEC=0\r\nWIDTH=" + width + "\r\n" + "HEIGHT=" + height + "\r\n") + "BITRATE=4000\r\nKEYGOP=50\r\nFRAMERATE=25\r\nRC=0\r\nQUALITY=4\r\nPROFILE=1\r\n";
        return Sendtoserver(("SET_VIDEOENC HDMI/1.0\r\n" + "CONTENT-LENGTH:" + content.length() + "\r\n\r\n") + content);
    }

    public boolean setResolution(int width, int height) {
        mResulution_height = height;
        mResulution_width = width;
        this.mHandler.post(new Runnable() {
            public void run() {
                ConnectServer.this.setResolution_thread(ConnectServer.mResulution_width, ConnectServer.mResulution_height);
            }
        });
        return true;
    }

    private boolean sendG2AMessage_thread(String content) {
        return Sendtoserver(content, 2018);
    }

    public boolean sendG2AMessage(final String content) {
        this.mHandler.post(new Runnable() {
            public void run() {
                ConnectServer.this.sendG2AMessage_thread(content);
            }
        });
        return true;
    }


    private boolean set_camerathread(int camerid, int angle) {
        Log.d(TAG, "set camera " + camerid + " with " + angle);
        String content = (("" + "ID=" + camerid + "\r\n") + "ORIENT=1\r\n") + "ANGLE=" + angle + "\r\n";
        String instruct = (("" + "ACTUATOR_MOVE HDMI/1.0\r\n") + "CONTENT-LENGTH:" + content.length() + "\r\n\r\n") + content;
        Sendtoserver(instruct);
        if ((camerid == 0 || camerid == 1) && angle == 90) {
            Sendtoserver(instruct);
            Sendtoserver(instruct);
        }
        Log.d(TAG, "camera control : " + instruct);
        return true;
    }

    public boolean set_camera(int camerid, int angle) {
        this.mHandler.post(new adjustcamera(camerid, angle) {
            public void run() {
                ConnectServer.this.set_camerathread(this.cameraid, this.angle);
            }
        });
        return true;
    }

    public boolean set_pwm1(String sip, int camerid, int angle) {
        mServerIp = sip;
        set_camerathread(camerid, angle);
        return true;
    }

    private boolean sendG2Amessage_thread(int value, int channel) {
        Log.d(TAG, "sendG2Gmessage_thread " + value + " with " + channel);
        String instruct = AirGroundCom.packageG2AMessage(value, channel);
        Sendtoserver(instruct, 2018);
        Log.d(TAG, "sendG2Gmessage : " + instruct);
        return true;
    }
    public boolean sendG2Amessage2(String sip, int camerid, int angle) {
        mServerIp = sip;
        sendG2Amessage_thread(camerid, angle);
        return true;
    }

}


