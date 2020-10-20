package com.suas.uxdual;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.suas.uxdual.internal.controller.DJISampleApplication;
import com.suas.uxdual.internal.utils.CallbackHandlers;
import com.suas.uxdual.internal.utils.ModuleVerificationUtil;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import dji.common.airlink.LightbridgeDataRate;
import dji.common.airlink.LightbridgeFrequencyBand;
import dji.common.airlink.OcuSyncBandwidth;
import dji.common.airlink.OcuSyncFrequencyBand;
import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.flightcontroller.RTKState;
import dji.common.gimbal.CapabilityKey;
import dji.common.gimbal.GimbalState;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.model.LocationCoordinate2D;
import dji.common.remotecontroller.GimbalAxis;
import dji.common.remotecontroller.HardwareState;
import dji.common.util.CommonCallbacks;
import dji.common.util.DJIParamCapability;
import dji.common.util.DJIParamMinMaxCapability;
import dji.sdk.airlink.AirLink;
import dji.sdk.airlink.LightbridgeLink;
import dji.sdk.airlink.OcuSyncLink;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.products.Aircraft;
import dji.sdk.remotecontroller.RemoteController;
import dji.sdk.sdkmanager.DJISDKManager;

import static android.content.ContentValues.TAG;
import static com.suas.uxdual.CompleteWidgetActivity.IR_DJI_TiltDiff_FineTuned;
import static com.suas.uxdual.CompleteWidgetActivity.seekBarIRTilt;
import static com.suas.uxdual.CompleteWidgetActivity.textviewFreg;
import static com.suas.uxdual.SetLimitsFragment.buttonenableMaxRadius;
import static com.suas.uxdual.SetLimitsFragment.currentmaxflightheight;
import static com.suas.uxdual.SetLimitsFragment.currentmaxflightradius;
import static com.suas.uxdual.SetLimitsFragment.currentmaxflightradiusstatus;
import static com.suas.uxdual.SetLimitsFragment.maxFlightAltitude;
import static com.suas.uxdual.SetLimitsFragment.maxFlightDistance;
import static com.suas.uxdual.ThermalVideoFrag.copyGPSdata;
import static dji.common.airlink.LightbridgeDataRate.BANDWIDTH_10_MBPS;
import static dji.common.airlink.LightbridgeDataRate.BANDWIDTH_4_MBPS;
import static dji.common.airlink.LightbridgeDataRate.BANDWIDTH_6_MBPS;
import static dji.common.airlink.LightbridgeDataRate.BANDWIDTH_8_MBPS;
import static dji.common.airlink.LightbridgeFrequencyBand.FREQUENCY_BAND_2_DOT_4_GHZ;
import static dji.common.airlink.OcuSyncBandwidth.Bandwidth10MHz;
import static dji.common.airlink.OcuSyncBandwidth.Bandwidth20MHz;
import static dji.common.airlink.OcuSyncBandwidth.Unknown;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GimbalControlFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GimbalControlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GimbalControlFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private Timer timer;
    private GimbalRotateTimerTask gimbalRotationTimerTask;
    private Gimbal gimbal = null;
    private int currentGimbalId = 0;
    Camera[] camera = {null, null};
    private int NoOfCameras = 0;
    private int currentCameraId = 0;
    SeekBar gimbaltiltseekbar;
    SeekBar seekBarDJIZoom;
    Handler updateConversationHandler;
    private long gimbalqueryMillis = System.currentTimeMillis();
    private long cameraqueryMillis = System.currentTimeMillis();
    private long gpsqueryMillis = System.currentTimeMillis();
    private long minmaxqueryMillis = System.currentTimeMillis();
    private long rcqueryMillis = System.currentTimeMillis();
    private long alqueryMillis = System.currentTimeMillis();
    long sendtiltMillis = System.currentTimeMillis();
    TextView textViewGPS;
    Number minPitchValue, maxPitchValue;
    FlightController flightController = null;
    RemoteController remoteController = null;
    static AirLink airLink = null;
    static Button updateflightlimits;
    static Button getcurrentflightlimits;
    static Button setcurrentflightlimitsstatus;
    static Button check24Button;
    static LinearLayout videobandlinearlayout;
    private Button hidebutton;
    private RadioGroup LBbandwidthRG;
    private RadioGroup occuBandWidthRG;
    private TextView videorateTextview;


    public GimbalControlFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GimbalControlFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GimbalControlFragment newInstance(String param1, String param2) {
        GimbalControlFragment fragment = new GimbalControlFragment();
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
        View view = inflater.inflate(R.layout.fragment_gimbal_control, container, false);

        updateConversationHandler = new Handler();

        gimbaltiltseekbar = (SeekBar) view.findViewById(R.id.seekBarDJITilt);
        seekBarDJIZoom = (SeekBar) view.findViewById(R.id.seekBarDJIZoom);
        textViewGPS = view.findViewById(R.id.textViewGPS);
        updateflightlimits = view.findViewById(R.id.updateflightlimits);
        getcurrentflightlimits = view.findViewById(R.id.getcurrentflightlimits);
        setcurrentflightlimitsstatus = view.findViewById(R.id.setcurrentflightlimitsstatus);
        check24Button = view.findViewById(R.id.check24Button);
        videobandlinearlayout = view.findViewById(R.id.videobandlinearlayout);
        hidebutton = view.findViewById(R.id.hidebutton);
        occuBandWidthRG = view.findViewById(R.id.occuBandWidthRG);
        LBbandwidthRG = view.findViewById(R.id.LBbandwidthRG);
        videorateTextview = view.findViewById(R.id.videorateTextview);

        gimbaltiltseekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (tiltbyseekbar) {
                    if (System.currentTimeMillis() - sendtiltMillis > (long) (tiltdelay * 1000) + 5) {
                        sendtiltMillis = System.currentTimeMillis();
                        float tilt = mappedGValue(progress, gimbaltiltseekbar.getMax());//(progress - 900) * 0.1f;
                        TiltDJIGimbal(tilt);
                        setThermalTiltSeekbar(tilt);
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tiltbyseekbar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tiltbyseekbar = false;
                    }
                }, (long) (tiltdelay * 1000) + 200);
            }
        });

        seekBarDJIZoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (camera[currentCameraId] != null && doOpticalZoom) {
                    final SettingsDefinitions.ZoomSpeed speed = zoomSpeed(Math.abs(progress - 50), 50);
                    final SettingsDefinitions.ZoomDirection direction;
                    if (progress > 50)
                        direction = SettingsDefinitions.ZoomDirection.ZOOM_IN;
                    else if (progress < 50)
                        direction = SettingsDefinitions.ZoomDirection.ZOOM_OUT;
                    else direction = SettingsDefinitions.ZoomDirection.UNKNOWN;
                    camera[currentCameraId].startContinuousOpticalZoom(direction, speed,
                            new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {

                                }
                            });
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                doOpticalZoom = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(50);
                if (camera[currentCameraId] != null && doOpticalZoom) {
                    camera[currentCameraId].stopContinuousOpticalZoom(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {

                        }
                    });
                }
                doOpticalZoom = false;
            }
        });

        setcurrentflightlimitsstatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flightController != null) {
                    flightController.setMaxFlightRadiusLimitationEnabled(buttonenableMaxRadius.isChecked(), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError == null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        currentmaxflightradiusstatus.setText(buttonenableMaxRadius.isChecked() ? "Enabled" : "Disabled");
                                        currentmaxflightradiusstatus.setTextColor(buttonenableMaxRadius.isChecked() ? Color.GREEN : Color.BLACK);
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        getcurrentflightlimits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flightController != null) {
                    flightController.getMaxFlightRadius(new CommonCallbacks.CompletionCallbackWith<Integer>() {
                        @Override
                        public void onSuccess(Integer integer) {
                            maxFlightDistance = integer;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    currentmaxflightradius.setText(maxFlightDistance + "");
                                }
                            });
                        }

                        @Override
                        public void onFailure(DJIError djiError) {

                        }
                    });

                    flightController.getMaxFlightHeight(new CommonCallbacks.CompletionCallbackWith<Integer>() {
                        @Override
                        public void onSuccess(Integer integer) {
                            maxFlightAltitude = integer;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    currentmaxflightheight.setText(maxFlightAltitude + "");
                                }
                            });
                        }

                        @Override
                        public void onFailure(DJIError djiError) {

                        }
                    });

                    flightController.getMaxFlightRadiusLimitationEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
                        @Override
                        public void onSuccess(final Boolean aBoolean) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    currentmaxflightradiusstatus.setText(aBoolean ? "Enabled" : "Disabled");
                                    currentmaxflightradiusstatus.setTextColor(aBoolean ? Color.GREEN : Color.BLACK);
                                    buttonenableMaxRadius.setChecked(aBoolean);
                                }
                            });
                        }

                        @Override
                        public void onFailure(DJIError djiError) {

                        }
                    });
                }
            }
        });

        updateflightlimits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flightController != null) {
                    flightController.setMaxFlightRadius(maxFlightDistance, new CommonCallbacks.CompletionCallback() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResult(DJIError djiError) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    currentmaxflightradius.setText(maxFlightDistance + "");
                                }
                            });
                        }
                    });

                    flightController.setMaxFlightHeight(maxFlightAltitude, new CommonCallbacks.CompletionCallback() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResult(DJIError djiError) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    currentmaxflightheight.setText(maxFlightAltitude + "");
                                }
                            });
                        }
                    });
                }
            }
        });

        check24Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkIfDroneIsIn2_4Ghz();
            }
        });

        hidebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int visibility;
                if (videobandlinearlayout.getVisibility() == View.VISIBLE) visibility = View.GONE;
                else visibility = View.VISIBLE;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        videobandlinearlayout.setVisibility(visibility);
                    }
                });
            }
        });

        occuBandWidthRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkID) {
                OcuSyncBandwidth ocuSyncBandwidth = Unknown;
                switch (checkID) {
                    case R.id.ocu10mhz:
                        ocuSyncBandwidth = Bandwidth10MHz;
                        break;
                    case R.id.ocu20mhz:
                        ocuSyncBandwidth = Bandwidth20MHz;
                        break;
                }
            }
        });

        LBbandwidthRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkID) {
                LightbridgeDataRate lightbridgeDataRate = LightbridgeDataRate.UNKNOWN;
                switch (checkID) {
                    case R.id.lb4mbps:
                        lightbridgeDataRate = BANDWIDTH_4_MBPS;
                        break;
                    case R.id.lb6mbps:
                        lightbridgeDataRate = BANDWIDTH_6_MBPS;
                        break;
                    case R.id.lb8mbps:
                        lightbridgeDataRate = BANDWIDTH_8_MBPS;
                        break;
                    case R.id.lb10mbps:
                        lightbridgeDataRate = BANDWIDTH_10_MBPS;
                        break;
                }
                setVideoBandwidth(null, lightbridgeDataRate);
            }
        });

        syncGimbals();
        checkforCameras();
        getFlightDataThread();
        checkRemoteController();
        //checkAirLinkThread();
        //checkAndSetDroneTo2_4ghzThread();

        //TODO: redesign GUI so there are not too many seekbars, which make it look bad

        return view;
    }

    private boolean doOpticalZoom = false;

    //This is supported onby by X5, X5S... but NOT Mavic 2 zoom
    private SettingsDefinitions.ZoomSpeed zoomSpeed(int input, int max) {
        float max7 = max / 7f;
        if (max7 == 0) return SettingsDefinitions.ZoomSpeed.NORMAL;
        if (input < max7) {
            return SettingsDefinitions.ZoomSpeed.SLOWEST;
        } else if (input < 2 * max7) {
            return SettingsDefinitions.ZoomSpeed.SLOW;
        } else if (input < 3 * max7) {
            return SettingsDefinitions.ZoomSpeed.MODERATELY_SLOW;
        } else if (input < 4 * max7) {
            return SettingsDefinitions.ZoomSpeed.NORMAL;
        } else if (input < 5 * max7) {
            return SettingsDefinitions.ZoomSpeed.MODERATELY_FAST;
        } else if (input < 6 * max7) {
            return SettingsDefinitions.ZoomSpeed.FAST;
        } else
            return SettingsDefinitions.ZoomSpeed.FASTEST;
    }

    private int mappedProgress(float gvaluein, int intscale) {
        if (minPitchValue != null && maxPitchValue != null) {
            int progress = (int) ((gvaluein - minPitchValue.floatValue()) / (maxPitchValue.floatValue() - minPitchValue.floatValue()) * intscale);
            if (progress < 0) progress = 0;
            else if (progress > intscale) progress = intscale;
            return progress;
        }
        return 0;
    }

    private void setThermalTiltSeekbar(float djitiltangle) {
        int max = seekBarIRTilt.getMax();
        int progress = mappedProgressIR(djitiltangle, max);
        seekBarIRTilt.setProgress(progress);
    }

    private int mappedProgressIR(float gvaluein, int intscale) {
        float irTiltmax = 15, irTiltmin = -90; // These are the angles of the IR gimbal (set using its own program in Windows)
        float gvalue = gvaluein;
        if (gvalue > irTiltmax)
            gvalue = irTiltmax; // DJI camera can tilt from up 30 degress to down -120 degrees (Inspire 2) or -90 down (Mavic 2)
        if (gvalue < irTiltmin)
            gvalue = irTiltmin; // So we need to stop IR following because it can't tilt further
        int progress = (int) ((gvalue - irTiltmin) / (irTiltmax - irTiltmin) * intscale) - IR_DJI_TiltDiff_FineTuned;
        if (progress < 0) progress = 0;
        else if (progress > intscale) progress = intscale;
        return progress;
    }

    private float mappedGValue(int progressvaluein, int inscale) {
        if (minPitchValue != null && maxPitchValue != null) {
            float gvalueout;
            gvalueout = ((progressvaluein * 1f) / (inscale)) * (maxPitchValue.floatValue() - minPitchValue.floatValue()) + minPitchValue.floatValue();
            if (gvalueout < minPitchValue.floatValue()) gvalueout = minPitchValue.floatValue();
            else if (gvalueout > maxPitchValue.floatValue()) gvalueout = maxPitchValue.floatValue();
            return gvalueout;
        }
        return 0;
    }

    Handler updateUIhandler = new Handler();

    void syncGimbals() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (gimbal == null) {
                    gimbal = getGimbalInstance();
                    if (gimbal != null) { // If successful
                        Object key = CapabilityKey.ADJUST_PITCH;
                        if (isFeatureSupported(CapabilityKey.ADJUST_PITCH)) {
                            minPitchValue = ((DJIParamMinMaxCapability) (gimbal.getCapabilities().get(key))).getMin();
                            key = CapabilityKey.ADJUST_PITCH;
                            maxPitchValue = ((DJIParamMinMaxCapability) (gimbal.getCapabilities().get(key))).getMax();
                        }

                        if (ModuleVerificationUtil.isGimbalModuleAvailable()) {
                            DJISampleApplication.getProductInstance().getGimbal().setStateCallback(new GimbalState.Callback() {
                                @Override
                                public void onUpdate(@NonNull GimbalState gimbalState) {
                                    mDJIGimDegrees[0] = gimbalState.getAttitudeInDegrees().getPitch();
                                    if (gimbaltiltseekbar != null && !tiltbyseekbar) {
                                        final int progress = mappedProgress(mDJIGimDegrees[0], gimbaltiltseekbar.getMax());
                                        updateUIhandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                gimbaltiltseekbar.setProgress(progress);
                                                setThermalTiltSeekbar(mDJIGimDegrees[0]);
                                            }
                                        });
                                    }

                                }
                            });
                        }

                    }
                }
                //Whenever the product is disconnected, we need to flag it as null so it will try to read again next time the product comes back up
                if (!ModuleVerificationUtil.isGimbalModuleAvailable()) gimbal = null;
            }
        }, 0, 5);
    }

    boolean tiltbyseekbar = false;

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private int SatCount = 0;
    private LocationCoordinate2D droneLocation = null;
    private float droneAltitude = 0;
    private double droneLat = 0;
    private double droneLon = 0;
    private LocationCoordinate3D locationCoordinate3D;

    void getDJIGPSdata() {
        if (flightController != null) {
            flightController.setStateCallback(new FlightControllerState.Callback() {
                @SuppressLint("DefaultLocale")
                @Override
                public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                    SatCount = flightControllerState.getSatelliteCount();
                    locationCoordinate3D = flightControllerState.getAircraftLocation();
                    if (locationCoordinate3D != null) {
                        droneAltitude = locationCoordinate3D.getAltitude();
                        droneLat = locationCoordinate3D.getLatitude();
                        droneLon = locationCoordinate3D.getLongitude();
                        updateUIhandler.post(new updateUIThread());
                    }
                }
            });
            flightController.getRTK().setStateCallback(new RTKState.Callback() {
                @Override
                public void onUpdate(@NonNull RTKState rtkState) {
                    SatCount = rtkState.getSatelliteCount();
                    droneLocation = rtkState.getMobileStationLocation();
                    droneAltitude = rtkState.getMobileStationAltitude();
                    updateUIhandler.post(new updateUIThread());
                }
            });
        } else {
            flightController = getFCInstance();
        }
    }

    private boolean needToGetMaxFlightRadius = true;
    private boolean needToGetMaxFlightHeight = true;

    private void getFlightDataThread() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (flightController == null) {
                    flightController = getFCInstance();
                    if (flightController != null) {
                        //The GPS callbacks just needs to be initiated once and it will work the entire time
                        flightController.setStateCallback(new FlightControllerState.Callback() {
                            @SuppressLint("DefaultLocale")
                            @Override
                            public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                                SatCount = flightControllerState.getSatelliteCount();
                                locationCoordinate3D = flightControllerState.getAircraftLocation();
                                if (locationCoordinate3D != null) {
                                    droneAltitude = locationCoordinate3D.getAltitude();
                                    droneLat = locationCoordinate3D.getLatitude();
                                    droneLon = locationCoordinate3D.getLongitude();
                                    copyGPSdata(droneAltitude, droneLat, droneLon, System.currentTimeMillis());
                                    updateUIhandler.post(new updateUIThread());
                                }
                                if (flightControllerState.hasReachedMaxFlightRadius())
                                    showToast("Max flight radius reach!");
                            }
                        });

                        if (needToGetMaxFlightRadius) {
                            flightController.getMaxFlightRadius(new CommonCallbacks.CompletionCallbackWith<Integer>() {
                                @Override
                                public void onSuccess(Integer integer) {
                                    Log.i(TAG, "onSuccess: max flight radius = " + integer);
                                    currentmaxflightradius.setText(integer + "");
                                    if (integer != maxFlightDistance) {
                                        flightController.setMaxFlightRadius(maxFlightDistance, new CommonCallbacks.CompletionCallback() {
                                            @Override
                                            public void onResult(DJIError djiError) {
                                                Log.i(TAG, "onResult: max flight set " + djiError);
                                                flightController.getMaxFlightRadius(new CommonCallbacks.CompletionCallbackWith<Integer>() {
                                                    @Override
                                                    public void onSuccess(Integer integer) {
                                                        Log.i(TAG, "onSuccess: max flight radius = " + integer);
                                                        currentmaxflightradius.setText(integer + "");
                                                    }

                                                    @Override
                                                    public void onFailure(DJIError djiError) {

                                                    }
                                                });
                                            }
                                        });
                                        needToGetMaxFlightRadius = false;
                                    }
                                }

                                @Override
                                public void onFailure(DJIError djiError) {

                                }
                            });
                        }
                        if (needToGetMaxFlightHeight) {
                            flightController.getMaxFlightHeight(new CommonCallbacks.CompletionCallbackWith<Integer>() {
                                @Override
                                public void onSuccess(Integer integer) {
                                    //Log.i(TAG, "onSuccess: max flight altitude = " + integer);
                                    currentmaxflightheight.setText(maxFlightAltitude + "");
                                    if (integer != maxFlightAltitude) {
                                        flightController.setMaxFlightHeight(maxFlightAltitude, new CommonCallbacks.CompletionCallback() {
                                            @Override
                                            public void onResult(DJIError djiError) {
                                                flightController.getMaxFlightHeight(new CommonCallbacks.CompletionCallbackWith<Integer>() {
                                                    @Override
                                                    public void onSuccess(Integer integer) {
                                                        currentmaxflightheight.setText(maxFlightAltitude + "");
                                                    }

                                                    @Override
                                                    public void onFailure(DJIError djiError) {

                                                    }
                                                });
                                            }
                                        });
                                    }
                                    needToGetMaxFlightHeight = false;
                                }

                                @Override
                                public void onFailure(DJIError djiError) {

                                }
                            });
                        }
                    }
                }
                if (!ModuleVerificationUtil.isFlightControllerAvailable()) {
                    flightController = null;
                    needToGetMaxFlightRadius = true;
                    needToGetMaxFlightHeight = true;
                }
            }
        }, 0, 1000);
    }

    private int getMaxFlightRadius() {
        if (flightController == null) return 0;
        else {
            return 0;
        }
    }

    static void setMaxFlightAltitude(int flightAltitude) {
        //if(flightController)
    }

    private class updateUIThread implements Runnable {
        private updateUIThread() {
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            if (textViewGPS != null) {
                if (SatCount > 0) {
                    textViewGPS.setText(String.format("GPS %5.2fm, %f, %f", droneAltitude, droneLat, droneLon));
                } else {
                    String string = "null";
                    if (flightController != null) string = flightController.getState() + "";
                    //textViewGPS.setText(String.format("flightcontroller = %s, state = %s", flightController, string));
                }
            }
        }
    }

    private class updateUIRCThread implements Runnable {
        String message;

        private updateUIRCThread(String mess) {
            this.message = mess;
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            showToast(message);
        }
    }

    private static AirLink getAirLinkInstance() {
        if (airLink == null) {
            initAirLink();
        }
        return airLink;
    }

    private static void initAirLink() {
        if (DJISDKManager.getInstance() != null) {
            BaseProduct product = DJISDKManager.getInstance().getProduct();
            if (product != null) {
                if (product instanceof Aircraft) {
                    airLink = ((Aircraft) product).getAirLink();
                }
            }
        }
    }

    private void checkAirLinkThread() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (airLink == null) {
                    airLink = getAirLinkInstance();
                }
                if (!ModuleVerificationUtil.isAirlinkAvailable())
                    airLink = null;
            }
        }, 0, 1000);
    }

    static void checkAirLinkOnce() {
        if (airLink == null) {
            airLink = getAirLinkInstance();
        }
        if (!ModuleVerificationUtil.isAirlinkAvailable()) {
            airLink = null;
            check24Interval = 2000;
        }
    }

    private void setVideoBandwidth(final OcuSyncBandwidth ocuSyncBandwidth, final LightbridgeDataRate lightbridgeDataRate) {
        if (airLink == null) {
            checkAirLinkOnce();
            if (airLink == null) return;
        }
        if (airLink.isOcuSyncLinkSupported()) {
            LBbandwidthRG.setVisibility(View.GONE);
            /*airLink.getOcuSyncLink().setVideoDataRateCallback(new OcuSyncLink.VideoDataRateCallback() {
                @Override
                public void onUpdate(float v) {

                }
            });*/
        } else if (airLink.isLightbridgeLinkSupported()) {
            occuBandWidthRG.setVisibility(View.GONE);
            LBbandwidthRG.setVisibility(View.VISIBLE);
            airLink.getLightbridgeLink().setDataRate(lightbridgeDataRate, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            videorateTextview.setText(stringLBDataRate(lightbridgeDataRate));
                        }
                    });
                }
            });
        }
    }

    private String stringLBDataRate(LightbridgeDataRate lightbridgeDataRate) {
        if (lightbridgeDataRate.toString().contains("4")) return "4 MBps";
        else if (lightbridgeDataRate.toString().contains("6")) return "6 MBps";
        else if (lightbridgeDataRate.toString().contains("8")) return "8 MBps";
        else if (lightbridgeDataRate.toString().contains("10")) return "10 MBps";
        else return "Unknown";
    }

    private boolean droneIsIn2_4 = false;
    private long needtoCheck24Millis = System.currentTimeMillis();
    static long check24Interval = 2000;
    static boolean needToCheck24 = true;

    private void checkAndSetDroneTo2_4ghzThread() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                checkAirLinkOnce();
                if (airLink != null) {
                    checkIfDroneIsIn2_4Ghz();
                }
            }
        }, 0, check24Interval);
    }

    private void setDroneTo2_4Ghz() {
        if (airLink == null) return;
        final CommonCallbacks.CompletionCallback callbackSet = new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError != null) {
                    Log.e(TAG, "onResult: djiError = " + djiError);
                } //else : successful!
            }
        };

        if (airLink.isLightbridgeLinkSupported()) {
            LightbridgeLink lightbridgeLink = new LightbridgeLink();
            lightbridgeLink.setFrequencyBand(FREQUENCY_BAND_2_DOT_4_GHZ, callbackSet);
        } else if (airLink.isOcuSyncLinkSupported()) {
            OcuSyncLink ocuSyncLink = new OcuSyncLink();
            ocuSyncLink.setFrequencyBand(OcuSyncFrequencyBand.FREQUENCY_BAND_2_DOT_4_GHZ, callbackSet);
        }
    }

    private boolean occuVideoRateCallBackSet = false;

    private void checkIfDroneIsIn2_4Ghz() {
        if (airLink == null) {
            checkAirLinkOnce();
            if (airLink == null) return;
        }
        if (airLink.isLightbridgeLinkSupported()) {
            LightbridgeLink lightbridgeLink = new LightbridgeLink();
            final CommonCallbacks.CompletionCallbackWith<LightbridgeFrequencyBand> callbackGet;
            callbackGet = new CommonCallbacks.CompletionCallbackWith<LightbridgeFrequencyBand>() {
                @Override
                public void onSuccess(LightbridgeFrequencyBand lightbridgeFrequencyBand) {
                    Log.i(TAG, "onSuccess: set2.4 lightbridgeFrequencyBand = " + lightbridgeFrequencyBand);
                    if (lightbridgeFrequencyBand.toString().contains("2_DOT_4")) {
                        if (check24Interval < 10000) {
                            droneIsIn2_4 = true;
                            final String band = lightbridgeFrequencyBand.toString();
                            //https://stackoverflow.com/questions/5161951/android-only-the-original-thread-that-created-a-view-hierarchy-can-touch-its-vi
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //textviewFreg.setText(freqStringShort(band));
                                    textviewFreg.setVisibility(View.GONE);
                                }
                            });
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    showToast(freqString(band));
                                }
                            });
                            check24Interval = 10000;
                        }
                    } else {
                        setDroneTo2_4Ghz();
                    }
                }

                @Override
                public void onFailure(DJIError djiError) {
                    droneIsIn2_4 = false;
                }
            };
            lightbridgeLink.getFrequencyBand(callbackGet);
            LBbandwidthRG.setVisibility(View.VISIBLE);
            occuBandWidthRG.setVisibility(View.GONE);
            airLink.getLightbridgeLink().getDataRate(new CommonCallbacks.CompletionCallbackWith<LightbridgeDataRate>() {
                @Override
                public void onSuccess(LightbridgeDataRate lightbridgeDataRate) {
                    final LightbridgeDataRate lightbridgeDataRate1 = lightbridgeDataRate;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            videorateTextview.setText(stringLBDataRate(lightbridgeDataRate1));
                        }
                    });
                }

                @Override
                public void onFailure(DJIError djiError) {

                }
            });
        } else if (airLink.isOcuSyncLinkSupported()) {
            OcuSyncLink ocuSyncLink = new OcuSyncLink();
            final CommonCallbacks.CompletionCallbackWith<OcuSyncFrequencyBand> callbackGet;
            callbackGet = new CommonCallbacks.CompletionCallbackWith<OcuSyncFrequencyBand>() {
                @Override
                public void onSuccess(OcuSyncFrequencyBand ocuSyncFrequencyBand) {
                    Log.i(TAG, "onSuccess: set2.4 ocuSyncFrequencyBand = " + ocuSyncFrequencyBand);
                    if (ocuSyncFrequencyBand.toString().contains("2_DOT_4")) {
                        if (check24Interval < 10000) {
                            droneIsIn2_4 = true;
                            final String band = ocuSyncFrequencyBand.toString();
                            //https://stackoverflow.com/questions/5161951/android-only-the-original-thread-that-created-a-view-hierarchy-can-touch-its-vi
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (textviewFreg.getVisibility() != View.VISIBLE)
                                        textviewFreg.setVisibility(View.VISIBLE);
                                    textviewFreg.setText(freqStringShort(band));
                                }
                            });
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    showToast(freqString(band));
                                }
                            });
                            check24Interval = 10000;
                        }
                    } else {
                        setDroneTo2_4Ghz();
                    }
                }

                @Override
                public void onFailure(DJIError djiError) {
                    droneIsIn2_4 = false;
                }
            };
            ocuSyncLink.getFrequencyBand(callbackGet);

            LBbandwidthRG.setVisibility(View.GONE);
            occuBandWidthRG.setVisibility(View.VISIBLE);

            if (!occuVideoRateCallBackSet) {
                airLink.getOcuSyncLink().setVideoDataRateCallback(new OcuSyncLink.VideoDataRateCallback() {
                    @Override
                    public void onUpdate(float v) {
                        final float v1 = v;
                        getActivity().runOnUiThread(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                videorateTextview.setText(String.format("%5.2f Mbps", v1));
                            }
                        });
                    }
                });
                occuVideoRateCallBackSet = true;
            }
        }
    }

    static String freqString(String inputstring) {
        if (inputstring.contains("2_DOT_4")) {
            return "Drone set to 2.4Ghz!";
        } else if (inputstring.contains("5_DOT_8")) {
            return "Drone set to 5.8Ghz!";
        } else if (inputstring.contains("5_DOT_7")) {
            return "Drone set to 5.7Ghz!";
        } else if (inputstring.contains("DUAL")) {
            return "Drone set to Dual frequencies (2.4 & 5.8ghz)!";
        }

        return "Unknown frequency";
    }

    static String freqStringShort(String inputstring) {
        if (inputstring.contains("2_DOT_4")) {
            return "2.4G";
        } else if (inputstring.contains("5_DOT_8")) {
            return "5.8G";
        } else if (inputstring.contains("5_DOT_7")) {
            return "5.7G";
        } else if (inputstring.contains("DUAL")) {
            return "Dual";
        }

        return "Unkn";
    }

    private FlightController getFCInstance() {
        if (flightController == null) {
            initFlightController();
        }
        return flightController;
    }

    private void initFlightController() {
        if (DJISDKManager.getInstance() != null) {
            BaseProduct product = DJISDKManager.getInstance().getProduct();
            if (product != null) {
                if (product instanceof Aircraft) {
                    flightController = ((Aircraft) product).getFlightController();
                }
            }
        }
    }

    private void checkRemoteController() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (remoteController == null) {
                    remoteController = getRCInstance();
                    //readRemoteController();
                }
                if (!ModuleVerificationUtil.isRemoteControllerAvailable())
                    remoteController = null;
            }
        }, 0, 1000);
    }

    Handler remoteControlHandler = new Handler();

    private void readRemoteController() {
        if (remoteController != null) {
            remoteController.setHardwareStateCallback(new HardwareState.HardwareStateCallback() {
                @Override
                public void onUpdate(@NonNull HardwareState hardwareState) {
                    //Log.i(TAG, "onUpdate: leftwheel = " + hardwareState.getLeftWheel());
                    //Log.i(TAG, "onUpdate: shutter = " + hardwareState.getShutterButton());
                    //Log.i(TAG, "onUpdate: record = " + hardwareState.getRecordButton());
                    //remoteControlHandler.post(new updateUIRCThread(hardwareState.getLeftWheel() + ""));
                }
            });
            remoteController.getLeftDialGimbalControlAxis(new CommonCallbacks.CompletionCallbackWith<GimbalAxis>() {
                @Override
                public void onSuccess(GimbalAxis gimbalAxis) {
                    //Log.i(TAG, "onSuccess: onUpdate axis = " + gimbalAxis.name());
                    remoteControlHandler.post(new updateUIRCThread(gimbalAxis.name() + ""));
                }

                @Override
                public void onFailure(DJIError djiError) {

                }
            });
        }
    }

    private RemoteController getRCInstance() {
        if (remoteController == null) {
            initRemoteController();
        }
        return remoteController;
    }

    private void initRemoteController() {
        if (DJISDKManager.getInstance() != null) {
            BaseProduct product = DJISDKManager.getInstance().getProduct();
            if (product != null) {
                Log.i(TAG, "initRemoteController: onUpdate product = " + product);
                if (product instanceof Aircraft) {
                    remoteController = ((Aircraft) product).getRemoteController();
                }
            }
        }
    }

    private void checkforCameras() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (camera[currentCameraId] == null) {
                    initCameras(currentCameraId);
                    if (camera[currentCameraId] != null) {
                        if (camera[currentCameraId].isOpticalZoomSupported()) {
                            updateUIhandler.post(new cameraUpdateUIthread(View.VISIBLE));
                            camera[currentCameraId].getOpticalZoomFactor(new CommonCallbacks.CompletionCallbackWith<Float>() {
                                @Override
                                public void onSuccess(Float zoomfactor) {
                                }

                                @Override
                                public void onFailure(DJIError djiError) {

                                }
                            });
                        } else
                            updateUIhandler.post(new cameraUpdateUIthread(View.GONE));
                    } else {
                        updateUIhandler.post(new cameraUpdateUIthread(View.GONE));
                    }
                }

                if (!ModuleVerificationUtil.isCameraModuleAvailable(currentCameraId))
                    camera[currentCameraId] = null;
            }
        }, 0, 1000);
    }

    private class cameraUpdateUIthread implements Runnable {
        private int visibility;

        private cameraUpdateUIthread(int visibility) {
            this.visibility = visibility;
        }

        @Override
        public void run() {
            seekBarDJIZoom.setVisibility(visibility);
        }
    }

    private Camera getCameraInstance(int cameraID) {
        if (camera[cameraID] == null) {
            initCameras(cameraID);
        }
        return camera[cameraID];
    }

    private void initCameras(int cameraID) {
        if (DJISDKManager.getInstance() != null) {
            BaseProduct product = DJISDKManager.getInstance().getProduct();
            if (product != null) {
                if (product instanceof Aircraft) {
                    try {
                        //NoOfCameras = ((Aircraft) product).getCameras().size();
                        //if (cameraID < NoOfCameras) {
                        camera[cameraID] = ((Aircraft) product).getCameras().get(cameraID);
                        //}
                    } catch (Exception ignored) {
                    }
                } else {
                    camera[0] = product.getCamera();
                }
            }
        }
    }

    private Gimbal getGimbalInstance() {
        if (gimbal == null) {
            initGimbal();
        }
        return gimbal;
    }

    private void initGimbal() {
        if (DJISDKManager.getInstance() != null) {
            BaseProduct product = DJISDKManager.getInstance().getProduct();
            if (product != null) {
                if (product instanceof Aircraft) {
                    List<Gimbal> gimbals = ((Aircraft) product).getGimbals();
                    if (gimbals != null) // For drones that have removable gimbals like Inspire 2, gimbals can be null and this will crash the app if not checked for null first like this.
                        gimbal = gimbals.get(currentGimbalId);
                    else
                        gimbal = null;
                } else {
                    gimbal = product.getGimbal();
                }
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*enablePitchExtensionIfPossible();
        if (getGimbalInstance() != null) {
            getGimbalInstance().setMode(GimbalMode.YAW_FOLLOW, new CallbackHandlers.CallbackToastHandler());
        } else {
            //ToastUtils.setResultToToast("Product disconnected");
        }*/

        /*if (ModuleVerificationUtil.isGimbalModuleAvailable()) {
            DJISampleApplication.getProductInstance().getGimbal().setStateCallback(new GimbalState.Callback() {
                @Override
                public void onUpdate(@NonNull GimbalState gimbalState) {
                    mDJIGimDegrees[0] = gimbalState.getAttitudeInDegrees().getPitch();
                    mDJIGimDegrees[1] = gimbalState.getAttitudeInDegrees().getRoll();
                    mDJIGimDegrees[2] = gimbalState.getAttitudeInDegrees().getYaw();
                }
            });
        }*/
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    private void enablePitchExtensionIfPossible() {

        Gimbal gimbal = getGimbalInstance();
        if (gimbal == null) {
            return;
        }
        boolean ifPossible = isFeatureSupported(CapabilityKey.PITCH_RANGE_EXTENSION);
        if (ifPossible) {
            gimbal.setPitchRangeExtensionEnabled(true, new CallbackHandlers.CallbackToastHandler());
        }
    }

    /*
     * Check if The Gimbal Capability is supported
     */
    private boolean isFeatureSupported(CapabilityKey key) {

        Gimbal gimbal = getGimbalInstance();
        if (gimbal == null) {
            return false;
        }

        DJIParamCapability capability = null;
        if (gimbal.getCapabilities() != null) {
            capability = gimbal.getCapabilities().get(key);
        }

        if (capability != null) {
            return capability.isSupported();
        }
        return false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (timer != null) {
            if (gimbalRotationTimerTask != null) {
                gimbalRotationTimerTask.cancel();
            }
            timer.cancel();
            timer.purge();
            gimbalRotationTimerTask = null;
            timer = null;
        }
        if (ModuleVerificationUtil.isGimbalModuleAvailable()) {
            DJISampleApplication.getProductInstance().getGimbal().setStateCallback(null);
        }
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

    private static class GimbalRotateTimerTask extends TimerTask {
        float pitchValue;

        GimbalRotateTimerTask(float pitchValue) {
            super();
            this.pitchValue = pitchValue;
        }

        @Override
        public void run() {
            if (ModuleVerificationUtil.isGimbalModuleAvailable()) {
                DJISampleApplication.getProductInstance().getGimbal().
                        rotate(new Rotation.Builder().pitch(pitchValue)
                                .mode(RotationMode.SPEED)
                                .yaw(Rotation.NO_ROTATION)
                                .roll(Rotation.NO_ROTATION)
                                .time(0)
                                .build(), new CommonCallbacks.CompletionCallback() {

                            @Override
                            public void onResult(DJIError error) {

                            }
                        });
            }
        }
    }

    //TODO: make a setting window so user can set this delay value
    double tiltdelay = 0.01; // this is probably in seconds, 0.1 = 0.1 second delay

    private void TiltDJIGimbal(float pitchValue) {
        if (gimbal != null) {
            if (ModuleVerificationUtil.isGimbalModuleAvailable()) {
                DJISampleApplication.getProductInstance().getGimbal().
                        rotate(new Rotation.Builder().pitch(pitchValue)
                                .mode(RotationMode.ABSOLUTE_ANGLE)
                                .yaw(Rotation.NO_ROTATION)
                                .roll(Rotation.NO_ROTATION)
                                .time(tiltdelay)
                                .build(), new CommonCallbacks.CompletionCallback() {

                            @Override
                            public void onResult(DJIError error) {
                                showToast("Tilt Error");
                            }
                        });
            }
        }
    }

    private void sendRotateGimbalCommand(Rotation rotation) {

        Gimbal gimbal = getGimbalInstance();
        if (gimbal == null) {
            return;
        }

        gimbal.rotate(rotation, new CallbackHandlers.CallbackToastHandler());
    }

    private void TiltDJIGimbal1(float pitchValue) {

        Rotation.Builder builder = new Rotation.Builder().mode(RotationMode.ABSOLUTE_ANGLE)
                .time(tiltdelay)
                .pitch(pitchValue);

        sendRotateGimbalCommand(builder.build());
    }

    float[] mDJIGimDegrees = new float[3];

    private void getDJIGimbalpry() {
        if (ModuleVerificationUtil.isGimbalModuleAvailable()) {
            DJISampleApplication.getProductInstance().getGimbal().setStateCallback(new GimbalState.Callback() {
                @Override
                public void onUpdate(@NonNull GimbalState gimbalState) {
                    mDJIGimDegrees[0] = gimbalState.getAttitudeInDegrees().getPitch();
                    mDJIGimDegrees[1] = gimbalState.getAttitudeInDegrees().getRoll();
                    mDJIGimDegrees[2] = gimbalState.getAttitudeInDegrees().getYaw();

                    int progress = mappedProgress(mDJIGimDegrees[0], gimbaltiltseekbar.getMax());
                    if (progress >= 0 && progress <= 1000) {
                        if (gimbaltiltseekbar != null) {
                            gimbaltiltseekbar.setProgress(progress);
                            setThermalTiltSeekbar(mDJIGimDegrees[0]);
                        }
                    }
                }
            });
        }
    }

    public void showToast(final String msg) {
        Toast toast = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
        toast.setMargin(0, 0.8f);
        toast.show();
    }
}
