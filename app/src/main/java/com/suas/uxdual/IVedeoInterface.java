package com.suas.uxdual;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.view.Surface;

import static android.content.ContentValues.TAG;

public interface IVedeoInterface extends IInterface {

    public static abstract class Stub extends Binder implements IVedeoInterface {
        private static final String DESCRIPTOR = "com.suas.uxdual.IVedeoInterface";
        static final int TRANSACTION_Pause = 2;
        static final int TRANSACTION_Resume = 3;
        static final int TRANSACTION_SetResolution = 4;
        static final int TRANSACTION_ShowLogo = 6;
        static final int TRANSACTION_StartVideo = 5;
        static final int TRANSACTION_Stop = 9;
        static final int TRANSACTION_sendpwm = 8;
        static final int TRANSACTION_setServerIp = 1;
        static final int TRANSACTION_startLink = 7;

        private static class Proxy implements IVedeoInterface {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void setServerIp(String sip) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                Log.d("setServerIp","serverIP = " + sip);
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sip);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void Pause(String token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(token);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void Resume(int w, int h, int type, String token, Surface sf) throws RemoteException {
                Log.d(TAG, "Resume: in IVedeoInterface");
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(w);
                    _data.writeInt(h);
                    _data.writeInt(type);
                    _data.writeString(token);
                    if (sf != null) {
                        _data.writeInt(1);
                        sf.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void SetResolution(int w, int h) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(w);
                    _data.writeInt(h);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void SendG2AMessage(String content) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(content);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void StartVideo(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void ShowLogo(int w, int h, String token, Surface sf) throws RemoteException {
                /*Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(w);
                    _data.writeInt(h);
                    _data.writeString(token);
                    if (sf != null) {
                        _data.writeInt(1);
                        sf.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }*/
            }

            public void startLink(String ip, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ip);
                    _data.writeInt(type);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendpwm(int pwmid, int pwmvalue) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pwmid);
                    _data.writeInt(pwmvalue);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void SendG2AMessage1(String content) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(content);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void SendG2AMessage2(int value, int channel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(value);
                    _data.writeInt(channel);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void Stop() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVedeoInterface asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVedeoInterface)) {
                return new Proxy(obj);
            }
            return (IVedeoInterface) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _arg0;
            int _arg1;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    setServerIp(data.readString());
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    Pause(data.readString());
                    reply.writeNoException();
                    return true;
                case 3:
                    Surface _arg4;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readInt();
                    int _arg2 = data.readInt();
                    String _arg3 = data.readString();
                    if (data.readInt() != 0) {
                        _arg4 = (Surface) Surface.CREATOR.createFromParcel(data);
                    } else {
                        _arg4 = null;
                    }
                    Resume(_arg0, _arg1, _arg2, _arg3, _arg4);
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    SetResolution(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    StartVideo(data.readInt());
                    reply.writeNoException();
                    return true;
                case 6:
                    /*Surface _arg32;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readInt();
                    String _arg22 = data.readString();
                    if (data.readInt() != 0) {
                        _arg32 = (Surface) Surface.CREATOR.createFromParcel(data);
                    } else {
                        _arg32 = null;
                    }
                    ShowLogo(_arg0, _arg1, _arg22, _arg32);
                    reply.writeNoException();*/
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    startLink(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    sendpwm(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    Stop();
                    reply.writeNoException();
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    SendG2AMessage(data.readString());
                    reply.writeNoException();
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    SendG2AMessage1(data.readString());
                    reply.writeNoException();
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    SendG2AMessage2(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void Pause(String str) throws RemoteException;

    void Resume(int i, int i2, int i3, String str, Surface surface) throws RemoteException;

    void SetResolution(int i, int i2) throws RemoteException;

    void SendG2AMessage(String content) throws RemoteException;

    void SendG2AMessage1(String content) throws RemoteException;

    void SendG2AMessage2(int value, int channel) throws RemoteException;

    void ShowLogo(int i, int i2, String str, Surface surface) throws RemoteException;

    void StartVideo(int i) throws RemoteException;

    void Stop() throws RemoteException;

    void sendpwm(int i, int i2) throws RemoteException;

    void setServerIp(String str) throws RemoteException;

    void startLink(String str, int i) throws RemoteException;
}
