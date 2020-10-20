package com.suas.uxdual;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import android.app.Fragment;

//import androidx.cardview.widget.CardView;
//import android.app.Fragment;

//import static com.suas.uxdual.MainActivity.cardViewvideogroup;
//import android.app.Fragment;

public class VideoWindow {
    private static final String HWTOKEN = "VideoWindow_h";
    private static final String TAG = VideoWindow.class.getName();
    public static final String VIDEOWINDOW_H = "videowindow_h";
    private static VideoFragmentMgr _ghVideoFragmentMgr = new VideoFragmentMgr(HWTOKEN);
    private static String mServerIp = "192.168.2.220";
    private static final Object mSync = new Object();
    private static int mType;
    private static VideoFragmentMgr mWorkFragmgr;

    @SuppressWarnings("deprecation")
    public static class Horizontal extends Fragment {
        private boolean mIntenthassend = false;
        private SurfaceView surfaceView;
        private SurfaceHolder surfaceHolder;
        private Surface surface;

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View r = inflater.inflate(R.layout.video_layout_horizontal, container, false);
            ((SurfaceView) r.findViewById(R.id.videoViewPreview)).getHolder().addCallback(VideoWindow._ghVideoFragmentMgr);
            //CardView vg;// = r.findViewById(R.id.cardviewvideogroup);
            /*if (cardViewvideogroup != null) {
                cardViewvideogroup.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        if (Horizontal.this.mIntenthassend) {
                            Log.d(VideoWindow.TAG, "you have touched");
                            return;
                        }
                        Log.d(VideoWindow.TAG, "will launch fullscreen");
                        Horizontal.this.mIntenthassend = true;
                        try {
                            ServiceBase.getServiceBase().getVideoService().startLink(VideoWindow.mServerIp, 0);
                        } catch (Exception e) {
                            Log.d(VideoWindow.TAG, "get exception " + e.toString());
                        }
                        Log.d(VideoWindow.TAG, "SEND INTENT TO LUNCH FULLSCREEN in video_h");
                        Intent intent = new Intent(getActivity(), FullScreenVideoActivity.class);
                        intent.putExtra("type", VideoWindow.mType);
                        intent.putExtra("ip", VideoWindow.mServerIp);
                        Horizontal.this.startActivity(intent);
                        //startActivity(intent);
                    }
                });
            }*/
            return r;
        }

        public void onStop() {
            super.onStop();
            this.mIntenthassend = false;
        }
    }

    public static class VideoFragmentMgr implements Callback {
        private Surface mSf;
        private int mSurface_h;
        private int mSurface_w;
        private String mToken;

        void StartVideo(String ip, int type) {
            try {
                Log.d(VideoWindow.TAG + " startvideo", "StartVideo(String ip, int type)");
                ServiceBase.getServiceBase().getVideoService().Pause(this.mToken);
                ServiceBase.getServiceBase().getVideoService().startLink(ip, type);
                Log.d("StartVideo", "this.mSurface_w = " + this.mSurface_w);
                Log.d("StartVideo", "this.mSurface_h = " + this.mSurface_h);
                ServiceBase.getServiceBase().getVideoService().Resume(this.mSurface_w, this.mSurface_h, 2, this.mToken, this.mSf);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(VideoWindow.TAG, "Got exception in VideoFragmentMgr::StartVideo " + e.toString());
            }
        }

        VideoFragmentMgr(String token) {
            this.mToken = token;
        }

        public void surfaceChanged(final SurfaceHolder holder, int format, final int width, final int height) {
            Log.d(VideoWindow.TAG, String.format("surfaceChanged %d %d %d", format, width, height));
            this.mSurface_w = width;
            this.mSurface_h = height;
            this.mSf = holder.getSurface();
            synchronized (VideoWindow.mSync) {
                VideoWindow.mWorkFragmgr = this;
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            Log.e(VideoWindow.TAG, "surfaceCreated");
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.e(VideoWindow.TAG, "surfaceDestroyed");
            try {
                synchronized (VideoWindow.mSync) {
                    VideoWindow.mWorkFragmgr = null;
                    this.mSf = null;
                }
                ServiceBase.getServiceBase().getVideoService().Pause(this.mToken);
            } catch (Exception e) {
                Log.d(VideoWindow.TAG, "pause got exception " + e.toString());
            }
        }
    }

    public static void StartVideo(String sip, int link) {
        synchronized (mSync) {
            if (mWorkFragmgr == null) {
                Log.e(TAG, "target window surface is not created yet. addCallback");
                return;
            }
            mType = link;
            mServerIp = sip;
            mWorkFragmgr.StartVideo(sip, link);
        }
    }
}