package com.suas.uxdual;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.util.Log;

import com.suas.uxdual.IVedeoInterface.Stub;

public class ServiceBase {
    private IVedeoInterface mVideoInterface;
    static ServiceBase svb = new ServiceBase();
    private final boolean DEBUG = true;
    private final String TAG = "ServiceBase";
    private boolean bound = false;
    private DeathRecipient mDeathRecipient = new DeathRecipient() {
        public void binderDied() {
            Log.e("ServiceBase", "traffice service vanished.");
            ServiceBase.this.mVideoInterface = null;
        }
    };
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder boundService) {
            Log.i("ServiceBase", "onServiceConnected(): Connected");
            ServiceBase.this.mVideoInterface = Stub.asInterface(boundService);
            ServiceBase.this.bound = true;
            if (ServiceBase.this.mVideoInterface == null) {
                Log.d("ServiceBase", "MobileService is null");
            }
            try {
                boundService.linkToDeath(ServiceBase.this.mDeathRecipient, 0);
            } catch (RemoteException re) {
                Log.d("ServiceBase", "got remote exception when connect death receipient " + re.toString());
            }
            synchronized (ServiceBase.svb) {
                ServiceBase.svb.notifyAll();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d("ServiceBase", "onServiceDisconnected()");
            ServiceBase.this.bound = false;
        }
    };

    public static ServiceBase getServiceBase() {
        return svb;
    }

    public IVedeoInterface getVideoService() {
        synchronized (svb) {
            if (this.mVideoInterface == null) {
                try {
                    wait(10000);
                } catch (Exception e) {
                    Log.e("ServiceBase","getVideoService got exception " + e.toString());
                }
            }
        }
        if (this.mVideoInterface == null) {
            Log.d("ServiceBase", "getVideoService mVideoInterface is null");
        } else {
            Log.d("ServiceBase", "mVideoInterface is  not null");
        }
        return this.mVideoInterface;
    }

    public void initService(Context ctx) {
        Intent i = new Intent();
        Log.d("ServiceBase", "running i.setAction(\"com.suas.vuirinsight.VideoService\");");
        //i.setAction("com.suas.vuirinsight.VideoService");
        //i.setPackage(ctx.getPackageName());
        Log.d("Package name", "Package name = " + ctx.getPackageName());
        i = new Intent(ctx.getApplicationContext(), VideoService.class);
        boolean result = false;
        try {
            //ctx.startService(i);
            result = ctx.bindService(i, this.mServiceConnection, Context.BIND_AUTO_CREATE);
        } catch (SecurityException e) {
            Log.d("ServiceBase", "Error while binding to GALService " + e.getMessage());
            e.printStackTrace();
        }
        if (!result) {
            Log.e("ServiceBase", "bindService() failed");
        }
        Log.d("ServiceBase", "initService:Wait for on service connected call back.");
    }
}



