package com.suas.uxdual;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
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
import android.widget.Toast;

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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FullScreenShotFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FullScreenShotFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FullScreenShotFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String TAG = "FullScreenShotFragment";
    private static final int REQUEST_CODE = 1000;
    private int mScreenDensity;
    private MediaProjectionManager mProjectionManager;
    private static int DISPLAY_WIDTH = (deviceWidth > 0) ? deviceWidth : 1024;//700;
    private static int DISPLAY_HEIGHT = (deviceHeight > 0) ? deviceHeight : 768;//700;1080;//512;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    @SuppressLint("StaticFieldLeak")
    static Button mButton;
    private ImageReader imageReader;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_PERMISSIONS = 10;
    static boolean makeVideoThumbNail = false;

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

    public FullScreenShotFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FullScreenShotFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FullScreenShotFragment newInstance(String param1, String param2) {
        FullScreenShotFragment fragment = new FullScreenShotFragment();
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
        rootView = inflater.inflate(R.layout.fragment_full_screen_shot, container, false);

        DisplayMetrics metrics = new DisplayMetrics();
        parentActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        DISPLAY_WIDTH = (deviceWidth > 0) ? deviceWidth : 1024;//700;
        DISPLAY_HEIGHT = (deviceHeight > 0) ? deviceHeight : 768;//700;1080;//512;

        mScreenDensity = metrics.densityDpi;

        mProjectionManager = (MediaProjectionManager) parentActivity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mButton = (Button) rootView.findViewById(R.id.buttonFSShot);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(parentActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(parentActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //mButton.setChecked(false);
                        Snackbar.make(rootView.findViewById(android.R.id.content), R.string.label_permissions,
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(parentActivity,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
                                    }
                                }).show();
                    } else {
                        ActivityCompat.requestPermissions(parentActivity,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
                    }
                } else {
                    onButtonScreenShare();
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
            //mButton.setChecked(false);
            return;
        }
        mMediaProjectionCallback = new MediaProjectionCallback();
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
        //imageReader = ImageReader.newInstance(DISPLAY_WIDTH, DISPLAY_HEIGHT, PixelFormat.RGBA_8888, 2);//ImageFormat.JPEG, 2);
        imageReader = ImageReader.newInstance(DISPLAY_WIDTH, DISPLAY_HEIGHT, ImageFormat.FLEX_RGBA_8888, 2);//ImageFormat.JPEG, 2);
        mVirtualDisplay = createVirtualDisplaySnapshot(imageReader.getSurface());
        Log.i(TAG, String.format("onActivityResult: DISPLAY_WIDTH = %d, DISPLAY_HEIGHT = %d", DISPLAY_WIDTH, DISPLAY_HEIGHT));

        Log.i(TAG, "onButtonScreenShare: mVirtualDisplay = " + mVirtualDisplay);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader imageReader) {
                //https://stackoverflow.com/questions/26673127/android-imagereader-acquirelatestimage-returns-invalid-jpg
                Image image = imageReader.acquireLatestImage();
                final Image.Plane[] planes = image.getPlanes();
                final ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * DISPLAY_WIDTH;
                Bitmap bitmapImage = Bitmap.createBitmap(DISPLAY_WIDTH + rowPadding / pixelStride, DISPLAY_HEIGHT, Bitmap.Config.ARGB_8888);
                bitmapImage.copyPixelsFromBuffer(byteBuffer);
                image.close();

                Log.i(TAG, "onImageAvailable: mVirtualDisplay bitmap = " + bitmapImage);

                processBitmaps(createBitmap(bitmapImage, 0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT));

                stopScreenSharing();
                imageReader.close();
            }
        }, new Handler());
    }

    public void onButtonScreenShare() {
        shareScreen();
    }

    private void shareScreen() {
        if (mMediaProjection == null) {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
        }
        //imageReader = ImageReader.newInstance(DISPLAY_WIDTH, DISPLAY_HEIGHT, PixelFormat.RGBA_8888, 2);//ImageFormat.JPEG, 2);
        //mVirtualDisplay = createVirtualDisplaySnapshot(imageReader.getSurface());
        //mVirtualDisplay = createVirtualDisplay();
        //Log.i(TAG, String.format("shareScreen: DISPLAY_WIDTH = %d, DISPLAY_HEIGHT = %d", DISPLAY_WIDTH, DISPLAY_HEIGHT));
        //mMediaRecorder.start();
    }


    private VirtualDisplay createVirtualDisplaySnapshot(Surface surface) {
        if (mMediaProjection == null) return null;
        return mMediaProjection.createVirtualDisplay("parentActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface, null /*Callbacks*/, null
                /*Handler*/);
    }

    private String now;


    private void processBitmaps(Bitmap bitmap) {
        String day;
        if(makeVideoThumbNail) {
            now = ScreenRecordingFragment.now;
            day = ScreenRecordingFragment.day;
        } else {
            now = (String) DateFormat.format("VuIR_yyyyMMdd_HHmmss", new Date());
            day = (String) DateFormat.format("yyyy-MM-dd", new Date());
        }

        String mfolderpath = "";//Environment.getExternalStorageDirectory() + "/sUAS.com";
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

        if(makeVideoThumbNail){
            Log.i(TAG, "processBitmaps: making video thumbnail");
            makeThumbnail(mfolderpath, "_thumb.jpg", bitmap);
            makeVideoThumbNail = false;
        } else {
            saveScreenShot(mfolderpath + "/" + now + "PIP.jpg", bitmap);
            makeThumbnail(mfolderpath, "PIP_thumb.jpg", bitmap);
        }
    }

    private void saveScreenShot(final String filepath, final Bitmap bitmap) {
        try {
            ///Saving a thumbnail of this
            File imageFile = new File(filepath);
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(imageFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if ((ThermalVideoFrag.lat != 0) && (ThermalVideoFrag.lon != 0)) {
                ThermalVideoFrag.GeoTagImage(filepath, ThermalVideoFrag.alt, ThermalVideoFrag.lat, ThermalVideoFrag.lon);
            }
        } catch (
                Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

    private void makeThumbnail(String mfolderpath, String filenameEnd, Bitmap bitmap) {
        try {
            if (!createFolder(mfolderpath + "/thumbs")) return;
            // image naming and path  to include sd card  appending name you choose for file
            final String thumbnailPath = mfolderpath + "/thumbs/" + now + filenameEnd;

            final Bitmap thumbbitmap = createScaledBitmap(bitmap, 80, 64, false);
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

    static void setScreenRecordResolution(int width, int height) {
        DISPLAY_WIDTH = (width > 0) ? width : 1024;
        DISPLAY_HEIGHT = (height > 0) ? height : 768;
        Log.i(TAG, String.format("setScreenShotResolution: DISPLAY_WIDTH = %d, DISPLAY_HEIGHT = %d", DISPLAY_WIDTH, DISPLAY_HEIGHT));
    }

    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
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
                    onButtonScreenShare();
                } else {
                    //mButton.setChecked(false);
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

