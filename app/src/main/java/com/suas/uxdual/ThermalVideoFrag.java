package com.suas.uxdual;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.exifinterface.media.ExifInterface;
import android.app.Fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Bitmap.createScaledBitmap;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
import static com.suas.uxdual.CompleteWidgetActivity.fpvWidget;
import static com.suas.uxdual.CompleteWidgetActivity.screenshotpreview;
import static com.suas.uxdual.CompleteWidgetActivity.screenshotpreviewlayout;
import static com.suas.uxdual.CompleteWidgetActivity.seekBarIRTilt;
import static com.suas.uxdual.CompleteWidgetActivity.thermalvidfragframe;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ThermalVideoFrag.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ThermalVideoFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ThermalVideoFrag extends Fragment implements SurfaceHolder.Callback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "ThermalVideoFrag";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    static TextureView textureViewThermalFrag = null;
    static TextView textviewhint;
    static TextView textviewmode;
    static TextView textViewZoomScale;
    static ImageView imageviewIRshot;
    static LinearLayout videolayout;
    static LinearLayout mainlinearLayout;


    public ThermalVideoFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ThermalVideoFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static ThermalVideoFrag newInstance(String param1, String param2) {
        ThermalVideoFrag fragment = new ThermalVideoFrag();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_thermal_video, container, false);

        activity = getActivity();
        //((SurfaceView) view.findViewById(R.id.videoViewThermalFrag)).getHolder().addCallback(this);
        //videoViewThermalFrag = (VideoView) view.findViewById(R.id.videoViewThermalFrag);
        textviewhint = (TextView) view.findViewById(R.id.textviewhint);
        textviewmode = (TextView) view.findViewById(R.id.textviewmode);
        textureViewThermalFrag = view.findViewById(R.id.textureViewThermalFrag);
        textViewZoomScale = (TextView) view.findViewById(R.id.textViewPTZintro);
        imageviewIRshot = (ImageView) view.findViewById(R.id.imageviewIRshot);
        videolayout = view.findViewById(R.id.videolayout);
        mainlinearLayout = view.findViewById(R.id.mainlinearLayout);
        setallZs();

        textureViewThermalFrag.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                try {
                    ServiceBase.getServiceBase().getVideoService().Resume(width, height, 2, "ThermalVideoFrag", new Surface(surfaceTexture));
                } catch (Exception e) {
                    Log.d("ThermalVideoFrag", ".getVideoService().Resume got exception " + e.toString());
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                //pause
                //startlink
                /*try {
                    ServiceBase.getServiceBase().getVideoService().Pause("ThermalVideoFrag");
                    ServiceBase.getServiceBase().getVideoService().startLink(MainActivity.mserverip, MainActivity.LinkType);
                    ServiceBase.getServiceBase().getVideoService().Resume(width, height, 2, "ThermalVideoFrag", new Surface(surface));
                } catch (RemoteException re) {
                    Log.d("FullScreenVideoActivity", "onCreate startlink got remote exception " + re.toString());
                }*/
                //resume

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                try {
                    ServiceBase.getServiceBase().getVideoService().Pause("ThermalVideoFrag");
                } catch (Exception e) {
                    Log.d("ThermalVideoFrag", ".getVideoService().Resume got exception " + e.toString());
                }
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

        return view;
    }

    public static void showHintMessage(String message) {
        textviewhint.setVisibility(View.VISIBLE);
        textviewhint.setText(message);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                textviewhint.setVisibility(View.GONE);
            }
        }, 1500);
    }

    static float alt = 0;
    static double lat = 0, lon = 0;

    static void copyGPSdata(float Alt, double Lat, double Lon, final long millis) {
        alt = Alt;
        lat = Lat;
        lon = Lon;
        //Reset all numbers after 1000 milliseconds because GPS data need to be updated at at least 5hz (every 200 milliseconds)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do not reset if they have just been updated in a different (future) thread
                if (System.currentTimeMillis() - millis > 200) {
                    alt = 0;
                    lat = 0;
                    lon = 0;
                }
            }
        }, 1000);
    }

    private void setallZs() {
        textviewhint.setZ(2);
        textviewmode.setZ(2);
        textureViewThermalFrag.setZ(1);
        textViewZoomScale.setZ(2);
        imageviewIRshot.setZ(0);
    }

    static Activity activity = null;
    static String now;
    static String day;

    static void takeScreenshotPIP() {
        now = (String) DateFormat.format("VuIR_yyyyMMdd_HHmmss", new Date());
        day = (String) DateFormat.format("yyyy-MM-dd", new Date());
        //Log.i(TAG, "takeScreenshot: day = " + day + " now = " + now);

        try {
            String mfolderpath = Environment.getExternalStorageDirectory() + "/sUAS.com";//Environment.getRootDirectory().getParent() +

            if (!createFolder(mfolderpath)) return;
            mfolderpath += "/VuIR_Media";
            if (!createFolder(mfolderpath)) return;
            mfolderpath += "/" + day;
            if (!createFolder(mfolderpath)) return;

            // image naming and path  to include sd card  appending name you choose for file
            Bitmap bitmap = ((TextureView) textureViewThermalFrag).getBitmap();

            int x, y, w, h, W = bitmap.getWidth(), H = bitmap.getHeight();
            w = (int) (W * 1.1f / 1.47f); // The float numbers are from XML file (fragment_thermal_video.xml)
            if (CompleteWidgetActivity.isBoson) {
                h = (int) (H * 1.0f / 1.077f);
            } else {
                h = (int) (H * 1.0f / 1.065f);
            }

            x = (W - w) / 2;
            y = CompleteWidgetActivity.dp2px; // from 8dp to pixels, android:translationY="8dp"
            //Log.i(TAG, String.format("takeScreenshot: y = %d, x = %d, w = %d, h = %d, W = %d, H = %d", y, x, w, h, W, H));

            Bitmap cropped_bitmap = createBitmap(bitmap, x, y, w, h);
            imageviewIRshot.setImageBitmap(cropped_bitmap);

            // Now capture the whole screen (picture-in-picture mode)
            float videoZ = textureViewThermalFrag.getZ();
            float imageZ = imageviewIRshot.getZ();
            textureViewThermalFrag.setZ(imageZ);
            imageviewIRshot.setZ(videoZ);
            imageviewIRshot.setVisibility(View.VISIBLE);

            String mPath = mfolderpath + "/" + now + "PIP.jpg";
            File imageFile = new File(mPath);
            if (activity != null) {
                View v1 = activity.getWindow().getDecorView().getRootView();
                v1.setDrawingCacheEnabled(true);
                bitmap = Bitmap.createBitmap(v1.getDrawingCache());
                v1.setDrawingCacheEnabled(false);
                FileOutputStream outputStream = new FileOutputStream(imageFile);
                int quality = 100;
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                outputStream.flush();
                outputStream.close();
            }

            textureViewThermalFrag.setZ(videoZ);
            imageviewIRshot.setZ(imageZ);
            imageviewIRshot.setVisibility(View.INVISIBLE);
            if ((lat != 0) && (lon != 0)) {
                GeoTagImage(mPath, alt, lat, lon);
            }

            //openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

    static boolean createFolder(String mfolderpath) {
        File mFolder = new File(mfolderpath);
        //Log.i(TAG, "createFolder: takeScreenshot creating folder " + mfolderpath);
        if (!mFolder.exists()) {
            if (!mFolder.mkdir()) {
                Log.i(ContentValues.TAG, "takeScreenshot: cannot create folder " + mfolderpath);
                return false; //
            }
        }
        return true;
    }

    private static String mfolderpath = "";

    static void takeScreenshot(boolean takePIP) {
        //if (day == null || now == null) {
        now = (String) DateFormat.format("VuIR_yyyyMMdd_HHmmss", new Date());
        day = (String) DateFormat.format("yyyy-MM-dd", new Date());
        Log.i(ContentValues.TAG, "takeScreenshot: day = " + day + " now = " + now);
        //}

        try {
            mfolderpath = "";//Environment.getExternalStorageDirectory() + "/sUAS.com";//Environment.getRootDirectory().getParent() +
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                mfolderpath = Environment.getExternalStorageDirectory() + "/sUAS.com";//Environment.getRootDirectory().toString();
            } else {
                mfolderpath = activity.getExternalFilesDir(null) + "/sUAS.com";
            }
            if (!createFolder(mfolderpath)) return;
            mfolderpath += "/VuIR_Media";
            if (!createFolder(mfolderpath)) return;
            mfolderpath += "/" + day;
            if (!createFolder(mfolderpath)) return;
            if (!createFolder(mfolderpath + "/thumbs")) return;

            // image naming and path  to include sd card  appending name you choose for file
            final String mPath = mfolderpath + "/" + now + ".jpg";
            final String thumbnailPath = mfolderpath + "/thumbs/" + now + "_thumb.jpg";

            final Bitmap bitmap = ((TextureView) textureViewThermalFrag).getBitmap();

            int x, y, w, h, W = bitmap.getWidth(), H = bitmap.getHeight();
            w = mainlinearLayout.getWidth();
            h = mainlinearLayout.getHeight();
            //Log.i(TAG, "takeScreenshot: W/w = " + W * 1.0f / w);
            x = (W - w) / 2;
            float px = 8 * activity.getResources().getDisplayMetrics().density;            //float dp = somePxValue / density;
            y = (int) px;//DensityUtil.dip2px(getApplicationContext(), 8);
            //Log.i(ContentValues.TAG, String.format("takeScreenshot: y = %d, x = %d, w = %d, h = %d, W = %d, H = %d", y, x, w, h, W, H));
            Point point = new Point(560, 20);
            Bitmap bitmap2 = createBitmap(bitmap, x, y, w, h);
            final Bitmap cropped_bitmap = mark(createScaledBitmap(bitmap2, 640, 512, false),
                    "sUAS.com", point, Color.GREEN, 127, 16, false);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    File imageFile = new File(mPath);

                    FileOutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(imageFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    int quality = 100;
                    cropped_bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);

                    try {
                        assert outputStream != null;
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ///Saving a thumbnail of this
                    imageFile = new File(thumbnailPath);
                    try {
                        outputStream = new FileOutputStream(imageFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Bitmap thumbbitmap = createScaledBitmap(cropped_bitmap, 80, 64, false);
                    thumbbitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                    try {
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if ((lat != 0) && (lon != 0)) {
                        GeoTagImage(mPath, alt, lat, lon);
                    }
                }
            }, 20);

            if (!takePIP) return;

            final String mPathPIP = mfolderpath + "/" + now + "PIP.jpg";
            final String thumbnailPathPIP = mfolderpath + "/thumbs/" + now + "PIP_thumb.jpg";
            Bitmap DJIBitmap = fpvWidget.getBitmap();
            final Bitmap bitmapPIP = overlay(DJIBitmap, bitmap2,
                    thermalvidfragframe.getX() - fpvWidget.getX(), thermalvidfragframe.getY() - fpvWidget.getY());

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    File imageFile = new File(mPathPIP);

                    FileOutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(imageFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    int quality = 100;
                    bitmapPIP.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);

                    try {
                        assert outputStream != null;
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ///Saving a thumbnail of this
                    imageFile = new File(thumbnailPathPIP);
                    try {
                        outputStream = new FileOutputStream(imageFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Bitmap thumbbitmap = createScaledBitmap(bitmapPIP, 80, 64, false);
                    thumbbitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                    try {
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if ((lat != 0) && (lon != 0)) {
                        GeoTagImage(mPathPIP, alt, lat, lon);
                    }
                }
            }, 40);

        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

    private static Bitmap overlay(Bitmap biggerBmp, Bitmap topBmp, float marginLeft, float marginTop) {
        //https://stackoverflow.com/questions/1540272/android-how-to-overlay-a-bitmap-and-draw-over-a-bitmap
        Bitmap bmOverlay = Bitmap.createBitmap(biggerBmp.getWidth(), biggerBmp.getHeight(), biggerBmp.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(biggerBmp, new Matrix(), null);
        Paint paint = new Paint();
        paint.setAlpha((int) (seekBarIRTilt.getProgress() * 255f / 100));
        canvas.drawBitmap(topBmp, marginLeft, marginTop, paint);

        int size = (int) (16 * biggerBmp.getWidth() / 640f);
        Point point = new Point(biggerBmp.getWidth() - (int) (80 * size / 16f), size + 4);
        mark(canvas, "sUAS.com", point, Color.GREEN, 127, size, false);

        return bmOverlay;
    }

    //https://stackoverflow.com/questions/10679445/how-might-i-add-a-watermark-effect-to-an-image-in-android
    private static Bitmap mark(Bitmap src, String watermark, Point location, int color, int alpha, int size, boolean underline) {
        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAlpha(alpha);
        paint.setTextSize(size);
        paint.setAntiAlias(true);
        paint.setUnderlineText(underline);
        canvas.drawText(watermark, location.x, location.y, paint);

        return result;
    }

    private static void mark(Canvas canvas, String watermark, Point location, int color, int alpha, int size, boolean underline) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAlpha(alpha);
        paint.setTextSize(size);
        paint.setAntiAlias(true);
        paint.setUnderlineText(underline);
        canvas.drawText(watermark, location.x, location.y, paint);
    }

    static void takeScreenshot_old() {
        if (day == null || now == null) {
            now = (String) DateFormat.format("VuIR_yyyyMMdd_HHmmss", new Date());
            day = (String) DateFormat.format("yyyy-MM-dd", new Date());
            //Log.i(TAG, "takeScreenshot: day = " + day + " now = " + now);
        }

        try {
            String mfolderpath = Environment.getExternalStorageDirectory() + "/sUAS.com";//Environment.getRootDirectory().getParent() +
            if (!createFolder(mfolderpath)) return;
            mfolderpath += "/VuIR_Media";
            if (!createFolder(mfolderpath)) return;
            mfolderpath += "/" + day;
            if (!createFolder(mfolderpath)) return;

            // image naming and path  to include sd card  appending name you choose for file
            String mPath = mfolderpath + "/" + now + ".jpg";
            Bitmap bitmap = ((TextureView) textureViewThermalFrag).getBitmap();

            int x, y, w, h, W = bitmap.getWidth(), H = bitmap.getHeight();
            w = 640;
            h = 512;
            x = (W - w) / 2;
            y = CompleteWidgetActivity.dp2px; // from 8dp to pixels, android:translationY="8dp"
            //Log.i(TAG, String.format("takeScreenshot: y = %d, x = %d, w = %d, h = %d, W = %d, H = %d", y, x, w, h, W, H));

            Bitmap cropped_bitmap = createBitmap(bitmap, x, y, w, h);
            screenshotpreview.setImageBitmap(cropped_bitmap);
            screenshotpreview.setScaleType(ImageView.ScaleType.FIT_XY);
            screenshotpreviewlayout.setVisibility(View.VISIBLE);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            cropped_bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
            if ((lat != 0) && (lon != 0)) {
                GeoTagImage(mPath, alt, lat, lon);
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    screenshotpreviewlayout.setVisibility(View.GONE);
                }
            }, 1000);

            //openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }

    //https://techspread.wordpress.com/2014/04/07/write-read-geotag-jpegs-exif-data-in-android-gps/
    // with my own modifications, Tony Ngo.
    @SuppressLint("DefaultLocale")
    static void GeoTagImage(String imagePath, float altitude, double latitude, double longitude) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, GPS.convert(altitude, 1000));
            exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, "0"); //0 if the altitude is above sea level. 1 if the altitude is below sea level. Type is int.
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, GPS.convert(latitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, GPS.latitudeRef(latitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, GPS.convert(longitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, GPS.longitudeRef(longitude));
            @SuppressLint("SimpleDateFormat") SimpleDateFormat fmt_Exif = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            Log.i(TAG, "GeoTagImage: fmt_Exif.format(Calendar.getInstance().getTime()) = " + fmt_Exif.format(Calendar.getInstance().getTime()));
            exif.setAttribute(ExifInterface.TAG_DATETIME, fmt_Exif.format(Calendar.getInstance().getTime()));
            exif.saveAttributes(); // This could be an expensive action because it needs to copy an image, delete it then save as a new one.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Code to convert  Degrees to DMS unit
    private static class GPS {
        private static StringBuilder sb = new StringBuilder(25);

        /**
         * returns ref for latitude which is S or N.
         *
         * @param latitude
         * @return S or N
         */
        static String latitudeRef(final double latitude) {
            return latitude < 0.0d ? "S" : "N";
        }

        /**
         * returns ref for latitude which is S or N.
         * <p>
         * //@param latitude
         *
         * @return S or N
         */
        static String longitudeRef(final double longitude) {
            return longitude < 0.0d ? "W" : "E";
        }

        /**
         * convert latitude into DMS (degree minute second) format. For instance<br/>
         * -79.948862 becomes<br/>
         * 79/1,56/1,55903/1000<br/>
         * It works for latitude and longitude<br/>
         *
         * @param latitude could be longitude.
         * @return
         */
        static String convert(double latitude) {
            latitude = Math.abs(latitude);
            final int degree = (int) latitude;
            latitude *= 60;
            latitude -= degree * 60.0d;
            final int minute = (int) latitude;
            latitude *= 60;
            latitude -= minute * 60.0d;
            final int second = (int) (latitude * 100000.0d);

            sb.setLength(0);
            sb.append(degree)
                    .append("/1,")
                    .append(minute)
                    .append("/1,")
                    .append(second)
                    .append("/100000,");
            return sb.toString();
        }

        static String convert(float altitude, int factor) {
            final int int_altitude = (int) (altitude * factor);

            sb.setLength(0);
            sb.append(int_altitude)
                    .append("/")
                    .append(factor)
                    .append(",");
            return sb.toString();
        }
    }

    //https://stackoverflow.com/questions/11644873/android-write-exif-gps-latitude-and-longitude-onto-jpeg-failed
    // with my own modifications. Tony Ngo
    public static void writeFile(File photo, double latitude, double longitude) throws IOException {
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(photo.getCanonicalPath());
            double alat = Math.abs(latitude);
            String dms = Location.convert(alat, Location.FORMAT_SECONDS);
            String[] splits = dms.split(":");
            String[] secnds = (splits[2]).split("\\.");
            String seconds;
            if (secnds.length == 0) {
                seconds = splits[2];
            } else {
                seconds = secnds[0];
            }

            String latitudeStr = splits[0] + "/1," + splits[1] + "/1," + seconds + "/1";
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, latitudeStr);

            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitude > 0 ? "N" : "S");

            double alon = Math.abs(longitude);


            dms = Location.convert(alon, Location.FORMAT_SECONDS);
            splits = dms.split(":");
            secnds = (splits[2]).split("\\.");

            if (secnds.length == 0) {
                seconds = splits[2];
            } else {
                seconds = secnds[0];
            }
            String longitudeStr = splits[0] + "/1," + splits[1] + "/1," + seconds + "/1";


            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, longitudeStr);
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, longitude > 0 ? "E" : "W");

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat fmt_Exif = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            exif.setAttribute(ExifInterface.TAG_DATETIME, fmt_Exif.format(Calendar.getInstance().getTime()));
            exif.saveAttributes();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static void setIRVideoTransparency(float alpha) {
        textureViewThermalFrag.setAlpha(alpha);
        imageviewIRshot.setAlpha(alpha);
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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        /*try {
            Log.d("ThermalVideoFrag", "show fullscreen");
            ServiceBase.getServiceBase().getVideoService().Resume(width, height, 2, "ThermalVideoFrag", holder.getSurface());
        } catch (Exception e) {
            Log.d("ThermalVideoFrag", ".getVideoService().Resume got exception " + e.toString());
        }*/
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e("ThermalVideoFrag", "surfaceDestroyed");
        try {
            ServiceBase.getServiceBase().getVideoService().Pause("ThermalVideoFrag");
        } catch (Exception e) {
            Log.d("ThermalVideoFrag", ".getVideoService().Resume got exception " + e.toString());
        }
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
}
