package com.suas.uxdual;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link IRstatusFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link IRstatusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IRstatusFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    static TextView irdatalinkstatus;
    static TextView batterypercentagetextView;
    static ImageView imageViewBattery;
    private TextView tabbatterypercentagetextView;
    private ImageView imageViewTabBattStatus;
    private TextView textViewsUAScom;


    public IRstatusFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment IRstatusFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static IRstatusFragment newInstance(String param1, String param2) {
        IRstatusFragment fragment = new IRstatusFragment();
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
        View view = inflater.inflate(R.layout.fragment_irstatus, container, false);
        irdatalinkstatus = (TextView) view.findViewById(R.id.irdatalinkstatus);
        batterypercentagetextView = (TextView) view.findViewById(R.id.batterypercentagetextView);
        imageViewBattery = view.findViewById(R.id.imageViewBattery);
        tabbatterypercentagetextView = view.findViewById(R.id.tabbatterypercentagetextView);
        imageViewTabBattStatus = view.findViewById(R.id.imageViewTabBattStatus);
        textViewsUAScom = view.findViewById(R.id.textViewsUAScom);

        //https://stackoverflow.com/questions/2680607/text-with-gradient-in-android
        /*Shader textShader=new LinearGradient(0, 0, 0, 20,
                new int[]{Color.GREEN,Color.BLUE},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        textViewsUAScom.getPaint().setShader(textShader);*/

        getActivity().registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        return view;
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
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mBatInfoReceiver);
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

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            //https://stackoverflow.com/questions/3291655/get-battery-level-and-state-in-android
            int tabletBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int tabletBateryPlugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            if (tabletBatteryLevel > 0) {
                tabbatterypercentagetextView.setText(tabletBatteryLevel + "%");
                imageViewTabBattStatus.setImageResource(R.drawable.ic_battery_std_black_24dp);
            }
            if (tabletBateryPlugged > 0) {
                imageViewTabBattStatus.setImageResource(R.drawable.ic_battery_charging_90_black_24dp);
            }
        }
    };


    @SuppressLint("SetTextI18n")
    static void displayIRBatteryPercentage(float BatteryVoltagePercent) {
        float percentage = 0;
        //percentage = 100 * (MainActivity.BatteryVoltagePercent - emptyvoltage) / (fullvoltage - emptyvoltage);
        percentage = BatteryVoltagePercent;
        if (percentage > 100) percentage = 100;
        else if (percentage < 0) percentage = 0;
        if (batterypercentagetextView != null)
            batterypercentagetextView.setText((int) percentage + "%");
        if (imageViewBattery != null) {
            if (percentage <= 5f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_0_black_48dp);
                imageViewBattery.setColorFilter(Color.RED);
            } else if (percentage <= 15f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_10_black_48dp);
                imageViewBattery.setColorFilter(Color.RED);
            } else if (percentage <= 25f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_20_black_48dp);
                imageViewBattery.setColorFilter(Color.argb(255, 249, 219, 34));
            } else if (percentage <= 35f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_30_black_48dp);
                imageViewBattery.setColorFilter(Color.argb(255, 164, 204, 68));
            } else if (percentage <= 45f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_40_black_48dp);
                imageViewBattery.setColorFilter(Color.argb(255, 137, 171, 13));
            } else if (percentage <= 55f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_50_black_48dp);
                imageViewBattery.setColorFilter(Color.argb(255, 137, 171, 13));
            } else if (percentage <= 65f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_60_black_48dp);
                imageViewBattery.setColorFilter(Color.argb(255, 137, 171, 13));
            } else if (percentage <= 75f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_70_black_48dp);
                imageViewBattery.setColorFilter(Color.argb(255, 137, 171, 13));
            } else if (percentage <= 85f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_80_black_48dp);
                imageViewBattery.setColorFilter(Color.argb(255, 137, 171, 13));
            } else if (percentage <= 95f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_90_black_48dp);
                imageViewBattery.setColorFilter(Color.argb(255, 137, 171, 13));
            } else if (percentage <= 110f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_full_black_48dp);
                imageViewBattery.setColorFilter(Color.argb(255, 137, 171, 13));
            } else {
                imageViewBattery.setImageResource(R.drawable.ic_battery_unknown_black_48dp);
                imageViewBattery.setColorFilter(Color.GRAY);
            }
        }
        //Log.d(TAG, "rtBatteryPercentage: percentage = " + percentage);
    }
}
