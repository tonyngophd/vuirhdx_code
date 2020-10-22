package com.suas.uxdual;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.AudioManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Bitmap.createScaledBitmap;
import static com.suas.uxdual.CompleteWidgetActivity.deviceHeight;
import static com.suas.uxdual.CompleteWidgetActivity.deviceWidth;
import static com.suas.uxdual.CompleteWidgetActivity.djiBitmap;
import static com.suas.uxdual.CompleteWidgetActivity.recordVoice;
import static com.suas.uxdual.ThermalVideoFrag.textureViewThermalFrag;
//import android.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScreenRecordingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScreenRecordingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScreenRecordingFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String TAG = "ScreenRecordingFragment";
    private static final int REQUEST_CODE = 1000;
    private int mScreenDensity;
    private MediaProjectionManager mProjectionManager;
    private static int DISPLAY_WIDTH = (deviceWidth > 0) ? deviceWidth : 1024;//700;
    private static int DISPLAY_HEIGHT = (deviceHeight > 0) ? deviceHeight : 768;//700;1080;//512;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    @SuppressLint("StaticFieldLeak")
    static ToggleButton mToggleButton;
    static Button fullscreenSnapshotBtn;
    private MediaRecorder mMediaRecorder;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_PERMISSIONS = 10;

    private Activity parentActivity;
    private View rootView;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ScreenRecordingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ScreenRecordingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScreenRecordingFragment newInstance(String param1, String param2) {
        ScreenRecordingFragment fragment = new ScreenRecordingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        parentActivity = getActivity();
        HideAndroidBottomNavigationBarforTrueFullScreenView();
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_screen_recording, container, false);

        DisplayMetrics metrics = new DisplayMetrics();
        parentActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        DISPLAY_WIDTH = (deviceWidth > 0) ? deviceWidth : 1024;//700;
        DISPLAY_HEIGHT = (deviceHeight > 0) ? deviceHeight : 768;//700;1080;//512;

        mScreenDensity = metrics.densityDpi;
        mMediaRecorder = new MediaRecorder();

        mProjectionManager = (MediaProjectionManager) parentActivity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mToggleButton = (ToggleButton) rootView.findViewById(R.id.toggle);
        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (ContextCompat.checkSelfPermission(parentActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat
                        .checkSelfPermission(parentActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(parentActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(parentActivity, Manifest.permission.RECORD_AUDIO)) {
                        mToggleButton.setChecked(false);
                        Snackbar.make(rootView.findViewById(android.R.id.content), R.string.label_permissions,
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(parentActivity,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                                                REQUEST_PERMISSIONS);
                                    }
                                }).show();
                    } else {
                        ActivityCompat.requestPermissions(parentActivity,
                                new String[]{Manifest.permission
                                        .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                                REQUEST_PERMISSIONS);
                    }
                } else {
                    Log.i(TAG, "onCheckedChanged: Recording ischecked = " + isChecked);
                    onToggleScreenShare(compoundButton);
                }
            }

        });

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(parentActivity,
                    "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
            mToggleButton.setChecked(false);
            return;
        }
        mMediaProjectionCallback = new MediaProjectionCallback();
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
        Log.i(TAG, String.format("onActivityResult: DISPLAY_WIDTH = %d, DISPLAY_HEIGHT = %d", DISPLAY_WIDTH, DISPLAY_HEIGHT));
    }

    public void onToggleScreenShare(View view) {
        if (((ToggleButton) view).isChecked()) {
            initRecorder();
            shareScreen();
        } else {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            Log.v(TAG, "Stopping Recording");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // https://stackoverflow.com/questions/58791474/api-level-29-mediaprojection-is-always-requesting-permission
                // For Q (android 10) or above, our temporary solution is to NOT stop screen sharing to reuse everything
                // The reason is for Android to NOT re-poping the security question again, which annoys users
                // The security questions just needs to be asked once in a session of FullScreenActivity only.
                // Subsequence screen recording should happen smoothly without the question being popped again
                // TODO: in the future, for Android Q and above, look for a way to stop screen share to reduce pending resources
                stopScreenSharing();
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    makeThumbnailFromVideo();
                }
            }, 50);
        }
    }

    public void onButtonScreenShare() {
        ImageReader imageReader = ImageReader.newInstance(DISPLAY_WIDTH, DISPLAY_HEIGHT, ImageFormat.JPEG, 2);
        VirtualDisplay virtualDisplay = createVirtualDisplaySnapshot(imageReader.getSurface());

        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader imageReader) {
                Image image = imageReader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                assert buffer != null;
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);

            }
        }, new Handler());

        assert virtualDisplay != null;
        virtualDisplay.release();
    }

    private void shareScreen() {
        if (mMediaProjection == null) {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }
        mVirtualDisplay = createVirtualDisplay();
        //Log.i(TAG, String.format("shareScreen: DISPLAY_WIDTH = %d, DISPLAY_HEIGHT = %d", DISPLAY_WIDTH, DISPLAY_HEIGHT));
        mMediaRecorder.start();
    }

    static void setScreenRecordResolution(int width, int height) {
        DISPLAY_WIDTH = (width > 0) ? width : 1024;
        DISPLAY_HEIGHT = (height > 0) ? height : 768;
        Log.i(TAG, String.format("setScreenRecordResolution: DISPLAY_WIDTH = %d, DISPLAY_HEIGHT = %d", DISPLAY_WIDTH, DISPLAY_HEIGHT));
    }

    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("parentActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);
    }

    private VirtualDisplay createVirtualDisplaySnapshot(Surface surface) {
        if (mMediaProjection == null) return null;
        return mMediaProjection.createVirtualDisplay("parentActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface, null /*Callbacks*/, null
                /*Handler*/);
    }

    static String now, day;
    String mfolderpath;

    private void initRecorder() {
        now = (String) DateFormat.format("VuIR_yyyyMMdd_HHmmss", new Date());
        day = (String) DateFormat.format("yyyy-MM-dd", new Date());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            mfolderpath = Environment.getExternalStorageDirectory() + "/sUAS.com";
        } else {
            mfolderpath = Objects.requireNonNull(getActivity()).getExternalFilesDir(null) + "/sUAS.com";//
        }

        if (!createFolder(mfolderpath)) return;
        mfolderpath += "/VuIR_Media";
        if (!createFolder(mfolderpath)) return;
        mfolderpath += "/" + day;
        if (!createFolder(mfolderpath)) return;

        try {
            if (recordVoice) {
                AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                assert audioManager != null;
                audioManager.setMode(AudioManager.MODE_NORMAL);
                //Log.i(TAG, "initRecorder: " + audioManager.getMicrophones());
                if (audioManager.isMicrophoneMute()) {
                    audioManager.setMicrophoneMute(false);
                }
                //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            }
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);//MPEG_4);//THREE_GPP);//MPEG_2_TS
            mMediaRecorder.setOutputFile(mfolderpath + "/" + now + ".mp4");
            //Log.i(TAG, "initRecorder: frameno recording to file" + (mfolderpath + "/" + now + ".mp4"));
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);//TODO set record resolution
            //mMediaRecorder.setVideoSize((int) (DISPLAY_WIDTH * 1080f / DISPLAY_HEIGHT) / 2 * 2, 1080); //integer / 2 * 2 to make sure an even number (divisible by 2)
            //Log.i(TAG, String.format("setVideoSize: DISPLAY_WIDTH = %d, DISPLAY_HEIGHT = %d", DISPLAY_WIDTH, DISPLAY_HEIGHT));
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            if (recordVoice) {
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);//DEFAULT);//
            }
            //mMediaRecorder.setAudioEncodingBitRate
            //if (android.os.Build.VERSION.SDK_INT > 26)
            if (deviceWidth > 1024)
                mMediaRecorder.setVideoEncodingBitRate(512 * 1000 * 22);
            else
                mMediaRecorder.setVideoEncodingBitRate(512 * 1000 * 18);
            mMediaRecorder.setVideoFrameRate(30);
            int rotation = parentActivity.getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();
            //makeThumbnail(mfolderpath);
            //FullScreenShotFragment.makeVideoThumbNail = true;
            //FullScreenShotFragment.mButton.performClick();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        //https://stackoverflow.com/questions/1540272/android-how-to-overlay-a-bitmap-and-draw-over-a-bitmap
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), null);
        return bmOverlay;
    }

    private void makeThumbnail(String mfolderpath) {
        try {
            if (!createFolder(mfolderpath + "/thumbs")) return;
            // image naming and path  to include sd card  appending name you choose for file
            final String thumbnailPath = mfolderpath + "/thumbs/" + now + "_thumb.jpg";
            Bitmap bitmap = textureViewThermalFrag.getBitmap();

            int x, y, w, h, W = bitmap.getWidth(), H = bitmap.getHeight();
            w = (int) (W / 1.61f);
            x = (W - w) / 2;
            y = 16; // from 16dp to pixels, android:translationY="16dp" //TODO: set this value correctly depending on screen size
            if(CompleteWidgetActivity.isBoson  || CompleteWidgetActivity.isBosonPi || CompleteWidgetActivity.isBosonPiM){
                h = (int) ((H / 1.077f) - y); //512
            } else {
                h = (int) ((H / 1.065f) - y); //512
            }

            final Bitmap irthumbbitmap = createScaledBitmap(createBitmap(bitmap, x, y, w, h), 40, 32, false);
            final Bitmap thumbbitmap;
            if (djiBitmap != null) {
                Log.d(TAG, "makeThumbnail: overlaying ir on top of dji bitmap");
                thumbbitmap = overlay(djiBitmap, irthumbbitmap);
            } else {
                thumbbitmap = irthumbbitmap;
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    ///Saving a thumbnail of this
                    File imageFile = new File(thumbnailPath);
                    FileOutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(imageFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (outputStream != null) {
                        thumbbitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        try {
                            outputStream.flush();
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, 20);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

    private boolean createFolder(String mFolderPath) {
        File mFolder = new File(mFolderPath);
        //Log.i(TAG, "createFolder: takeScreenshot creating folder " + mfolderpath);
        if (!mFolder.exists()) {
            if (!mFolder.mkdir()) {
                Log.i(TAG, "takeScreenshot: cannot create folder " + mFolderPath);
                return false; //
            }
        }
        return true;
    }

    private void makeThumbnailFromVideo() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        // Set data source to retriever.
        // From your code, you might want to use your 'String path' here.
        retriever.setDataSource(mfolderpath + "/" + now + ".mp4");
        Log.i(TAG, "makeThumbnailFromVideo: retriever = " + retriever);

        // Get a frame in Bitmap by specifying time.
        // Be aware that the parameter must be in "microseconds", not milliseconds.
        final Bitmap thumbBitmap = createScaledBitmap(retriever.getFrameAtTime(1), 80, 64, false);
        if (thumbBitmap != null) {
            Log.i(TAG, "makeThumbnailFromVideo: first frame created");
            if (!createFolder(mfolderpath + "/thumbs")) return;
            // image naming and path  to include sd card  appending name you choose for file
            final String thumbnailPath = mfolderpath + "/thumbs/" + now + "_thumb.jpg";

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ///Saving a thumbnail of this
                    File imageFile = new File(thumbnailPath);
                    FileOutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(imageFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (outputStream != null) {
                        thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        try {
                            outputStream.flush();
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, 20);
        }
    }

    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if (mToggleButton.isChecked()) {
                mToggleButton.setChecked(false);
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                Log.v(TAG, "Recording Stopped");
            }
            mMediaProjection = null;
            stopScreenSharing();
        }
    }

    private void stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        //mMediaRecorder.release(); //If used: mMediaRecorder object cannot
        // be reused again
        destroyMediaProjection();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyMediaProjection();
    }

    private void destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG, "MediaProjection Stopped");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if ((grantResults.length > 0) && (grantResults[0] + grantResults[1]) == PackageManager.PERMISSION_GRANTED) {
                    onToggleScreenShare(mToggleButton);
                } else {
                    mToggleButton.setChecked(false);
                    //https://stackoverflow.com/questions/34263418/cant-find-android-support-design-widget-snackbar-in-support-design-library
                    Snackbar.make(rootView.findViewById(android.R.id.content), R.string.label_permissions,
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setData(Uri.parse("package:" + parentActivity.getPackageName()));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    startActivity(intent);
                                }
                            }).show();
                }
                return;
            }
        }
    }

    private void HideAndroidBottomNavigationBarforTrueFullScreenView() {
        //https://stackoverflow.com/questions/16713845/permanently-hide-navigation-bar-in-an-activity/26013850
        View decorView = parentActivity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
