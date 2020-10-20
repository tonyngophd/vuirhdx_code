package com.suas.uxdual;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import android.app.Fragment;

import static com.suas.uxdual.GimbalControlFragment.setcurrentflightlimitsstatus;
import static com.suas.uxdual.GimbalControlFragment.updateflightlimits;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SetLimitsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SetLimitsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetLimitsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    protected static final String PREFS_NAME = "VuIRPrefsFile";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    static EditText maxflightheightnumber, maxflightradiusnumber;
    static TextView currentmaxflightheight, currentmaxflightradius, currentmaxflightradiusstatus;
    static int maxFlightAltitude = 40, maxFlightDistance = 200;
    private Button buttonOK;
    static ToggleButton buttonenableMaxRadius;

    private OnFragmentInteractionListener mListener;

    public SetLimitsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SetLimitsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SetLimitsFragment newInstance(String param1, String param2) {
        SetLimitsFragment fragment = new SetLimitsFragment();
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
        View view = inflater.inflate(R.layout.fragment_set_limits, container, false);

        //https://stackoverflow.com/questions/14212518/is-there-a-way-to-define-a-min-and-max-value-for-edittext-in-android
        maxflightradiusnumber = view.findViewById(R.id.maxflightradiusnumber);
        maxflightheightnumber = view.findViewById(R.id.maxflightheightnumber);
        currentmaxflightradius = view.findViewById(R.id.currentmaxflightradius);
        currentmaxflightradiusstatus = view.findViewById(R.id.currentmaxflightradiusstatus);
        currentmaxflightheight = view.findViewById(R.id.currentmaxflightheight);
        buttonOK = view.findViewById(R.id.buttonOK);
        buttonenableMaxRadius = view.findViewById(R.id.buttonenableMaxRadius);
        //https://stackoverflow.com/questions/26645212/hide-soft-keyboard-on-return-key-press
        maxflightradiusnumber.setFilters(new InputFilter[]{new InputFilterMinMax("1", "8000")});
        maxflightheightnumber.setFilters(new InputFilter[]{new InputFilterMinMax("1", "120")});

        RestoreUserSettings();

        maxflightheightnumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String textstring = textView.getText().toString();
                //showToast(textstring);
                try {
                    maxFlightAltitude = Integer.valueOf(textstring);
                    updateflightlimits.performClick();
                    SaveUserSettingInt("Max Flight Altitude", maxFlightAltitude);
                } catch (NumberFormatException ignored) {
                }

                return false;
            }
        });

        //https://stackoverflow.com/questions/1489852/android-handle-enter-in-an-edittext
        maxflightradiusnumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String textstring = textView.getText().toString();
                //showToast(textstring);
                try {
                    maxFlightDistance = Integer.valueOf(textstring);
                    updateflightlimits.performClick();
                    SaveUserSettingInt("Max Flight Distance", maxFlightDistance);
                } catch (NumberFormatException ignored) {
                }
                return false;
            }
        });

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CompleteWidgetActivity.flightModeWidget.performClick();
            }
        });

        buttonenableMaxRadius.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                setcurrentflightlimitsstatus.performClick();
            }
        });

        return view;
    }

    private void RestoreUserSettings() {
        // Restore preferences
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        maxFlightDistance = settings.getInt("Max Flight Distance", 100);
        maxFlightAltitude = settings.getInt("Max Flight Altitude", 120);
    }

    private void SaveUserSettingInt(String settingName, int settingValue) {
        try {
            SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(settingName, settingValue);
            editor.apply();
        } catch (Exception ignored) {
        }
    }

    private void getLimits(){
        String textstring = maxflightradiusnumber.getText().toString();
        try {
            maxFlightDistance = Integer.valueOf(textstring);
        } catch (NumberFormatException ignored) {
        }

        textstring = maxflightheightnumber.getText().toString();
        try {
            maxFlightAltitude = Integer.valueOf(textstring);
        } catch (NumberFormatException ignored) {
        }
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
        SaveUserSettingInt("Max Flight Distance", maxFlightDistance);
        SaveUserSettingInt("Max Flight Altitude", maxFlightAltitude);
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

    public class InputFilterMinMax implements InputFilter {

        private int min, max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public InputFilterMinMax(String min, String max) {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(min, max, input)) {
                    return null;
                } else {
                    showToast("Enter in range from " + min + " to " + max);
                }
            } catch (NumberFormatException nfe) {
            }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }

    public void showToast(final String msg) {
        Toast toast = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
        toast.setMargin(0, 0.8f);
        toast.show();
    }
}
