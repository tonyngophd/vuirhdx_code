package com.suas.uxdual;

import android.annotation.SuppressLint;

import android.app.Activity;
import android.app.AlertDialog;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.dji.mapkit.core.maps.DJIMap;
import com.dji.mapkit.core.models.DJILatLng;

import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import dji.common.airlink.LightbridgeFrequencyBand;
import dji.common.airlink.OcuSyncFrequencyBand;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.keysdk.CameraKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.sdk.airlink.LightbridgeLink;
import dji.sdk.airlink.OcuSyncLink;
import dji.ux.widget.FPVWidget;
import dji.ux.widget.FlightModeWidget;
import dji.ux.widget.MapWidget;
import dji.ux.widget.PreFlightStatusWidget;
import dji.ux.widget.VideoSignalWidget;
import dji.ux.widget.VisionWidget;
import dji.ux.widget.controls.CameraControlsWidget;

import static com.suas.uxdual.GimbalControlFragment.airLink;
import static com.suas.uxdual.GimbalControlFragment.checkAirLinkOnce;
import static com.suas.uxdual.GimbalControlFragment.freqString;
import static com.suas.uxdual.GimbalControlFragment.freqStringShort;
import static com.suas.uxdual.GimbalControlFragment.getcurrentflightlimits;
import static com.suas.uxdual.GimbalControlFragment.videobandlinearlayout;
import static com.suas.uxdual.IRstatusFragment.displayIRBatteryPercentage;
import static com.suas.uxdual.IRstatusFragment.irdatalinkstatus;
import static com.suas.uxdual.ScreenRecordingFragment.setScreenRecordResolution;
import static com.suas.uxdual.ThermalVideoFrag.showHintMessage;
import static com.suas.uxdual.ThermalVideoFrag.takeScreenshot;
import static com.suas.uxdual.ThermalVideoFrag.textViewZoomScale;
import static com.suas.uxdual.ThermalVideoFrag.textureViewThermalFrag;
import static com.suas.uxdual.ThermalVideoFrag.textviewmode;
import static com.suas.uxdual.ThermalVideoFrag.videolayout;
import static dji.common.airlink.LightbridgeFrequencyBand.FREQUENCY_BAND_2_DOT_4_GHZ;
import static dji.common.airlink.LightbridgeFrequencyBand.FREQUENCY_BAND_5_DOT_7_GHZ;
import static dji.common.airlink.LightbridgeFrequencyBand.FREQUENCY_BAND_5_DOT_8_GHZ;
import static dji.keysdk.FlightControllerKey.DETECTION_SECTORS;
import static dji.keysdk.FlightControllerKey.VISION_DETECTION_STATE;
import static dji.keysdk.FlightControllerKey.createFlightAssistantKey;
import static dji.log.GlobalConfig.TAG;

/**
 * Activity that shows all the UI elements together
 */
public class CompleteWidgetActivity extends Activity {

    private MapWidget mapWidget;
    private ViewGroup parentView;
    static FPVWidget fpvWidget;
    private FPVWidget secondaryFPVWidget;
    private FrameLayout secondaryVideoView;
    private boolean isMapMini = true;

    private int height;
    private int width;
    private int margin;
    static int deviceWidth;
    static int deviceHeight;
    static Window window;
    public static String mserverip = "192.168.2.220";
    public static int LinkType = 0; //0 = TCP Link, 1 = UDP link
    private static int mWidth = 1280;
    private static int mHeight = 720;
    static RelativeLayout thermalvidfragframe;
    private RelativeLayout relativelayout_screenrecord;
    private RelativeLayout relativelayout_screenshot;
    RelativeLayout.LayoutParams parms;
    private Button buttonstartThermal;
    //static Button buttonsetminmax;
    private boolean ThermalAlreadyStarted = false;
    private boolean ThermalFragShown = false;
    private boolean setLimitsFragShown = false;
    private Fragment thermalVidFragment;
    private Fragment setLimitsFragment;
    private SeekBar seekBarIRTransparency;
    protected static SeekBar seekBarIRTilt;
    private SeekBar seekBarIRPan;
    private SeekBar gainseekBar;
    private float thermalvidfragframeXo, thermalvidfragframeYo, thermalvidfragframeXnow, thermalvidfragframeYnow;
    private float thermalvidW, thermalvidH;
    private int thermalvidfragframeRo, thermalvidfragframeBo;
    private float Xo, Yo, Xnow, Ynow;
    private boolean ThermalWindowDraggable = false;
    private boolean ThermalWindowResizeable = false;
    private boolean ThermalWindowTiltable = false;
    private int panseekbarVisibility = View.GONE;
    private GestureDetector mDetector;
    private float ParentX, ParentY, ParentW, ParentH;

    private int ThermalMinWidth_inFullScreenAspectRatio = 290, ThermalMinHeight_inFullScreenAspectRatio = 232;
    private static boolean MapOnTop = false;
    private boolean CantToastShown = false;
    private long CantToastMillis = System.currentTimeMillis();
    private boolean cantMoveMapFurtherHorizontal = false, isCantMoveMapFurtherVertical = false;
    private float viewminX = 0, viewmaxX = 0, viewminY = 0, viewmaxY = 0;
    private int panprogress_pre = 120, tiltprogress_pre = 120;
    protected static final String PREFS_NAME = "VuIRPrefsFile";
    RelativeLayout PTZdetectionbox;
    private int panProgressOnDown = 50, tiltProgressOnDown = 50;
    private static float TiltSensitivityFactorFixed = 120.0f;
    private static float TiltSensitivityFactor = TiltSensitivityFactorFixed;
    private static float PanSensitivityFactorFixed = 170.0f;
    private static float PanSensitivityFactor = PanSensitivityFactorFixed;
    private static final int SamSungTab5eWidth = 2560, SamSungTab5eHeight = 1600;
    private static boolean TiltSensitivityRescaledPerWindowSize = false;
    private static boolean PanSensitivityRescaledPerWindowSize = false;
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private long dragmillis, zoommillis;
    float HalfFingerSize = 100;
    public static TcpClient mTcpClient = null;
    int Filenumber = 1;
    private ImageView imageviewGallery;
    PreFlightStatusWidget preFlightStatusWidget;
    private LinearLayout connectionbar;
    private ImageView imageViewPaletteSbBg;
    private ImageView imageViewFFC;
    private ImageView imageViewRecord;
    private ImageView imageViewMic;
    private ImageView imageViewgainbkg;
    private ImageView imageViewRecordScreen;
    private SeekBar seekbarPalette;
    private int minThermalWidth = 320;
    private int minThermalHeight = 256;
    static int dp2px = 1;
    VideoSignalWidget bandfreqwidget;
    private int freq = 0;
    VisionWidget visionWidget;
    private GimbalControlFragment gimbalControlFragment;
    IRstatusFragment iRstatusFragment;
    protected static int IR_DJI_TiltDiff_FineTuned = 0;
    private int IR_DJI_TiltDiff_FineTuned_start = 0;
    public static IRCamera irCamera = new IRCamera();
    static RadioGroup radiovisibility;
    static TextView textviewFreg;
    ToggleButton toggle24Freg;
    float BatteryVoltagePercent;
    static boolean isBoson = false;
    static boolean isBosonPi = false;
    static boolean isBosonPiM = false;
    static boolean recordVoice = true;
    private ScreenRecordingFragment screenRecordingFragment = new ScreenRecordingFragment();
    private FullScreenShotFragment fullScreenShotFragment = new FullScreenShotFragment();
    private int RECscreen = 0, screenRecBlink = 0;
    private long screenrecOnOFFmillis = System.currentTimeMillis();
    static FlightModeWidget flightModeWidget;
    static FrameLayout videobandframelayout;
    private ToggleButton buttonMSX;
    private ToggleButton buttonMSXoverlaymode;
    private ToggleButton buttonMSXAutoSwitch;
    static ImageView screenshotpreview;
    static RelativeLayout screenshotpreviewlayout;
    private Button buttonsaveMSX, buttonloadMSX;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        window = getWindow();
        HideAndroidBottomNavigationBarforTrueFullScreenView();
        setContentView(R.layout.activity_default_widgets);
        thermalvidfragframe = findViewById(R.id.thermalvidfragframe);
        buttonstartThermal = findViewById(R.id.buttonstartThermal);
        //buttonsetminmax = findViewById(R.id.buttonsetminmax);
        seekBarIRTransparency = findViewById(R.id.seekBarIRTransparency);
        seekBarIRTilt = findViewById(R.id.seekBarIRTilt);
        seekBarIRPan = findViewById(R.id.seekBarIRPan);
        gainseekBar = findViewById(R.id.gainseekBar);
        preFlightStatusWidget = findViewById(R.id.preFlightStatusWidget);
        connectionbar = findViewById(R.id.connectionbar);
        imageViewPaletteSbBg = findViewById(R.id.imageViewPaletteSbBg);
        imageViewgainbkg = findViewById(R.id.imageViewgainbkg);
        imageViewFFC = findViewById(R.id.imageViewFFC);
        imageViewRecord = findViewById(R.id.imageViewRecord);
        imageViewRecordScreen = findViewById(R.id.imageViewRecordScreen);
        imageViewMic = findViewById(R.id.imageViewMic);
        screenshot = findViewById(R.id.screenshot);
        bandfreqwidget = findViewById(R.id.bandfreqwidget);
        visionWidget = findViewById(R.id.visionWidget);
        seekbarPalette = findViewById(R.id.seekbarPalette);
        radiovisibility = findViewById(R.id.radiovisibility);
        textviewFreg = findViewById(R.id.textviewFreg);
        toggle24Freg = findViewById(R.id.toggle24Freg);
        flightModeWidget = findViewById(R.id.flightmodel);
        videobandframelayout = findViewById(R.id.videobandframelayout);
        buttonMSX = findViewById(R.id.buttonMSX);
        buttonMSXoverlaymode = findViewById(R.id.buttonMSXoverlaymode);
        buttonMSXAutoSwitch = findViewById(R.id.buttonMSXAutoSwitch);
        buttonsaveMSX = findViewById(R.id.buttonsaveMSX);
        buttonloadMSX = findViewById(R.id.buttonloadMSX);
        screenshotpreview = (ImageView) findViewById(R.id.screenshotpreview);
        screenshotpreviewlayout = (RelativeLayout) findViewById(R.id.screenshotpreviewlayout);

        seekBarIRTransparency.setProgress(100);
        seekBarIRTilt.setProgress(seekBarIRTilt.getMax() / 2);

        height = DensityUtil.dip2px(this, 154);
        width = DensityUtil.dip2px(this, 230);
        minThermalHeight = DensityUtil.dip2px(this, 232);
        minThermalWidth = DensityUtil.dip2px(this, 290);
        margin = DensityUtil.dip2px(this, 8);
        dp2px = DensityUtil.px2dip(this, 8);

        parentView = findViewById(R.id.root_view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForegroundService(new Intent(this, ScreenRecService.class));
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                HideAndroidBottomNavigationBarforTrueFullScreenView();
                //DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                //deviceHeight = displayMetrics.heightPixels;
                //deviceWidth = displayMetrics.widthPixels;
                deviceHeight = parentView.getHeight();
                deviceWidth = parentView.getWidth();
                //Log.i(TAG, "onCreate postdelayed: deviceHeight = " + deviceHeight + " deviceWidth = " + deviceWidth);
                //confirmUpdateTheApp();
                confirmUsingTheApp();
                //showToast("onCreate: deviceHeight = " + deviceHeight + " deviceWidth = " + deviceWidth);
            }
        }, 3500);

        mapWidget = findViewById(R.id.map_widget);
        /*mapWidget.initAMap(new MapWidget.OnMapReadyListener() {
            @Override
            public void onMapReady(@NonNull DJIMap map) {
                map.setOnMapClickListener(new DJIMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(DJILatLng latLng) {
                        onViewClick(mapWidget);
                    }
                });
            }
        });*/
        mapWidget.initGoogleMap(new MapWidget.OnMapReadyListener() {
            @Override
            public void onMapReady(@NonNull DJIMap map) {
                map.setOnMapClickListener(new DJIMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(DJILatLng latLng) {
                        onViewClick(mapWidget);
                    }
                });
            }
        });
        mapWidget.onCreate(savedInstanceState);

        fpvWidget = findViewById(R.id.fpv_widget);
        fpvWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewClick(fpvWidget);
            }
        });
        /*secondaryVideoView = findViewById(R.id.secondary_video_view);
        secondaryFPVWidget = findViewById(R.id.secondary_fpv_widget);
        secondaryFPVWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapVideoSource();
            }
        });
        updateSecondaryVideoVisibility();*/


        //////// Thermal view methods and functions ///////////////
        connectionbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: hide thermal Tilt seekbar when this menu is shown
                onViewClick(preFlightStatusWidget);
                //showToast("hello!");
                //Log.i(TAG, "onClick: preFlightStatusWidget clicked");
            }
        });
        SetResolution(mWidth, mHeight);

        PTZdetectionbox = findViewById(R.id.thermalvidfragframe);
        mGestureDetector = new GestureDetector(PTZdetectionbox.getContext(), new DragListener());
        mScaleGestureDetector = new ScaleGestureDetector(PTZdetectionbox.getContext(), new ScaleListener());
        dragmillis = System.currentTimeMillis();
        zoommillis = dragmillis;

        PTZdetectionbox.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                int pointerCount = event.getPointerCount();
                Log.d(ContentValues.TAG, "onTouch: pointer count = " + pointerCount);
                if (pointerCount > 1) {
                    zoommillis = System.currentTimeMillis();
                    mScaleGestureDetector.onTouchEvent(event);
                } else {
                    dragmillis = System.currentTimeMillis();
                    if (System.currentTimeMillis() - zoommillis > 500) { // This is to avoid accidental pan&tilt when finishing zoom (2 fingers briefly become 1 finger)
                        mGestureDetector.onTouchEvent(event);
                        //Log.d(ContentValues.TAG, "onTouch: MotionEvent.ACTION_UP " + event.getAction());
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // This is to make sure is user just left finger up but then put it down within 1.5 second to pan again
                                    // seekBarIRPan should still be kept visible, to make it looks smooth, not appearing and disappearing repeatedly
                                    if (System.currentTimeMillis() - dragmillis > 500) {
                                        panseekbarVisibility = View.GONE;
                                        seekBarIRPan.setVisibility(panseekbarVisibility);
                                    }
                                }
                            }, 600);

                            //int progress = 50;
                            //tiltseekBar.setProgress(progress);
                            //tiltprogress_pre = progress;
                            //AirGroundCom.sendG2Amessage(progress, AirGroundCom.TILT_CHANNEL);
                            //SendG2AMessage(progress, AirGroundCom.TILT_CHANNEL);
                        }
                    }
                }
                //Log.d(TAG, "onTouch: getAction() = " + event.getAction());

                return true;
            }
        });
        new Thread(new Runnable() {
            public void run() {
                try {
                    ServiceBase.getServiceBase().getVideoService().startLink(mserverip, LinkType);
                } catch (RemoteException re) {
                    Log.d("CompleteWidgetActivity", "onCreate startlink got remote exception " + re.toString());
                }
            }
        }).start();

        flightModeWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //buttonsetminmax.performClick();
                HideAndroidBottomNavigationBarforTrueFullScreenView();
                if (setLimitsFragment == null) {
                    setLimitsFragment = SetLimitsFragment.newInstance("test", "test 2");

                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.add(R.id.relativelayout_setlimits, setLimitsFragment, "Set Limits")
                            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                            .commitAllowingStateLoss();
                    setLimitsFragShown = true;
                } else {
                    setLimitsFragShown = !setLimitsFragShown;
                    showHideFragment(setLimitsFragment, setLimitsFragShown);
                    getcurrentflightlimits.performClick();
                }
            }
        });

        buttonstartThermal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HideAndroidBottomNavigationBarforTrueFullScreenView();
                GimbalControlFragment.check24Button.performClick();
                if (!ThermalAlreadyStarted) {
                    if (thermalVidFragment == null) {
                        thermalVidFragment = ThermalVideoFrag.newInstance("test", "test 2");
                    }
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.add(R.id.thermalvidfragframe, thermalVidFragment, "Thermal Vid")
                            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                            .commitAllowingStateLoss();
                    ThermalAlreadyStarted = true;
                    ThermalFragShown = true;
                    setThermalControlsVisibility(View.VISIBLE);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (textureViewThermalFrag != null) {
                                RestoreUserSettings();
                                pivotXo = textureViewThermalFrag.getPivotX();
                                pivotYo = textureViewThermalFrag.getPivotY();
                            }
                        }
                    }, 5);
                } else {
                    ThermalFragShown = !ThermalFragShown;
                    showHideFragment(thermalVidFragment, ThermalFragShown);
                    int visibility = ThermalFragShown ? View.VISIBLE : View.GONE;
                    setThermalControlsVisibility(visibility);
                    //Log.d(TAG, "onClick: Thermal already started!");
                }
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                deviceHeight = displayMetrics.heightPixels;
                deviceWidth = displayMetrics.widthPixels;
                //Log.i(TAG, "thermal onCreate: deviceHeight = " + deviceHeight + " deviceWidth = " + deviceWidth);
            }
        });
        seekBarIRTransparency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (thermalVidFragment != null) {
                    if (buttonMSXoverlaymode.isChecked()) {
                        fpvWidget.setAlpha(0.01f * progress);
                    } else {
                        ThermalVideoFrag.setIRVideoTransparency(0.01f * progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                HideAndroidBottomNavigationBarforTrueFullScreenView();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SaveUserSettingInt("Thermal Transparency", seekBar.getProgress());
            }
        });

        seekBarIRTilt.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tiltprogress_pre = progress;
                AirGroundCom.sendG2Amessage(progress, AirGroundCom.TILT_CHANNEL);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarIRPan.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                panprogress_pre = progress;
                AirGroundCom.sendG2Amessage(progress, AirGroundCom.PAN_CHANNEL);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        updateConversationHandler = new Handler();
        updateUIStatusHandler = new Handler();

        connectionStatusMillis = System.currentTimeMillis();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                addSetLimitsFragment();
                addGimbalControlFragment();
                addScreenRecordFragment();
                addScreenShotFragment();
                addIRstatusFragment();
                //connectThermalLink();
                new CheckRealTimeConnectionTask().execute();
                // Do something after 2s = 2000ms
                while (mTcpClient == null) {
                    new ConnectTask().execute("");
                    new CheckRealTimeConnectionTask().execute();
                }
                //VideoWindow.StartVideo(mserverip, 0);
            }
        }, 500);/**/

        imageviewGallery = findViewById(R.id.imageviewGallery);
        imageviewGallery.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                confirm();
            }
        });

        seekbarPalette.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    SetPalette(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        imageViewFFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DoFFC();
            }
        });
        imageViewRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.i(ContentValues.TAG, "onClick: just clicked + REC before = " + REC);
                /// TODO: need to get confirmation  back from Gimmera it did receive REC command
                REC_Actual = REC;
                if (REC == 0) {
                    REC = 1;
                    if (RECMode == 1) {
                        if (android.os.Build.VERSION.SDK_INT < 27) {
                            imageViewRecord.setColorFilter(Color.RED);
                        } else {
                            final Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if (REC == 1) {
                                        if (recblink == 0) {
                                            recblink = 1;
                                            imageViewRecord.setColorFilter(Color.RED);
                                        } else {
                                            recblink = 0;
                                            imageViewRecord.setColorFilter(Color.GRAY);
                                        }
                                    } else {
                                        timer.cancel();
                                        timer.purge();
                                    }
                                }
                            }, 0, 1000);
                        }
                    }
                } else {
                    REC = 0;
                    if (RECMode == 1) {
                        if (android.os.Build.VERSION.SDK_INT < 27) {
                            imageViewRecord.setColorFilter(Color.GRAY);
                        } else {
                            imageViewRecord.setColorFilter(Color.RED);
                        }
                        //imageViewRecord.setImageResource(R.drawable.ic_videocam_red_48dp);
                    }
                }
                Log.i(ContentValues.TAG, "onClick: sending REC = " + REC);
                AirGroundCom.sendG2Amessage(REC, AirGroundCom.REC_CHANNEL);
            }
        });

        setThermalControlsVisibility(View.GONE);

        final MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.camera_shutter_click_01);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        final int Gray = Color.argb(99, 0, 0, 0);
        final int DarkRed = Color.argb(99, 255, 0, 0);
        final int DarkBlue = Color.argb(0xAA, 0x25, 0x82, 0xCE);//"#AA2582CE"

        screenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (parentView != null) {
                    deviceHeight = parentView.getHeight();
                    deviceWidth = parentView.getWidth();
                    FullScreenShotFragment.setScreenRecordResolution(deviceWidth, deviceHeight);
                }

                takeScreenshot(RECscreen == 1);
                if (RECscreen != 1) {//Exclude full screenshot while full-screen recording as it will crash the app
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        FullScreenShotFragment.mButton.performClick(); //TODO: make fullscreenshot available while full-screen recording
                    }
                    //Todo: make this work OK in Android 10
                }
                mPlayer.start(); // Make camera shutter sound
                Handler handler1 = new Handler();
                handler1.post(new Runnable() {
                    @Override
                    public void run() {
                        screenshot.setColorFilter(Gray);
                    }
                });
                handler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        screenshot.setColorFilter(DarkBlue);
                    }
                }, 250);
            }
        });

        imageViewMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordVoice = !recordVoice;
                if (recordVoice) {
                    imageViewMic.setImageResource(R.drawable.ic_mic_black_24dp);
                } else {
                    imageViewMic.setImageResource(R.drawable.ic_mic_off_black_24dp);
                }
            }
        });

        //TO DO: code to turn on/off downward sensor obstacle avoidance...
        // Try to see if we can turn on only the obstacle avoidance, but still enable vision positioning
        // Problem partial solved (but very practical): by moving the Tab HD to the side of Inspire 2 to be out of the FOVs of the sensors

        visionWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FlightControllerKey flightControllerKey = createFlightAssistantKey(DETECTION_SECTORS);
                FlightControllerKey flightControllerKey1 = createFlightAssistantKey(VISION_DETECTION_STATE);
                //ObstacleDetectionSector obstacleDetectionSector = new ObstacleDetectionSector();
            }
        });

        bandfreqwidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAirLinkOnce();
                if (airLink == null) return;
                final CommonCallbacks.CompletionCallback callbackSet = new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            Log.e(TAG, "onResult: djiError = " + djiError);
                        }
                    }
                };

                if (airLink.isLightbridgeLinkSupported()) {
                    LightbridgeLink lightbridgeLink = new LightbridgeLink();
                    LightbridgeFrequencyBand frequencyBand;
                    switch (freq) {
                        case 0:
                            frequencyBand = FREQUENCY_BAND_2_DOT_4_GHZ;
                            break;
                        case 1:
                            frequencyBand = FREQUENCY_BAND_5_DOT_7_GHZ;
                            break;
                        case 2:
                            frequencyBand = FREQUENCY_BAND_5_DOT_8_GHZ;
                            break;
                        default:
                            frequencyBand = FREQUENCY_BAND_2_DOT_4_GHZ;
                            break;
                    }
                    lightbridgeLink.setFrequencyBand(frequencyBand, callbackSet);

                    final CommonCallbacks.CompletionCallbackWith<LightbridgeFrequencyBand> callbackGet;
                    callbackGet = new CommonCallbacks.CompletionCallbackWith<LightbridgeFrequencyBand>() {
                        @Override
                        public void onSuccess(LightbridgeFrequencyBand lightbridgeFrequencyBand) {
                            Log.i(TAG, "onSuccess: lightbridgeFrequencyBand = " + lightbridgeFrequencyBand);
                            final String band = lightbridgeFrequencyBand.toString();
                            //https://stackoverflow.com/questions/5161951/android-only-the-original-thread-that-created-a-view-hierarchy-can-touch-its-vi
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textviewFreg.setText(freqStringShort(band));
                                }
                            });
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    showToast(freqString(band));
                                }
                            });
                        }

                        @Override
                        public void onFailure(DJIError djiError) {

                        }
                    };
                    lightbridgeLink.getFrequencyBand(callbackGet);
                    freq++;
                    if (freq > 2) freq = 0;

                } else if (airLink.isOcuSyncLinkSupported()) {
                    OcuSyncLink ocuSyncLink = new OcuSyncLink();
                    OcuSyncFrequencyBand frequencyBand;
                    switch (freq) {
                        case 0:
                            frequencyBand = OcuSyncFrequencyBand.FREQUENCY_BAND_2_DOT_4_GHZ;
                            break;
                        case 1:
                            frequencyBand = OcuSyncFrequencyBand.FREQUENCY_BAND_5_DOT_8_GHZ;
                            break;
                        case 2:
                            frequencyBand = OcuSyncFrequencyBand.FREQUENCY_BAND_DUAL;
                            break;
                        default:
                            frequencyBand = OcuSyncFrequencyBand.FREQUENCY_BAND_2_DOT_4_GHZ;
                            break;
                    }
                    ocuSyncLink.setFrequencyBand(frequencyBand, callbackSet);
                    final CommonCallbacks.CompletionCallbackWith<OcuSyncFrequencyBand> callbackGet;
                    callbackGet = new CommonCallbacks.CompletionCallbackWith<OcuSyncFrequencyBand>() {
                        @Override
                        public void onSuccess(OcuSyncFrequencyBand ocuSyncFrequencyBand) {
                            Log.i(TAG, "onSuccess: ocuSyncFrequencyBand = " + ocuSyncFrequencyBand);
                            final String band = ocuSyncFrequencyBand.toString();
                            //https://stackoverflow.com/questions/5161951/android-only-the-original-thread-that-created-a-view-hierarchy-can-touch-its-vi
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textviewFreg.setText(freqStringShort(band));
                                }
                            });
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    showToast(freqString(band));
                                }
                            });
                        }

                        @Override
                        public void onFailure(DJIError djiError) {

                        }
                    };
                    ocuSyncLink.getFrequencyBand(callbackGet);
                    freq++;
                    if (freq > 2) freq = 0;
                }
            }
        });

        radiovisibility.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedID) {
                if (checkedID == R.id.radioButtonBoson) {
                    isBoson = true;
                    isBosonPi = false;
                    isBosonPiM = false;
                } else if (checkedID == R.id.radioButtonBosonPi) {
                    isBoson = true;
                    isBosonPi = true;
                    isBosonPiM = false;
                } else if (checkedID == R.id.radioButtonBosonPiM) {
                    isBoson = true;
                    isBosonPi = false;
                    isBosonPiM = true;
                } else {
                    isBoson = false;
                    isBosonPi = false;
                    isBosonPiM = false;
                }

                setButtonVisibility(isBoson || isBosonPi || isBosonPiM);

//                if (checkedID == R.id.radioButtonBoson) {
//                    isBoson = true;
//                } else {
//                    isBoson = false;
//                }
                //setButtonVisibility(isBoson);
            }
        });

        gainseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                SetGain(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        imageViewRecordScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HideAndroidBottomNavigationBarforTrueFullScreenView();
                if (System.currentTimeMillis() - screenrecOnOFFmillis < 2000) {
                    //Wait at least 2 seconds between clicks to avoid crash of overloading
                    return;
                }
                screenrecOnOFFmillis = System.currentTimeMillis();
                final Handler handlerUI = new Handler();
                final int Gray = Color.argb(99, 0, 0, 0);
                final int DarkRed = Color.argb(99, 255, 0, 0);
                final int DarkBlue = Color.argb(0xAA, 0x25, 0x82, 0xCE);//"#AA2582CE"
                if (RECscreen == 0) {
                    RECscreen = 1;
                    imageViewMic.setEnabled(false);
                    imageViewMic.setColorFilter(Gray);
                    final Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (RECscreen == 1) {
                                if (screenRecBlink == 0) {
                                    screenRecBlink = 1;
                                } else {
                                    screenRecBlink = 0;
                                }
                                handlerUI.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        int color = (screenRecBlink == 1) ? DarkRed : Color.GRAY;
                                        imageViewRecordScreen.setColorFilter(color);
                                    }
                                });
                            } else {
                                timer.cancel();
                                timer.purge();
                            }
                        }
                    }, 0, 1000);
                } else {
                    RECscreen = 0;
                    imageViewMic.setEnabled(true);
                    imageViewMic.setColorFilter(DarkBlue);
                    handlerUI.post(new Runnable() {
                        @Override
                        public void run() {
                            imageViewRecordScreen.setColorFilter(DarkBlue);
                        }
                    });
                }
                if (parentView != null) {
                    deviceHeight = parentView.getHeight();
                    deviceWidth = parentView.getWidth();
                    setScreenRecordResolution(deviceWidth, deviceHeight);
                }
                //djiBitmap = Bitmap.createScaledBitmap(fpvWidget.getBitmap(), 80, 64, false);
                ScreenRecordingFragment.mToggleButton.setChecked(RECscreen == 1);
            }
        });

        toggle24Freg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                GimbalControlFragment.needToCheck24 = isChecked;
                if (isChecked) GimbalControlFragment.check24Interval = 2000;
            }
        });

        videobandframelayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int visibility;
                if (videobandlinearlayout.getVisibility() == View.VISIBLE) visibility = View.GONE;
                else visibility = View.VISIBLE;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        videobandlinearlayout.setVisibility(visibility);
                    }
                });
            }
        });


        buttonMSX.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if (buttonloadMSX != null)
                        buttonloadMSX.setVisibility(View.VISIBLE);
                    if (buttonsaveMSX != null)
                        buttonsaveMSX.setVisibility(View.VISIBLE);
                    regularIRX = thermalvidfragframe.getX();
                    regularIRY = thermalvidfragframe.getY();
                    regularParams = (RelativeLayout.LayoutParams) thermalvidfragframe.getLayoutParams();

                    final int wi, hi;
                    int w, h, p;
                    wi = (int) (parentView.getWidth() * 34f / 84f);
                    hi = (int) (wi / 640f * 512);
                    final SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    w = Normalize(settings.getInt("Thermal MSX Wi", wi), 640, deviceWidth - 200);
                    h = Normalize(settings.getInt("Thermal MSX Hi", hi), 512, deviceHeight - 120);
                    p = Normalize(settings.getInt("MSX Alpha", (int) (0.8f * seekBarIRTransparency.getMax())), 0, seekBarIRTransparency.getMax());

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);//thermalvidfragframe.getLayoutParams();
                    //https://stackoverflow.com/questions/3985787/android-relativelayout-programmatically-set-centerinparent
                    params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                    //https://stackoverflow.com/questions/13856180/usage-of-forcelayout-requestlayout-and-invalidate
                    thermalvidfragframe.setLayoutParams(params);

                    seekBarIRTransparency.setProgress(p);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            float xi = (parentView.getWidth() - thermalvidfragframe.getWidth()) / 2f;
                            float yi = (parentView.getHeight() - thermalvidfragframe.getHeight()) / 2f;
                            float x = xi, y = yi;
                            try {
                                x = Normalize(settings.getFloat("Thermal MSX Xi", xi), 100, deviceWidth - (wi + 100));
                                y = Normalize(settings.getFloat("Thermal MSX Yi", yi), 200, deviceHeight - (hi + 100));
                            } catch (Exception ignored) {
                            }

                            thermalvidfragframe.setX(x);
                            thermalvidfragframe.setY(y);
                        }
                    }, 20);
                } else {
                    if (buttonloadMSX != null)
                        buttonloadMSX.setVisibility(View.GONE);
                    if (buttonsaveMSX != null)
                        buttonsaveMSX.setVisibility(View.GONE);
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("Thermal MSX Wi", thermalvidfragframe.getWidth())
                            .putInt("Thermal MSX Hi", thermalvidfragframe.getHeight())
                            .putFloat("Thermal MSX Xi", thermalvidfragframe.getX())
                            .putFloat("Thermal MSX Yi", thermalvidfragframe.getY())
                            .putInt("MSX Alpha", seekBarIRTransparency.getProgress())
                            .apply();
                    seekBarIRTransparency.setProgress(seekBarIRTransparency.getMax());
                    thermalvidfragframe.setLayoutParams(regularParams);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            thermalvidfragframe.setX(regularIRX);
                            thermalvidfragframe.setY(regularIRY);
                        }
                    }, 10);
                }
            }
        });

        buttonMSXoverlaymode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    reguarIRZ = thermalvidfragframe.getZ();
                    regularVisualZ = fpvWidget.getZ();
                    Log.i(TAG, "onCheckedChanged: reguarIRZ = " + reguarIRZ + " regularVisualZ = " + regularVisualZ);
                    thermalvidfragframe.setZ(reguarIRZ - 1f);
                    Log.i(TAG, "onCheckedChanged: reguarIRZ now = " + thermalvidfragframe.getZ());
                    //fpvWidget.setZ(regularVisualZ + 1);
                    fpvWidget.setAlpha(0.01f * seekBarIRTransparency.getProgress());
                    ThermalVideoFrag.setIRVideoTransparency(1f);
                    //Todo: Android 10 (API 29+) and above, we can set this effect to make MSX even more close to Zenmuse XT2
                    //fpvWidget.setForegroundTintBlendMode(BlendMode.COLOR_BURN);
                } else {
                    thermalvidfragframe.setZ(reguarIRZ);
                    //fpvWidget.setZ(regularVisualZ);
                    ThermalVideoFrag.setIRVideoTransparency(0.01f * seekBarIRTransparency.getProgress());
                    fpvWidget.setAlpha(1f);
                }
            }
        });

        buttonMSXAutoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    swingVisions();
                }
            }
        });

        buttonsaveMSX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmSaveMSX();
            }
        });

        buttonloadMSX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int wi, hi;
                int w, h, p;
                wi = (int) (parentView.getWidth() * 34f / 84f);
                hi = (int) (wi / 640f * 512);
                final SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                w = Normalize(settings.getInt("Default MSX Wi", wi), 640, deviceWidth - 200);
                h = Normalize(settings.getInt("Default MSX Hi", hi), 512, deviceHeight - 120);
                p = Normalize(settings.getInt("Default MSX Alpha", (int) (0.8f * seekBarIRTransparency.getMax())), 0, seekBarIRTransparency.getMax());

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);//thermalvidfragframe.getLayoutParams();
                //https://stackoverflow.com/questions/3985787/android-relativelayout-programmatically-set-centerinparent
                params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                //https://stackoverflow.com/questions/13856180/usage-of-forcelayout-requestlayout-and-invalidate
                thermalvidfragframe.setLayoutParams(params);
                seekBarIRTransparency.setProgress(p);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        float xi = (parentView.getWidth() - thermalvidfragframe.getWidth()) / 2f;
                        float yi = (parentView.getHeight() - thermalvidfragframe.getHeight()) / 2f;
                        float x = xi, y = yi;
                        try {
                            x = Normalize(settings.getFloat("Default MSX Xi", xi), 100, deviceWidth - (wi + 100));
                            y = Normalize(settings.getFloat("Default MSX Yi", yi), 200, deviceHeight - (hi + 100));
                        } catch (Exception ignored) {
                        }

                        thermalvidfragframe.setX(x);
                        thermalvidfragframe.setY(y);
                    }
                }, 20);
            }
        });
    }


    private int transparency_progress = 100;
    private int increment = 2;
    private int swingEndWaits = 0;

    private void swingVisions() {
        final int max = 100;
        final int min = 10;
        final int waitTime = 20;
        swingEndWaits = 0;
        transparency_progress = seekBarIRTransparency.getProgress();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (buttonMSXAutoSwitch.isChecked() && ThermalFragShown) {
                    transparency_progress += increment;
                    if (transparency_progress > max) {
                        transparency_progress = max;
                        swingEndWaits++;
                        if (swingEndWaits >= 10) {
                            swingEndWaits = 0;
                            increment = -2;
                        }
                    } else if (transparency_progress < min) {
                        transparency_progress = min;
                        swingEndWaits++;
                        if (swingEndWaits >= 10) {
                            swingEndWaits = 0;
                            increment = 2;
                        }
                    } else {
                        swingEndWaits = 0;
                    }
                    if (swingEndWaits == 0)
                        seekBarIRTransparency.setProgress(transparency_progress);
                } else {
                    timer.cancel();
                    timer.purge();
                }
            }
        }, 0, waitTime);
    }

    private float reguarIRZ, regularVisualZ;
    private float regularIRX, regularIRY;
    private RelativeLayout.LayoutParams regularParams;
    static Bitmap djiBitmap;

    public void setButtonVisibility(boolean isBoson) {
        if (isBoson || isBosonPi || isBosonPiM) {
            gainseekBar.setVisibility(View.VISIBLE);
            imageViewgainbkg.setVisibility(View.VISIBLE);
        } else {
            gainseekBar.setVisibility(View.GONE);
            imageViewgainbkg.setVisibility(View.GONE);
        }
    }

    private int appUseConfirmNo = 0;
    private boolean APKinstall = true;
    private boolean needToGetStartedDate = true;
    private long daysSinceInstalled = 0;

    public void confirmUpdateTheApp() {
        //TODO: disable this for apps uploaded on Google Play store. This is only for temporary APK installations
        //return;
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        //Log.i(TAG, "confirmUpdateTheApp: date = " + new Date());
        long startedTime = 0;
        needToGetStartedDate = settings.getBoolean("Need SDate", true);
        Log.i(TAG, "confirmUpdateTheApp: needToGetStartedDate = " + needToGetStartedDate);
        if (needToGetStartedDate) {
            startedTime = new Date().getTime();
            SaveUserSettingLong("Started Time", startedTime);
            needToGetStartedDate = false;
            SaveUserSettingBoolean("Need SDate", needToGetStartedDate);
        } else {
            startedTime = settings.getLong("Started Time", new Date().getTime());
            long diff = new Date().getTime() - startedTime;
            daysSinceInstalled = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            Log.i(TAG, "confirmUpdateTheApp: Days = " + daysSinceInstalled + " diff = " + diff + " needToGetStartedDate = " + needToGetStartedDate);
        }

        if(daysSinceInstalled < 45) return;

        //https://stackoverflow.com/questions/13675822/android-alertdialog-builder with my own modifications
        //https://stackoverflow.com/questions/18346920/change-the-background-color-of-a-pop-up-dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CompleteWidgetActivity.this, R.style.MyDialogTheme);//AlertDialog.THEME_HOLO_LIGHT);

        // set title
        alertDialogBuilder.setTitle("App Update Needed");
        alertDialogBuilder.setIcon(R.drawable.ic_live_help_black_24dp);

        // set dialog message
        alertDialogBuilder
                .setMessage("This is a temporary installation of the app \n" +
                        "Please check www.sUAS.com or Google Play Store\n" +
                        "for new version of the app and install them if any. \n" +
                        "The app will not run after 90 days of the first manual install from APK\n" +
                        "App has been installed manually for " + daysSinceInstalled + " days.")
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(daysSinceInstalled < 60) {
                            dialog.cancel();
                        } else {
                            finish();
                        }
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                HideAndroidBottomNavigationBarforTrueFullScreenView();
            }
        }, 20);
        // show it
        alertDialog.show();
        //https://stackoverflow.com/questions/4406804/how-to-control-the-width-and-height-of-the-default-alert-dialog-in-android
        //Objects.requireNonNull(alertDialog.getWindow()).setLayout(400, 200);
        HideAndroidBottomNavigationBarforTrueFullScreenView();
    }

    public void confirmUsingTheApp() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        try {
            //https://stackoverflow.com/questions/27359179/delete-shared-preferences-on-installing-new-version-of-application-in-developmen
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (settings.getInt("VERSION_CODE", 0) != pInfo.versionCode) {
                SaveUserSettingInt("VERSION_CODE", pInfo.versionCode);
                appUseConfirmNo = 0;
            } else {
                appUseConfirmNo = settings.getInt("App Comfirn No", 0);
                if (appUseConfirmNo >= 2) return;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //https://stackoverflow.com/questions/13675822/android-alertdialog-builder with my own modifications
        //https://stackoverflow.com/questions/18346920/change-the-background-color-of-a-pop-up-dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CompleteWidgetActivity.this, R.style.MyDialogTheme);//AlertDialog.THEME_HOLO_LIGHT);

        // set title
        alertDialogBuilder.setTitle("App Usage Agreement");
        alertDialogBuilder.setIcon(R.drawable.ic_live_help_black_24dp);

        // set dialog message
        alertDialogBuilder
                .setMessage("This is an app that uses DJI SDK to control and fly your DJI drone. " +
                        "By using this app, you will NOT use either DJI GO or DJI GO 4 at the same time. " +
                        "Even though care and extensive tests have been taken and the part of the app that controls and fly the drone is provided by DJI, " +
                        "there might be risks of damaging or crashing the drone while using the app. The risks are very small." +
                        "\n\nDo you agree to continue to use the app and accept the small risks?" +
                        "\n\n(This confirmation will appear twice if YES is selected)")
                .setCancelable(false)
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        finish();
                        HideAndroidBottomNavigationBarforTrueFullScreenView();
                    }
                })
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        appUseConfirmNo++;
                        SaveUserSettingInt("App Comfirn No", appUseConfirmNo);
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                HideAndroidBottomNavigationBarforTrueFullScreenView();
            }
        }, 20);
        // show it
        alertDialog.show();
        //https://stackoverflow.com/questions/4406804/how-to-control-the-width-and-height-of-the-default-alert-dialog-in-android
        //Objects.requireNonNull(alertDialog.getWindow()).setLayout(400, 200);
        HideAndroidBottomNavigationBarforTrueFullScreenView();
    }

    public void confirm() {
        //https://stackoverflow.com/questions/13675822/android-alertdialog-builder with my own modifications
        //https://stackoverflow.com/questions/18346920/change-the-background-color-of-a-pop-up-dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CompleteWidgetActivity.this, R.style.MyDialogTheme);//AlertDialog.THEME_HOLO_LIGHT);

        // set title
        alertDialogBuilder.setTitle("Media Gallery");
        alertDialogBuilder.setIcon(R.drawable.ic_perm_media_black_24dp);

        // set dialog message
        alertDialogBuilder
                .setMessage("Open Media Gallery and close this screen?")
                .setCancelable(false)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                        HideAndroidBottomNavigationBarforTrueFullScreenView();
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        //Intent intent = new Intent(getApplicationContext(), MediaGalleryActivity.class);
                        finish();
                        //MainActivity.navView.setSelectedItemId(R.id.navigation_gallery);
                        //startActivity(intent);
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                HideAndroidBottomNavigationBarforTrueFullScreenView();
            }
        }, 20);
        // show it
        alertDialog.show();
        //https://stackoverflow.com/questions/4406804/how-to-control-the-width-and-height-of-the-default-alert-dialog-in-android
        //Objects.requireNonNull(alertDialog.getWindow()).setLayout(400, 200);
        HideAndroidBottomNavigationBarforTrueFullScreenView();
    }

    public void confirmSaveMSX() {
        //https://stackoverflow.com/questions/13675822/android-alertdialog-builder with my own modifications
        //https://stackoverflow.com/questions/18346920/change-the-background-color-of-a-pop-up-dialog
        HideAndroidBottomNavigationBarforTrueFullScreenView();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CompleteWidgetActivity.this, R.style.MSXDialogTheme);//AlertDialog.THEME_HOLO_LIGHT);

        // set title
        alertDialogBuilder.setTitle("MSX Save Settings");
        alertDialogBuilder.setIcon(R.drawable.ic_picture_in_picture_alt_black_24dp);
        // set dialog message
        alertDialogBuilder
                .setMessage("Save current MSX settings as default (this overwrites existing default settings)?")
                .setCancelable(false)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                        HideAndroidBottomNavigationBarforTrueFullScreenView();
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        saveDefaultMSXSettings();
                        HideAndroidBottomNavigationBarforTrueFullScreenView();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        Window window = alertDialog.getWindow();
        if (window != null) {
            //https://stackoverflow.com/questions/9467026/changing-position-of-the-dialog-on-screen-android
            WindowManager.LayoutParams wlp = window.getAttributes();

            wlp.gravity = Gravity.BOTTOM;
            //wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            //wlp.width = DensityUtil.dip2px(this, 250);
            window.setAttributes(wlp);
        }

        // show it
        alertDialog.show();

        //https://coderanch.com/t/570777/AlertDialog-changing-font-size-colour
        TextView textView = ((TextView) alertDialog.findViewById(android.R.id.message));
        if (textView != null) {
            //textView.setTextColor(Color.LTGRAY);
            textView.setTextSize(12);
            //textView.setGravity(Gravity.CENTER);
        }
        //https://stackoverflow.com/questions/2306503/how-to-make-an-alert-dialog-fill-90-of-screen-size
        if (window != null) {
            WindowManager.LayoutParams wlp = window.getAttributes();

            wlp.gravity = Gravity.BOTTOM;
            //wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            wlp.width = DensityUtil.dip2px(this, 400);
            wlp.height = DensityUtil.dip2px(this, 180);
            window.setAttributes(wlp);
        }
        //https://stackoverflow.com/questions/4406804/how-to-control-the-width-and-height-of-the-default-alert-dialog-in-android
        //Objects.requireNonNull(alertDialog.getWindow()).setLayout(400, 200);
        HideAndroidBottomNavigationBarforTrueFullScreenView();
    }

    private class MSXAlertDialog extends AlertDialog {

        protected MSXAlertDialog(Context context) {
            super(context);
        }

        //https://stackoverflow.com/questions/23520892/unable-to-hide-navigation-bar-during-alertdialog-logindialog
        // This still does NOT work!
        @Override
        public void show() {
            Objects.requireNonNull(this.getWindow()).setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            super.show();
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            this.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
    }

    private void saveDefaultMSXSettings() {
        if (buttonMSX == null || !buttonMSX.isChecked()) return; //Do not save
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("Default MSX Wi", thermalvidfragframe.getWidth())
                .putInt("Default MSX Hi", thermalvidfragframe.getHeight())
                .putFloat("Default MSX Xi", thermalvidfragframe.getX())
                .putFloat("Default MSX Yi", thermalvidfragframe.getY())
                .putInt("Default MSX Alpha", seekBarIRTransparency.getProgress())
                .apply();
    }

    private long mIRLinkMillis = System.currentTimeMillis();

    void connectThermalLink() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - mIRLinkMillis > 500) {
                    mIRLinkMillis = System.currentTimeMillis();
                    if ((mTcpClient == null) || (mTcpClient.mBufferOut == null)) {
                        new ConnectTask().execute("");
                    }
                    //Log.i(TAG, "run: connectThermalLink mTcpClient = " + mTcpClient + " mTcpClient.mBufferOut = " + mTcpClient.mBufferOut);
                }
            }
        }, 0, 500);
    }

    ImageView screenshot;

    private static int paletteNumber = 0;
    private static int REC = 0, REC_Actual = 0, recblink = 0, FFC = 0;
    private static int RECMode = 1;
    private static long recbuttonmillis = System.currentTimeMillis();

    void SetPalette(int paletteToSet) {
        paletteNumber = paletteToSet;
        AirGroundCom.sendG2Amessage(paletteNumber, AirGroundCom.PALETTE_CHANNEL);
    }

    void DoFFC() {
        if (FFC == 0) FFC = 1;
        else FFC = 0;
        AirGroundCom.sendG2Amessage(FFC, AirGroundCom.FLIR4_CHANNEL);
    }

    private int gainNumber = 0;

    private void SetGain(int gainToSet) {
        switch (gainToSet) {
            case 0:
                gainNumber = gainToSet;
                break;
            case 1:
                gainNumber = 4;
                break;
            case 2:
                gainNumber = 2;
                break;
            default:
                gainNumber = 0;
                break;
        }
        AirGroundCom.sendG2Amessage(gainNumber, AirGroundCom.THERMAL_GAINMODE_CHANNEL);
    }

    void setThermalControlsVisibility(int visibility) {
        seekBarIRTransparency.setVisibility(visibility);
        seekBarIRTilt.setVisibility(visibility);
        //seekBarIRPan.setVisibility(visibility);
        imageviewGallery.setVisibility(visibility);
        imageViewPaletteSbBg.setVisibility(visibility);
        seekbarPalette.setVisibility(visibility);
        imageViewFFC.setVisibility(visibility);
        imageViewRecord.setVisibility(visibility);
        screenshot.setVisibility(visibility);
        buttonMSX.setVisibility(visibility);
        buttonMSXoverlaymode.setVisibility(visibility);
        buttonMSXAutoSwitch.setVisibility(visibility);
        if (!(isBoson  || isBosonPi || isBosonPiM) || (seekbarPalette.getMax() < 9)) {
            gainseekBar.setVisibility(View.GONE);
            imageViewgainbkg.setVisibility(View.GONE);
        } else {
            gainseekBar.setVisibility(visibility);
            imageViewgainbkg.setVisibility(visibility);
        }
        //Log.i(TAG, "setThermalControlsVisibility: gainseekBar visibility = " + gainseekBar.getVisibility());
        if (visibility == View.GONE || buttonMSX.isChecked()) {
            buttonloadMSX.setVisibility(visibility);
            buttonsaveMSX.setVisibility(visibility);
        }
    }

    Handler updateConversationHandler, updateUIStatusHandler;
    String LongerMessage = "";
    public static final int CONFIRM_OK = 100;
    public static final int CONFIRM_NOT_OK = 1;
    final boolean[] NOTDisplayed = {true};
    long Millis = System.currentTimeMillis();
    public static String MessageReceivedFromGimmera = "";
    private long connectionStatusMillis;

    //TODO need to rewrite this to utilize the existing video service to connect to Gimmera (send commands up)
    // Right now using the existing service can only send commands, but not to listen to the port actively
    @SuppressLint("StaticFieldLeak")
    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        private boolean NOTDisplayed = true;

        @SuppressLint("SetTextI18n")
        @Override
        protected TcpClient doInBackground(String... message) {

            //we create a TCPClient object and
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    if (MessageReceivedFromGimmera.length() > 300) {
                        // TODO: make a shifting mechanism to the left so no need to discard the whole received buffer
                        MessageReceivedFromGimmera = "";
                    }
                    MessageReceivedFromGimmera += message;
                    publishProgress(message);
                    connectionStatusMillis = System.currentTimeMillis();
                }
            });
            mTcpClient.run();
            int i = 0;

            while ((mTcpClient != null) && (mTcpClient.mBufferOut == null)) {
                if (System.currentTimeMillis() - Millis > 500) {
                    Millis = System.currentTimeMillis();
                    //Log.i(TAG, "run: onProgressUpdate mTcpClient.mBufferOut = " + mTcpClient.mBufferOut + " mTcpClient = " + mTcpClient);
                    i++;
                    if (i >= 4) {
                        Log.i(TAG, "doInBackground: onProgressUpdate (updated Disconnected Status) i = " + i);
                        updateConversationHandler.post(new updateUIThread_Disconnected());
                        i = 0;
                    }
                    mTcpClient.run();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            updateUIStatusHandler.post(new updateUIThread(values[0]));
        }
    }

    //@SuppressLint("StaticFieldLeak")
    //AsyncTask<Params, Progress, Result>
    //https://stackoverflow.com/questions/14250989/how-to-use-asynctask-correctly-in-android
    public class CheckRealTimeConnectionTask extends AsyncTask<Void, String, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (System.currentTimeMillis() - connectionStatusMillis > 2010) {
                connectionStatusMillis = System.currentTimeMillis();
                publishProgress("Disconnected");
            } else {
            }
            return null;
        }


        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Log.i(TAG, "onProgressUpdate: values[0] = " + values[0]);
            if (values[0].equals("Disconnected")) {
                updateConversationHandler.post(new updateUIThread_Disconnected());
                if (mTcpClient != null)
                    mTcpClient.stopClient();
                NOTDisplayed[0] = true;
                new ConnectTask().execute("");
                /*if(mTcpClient != null) {
                    if (mTcpClient.ClientStopped) {
                        mTcpClient = null;
                        new ConnectTask().execute("");
                    }
                }*/
            }
        }
    }

    //https://examples.javacodegeeks.com/android/core/socket-core/android-socket-example/
    class updateUIThread_Disconnected implements Runnable {

        public updateUIThread_Disconnected() {
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            // NOTE that both SetGimeraVersion_RealTimeUpdate and UpdateStatusBar have null-checks already
            // So no worry if these cause crashes if FullScreenAcvity hasn't been instantiated yet.
            /*SettingsFragment.SetGimeraVersion_RealTimeUpdate(false);
            BatteryVoltagePercent = 0.0f;
            NumberofSats = 0;
            ConnectionStatus = getString(R.string.disconnected);
            Log.i(TAG, "run: onProgressUpdate: values ConnectionStatus = " + ConnectionStatus);
            StatusBarFrag.UpdateStatusBar();
            ConnectStatustextView.setText(ConnectionStatus);
            StatusBarFrag.UpdateStatusBar();//Do it again to make sure it works*/
            if (irdatalinkstatus != null)
                irdatalinkstatus.setText("IR-Link Off");
            NOTDisplayed[0] = true;
        }
    }

    //https://examples.javacodegeeks.com/android/core/socket-core/android-socket-example/
    class updateUIThread implements Runnable {
        private String message;

        updateUIThread(String str) {
            this.message = str;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            Log.d(TAG, "onProgressUpdate: message = " + message);
            if (message.contains("VOL")) {
                try {
                    //BatteryVoltage = Float.valueOf(message.substring(3));
                    BatteryVoltagePercent = Float.valueOf(message.substring(3));
                    displayIRBatteryPercentage(BatteryVoltagePercent);
                } catch (NumberFormatException e) {
                    try {
                        //BatteryVoltage = Float.valueOf(message.substring(3));
                        BatteryVoltagePercent = Float.valueOf(message.substring(4));
                        displayIRBatteryPercentage(BatteryVoltagePercent);
                    } catch (NumberFormatException e1) {
                        Log.e(TAG, "onProgressUpdate: exception, can't read number" + message);
                    }
                }
                Log.d(TAG, "onProgressUpdate: BatteryVoltagePercent = " + BatteryVoltagePercent);

                //StatusBarFrag.UpdateStatusBar();
            }
            if (message.contains("SAT")) { //NO need the SATS here as the drone will show SATS
//                try {
////                    NumberofSats = Integer.valueOf(message.substring(3));
////                } catch (NumberFormatException e) {
////                    try {
////                        NumberofSats = Integer.valueOf(message.substring(4));
////                    } catch (NumberFormatException e1) {
////                        Log.e(TAG, "onProgressUpdate: exception, can't read number" + message);
////                    }
////                }
                //StatusBarFrag.UpdateStatusBar();
            }

            if (message.contains("CAM")) {
                if (message.contains("Bos")) {
                    if (irCamera == null) {
                        irCamera = new IRCamera();
                    }
                    int[] resolution = {320, 256};
                    try {
                        //TODO make this resolution setting work better
                        resolution[0] = Integer.valueOf(message.substring(3, 6));
                        if (resolution[0] == 640) resolution[1] = 512;
                        else if (resolution[0] == 160) resolution[1] = 120;
                        else {
                            resolution[0] = 320;
                        }
                        Log.i(TAG, "run: onProgressUpdate Resolution = " + resolution[0]);
                    } catch (NumberFormatException ignored) {
                    }
                    irCamera.setIRCamera("Boson", irCamera.Boson, 0, resolution);
                    if (radiovisibility != null) {
                        radiovisibility.check(R.id.radioButtonBoson);
                    }
                    if (videolayout != null) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.weight = 1.61f;
                        videolayout.setLayoutParams(params);
                        if (textureViewThermalFrag != null) {
                            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
                            params1.weight = 1.077f;
                            textureViewThermalFrag.setLayoutParams(params1);
                        }
                    }
                    /*if (FullScreenVideoActivity.vidrecSurfaceview != null) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.weight = 1.61f;
                        FullScreenVideoActivity.vidrecSurfaceview.setLayoutParams(params);
                        FullScreenVideoActivity.vidrecSurfaceview.setZ(10f);
                    }*/

                    if (imageViewRecord != null) {
                        if (imageViewRecord.getVisibility() == View.VISIBLE) {
                            imageViewRecord.setVisibility(View.INVISIBLE);
                        }
                    }

                    if (seekBarIRTilt != null) {
                        if (seekBarIRTilt.getMax() < 250) {
                            seekBarIRTilt.setMax(250);
                            seekBarIRTilt.setProgress(125);
                            AirGroundCom.sendG2Amessage(125, AirGroundCom.TILT_CHANNEL);
                        }
                    }

                    if (seekbarPalette != null) {
                        if (seekbarPalette.getMax() < 9) {
                            seekbarPalette.setMax(9);
                            RelativeLayout.LayoutParams seekBarparams = (RelativeLayout.LayoutParams) seekbarPalette.getLayoutParams();
                            seekBarparams.width = (int) (DensityUtil.dip2px(getApplicationContext(), 117) * 10f / 3);
                            seekbarPalette.setLayoutParams(seekBarparams);
                            if (imageViewPaletteSbBg != null) {
                                imageViewPaletteSbBg.setImageResource(R.drawable.bosonpalettes);
                                RelativeLayout.LayoutParams imageparams = (RelativeLayout.LayoutParams) imageViewPaletteSbBg.getLayoutParams();
                                imageparams.width = (int) (DensityUtil.dip2px(getApplicationContext(), 117) * 10f / 3);
                                imageViewPaletteSbBg.setLayoutParams(imageparams);
                            }
                        }
                    }
                } if (message.contains("BoP")) {
                    if (irCamera == null) {
                        irCamera = new IRCamera();
                    }
                    int[] resolution = {320, 256};
                    try {
                        //TODO make this resolution setting work better
                        resolution[0] = Integer.valueOf(message.substring(3, 6));
                        if (resolution[0] == 640) resolution[1] = 512;
                        else if (resolution[0] == 160) resolution[1] = 120;
                        else {
                            resolution[0] = 320;
                        }
                        Log.i(TAG, "run: onProgressUpdate Resolution = " + resolution[0]);
                    } catch (NumberFormatException ignored) {
                    }
                    irCamera.setIRCamera("BosonPi", irCamera.BosonPi, 0, resolution);
                    if (radiovisibility != null) {
                        radiovisibility.check(R.id.radioButtonBosonPi);
                    }
                    if (videolayout != null) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.weight = 1.565f;
                        Log.i(TAG, "run: reset to 1.57f");
                        videolayout.setLayoutParams(params);
                        if (textureViewThermalFrag != null) {
                            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
                            params1.weight = 1.00f;
                            textureViewThermalFrag.setLayoutParams(params1);
                        }
                    }
                    /*if (FullScreenVideoActivity.vidrecSurfaceview != null) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.weight = 1.565f;
                        FullScreenVideoActivity.vidrecSurfaceview.setLayoutParams(params);
                        FullScreenVideoActivity.vidrecSurfaceview.setZ(10f);
                    }*/

                    if (imageViewRecord != null) {
                        if (imageViewRecord.getVisibility() == View.VISIBLE) {
                            imageViewRecord.setVisibility(View.INVISIBLE);
                        }
                    }

                    if (seekBarIRTilt != null) {
                        if (seekBarIRTilt.getMax() < 250) {
                            seekBarIRTilt.setMax(250);
                            seekBarIRTilt.setProgress(125);
                            AirGroundCom.sendG2Amessage(125, AirGroundCom.TILT_CHANNEL);
                        }
                    }

                    if (seekbarPalette != null) {
                        if (seekbarPalette.getMax() < 9) {
                            seekbarPalette.setMax(9);
                            RelativeLayout.LayoutParams seekBarparams = (RelativeLayout.LayoutParams) seekbarPalette.getLayoutParams();
                            seekBarparams.width = (int) (DensityUtil.dip2px(getApplicationContext(), 117) * 10f / 3);
                            seekbarPalette.setLayoutParams(seekBarparams);
                            if (imageViewPaletteSbBg != null) {
                                imageViewPaletteSbBg.setImageResource(R.drawable.bosonpalettes);
                                RelativeLayout.LayoutParams imageparams = (RelativeLayout.LayoutParams) imageViewPaletteSbBg.getLayoutParams();
                                imageparams.width = (int) (DensityUtil.dip2px(getApplicationContext(), 117) * 10f / 3);
                                imageViewPaletteSbBg.setLayoutParams(imageparams);
                            }
                        }
                    }
                } if (message.contains("BPM")) {
                    if (irCamera == null) {
                        irCamera = new IRCamera();
                    }
                    int[] resolution = {320, 256};
                    try {
                        //TODO make this resolution setting work better
                        resolution[0] = Integer.valueOf(message.substring(3, 6));
                        if (resolution[0] == 640) resolution[1] = 512;
                        else if (resolution[0] == 160) resolution[1] = 120;
                        else {
                            resolution[0] = 320;
                        }
                        Log.i(TAG, "run: onProgressUpdate Resolution = " + resolution[0]);
                    } catch (NumberFormatException ignored) {
                    }
                    irCamera.setIRCamera("BosonPiMulti", irCamera.BosonPiMulti, 0, resolution);
                    if (radiovisibility != null) {
                        radiovisibility.check(R.id.radioButtonBosonPiM);
                    }
                    if (videolayout != null) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.weight = 1.2345679f;
                        Log.i(TAG, "run: reset to width = 1.234567..");
                        videolayout.setLayoutParams(params);
                        if (textureViewThermalFrag != null) {
                            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
                            params1.weight = 1.00f;
                            textureViewThermalFrag.setLayoutParams(params1);
                        }
                    }
                    /*if (FullScreenVideoActivity.vidrecSurfaceview != null) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.weight = 1.2345679f;
                        FullScreenVideoActivity.vidrecSurfaceview.setLayoutParams(params);
                        FullScreenVideoActivity.vidrecSurfaceview.setZ(10f);
                    }*/

                    if (imageViewRecord != null) {
                        if (imageViewRecord.getVisibility() == View.VISIBLE) {
                            imageViewRecord.setVisibility(View.INVISIBLE);
                        }
                    }

                    if (seekBarIRTilt != null) {
                        if (seekBarIRTilt.getMax() < 250) {
                            seekBarIRTilt.setMax(250);
                            seekBarIRTilt.setProgress(125);
                            AirGroundCom.sendG2Amessage(125, AirGroundCom.TILT_CHANNEL);
                        }
                    }

                    if (seekbarPalette != null) {
                        if (seekbarPalette.getMax() < 9) {
                            seekbarPalette.setMax(9);
                            RelativeLayout.LayoutParams seekBarparams = (RelativeLayout.LayoutParams) seekbarPalette.getLayoutParams();
                            seekBarparams.width = (int) (DensityUtil.dip2px(getApplicationContext(), 117) * 10f / 3);
                            seekbarPalette.setLayoutParams(seekBarparams);
                            if (imageViewPaletteSbBg != null) {
                                imageViewPaletteSbBg.setImageResource(R.drawable.bosonpalettes);
                                RelativeLayout.LayoutParams imageparams = (RelativeLayout.LayoutParams) imageViewPaletteSbBg.getLayoutParams();
                                imageparams.width = (int) (DensityUtil.dip2px(getApplicationContext(), 117) * 10f / 3);
                                imageViewPaletteSbBg.setLayoutParams(imageparams);
                            }
                        }
                    }
                } else {

                    if (videolayout != null) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.weight = 1.47f;
                        videolayout.setLayoutParams(params);
                        if (textureViewThermalFrag != null) {
                            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
                            params1.weight = 1.065f;
                            textureViewThermalFrag.setLayoutParams(params1);
                        }
                    }

                    if (seekBarIRTilt != null) {
                        seekBarIRTilt.setMax(100);
                    }
                    if (seekbarPalette != null) {
                        if (seekbarPalette.getMax() > 2) {
                            seekbarPalette.setMax(2);
                            if (imageViewPaletteSbBg != null) {
                                imageViewPaletteSbBg.setImageResource(R.drawable.threepalettes);
                            }
                        }
                    }
                    if (imageViewRecord != null) {
                        if (imageViewRecord.getVisibility() != View.VISIBLE) {
                            imageViewRecord.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            if (message.contains("PAL")) {
                if (isBoson) {//Todo for VuIR Tab HD for Boson: not here, but in gimmera's code: correct the fed-back palette. right now it's always 1
                    if (seekbarPalette != null) {
                        //Log.i(TAG, "run: message.substring(3, 4) = " + message.substring(3, 4));
                        try {
                            int palette = Integer.valueOf(message.substring(3, 4));
                            Log.i(TAG, "run: palette = " + palette);
                            if (palette >= 0 && palette <= 9) {
                                seekbarPalette.setProgress(palette);
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }

            if (irdatalinkstatus != null) {
                irdatalinkstatus.setText("IR-Link ON");
                Log.i(TAG, "run: onProgressUpdate irdatalinkstatustext = " + irdatalinkstatus.getText());
            }
            //Log.i(TAG, "run: onProgressUpdate batterypercentagetextView = " + batterypercentagetextView + " irdatalinkstatus = " + irdatalinkstatus);
            //Log.d(TAG, "messageReceived: NOTDisplayed " + NOTDisplayed[0]);
            if (NOTDisplayed[0] && mTcpClient.mBufferOut != null) {
                //SettingsFragment.SetGimeraVersion_RealTimeUpdate(true);
                //recmsgtextView.setText("Connection established successfully!");
                NOTDisplayed[0] = false;
            }
        }
    }

    private void addSetLimitsFragment() {
        if (setLimitsFragment == null) {
            setLimitsFragment = SetLimitsFragment.newInstance("test", "test 2");
        }
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.relativelayout_setlimits, setLimitsFragment, "Set Limits")
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                //.hide(setLimitsFragment)
                .commitAllowingStateLoss();
        setLimitsFragShown = false;
        showHideFragment(setLimitsFragment, setLimitsFragShown);
    }

    private void addGimbalControlFragment() {
        if (gimbalControlFragment == null) {
            gimbalControlFragment = GimbalControlFragment.newInstance("test", "test 2");
        }
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.relativelayout_gimbalcontrols, gimbalControlFragment, "Gimbal Controls")
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .commitAllowingStateLoss();
    }

    private void addScreenRecordFragment() {
        if (screenRecordingFragment == null) {
            screenRecordingFragment = ScreenRecordingFragment.newInstance("test", "test 2");
        }
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.relativelayout_screenrecord, screenRecordingFragment, "Screen Recording")
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .commitAllowingStateLoss();
    }

    private void addScreenShotFragment() {
        if (fullScreenShotFragment == null) {
            fullScreenShotFragment = FullScreenShotFragment.newInstance("test", "test 2");
        }
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.relativelayout_screenshot, fullScreenShotFragment, "Screen Shot")
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .commitAllowingStateLoss();
    }

    private void addIRstatusFragment() {
        if (iRstatusFragment == null) {
            iRstatusFragment = IRstatusFragment.newInstance("test", "test 2");
        }
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.relativelayout_irstatus, iRstatusFragment, "Gimbal Controls")
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .commitAllowingStateLoss();
    }

    public void showHideFragment(Fragment fragment, boolean Shown) {
        if (fragment == null) return;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Log.d(TAG, "showHideFragment: fragment.isHidden() = " + fragment.isHidden() + " fragment.isAdded() = " + fragment.isAdded());
        if (Shown) {
            if (fragment.isHidden()) {
                ft.show(fragment);
                Log.d("showHideFragment hidden", "Show");
            }
        } else {
            if (!fragment.isHidden()) {
                ft.hide(fragment);
                Log.d("showHideFragment Shown", "Hide");
            }
        }
        ft.commitAllowingStateLoss();
    }

    public static void SetResolution(int width, int height) {
        mWidth = width;
        mHeight = height;
        new Thread(new Runnable() {
            public void run() {
                SetResolutionThread();
            }
        }).start();
    }

    private static void SetResolutionThread() {
        try {
            Log.d(TAG, "In SetResolutionThread, serverip = " + mserverip);
            ServiceBase.getServiceBase().getVideoService().setServerIp(mserverip);
            ServiceBase.getServiceBase().getVideoService().SetResolution(mWidth, mHeight);
        } catch (Exception e) {
            Log.d(TAG, "SetResolutionThread got exception " + e.toString());
        }
    }

    public void showToast(final String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.setMargin(0, 0.8f);
        toast.show();
    }

    private void onViewClick(View view) {
        if (view == fpvWidget && !isMapMini) {
            resizeFPVWidget(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, 0, 0);
            reorderCameraCapturePanel();
            ResizeAnimation mapViewAnimation = new ResizeAnimation(mapWidget, deviceWidth, deviceHeight, width, height, margin);
            mapWidget.startAnimation(mapViewAnimation);
            isMapMini = true;
        } else if (view == mapWidget && isMapMini) {
            hidePanels();
            resizeFPVWidget(width, height, margin, 12);
            reorderCameraCapturePanel();
            ResizeAnimation mapViewAnimation = new ResizeAnimation(mapWidget, width, height, deviceWidth, deviceHeight, 0);
            mapWidget.startAnimation(mapViewAnimation);
            isMapMini = false;
        }
        HideAndroidBottomNavigationBarforTrueFullScreenView();
    }

    private void resizeFPVWidget(int width, int height, int margin, int fpvInsertPosition) {
        RelativeLayout.LayoutParams fpvParams = (RelativeLayout.LayoutParams) fpvWidget.getLayoutParams();
        fpvParams.height = height;
        fpvParams.width = width;
        fpvParams.rightMargin = margin;
        fpvParams.bottomMargin = margin;
        if (isMapMini) {
            fpvParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
            fpvParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            fpvParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        } else {
            fpvParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            fpvParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            fpvParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        }
        fpvWidget.setLayoutParams(fpvParams);

        parentView.removeView(fpvWidget);
        parentView.addView(fpvWidget, fpvInsertPosition);
        HideAndroidBottomNavigationBarforTrueFullScreenView();
    }

    private void reorderCameraCapturePanel() {
        View cameraCapturePanel = findViewById(R.id.CameraCapturePanel);
        parentView.removeView(cameraCapturePanel);
        parentView.addView(cameraCapturePanel, isMapMini ? 9 : 13);
    }

    private void swapVideoSource() {
        if (secondaryFPVWidget.getVideoSource() == FPVWidget.VideoSource.SECONDARY) {
            fpvWidget.setVideoSource(FPVWidget.VideoSource.SECONDARY);
            secondaryFPVWidget.setVideoSource(FPVWidget.VideoSource.PRIMARY);
        } else {
            fpvWidget.setVideoSource(FPVWidget.VideoSource.PRIMARY);
            secondaryFPVWidget.setVideoSource(FPVWidget.VideoSource.SECONDARY);
        }
    }

    private void updateSecondaryVideoVisibility() {
        if (secondaryFPVWidget.getVideoSource() == null) {
            secondaryVideoView.setVisibility(View.GONE);
        } else {
            secondaryVideoView.setVisibility(View.VISIBLE);
        }
    }

    private void hidePanels() {
        //These panels appear based on keys from the drone itself.
        KeyManager.getInstance().setValue(CameraKey.create(CameraKey.HISTOGRAM_ENABLED), false, null);
        KeyManager.getInstance().setValue(CameraKey.create(CameraKey.COLOR_WAVEFORM_ENABLED), false, null);

        //These panels have buttons that toggle them, so call the methods to make sure the button state is correct.
        CameraControlsWidget controlsWidget = findViewById(R.id.CameraCapturePanel);
        controlsWidget.setAdvancedPanelVisibility(false);
        controlsWidget.setExposurePanelVisibility(false);

        //These panels don't have a button state, so we can just hide them.
        findViewById(R.id.pre_flight_check_list).setVisibility(View.GONE);
        //findViewById(R.id.rtk_panel).setVisibility(View.GONE);
        findViewById(R.id.spotlight_panel).setVisibility(View.GONE);
        findViewById(R.id.speaker_panel).setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Hide both the navigation bar and the status bar.
        /*View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);*/
        HideAndroidBottomNavigationBarforTrueFullScreenView();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                HideAndroidBottomNavigationBarforTrueFullScreenView();
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                deviceHeight = displayMetrics.heightPixels;
                deviceWidth = displayMetrics.widthPixels;
                if (parentView != null) {
                    deviceHeight = parentView.getHeight();
                    deviceWidth = parentView.getWidth();
                }
                Log.i(TAG, "on resume onCreate: deviceHeight = " + deviceHeight + " deviceWidth = " + deviceWidth);
                //showToast("onCreate: deviceHeight = " + deviceHeight + " deviceWidth = " + deviceWidth);
            }
        }, 4000);
        mapWidget.onResume();

        connectThermalLink();
    }

    @Override
    protected void onPause() {
        mapWidget.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapWidget.onDestroy();
        if (mTcpClient != null) {
            mTcpClient.stopClient();
            mTcpClient = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapWidget.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapWidget.onLowMemory();
    }

    private class ResizeAnimation extends Animation {

        private View mView;
        private int mToHeight;
        private int mFromHeight;

        private int mToWidth;
        private int mFromWidth;
        private int mMargin;

        private ResizeAnimation(View v, int fromWidth, int fromHeight, int toWidth, int toHeight, int margin) {
            mToHeight = toHeight;
            mToWidth = toWidth;
            mFromHeight = fromHeight;
            mFromWidth = fromWidth;
            mView = v;
            mMargin = margin;
            setDuration(300);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float height = (mToHeight - mFromHeight) * interpolatedTime + mFromHeight;
            float width = (mToWidth - mFromWidth) * interpolatedTime + mFromWidth;
            RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) mView.getLayoutParams();
            p.height = (int) height;
            p.width = (int) width;
            p.rightMargin = mMargin;
            p.bottomMargin = mMargin;
            mView.requestLayout();
            HideAndroidBottomNavigationBarforTrueFullScreenView();
        }
    }

    static void HideAndroidBottomNavigationBarforTrueFullScreenView() {
        //https://stackoverflow.com/questions/16713845/permanently-hide-navigation-bar-in-an-activity/26013850
        if (window != null) {
            View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private class DragListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        @SuppressLint("SetTextI18n")
        @Override
        public boolean onDown(MotionEvent motionEvent) {
            if (!ThermalFragShown) return false;
            panProgressOnDown = seekBarIRPan.getProgress();
            tiltProgressOnDown = seekBarIRTilt.getProgress();
            //if ((deviceWidth == 0) || (deviceHeight == 0))
            deviceHeight = displayMetrics.heightPixels;
            deviceWidth = displayMetrics.widthPixels;
            if (!TiltSensitivityRescaledPerWindowSize) {
                TiltSensitivityFactor = TiltSensitivityFactorFixed * PTZdetectionbox.getHeight() * 1.0f / SamSungTab5eHeight;
                TiltSensitivityRescaledPerWindowSize = true;
                Log.i(TAG, "onDown onScroll: PanSensitivityFactor = " + TiltSensitivityFactor);
            }
            if (!PanSensitivityRescaledPerWindowSize) {
                PanSensitivityFactor = PanSensitivityFactorFixed * PTZdetectionbox.getWidth() * 1.0f / SamSungTab5eWidth;
                PanSensitivityRescaledPerWindowSize = true;
                Log.i(TAG, "onDown onScroll: PanSensitivityFactor = " + TiltSensitivityFactor);
            }

            ThermalMinWidth_inFullScreenAspectRatio = deviceWidth / 5;
            ThermalMinHeight_inFullScreenAspectRatio = deviceHeight / 5;
            if (ThermalMinWidth_inFullScreenAspectRatio < minThermalWidth)
                ThermalMinWidth_inFullScreenAspectRatio = minThermalWidth;
            if (ThermalMinHeight_inFullScreenAspectRatio < minThermalHeight)
                ThermalMinHeight_inFullScreenAspectRatio = minThermalHeight;
            // don't return false here or else none of the other
            // gestures will work
            Xo = motionEvent.getX() + PTZdetectionbox.getX(); // The motion event is recognized only within PTZdetectionbox. Therefore to get the Absolute X and Y of the touch point,
            Yo = motionEvent.getY() + PTZdetectionbox.getY(); // we need to add X & Y of PTZdetectionbox, which is contained in a match_parent-match_parent holder.
            thermalvidfragframeXo = thermalvidfragframe.getX();
            thermalvidfragframeYo = thermalvidfragframe.getY();
            thermalvidW = thermalvidfragframe.getWidth();
            thermalvidH = thermalvidfragframe.getHeight();
            parms = (RelativeLayout.LayoutParams) thermalvidfragframe.getLayoutParams();
            thermalvidfragframeRo = parms.rightMargin;
            thermalvidfragframeBo = parms.bottomMargin;
            if (!ThermalWindowTiltable) {
                if ((CloseToPoint(Xo, Yo, thermalvidfragframeXo, thermalvidfragframeYo, HalfFingerSize, HalfFingerSize)
                        || CloseToPoint(Xo, Yo, thermalvidfragframeXo + thermalvidW, thermalvidfragframeYo, HalfFingerSize, HalfFingerSize)
                        || CloseToPoint(Xo, Yo, thermalvidfragframeXo, thermalvidfragframeYo + thermalvidH, HalfFingerSize, HalfFingerSize)
                        || CloseToPoint(Xo, Yo, thermalvidfragframeXo + thermalvidW, thermalvidfragframeYo + thermalvidH, HalfFingerSize, HalfFingerSize))
                        && (thermalvidfragframe != null)) {
                    ThermalWindowResizeable = true;
                    //showToast("Drag to resize thermal view");
                    showHintMessage("Drag to resize thermal view");
                } else {
                    ThermalWindowResizeable = false;
                }

                if ((Xo >= thermalvidfragframeXo)
                        && (Xo <= thermalvidfragframeXo + thermalvidW)
                        && (Yo >= thermalvidfragframeYo)
                        && (Yo <= thermalvidfragframeYo + thermalvidH)
                        && (!ThermalWindowResizeable)  // Resize will exclude move: we don't want move and resize at the same time.
                        && (thermalvidfragframe != null)) {
                    ThermalWindowDraggable = true;
                    //showToast("Drag to move thermal view");
                    showHintMessage("Drag to move thermal view");
                } else {
                    ThermalWindowDraggable = false;
                }
            }
            //Log.i(TAG, "onDown: FXo = " + thermalvidfragframeXo + " Xo = " + Xo + " R = " + (thermalvidfragframeXo + thermalvidW) + " FYo = " + thermalvidfragframeYo + " Yo = " + Yo + " B = " + (thermalvidfragframeYo + thermalvidH));
            //Log.i(TAG, "onDown: ThermalWindowResizeable = " + ThermalWindowResizeable + " ThermalWindowDraggable = " + ThermalWindowDraggable);
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float distanceX, float distanceY) {
            if (!ThermalFragShown) return false;
            //Log.i(TAG, "onScroll: PanSensitivityFactor ThermalWindowTiltable = " + ThermalWindowTiltable);
            if (ThermalWindowResizeable || ThermalWindowDraggable && !ThermalWindowTiltable) {
                float scrollX = motionEvent1.getX() + PTZdetectionbox.getX();
                float scrollY = motionEvent1.getY() + PTZdetectionbox.getY();
                float dx = scrollX - Xo;
                float dy = scrollY - Yo;
                float Xnow = thermalvidfragframeXo, Ynow = thermalvidfragframeYo, Wnow = thermalvidW, Hnow = thermalvidH;
                int Rnow = parms.rightMargin, Bnow = parms.bottomMargin;
                int initTouchPoint = 0;
                //getAction() == MotionEvent.ACTION_UP
                if (ThermalWindowResizeable) {
                    if (CloseToPoint(Xo, Yo, thermalvidfragframeXo, thermalvidfragframeYo, HalfFingerSize, HalfFingerSize)) {
                        Xnow = thermalvidfragframeXo + dx;
                        Ynow = thermalvidfragframeYo + dy;
                        Wnow = thermalvidW - dx;
                        Hnow = thermalvidH - dy;
                        initTouchPoint = 0;
                    } else if (CloseToPoint(Xo, Yo, thermalvidfragframeXo + thermalvidW, thermalvidfragframeYo, HalfFingerSize, HalfFingerSize)) {
                        Xnow = thermalvidfragframeXo;
                        Ynow = thermalvidfragframeYo + dy;
                        Wnow = thermalvidW + dx;
                        Hnow = thermalvidH - dy;
                        Rnow = thermalvidfragframeRo - (int) dx;
                        initTouchPoint = 1;
                    } else if (CloseToPoint(Xo, Yo, thermalvidfragframeXo, thermalvidfragframeYo + thermalvidH, HalfFingerSize, HalfFingerSize)) {
                        Xnow = thermalvidfragframeXo + dx;
                        Ynow = thermalvidfragframeYo;
                        Wnow = thermalvidW - dx;
                        Hnow = thermalvidH + dy;
                        Bnow = thermalvidfragframeBo - (int) dy;
                        initTouchPoint = 2;
                    } else if (CloseToPoint(Xo, Yo, thermalvidfragframeXo + thermalvidW, thermalvidfragframeYo + thermalvidH, HalfFingerSize, HalfFingerSize)) {
                        Xnow = thermalvidfragframeXo;
                        Ynow = thermalvidfragframeYo;
                        Wnow = thermalvidW + dx;
                        Hnow = thermalvidH + dy;
                        Rnow = thermalvidfragframeRo - (int) dx;
                        Bnow = thermalvidfragframeBo - (int) dy;
                        initTouchPoint = 3;
                    }

                    // Limit to the minimum window dimensions, or the map view becomes too small to adjust and may cause crash
                    if ((Wnow >= ThermalMinWidth_inFullScreenAspectRatio) || (Hnow >= ThermalMinHeight_inFullScreenAspectRatio)) {
                        if (Wnow >= ThermalMinWidth_inFullScreenAspectRatio) {
                            if (((initTouchPoint == 1 || initTouchPoint == 3) && (scrollX > ThermalMinWidth_inFullScreenAspectRatio / 2))
                                    || ((initTouchPoint == 0 || initTouchPoint == 2) && (scrollX < (deviceWidth - ThermalMinWidth_inFullScreenAspectRatio / 2)))) {
                                parms.width = (int) Wnow;
                                //Log.i(TAG, "onScroll: scrollX = " + scrollX);
                                thermalvidfragframe.setX(Xnow);
                            }
                        }
                        if (Hnow >= ThermalMinHeight_inFullScreenAspectRatio) {
                            if (((initTouchPoint == 2 || initTouchPoint == 3) && (scrollY > ThermalMinHeight_inFullScreenAspectRatio / 2))
                                    || ((initTouchPoint == 0 || initTouchPoint == 1) && (scrollY < (deviceHeight - ThermalMinHeight_inFullScreenAspectRatio / 2)))) {
                                parms.height = (int) Hnow;
                                //Log.i(TAG, "onScroll: scrollY = " + scrollY);
                                //thermalvidfragframe.setLayoutParams(parms);
                                thermalvidfragframe.setY(Ynow);
                            }
                        }
                        thermalvidfragframe.setLayoutParams(parms);
                        TiltSensitivityRescaledPerWindowSize = false;
                        PanSensitivityRescaledPerWindowSize = false;

                    } else if ((Wnow < ThermalMinWidth_inFullScreenAspectRatio) && (Hnow < ThermalMinHeight_inFullScreenAspectRatio)) {
                        if (System.currentTimeMillis() - CantToastMillis > 2000) {
                            CantToastMillis = System.currentTimeMillis();
                            //showToast("Can't make smaller");
                            showHintMessage("Can't make smaller");
                        }
                    }
                } else if (ThermalWindowDraggable) {
                    float xnow = thermalvidfragframeXo + dx, ynow = thermalvidfragframeYo + dy;
                    if ((xnow > -(thermalvidfragframe.getWidth() - ThermalMinWidth_inFullScreenAspectRatio / 2))
                            && (xnow < deviceWidth - ThermalMinWidth_inFullScreenAspectRatio / 2)) {
                        thermalvidfragframe.setX(xnow);
                        cantMoveMapFurtherHorizontal = false;
                    } else {
                        if (System.currentTimeMillis() - CantToastMillis > 2000) {
                            CantToastMillis = System.currentTimeMillis();
                            cantMoveMapFurtherHorizontal = true;
                            //showToast("Can't move further");
                            showHintMessage("Can't move further");
                        }
                    }
                    if ((ynow > 0)// - ThermalMinHeight_inFullScreenAspectRatio / 2)
                            && (ynow < deviceHeight - ThermalMinHeight_inFullScreenAspectRatio / 2)) {
                        thermalvidfragframe.setY(ynow);
                        isCantMoveMapFurtherVertical = false;
                    } else {
                        if (System.currentTimeMillis() - CantToastMillis > 2000) {
                            CantToastMillis = System.currentTimeMillis();
                            isCantMoveMapFurtherVertical = true;
                            //showToast("Can't move further");
                            showHintMessage("Can't move further");
                        }
                    }
                }
                try {
                    SharedPreferences settings = getSharedPreferences(CompleteWidgetActivity.PREFS_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    if (buttonMSX.isChecked()) {
                        editor.putInt("Thermal MSX Xi", (int) thermalvidfragframe.getX())
                                .putInt("Thermal MSX Yi", (int) thermalvidfragframe.getY())
                                .putInt("Thermal MSX Wi", thermalvidfragframe.getWidth())
                                .putInt("Thermal MSX Hi", thermalvidfragframe.getHeight())
                                .apply();
                    } else {
                        editor.putInt("Thermal Xi", (int) thermalvidfragframe.getX())
                                .putInt("Thermal Yi", (int) thermalvidfragframe.getY())
                                .putInt("Thermal Wi", thermalvidfragframe.getWidth())
                                .putInt("Thermal Hi", thermalvidfragframe.getHeight())
                                .apply();
                    }
                    //ServiceBase.getServiceBase().getVideoService().Stop();
                } catch (Exception e) {
                    Log.d("CompleteWidgetActivity", ".getVideoService().Resume got exception " + e.toString());
                }
            } else if (ThermalWindowTiltable) {
                int tiltnow = seekBarIRTilt.getProgress(), pannow = seekBarIRPan.getProgress();
                viewminX = PTZdetectionbox.getX();
                viewminY = PTZdetectionbox.getY();
                viewmaxX = PTZdetectionbox.getWidth() + viewminX;
                viewmaxY = PTZdetectionbox.getHeight() + viewminY;

                float X = motionEvent.getX() + viewminX, Y = motionEvent.getY() + viewminY;
                float rangeX = 0, rangeY = 0;

                rangeX = (viewmaxX - viewminX);
                rangeY = (viewmaxY - viewminY);
                if (X < viewmaxX - rangeX / 4) {
                    pannow = normalizevalue(panProgressOnDown + (int) ((motionEvent1.getX() - motionEvent.getX()) / PanSensitivityFactor), 0, seekBarIRPan.getMax());
                }
                if (X > viewminX + rangeX / 4) { // overlapping middle 1/3, where it resets both
                    tiltnow = normalizevalue(tiltProgressOnDown - (int) ((motionEvent1.getY() - motionEvent.getY()) / TiltSensitivityFactor), 0, seekBarIRTilt.getMax());
                }

                panseekbarVisibility = View.VISIBLE;
                seekBarIRPan.setVisibility(panseekbarVisibility);
                seekBarIRPan.setX(thermalvidfragframeXo + ((PTZdetectionbox.getWidth() - seekBarIRPan.getWidth()) / 2));
                seekBarIRPan.setY(thermalvidfragframeYo);
                seekBarIRTilt.setProgress(tiltnow);
                seekBarIRPan.setProgress(pannow);
                if (tiltnow != tiltprogress_pre) {
                    IR_DJI_TiltDiff_FineTuned = tiltnow - IR_DJI_TiltDiff_FineTuned_start;
                    AirGroundCom.sendG2Amessage(tiltnow, AirGroundCom.TILT_CHANNEL);
                    //SendG2AMessage(tiltnow, AirGroundCom.TILT_CHANNEL);
                    tiltprogress_pre = tiltnow;
                }
                if (pannow != panprogress_pre) {
                    AirGroundCom.sendG2Amessage(pannow, AirGroundCom.PAN_CHANNEL);
                    //SendG2AMessage(pannow, AirGroundCom.PAN_CHANNEL);
                    panprogress_pre = pannow;
                }

                SaveUserSettingInt("Thermal Pan", seekBarIRPan.getProgress());
                //Log.d(TAG, "onScroll: (int) (distanceX / PanSensitivityFactor) = " + (distanceX / PanSensitivityFactor));
                //Log.d(TAG, "onScroll: distanceX since onDown = " + (motionEvent1.getX() - motionEvent.getX()));
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {
            if (!ThermalFragShown) return;
            /*showToast("Slide to fine tune tilt & pan");
            ThermalWindowResizeable = false;
            ThermalWindowDraggable = false;
            ThermalWindowTiltable = true;*/
        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float velocityX, float velocityY) {
            Log.d(ContentValues.TAG, "onTouch onFling: velocityX = " + velocityX + " velocityY = " + velocityY);
            /*int progress = 50;
            tiltseekBar.setProgress(progress);
            tiltprogressBar.setProgress(progress);
            AirGroundCom.sendG2Amessage(progress, AirGroundCom.TILT_CHANNEL);*/
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            return false;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {
            if (!ThermalFragShown) return false;
            viewminX = PTZdetectionbox.getX();
            viewminY = PTZdetectionbox.getY();
            viewmaxX = PTZdetectionbox.getWidth() + viewminX;
            viewmaxY = PTZdetectionbox.getHeight() + viewminY;

            float X = motionEvent.getX() + viewminX, Y = motionEvent.getY() + viewminY;
            float rangeX = 0, rangeY = 0;
            rangeX = (viewmaxX - viewminX);
            rangeY = (viewmaxY - viewminY);
            //Log.d(ContentValues.TAG, "onDoubleTap: X = " + X + " from min " + viewminX + " to " + viewmaxX);
            //Log.d(ContentValues.TAG, "onDoubleTap: Y = " + Y + " from min " + viewminY + " to " + viewmaxY);
            int tiltnow = seekBarIRTilt.getProgress(), pannow = seekBarIRPan.getProgress();
            if (Y > viewminY + rangeY / 3) {
                if (X < viewmaxX - rangeX / 3) {
                    //pannow = 50;
                    tiltnow = seekBarIRTilt.getMax() / 2;
                }
                if (X > viewminX + rangeX / 3) { // overlapping middle 1/3, where it resets both
                    //tiltnow = 50;
                    pannow = seekBarIRPan.getMax() / 2;
                }
                seekBarIRPan.setVisibility(View.VISIBLE);
                seekBarIRPan.setProgress(pannow);
                seekBarIRTilt.setProgress(tiltnow);
                AirGroundCom.sendG2Amessage(tiltnow, AirGroundCom.TILT_CHANNEL);
                AirGroundCom.sendG2Amessage(pannow, AirGroundCom.PAN_CHANNEL);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        seekBarIRPan.setVisibility(View.GONE);
                    }
                }, 2000);
                //SendG2AMessage(pannow, AirGroundCom.PAN_CHANNEL);
            } else {
                ThermalWindowTiltable = !ThermalWindowTiltable;
                if (ThermalWindowTiltable) {
                    //showToast("Slide to fine tune tilt & pan");
                    showHintMessage("Slide to fine tune tilt & pan");
                    textviewmode.setText("Pan&Tilt");
                    IR_DJI_TiltDiff_FineTuned_start = seekBarIRTilt.getProgress();
                } else {
                    //showToast("Back to move/resize");
                    showHintMessage("Back to move/resize");
                    textviewmode.setText("Move/Resize");
                }
            }
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
            return false;
        }
    }

    private float mScaleFactor = 1.0f, mScaleFactor_pre = 1.0f;
    private float VideoFocusX, VideoFocusY, VideoFocusX_pre, VideoFocusY_pre;
    private int intscale = 10, inscale_pre = 10;
    public static float realScaleFactor = 1.0f;

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mScaleFactor_pre = mScaleFactor;
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, 8.0f));
            VideoFocusX_pre = VideoFocusX;
            VideoFocusY_pre = VideoFocusY;
            VideoFocusX = scaleGestureDetector.getFocusX();// + PTZdetectionbox.getX();
            VideoFocusY = scaleGestureDetector.getFocusY();// + PTZdetectionbox.getY();
            @SuppressLint("DefaultLocale") String Scale1digit = String.format("%.01fX", mScaleFactor);
            textViewZoomScale.setText(Scale1digit);
            textViewZoomScale.setVisibility(View.VISIBLE);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    textViewZoomScale.setVisibility(View.INVISIBLE);
                }
            }, 1000);
            //showToast(Scale1digit);

            // TODO: if doing MAVLink Geotagging, the camera zoom is NOT available. So let's not refactor the scalefactor
            inscale_pre = intscale;
            if (mScaleFactor < 2.0f) {
                realScaleFactor = mScaleFactor;
                intscale = 0;
            } else if (mScaleFactor < 4.0f) {
                realScaleFactor = mScaleFactor / 2.0f;
                intscale = 1;
            } else {
                realScaleFactor = mScaleFactor / 4.0f;
                intscale = 2;
            }
            //TODO need to receive confirmation from air end if zoom is available
            //FullScreenVideoActivity.ScaleVideo(realScaleFactor); // Let's just still do real
            //ScaleVideo(realScaleFactor);
            if (isBoson || isBosonPi || isBosonPiM) {//TODO: allow user to choose between tablet screen zooming and IR camera zooming
                ScaleVideo(mScaleFactor, VideoFocusX, VideoFocusY);
            } else {
                ScaleVideo(realScaleFactor, VideoFocusX, VideoFocusY);
            }
            if (intscale != inscale_pre) {
                SetCameraZoom(intscale);
            }
            //FullScreenVideoActivity.ScaleVideo(mScaleFactor, VideoFocusX, VideoFocusY);
            //FullScreenVideoActivity.ScaleVideo(mScaleFactor, mScaleFactor_pre, VideoFocusX, VideoFocusX_pre, VideoFocusY, VideoFocusY_pre);
            Log.d(ContentValues.TAG, "ScaleListener onScale: realScaleFactor = " + realScaleFactor);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            super.onScaleBegin(scaleGestureDetector);
            Log.d(ContentValues.TAG, "ScaleListener onScaleBegin: ");
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            Log.d(ContentValues.TAG, "ScaleListener onScaleEnd: ");
            super.onScaleEnd(detector);
        }
    }

    private void SetCameraZoom(int intScale) {
        if (mTcpClient != null) {
            AirGroundCom.sendG2Amessage(intScale, AirGroundCom.ZOOM_CHANNEL);
        }
    }

    public static void ScaleVideo(float scalefactor) {
        //textureViewThermalFrag.setPivotX(100);
        //textureViewThermalFrag.setPivotY(1000);
        Log.d(TAG, "ScaleVideo: pivotX = " + textureViewThermalFrag.getPivotX() + " pivotY = " + textureViewThermalFrag.getPivotY());
        textureViewThermalFrag.setScaleX(scalefactor);
        textureViewThermalFrag.setScaleY(scalefactor);
        //Log.i(TAG, "ScaleVideo: X = " + textureViewThermalFrag.getX() + " Y = " + textureViewThermalFrag.getY());
    }

    private static float pivotXo, pivotYo;

    public static void ScaleVideo(float scalefactor, float VideoFocusX, float VideoFocusY) {
        float scale = scalefactor;
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (VideoFocusX != 0) {
                textureViewThermalFrag.setPivotX(VideoFocusX);
            } else {
                textureViewThermalFrag.resetPivot();
            }
            if (VideoFocusY != 0) {
                textureViewThermalFrag.setPivotY(VideoFocusY);
            } else {
                textureViewThermalFrag.resetPivot();
            }
            if (scale <= 1.01f) textureViewThermalFrag.resetPivot();
            textureViewThermalFrag.setScaleX(scale * scalefactorX);
            textureViewThermalFrag.setScaleY(scale);
        } else*/
        if (scale <= 1.01f) {
            textureViewThermalFrag.setPivotX(pivotXo);
            textureViewThermalFrag.setPivotY(pivotYo);
        } else {
            if (VideoFocusX != 0) {
                textureViewThermalFrag.setPivotX(VideoFocusX);
            }
            if (VideoFocusY != 0) {
                textureViewThermalFrag.setPivotY(VideoFocusY);
            }
        }
        textureViewThermalFrag.setScaleX(scale);// * scalefactorX);
        textureViewThermalFrag.setScaleY(scale);
        //Log.d(TAG, "ScaleVideo: pivotX = " + textureViewThermalFrag.getPivotX() + " pivotY = " + textureViewThermalFrag.getPivotY());
    }

    private boolean CloseToPoint(float x, float y, float pointx, float pointy, float marginx, float marginy) {
        return (x >= pointx - marginx)
                && (x <= pointx + marginx)
                && (y >= pointy - marginy)
                && (y <= pointy + marginy);
    }

    public static void ScalePTZdetectionbox(int size) {
        //ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) PTZdetectionbox.getLayoutParams();
        //params.width = size;
        //PTZdetectionbox.setLayoutParams(params);
    }

    private int normalizevalue(int i, int min, int max) {
        if (i < min) i = min;
        if (i > max) i = max;
        return i;
    }

    private void PanTiltviaPWM(int axis, int progress) {
        int value;
        if (axis == 0) {
            value = (int) (136.0d - (3.0d * (progress * 0.27d)));
        } else {
            value = (int) (50.0d + (3.0d * (progress * 0.27d)));
        }
        try {
            ServiceBase.getServiceBase().getVideoService().sendpwm(axis, value);
        } catch (Exception e) {
            Log.d(ContentValues.TAG, "set camera " + axis + " got exception " + e.toString());
        }
    }

    public static int IRx, IRy, IRw = 0, IRh = 0;

    private int Normalize(int valuein, int min, int max) {
        if (valuein < min) return min;
        if (valuein > max) return max;
        return valuein;
    }

    private float Normalize(float valuein, float min, float max) {
        if (valuein < min) return min;
        if (valuein > max) return max;
        return valuein;
    }

    private void RestoreUserSettings() {
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        IRw = Normalize(settings.getInt("Thermal Wi", DensityUtil.dip2px(this, 400)), 400, deviceWidth - 200);
        IRh = Normalize(settings.getInt("Thermal Hi", DensityUtil.dip2px(this, 320)), 320, deviceHeight - 120);
        IRx = Normalize(settings.getInt("Thermal Xi", DensityUtil.dip2px(this, 100)), 100, deviceWidth - (IRw + 100));
        IRy = Normalize(settings.getInt("Thermal Yi", deviceHeight - (IRh + DensityUtil.dip2px(this, 200))), 200, deviceHeight - (IRh + 100));
        int panprogress = Normalize(settings.getInt("Thermal Pan", seekBarIRPan.getMax() / 2), 0, seekBarIRPan.getMax());
        seekBarIRPan.setProgress(panprogress);
        if (mTcpClient != null)
            AirGroundCom.sendG2Amessage(panprogress, AirGroundCom.PAN_CHANNEL);
        int transparencyProgress = Normalize(settings.getInt("Thermal Transparency", 100), 0, 100);
        seekBarIRTransparency.setProgress(transparencyProgress);

        Log.i(TAG, "RestoreUserSettings: ResizeThermalView IRw = " + IRw + " IRh = " + IRh + " IRx = " + IRx + " IRy = " + IRy);
        RestorThermalViewWindowDimensions(IRh, IRw, IRx, IRy);
    }

    private void RestorThermalViewWindowDimensions(int irheight, int irwidth, int start, int top) {
        int bottom;
        bottom = deviceHeight - (irheight + top);
        Log.i(TAG, "RestorThermalViewWindowDimensions: RestoreUserSettings bottom = " + bottom);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) thermalvidfragframe.getLayoutParams();// new RelativeLayout.LayoutParams(irwidth, irheight);
        layoutParams.setMarginStart(start);
        layoutParams.alignWithParent = true;
        layoutParams.bottomMargin = bottom;
        layoutParams.width = irwidth;
        layoutParams.height = irheight;
        //layoutParams.topMargin = top;
        thermalvidfragframe.setLayoutParams(layoutParams);
    }

    private void SaveUserSettingInt(String settingName, int settingValue) {
        try {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(settingName, settingValue);
            editor.apply();
        } catch (Exception ignored) {
        }
    }

    private void SaveUserSettingLong(String settingName, long settingValue) {
        try {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong(settingName, settingValue);
            editor.apply();
        } catch (Exception ignored) {
        }
    }

    private void SaveUserSettingBoolean(String settingName, boolean settingValue) {
        try {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(settingName, settingValue);
            editor.apply();
        } catch (Exception ignored) {
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mTcpClient != null) {
            mTcpClient.stopClient();
            mTcpClient = null;
        }
        if (!isFinishing()) {
            try {
                //ServiceBase.getServiceBase().getVideoService().Pause("completeactivity");
                ServiceBase.getServiceBase().getVideoService().Stop();
            } catch (Exception e2) {
                Log.e(TAG, "CompleteActivity ServiceBase.getServcieBase().getVideoService().Stop() got exception " + e2.toString());
            }
        }
        if (!buttonMSX.isChecked()) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("Thermal Xi", (int) thermalvidfragframe.getX())
                    .putInt("Thermal Yi", (int) thermalvidfragframe.getY())
                    .putInt("Thermal Wi", thermalvidfragframe.getWidth())
                    .putInt("Thermal Hi", thermalvidfragframe.getHeight())
                    .putInt("Thermal Pan", seekBarIRPan.getProgress())
                    .putInt("Thermal Transparency", seekBarIRTransparency.getProgress())
                    .apply();
        }
    }

    @Override
    public void onBackPressed() {
        // Do nothing! will not allow it to back
        //super.onBackPressed();
    }
}

/*import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.dji.mapkit.core.maps.DJIMap;
import com.dji.mapkit.core.models.DJILatLng;

import dji.keysdk.CameraKey;
import dji.keysdk.KeyManager;
import dji.ux.widget.FPVWidget;
import dji.ux.widget.MapWidget;
import dji.ux.widget.controls.CameraControlsWidget;

/**
 * Activity that shows all the UI elements together
 */
/*
public class CompleteWidgetActivity extends Activity {

    private MapWidget mapWidget;
    private ViewGroup parentView;
    private FPVWidget fpvWidget;
    private FPVWidget secondaryFPVWidget;
    private RelativeLayout primaryVideoView;
    private FrameLayout secondaryVideoView;
    private boolean isMapMini = true;

    private int height;
    private int width;
    private int margin;
    private int deviceWidth;
    private int deviceHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_widgets);

        height = DensityUtil.dip2px(this, 100);
        width = DensityUtil.dip2px(this, 150);
        margin = DensityUtil.dip2px(this, 12);

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        Point outPoint = new Point();
        display.getRealSize(outPoint);
        deviceHeight = outPoint.y;
        deviceWidth = outPoint.x;

        mapWidget = findViewById(R.id.map_widget);
        mapWidget.initAMap(new MapWidget.OnMapReadyListener() {
            @Override
            public void onMapReady(@NonNull DJIMap map) {
                map.setOnMapClickListener(new DJIMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(DJILatLng latLng) {
                        onViewClick(mapWidget);
                    }
                });
            }
        });
        mapWidget.onCreate(savedInstanceState);

        parentView = (ViewGroup) findViewById(R.id.root_view);

        fpvWidget = findViewById(R.id.fpv_widget);
        fpvWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewClick(fpvWidget);
            }
        });
        primaryVideoView = (RelativeLayout) findViewById(R.id.fpv_container);
        secondaryVideoView = (FrameLayout) findViewById(R.id.secondary_video_view);
        secondaryFPVWidget = findViewById(R.id.secondary_fpv_widget);
        secondaryFPVWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapVideoSource();
            }
        });
        updateSecondaryVideoVisibility();
    }

    private void onViewClick(View view) {
        if (view == fpvWidget && !isMapMini) {
            resizeFPVWidget(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, 0, 0);
            reorderCameraCapturePanel();
            ResizeAnimation mapViewAnimation = new ResizeAnimation(mapWidget, deviceWidth, deviceHeight, width, height, margin);
            mapWidget.startAnimation(mapViewAnimation);
            isMapMini = true;
        } else if (view == mapWidget && isMapMini) {
            hidePanels();
            resizeFPVWidget(width, height, margin, 12);
            reorderCameraCapturePanel();
            ResizeAnimation mapViewAnimation = new ResizeAnimation(mapWidget, width, height, deviceWidth, deviceHeight, 0);
            mapWidget.startAnimation(mapViewAnimation);
            isMapMini = false;
        }
    }

    private void resizeFPVWidget(int width, int height, int margin, int fpvInsertPosition) {
        RelativeLayout.LayoutParams fpvParams = (RelativeLayout.LayoutParams) primaryVideoView.getLayoutParams();
        fpvParams.height = height;
        fpvParams.width = width;
        fpvParams.rightMargin = margin;
        fpvParams.bottomMargin = margin;
        if (isMapMini) {
            fpvParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
            fpvParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            fpvParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        } else {
            fpvParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            fpvParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            fpvParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        }
        primaryVideoView.setLayoutParams(fpvParams);

        parentView.removeView(primaryVideoView);
        parentView.addView(primaryVideoView, fpvInsertPosition);
    }

    private void reorderCameraCapturePanel() {
        View cameraCapturePanel = findViewById(R.id.CameraCapturePanel);
        parentView.removeView(cameraCapturePanel);
        parentView.addView(cameraCapturePanel, isMapMini ? 9 : 13);
    }

    private void swapVideoSource() {
        if (secondaryFPVWidget.getVideoSource() == FPVWidget.VideoSource.SECONDARY) {
            fpvWidget.setVideoSource(FPVWidget.VideoSource.SECONDARY);
            secondaryFPVWidget.setVideoSource(FPVWidget.VideoSource.PRIMARY);
        } else {
            fpvWidget.setVideoSource(FPVWidget.VideoSource.PRIMARY);
            secondaryFPVWidget.setVideoSource(FPVWidget.VideoSource.SECONDARY);
        }
    }

    private void updateSecondaryVideoVisibility() {
        if (secondaryFPVWidget.getVideoSource() == null) {
            secondaryVideoView.setVisibility(View.GONE);
        } else {
            secondaryVideoView.setVisibility(View.VISIBLE);
        }
    }

    private void hidePanels() {
        //These panels appear based on keys from the drone itself.
        if (KeyManager.getInstance() != null) {
            KeyManager.getInstance().setValue(CameraKey.create(CameraKey.HISTOGRAM_ENABLED), false, null);
            KeyManager.getInstance().setValue(CameraKey.create(CameraKey.COLOR_WAVEFORM_ENABLED), false, null);
        }

        //These panels have buttons that toggle them, so call the methods to make sure the button state is correct.
        CameraControlsWidget controlsWidget = findViewById(R.id.CameraCapturePanel);
        controlsWidget.setAdvancedPanelVisibility(false);
        controlsWidget.setExposurePanelVisibility(false);

        //These panels don't have a button state, so we can just hide them.
        findViewById(R.id.pre_flight_check_list).setVisibility(View.GONE);
        findViewById(R.id.rtk_panel).setVisibility(View.GONE);
        findViewById(R.id.spotlight_panel).setVisibility(View.GONE);
        findViewById(R.id.speaker_panel).setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Hide both the navigation bar and the status bar.
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        mapWidget.onResume();
    }

    @Override
    protected void onPause() {
        mapWidget.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapWidget.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapWidget.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapWidget.onLowMemory();
    }

    private class ResizeAnimation extends Animation {

        private View mView;
        private int mToHeight;
        private int mFromHeight;

        private int mToWidth;
        private int mFromWidth;
        private int mMargin;

        private ResizeAnimation(View v, int fromWidth, int fromHeight, int toWidth, int toHeight, int margin) {
            mToHeight = toHeight;
            mToWidth = toWidth;
            mFromHeight = fromHeight;
            mFromWidth = fromWidth;
            mView = v;
            mMargin = margin;
            setDuration(300);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float height = (mToHeight - mFromHeight) * interpolatedTime + mFromHeight;
            float width = (mToWidth - mFromWidth) * interpolatedTime + mFromWidth;
            RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) mView.getLayoutParams();
            p.height = (int) height;
            p.width = (int) width;
            p.rightMargin = mMargin;
            p.bottomMargin = mMargin;
            mView.requestLayout();
        }
    }
}
*/