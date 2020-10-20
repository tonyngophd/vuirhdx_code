package com.taihang.insight;

import android.util.Log;

import java.nio.ByteBuffer;

public class JniBridge {
    private networkInteface mListener;

    public interface networkInteface {
        int onData(int i, int i2, long j, int i3);

        int onMetaChanged(int i, int i2, int i3);
    }

    public native void native_sink_consumeByteArray(byte[] bArr, int i, long j);

    public native int native_sink_enqueDecoderBuffer(int i);

    public native void native_sink_init(String str, int i);

    public native void native_sink_registerInputBuffers(ByteBuffer byteBuffer, int i);

    public native void native_sink_stop();

    public JniBridge(networkInteface ni) {
        this.mListener = ni;
    }

    public JniBridge(networkInteface ni, String ip, int conntype) {
        this.mListener = ni;
        native_sink_init(ip, conntype);
    }

    public int onData(int idx, int len, long timestamp, int flag) {
        return this.mListener.onData(idx, len, timestamp, flag);
    }

    public int onMetaChanged(int width, int hight, int fps) {
        return this.mListener.onMetaChanged(width, hight, fps);
    }

    static {
        System.loadLibrary("jni-taihang-glsink");
        Log.d("SINK", "loaded jni");
    }
}
