package com.suas.uxdual;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.log.DJILog;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.useraccount.UserAccountManager;

public class MainActivity extends Activity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
    private static final String TAG = "MainActivity";
    private static final String LAST_USED_BRIDGE_IP = "bridgeip";
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static boolean isAppStarted = false;
    static BottomNavigationView navView;
    private CardView cardViewflipper;
    private ViewFlipper viewFlipper;
    private WebsiteFragment websiteFragment = null;
    private AboutFragment aboutFragment = new AboutFragment();
    private FragmentTransaction fragmentTransaction;
    private MediaGalleryFragment mediaFragment = null;
    private CardView cardviewTitlte;
    private TableLayout mainbuttonsTableLayout;
    private boolean inHomeScreen = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HideAndroidBottomNavigationBarforTrueFullScreenView();
        setContentView(R.layout.activity_main);
        ServiceBase.getServiceBase().initService(getApplicationContext());

        navView = findViewById(R.id.nav_view);
        cardViewflipper = findViewById(R.id.cardviewflipper);
        viewFlipper = findViewById(R.id.viewFlipper);
        cardviewTitlte = findViewById(R.id.cardviewTitlte);
        mainbuttonsTableLayout = findViewById(R.id.mainbuttonsTableLayout);

        isAppStarted = true;
        findViewById(R.id.complete_ui_widgets).setOnClickListener(this);
        findViewById(R.id.bt_customized_ui_widgets).setOnClickListener(this);
        findViewById(R.id.bt_map_widget).setOnClickListener(this);
        findViewById(R.id.bt_gallery).setOnClickListener(this);
        TextView versionText = (TextView) findViewById(R.id.version);
        versionText.setText(getResources().getString(R.string.sdk_version, DJISDKManager.getInstance().getSDKVersion()));
        bridgeModeEditText = (EditText) findViewById(R.id.edittext_bridge_ip);
        bridgeModeEditText.setText(PreferenceManager.getDefaultSharedPreferences(this).getString(LAST_USED_BRIDGE_IP, ""));
        bridgeModeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event != null
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event != null && event.isShiftPressed()) {
                        return false;
                    } else {
                        // the user is done typing.
                        handleBridgeIPTextChange();
                    }
                }
                return false; // pass on to other listeners.
            }
        });
        bridgeModeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.toString().contains("\n")) {
                    // the user is done typing.
                    // remove new line characcter
                    final String currentText = bridgeModeEditText.getText().toString();
                    bridgeModeEditText.setText(currentText.substring(0, currentText.indexOf('\n')));
                    handleBridgeIPTextChange();
                }
            }
        });
        //checkAndRequestPermissions();

        startfullscreen = (RadioButton) findViewById(R.id.radiobuttonstartfullscreen);
        radioGroupstartfullscreen = (RadioGroup) findViewById(R.id.radiogroupstart);
        //Start the fullscreen activity immediately, avoiding the annoying starting main activity
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //if (inHomeScreen)
                //radioGroupstartfullscreen.check(R.id.radiobuttonstartfullscreen);
            }
        }, 60000); //TODO adjust this value to make a smooth opening yet it's not too quickly
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAndRequestPermissions();
            }
        }, 1000);
        radioGroupstartfullscreen.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radiobuttonstartfullscreen) {
                    startFullScreen();
                } else if (checkedId == R.id.radiobuttonstopmain) {
                    finish();
                }
            }
        });

        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.aboutfragframe, aboutFragment, "About")
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .addToBackStack("MainFragStack")
                .commitAllowingStateLoss();
        showHideFragment(aboutFragment, false);

        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startSlideshow();
            }
        }, 200);/**/
    }

    private static final int FLIP_DURATION = 4000;

    private void startSlideshow() {
        if (!viewFlipper.isFlipping()) {
            viewFlipper.setAutoStart(true);
            viewFlipper.setFlipInterval(FLIP_DURATION);
            viewFlipper.startFlipping();
        }
    }

    private void stopSlideshow() {
        if (viewFlipper.isFlipping()) {
            viewFlipper.stopFlipping();
        }
    }

    private DJISDKManager.SDKManagerCallback registrationCallback = new DJISDKManager.SDKManagerCallback() {

        @Override
        public void onRegister(DJIError error) {
            isRegistrationInProgress.set(false);
            if (error == DJISDKError.REGISTRATION_SUCCESS) {
                //loginAccount(); //Just disable this Data collecting piece!
                DJISDKManager.getInstance().startConnectionToProduct();

                Toast.makeText(getApplicationContext(), "Registration succeeded!", Toast.LENGTH_LONG).show();
            } else {

                Toast.makeText(getApplicationContext(),"Registration failed, connect to internet and retry!", Toast.LENGTH_LONG).show();
            }
        }
        @Override
        public void onProductDisconnect() {
            Toast.makeText(getApplicationContext(), "product disconnect!", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProductConnect(BaseProduct product) {
            Toast.makeText(getApplicationContext(), "product connect!", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProductChanged(BaseProduct product) {

        }

        @Override
        public void onComponentChange(BaseProduct.ComponentKey key,
                                      BaseComponent oldComponent,
                                      BaseComponent newComponent) {
            Toast.makeText(getApplicationContext(), key.toString() + " changed", Toast.LENGTH_LONG).show();

        }

        @Override
        public void onInitProcess(DJISDKInitEvent event, int totalProcess) {

        }

        @Override
        public void onDatabaseDownloadProgress(long current, long total) {

        }
    };

    @Override
    protected void onPause() {
        stopSlideshow();
        super.onPause();
    }

    private void loginAccount() {
        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        Toast.makeText(getApplicationContext(), "Login Success!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(DJIError error) {
                        Toast.makeText(getApplicationContext(), "Login Error!", Toast.LENGTH_LONG).show();
                    }
                });

    }

    public static boolean isStarted() {
        return isAppStarted;
    }

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            //Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.CAMERA
    };
    private static final int REQUEST_PERMISSION_CODE = 12345;
    private List<String> missingPermission = new ArrayList<>();
    private EditText bridgeModeEditText;


    void startFullScreen() {
        startActivity(new Intent(this, CompleteWidgetActivity.class));
    }

    RadioButton startfullscreen;
    static RadioGroup radioGroupstartfullscreen;

    @Override
    protected void onDestroy() {
        DJISDKManager.getInstance().destroy();
        stopSlideshow();
        isAppStarted = false;
        super.onDestroy();
    }

    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */
    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }
    }

    /**
     * Result of runtime permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            //Toast.makeText(getApplicationContext(), "Missing permissions! Will not register SDK to connect to aircraft.", Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(), "Please connect to internet and relaunch app (only this very first time only)", Toast.LENGTH_LONG).show();
        }
    }

    private void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    DJISDKManager.getInstance().registerApp(MainActivity.this, registrationCallback);
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        Class nextActivityClass;

        int id = view.getId();
        if (id == R.id.complete_ui_widgets) {
            navView.setSelectedItemId(R.id.navigation_vuir);
            //nextActivityClass = CompleteWidgetActivity.class;
            return;
        } else if (id == R.id.bt_customized_ui_widgets) {
            nextActivityClass = CustomizedWidgetsActivity.class;
        } else if (id == R.id.bt_map_widget) {
            //nextActivityClass = MapWidgetActivity.class;
            PopupMenu popup = new PopupMenu(this, view);
            popup.setOnMenuItemClickListener(this);
            Menu popupMenu = popup.getMenu();
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.map_select_menu, popupMenu);
            popupMenu.findItem(R.id.here_map).setEnabled(isHereMapsSupported());
            popupMenu.findItem(R.id.google_map).setEnabled(isGoogleMapsSupported(this));
            popup.show();
            return;
        } else {
            navView.setSelectedItemId(R.id.navigation_gallery);
            //nextActivityClass = MediaGalleryActivity.class;
            return;
        }

        Intent intent = new Intent(this, nextActivityClass);
        startActivity(intent);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        Intent intent = new Intent(this, MapWidgetActivity.class);
        int mapBrand = 0;
        switch (menuItem.getItemId()) {
            case R.id.here_map:
                mapBrand = 0;
                break;
            case R.id.google_map:
                mapBrand = 1;
                break;
            case R.id.amap:
                mapBrand = 2;
                break;
            case R.id.mapbox:
                mapBrand = 3;
                break;
        }
        intent.putExtra(MapWidgetActivity.MAP_PROVIDER, mapBrand);
        startActivity(intent);
        return false;
    }

    public static boolean isHereMapsSupported() {
        String abi;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            abi = Build.CPU_ABI;
        } else {
            abi = Build.SUPPORTED_ABIS[0];
        }
        DJILog.d(TAG, "abi=" + abi);

        //The possible values are armeabi, armeabi-v7a, arm64-v8a, x86, x86_64, mips, mips64.
        return abi.contains("arm");
    }

    public static boolean isGoogleMapsSupported(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    private void handleBridgeIPTextChange() {
        // the user is done typing.
        final String bridgeIP = bridgeModeEditText.getText().toString();

        if (!TextUtils.isEmpty(bridgeIP)) {
            DJISDKManager.getInstance().enableBridgeModeWithBridgeAppIP(bridgeIP);
            Toast.makeText(getApplicationContext(), "BridgeMode ON!\nIP: " + bridgeIP, Toast.LENGTH_SHORT).show();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(LAST_USED_BRIDGE_IP, bridgeIP).apply();
        }
    }

    private void HideAndroidBottomNavigationBarforTrueFullScreenView() {
        //https://stackoverflow.com/questions/16713845/permanently-hide-navigation-bar-in-an-activity/26013850
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        HideAndroidBottomNavigationBarforTrueFullScreenView();
        startSlideshow();
        super.onResume();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @SuppressLint("CommitTransaction")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    cardViewflipper.setVisibility(View.VISIBLE);
                    showHideFragment(websiteFragment, false);
                    showHideFragment(aboutFragment, false);
                    showHideFragment(mediaFragment, false);
                    cardviewTitlte.setVisibility(View.VISIBLE);
                    mainbuttonsTableLayout.setVisibility(View.VISIBLE);
                    inHomeScreen = true;
                    inGallery = false;
                    return true;
                case R.id.navigation_vuir:
                    startActivity(new Intent(getApplicationContext(), CompleteWidgetActivity.class));
                    cardViewflipper.setVisibility(View.INVISIBLE);
                    showHideFragment(websiteFragment, false);
                    showHideFragment(aboutFragment, false);
                    showHideFragment(mediaFragment, false);
                    cardviewTitlte.setVisibility(View.INVISIBLE);
                    inGallery = false;
                    inHomeScreen = false;
                    return true;
                case R.id.navigation_gallery: //R.id.navigation_dashboard:
                    cardViewflipper.setVisibility(View.INVISIBLE);
                    showHideFragment(websiteFragment, false);
                    showHideFragment(aboutFragment, false);
                    if (mediaFragment == null) addMediaFragment();
                    showHideFragment(mediaFragment, true);
                    cardviewTitlte.setVisibility(View.INVISIBLE);
                    mainbuttonsTableLayout.setVisibility(View.INVISIBLE);
                    inGallery = true;
                    inHomeScreen = false;
                    return true;
                case R.id.navigation_web:
                    cardViewflipper.setVisibility(View.INVISIBLE);
                    if (websiteFragment == null) addWebsiteFragment();
                    showHideFragment(websiteFragment, true);
                    showHideFragment(aboutFragment, false);
                    showHideFragment(mediaFragment, false);
                    //(findViewById(R.id.websitefragframe)).setZ(10);
                    cardviewTitlte.setVisibility(View.INVISIBLE);
                    inGallery = false;
                    inHomeScreen = false;
                    return true;
                case R.id.navigation_about:
                    cardViewflipper.setVisibility(View.INVISIBLE);
                    showHideFragment(websiteFragment, false);
                    showHideFragment(aboutFragment, true);
                    showHideFragment(mediaFragment, false);
                    cardviewTitlte.setVisibility(View.VISIBLE);
                    inGallery = false;
                    inHomeScreen = false;
                    return true;
            }
            return false;
        }
    };

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

    private boolean mBackPressed = false;
    private boolean inGallery = false;

    @Override
    public void onBackPressed() {
        mBackPressed = true;
        Log.i(TAG, "onBackPressed: MainActivityMainActivity mBackPressed = " + mBackPressed);
        if ((websiteFragment != null) && (WebsiteFragment.webView != null)) {
            if (WebsiteFragment.webView.canGoBack() && websiteFragment.isVisible())
                WebsiteFragment.webView.goBack();
            else if (!inGallery)
                super.onBackPressed();
        } else if (!inGallery) {
            super.onBackPressed();
        }
    }

    private void addMediaFragment() {
        if (mediaFragment == null) {
            mediaFragment = new MediaGalleryFragment();
        }
        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.mediafragframe, mediaFragment, "MediaGallery")
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .addToBackStack("MainFragStack")
                .commitAllowingStateLoss();
    }

    private void addWebsiteFragment() {
        if (websiteFragment == null) {
            websiteFragment = new WebsiteFragment();
        }
        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.mediafragframe, websiteFragment, "Website")
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .addToBackStack("MainFragStack")
                .commitAllowingStateLoss();
    }
}


/** Main activity that displays three choices to user */
/*
public class MainActivity extends Activity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
    private static final String TAG = "MainActivity";
    private static final String LAST_USED_BRIDGE_IP = "bridgeip";
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static boolean isAppStarted = false;
    private DJISDKManager.SDKManagerCallback registrationCallback = new DJISDKManager.SDKManagerCallback() {

        @Override
        public void onRegister(DJIError error) {
            isRegistrationInProgress.set(false);
            if (error == DJISDKError.REGISTRATION_SUCCESS) {
                loginAccount();
                DJISDKManager.getInstance().startConnectionToProduct();

                Toast.makeText(getApplicationContext(), "SDK registration succeeded!", Toast.LENGTH_LONG).show();
            } else {

                Toast.makeText(getApplicationContext(),
                               "SDK registration failed, check network and retry!",
                               Toast.LENGTH_LONG).show();
            }
        }
        @Override
        public void onProductDisconnect() {
            Toast.makeText(getApplicationContext(),
                           "product disconnect!",
                           Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProductConnect(BaseProduct product) {
            Toast.makeText(getApplicationContext(),
                           "product connect!",
                           Toast.LENGTH_LONG).show();
        }
        
        @Override
        public void onProductChanged(BaseProduct product) {

        }

        @Override
        public void onComponentChange(BaseProduct.ComponentKey key,
                                      BaseComponent oldComponent,
                                      BaseComponent newComponent) {
            Toast.makeText(getApplicationContext(),
                           key.toString() + " changed",
                           Toast.LENGTH_LONG).show();

        }

        @Override
        public void onInitProcess(DJISDKInitEvent event, int totalProcess) {

        }

        @Override
        public void onDatabaseDownloadProgress(long current, long total) {

        }
    };

    private void loginAccount(){
        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        Toast.makeText(getApplicationContext(),
                                "Login Success!",
                                Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onFailure(DJIError error) {
                        Toast.makeText(getApplicationContext(),
                                "Login Error!",
                                Toast.LENGTH_LONG).show();
                    }
                });

    }

    public static boolean isStarted() {
        return isAppStarted;
    }
    private static final String[] REQUIRED_PERMISSION_LIST = new String[] {
        Manifest.permission.VIBRATE, // Gimbal rotation
        Manifest.permission.INTERNET, // API requests
        Manifest.permission.ACCESS_WIFI_STATE, // WIFI connected products
        Manifest.permission.ACCESS_COARSE_LOCATION, // Maps
        Manifest.permission.ACCESS_NETWORK_STATE, // WIFI connected products
        Manifest.permission.ACCESS_FINE_LOCATION, // Maps
        Manifest.permission.CHANGE_WIFI_STATE, // Changing between WIFI and USB connection
        Manifest.permission.WRITE_EXTERNAL_STORAGE, // Log files
        Manifest.permission.BLUETOOTH, // Bluetooth connected products
        Manifest.permission.BLUETOOTH_ADMIN, // Bluetooth connected products
        Manifest.permission.READ_EXTERNAL_STORAGE, // Log files
        Manifest.permission.READ_PHONE_STATE, // Device UUID accessed upon registration
        Manifest.permission.RECORD_AUDIO // Speaker accessory
    };
    private static final int REQUEST_PERMISSION_CODE = 12345;
    private List<String> missingPermission = new ArrayList<>();
    private EditText bridgeModeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isAppStarted = true;
        findViewById(R.id.complete_ui_widgets).setOnClickListener(this);
        findViewById(R.id.bt_customized_ui_widgets).setOnClickListener(this);
        findViewById(R.id.bt_map_widget).setOnClickListener(this);
        TextView versionText = (TextView) findViewById(R.id.version);
        versionText.setText(getResources().getString(R.string.sdk_version, DJISDKManager.getInstance().getSDKVersion()));
        bridgeModeEditText = (EditText) findViewById(R.id.edittext_bridge_ip);
        bridgeModeEditText.setText(PreferenceManager.getDefaultSharedPreferences(this).getString(LAST_USED_BRIDGE_IP,""));
        bridgeModeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || event != null
                    && event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event != null && event.isShiftPressed()) {
                        return false;
                    } else {
                        // the user is done typing.
                        handleBridgeIPTextChange();
                    }
                }
                return false; // pass on to other listeners.
            }
        });
        bridgeModeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.toString().contains("\n")) {
                    // the user is done typing.
                    // remove new line characcter
                    final String currentText = bridgeModeEditText.getText().toString();
                    bridgeModeEditText.setText(currentText.substring(0, currentText.indexOf('\n')));
                    handleBridgeIPTextChange();
                }
            }
        });
        checkAndRequestPermissions();
    }

    @Override
    protected void onDestroy() {
        DJISDKManager.getInstance().destroy();
        isAppStarted = false;
        super.onDestroy();
    }

    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */
/*
    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            ActivityCompat.requestPermissions(this,
                                              missingPermission.toArray(new String[missingPermission.size()]),
                                              REQUEST_PERMISSION_CODE);
        }
    }

    /**
     * Result of runtime permission request
     */
    /*
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            Toast.makeText(getApplicationContext(), "Missing permissions! Will not register SDK to connect to aircraft.", Toast.LENGTH_LONG).show();
        }
    }

    private void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    DJISDKManager.getInstance().registerApp(MainActivity.this, registrationCallback);
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        Class nextActivityClass;

        int id = view.getId();
        if (id == R.id.complete_ui_widgets) {
            nextActivityClass = CompleteWidgetActivity.class;
        } else if (id == R.id.bt_customized_ui_widgets) {
            nextActivityClass = CustomizedWidgetsActivity.class;
        } else {
            //nextActivityClass = MapWidgetActivity.class;
            PopupMenu popup = new PopupMenu(this, view);
            popup.setOnMenuItemClickListener(this);
            Menu popupMenu = popup.getMenu();
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.map_select_menu, popupMenu);
            popupMenu.findItem(R.id.here_map).setEnabled(isHereMapsSupported());
            popupMenu.findItem(R.id.google_map).setEnabled(isGoogleMapsSupported(this));
            popup.show();
            return;
        }

        Intent intent = new Intent(this, nextActivityClass);
        startActivity(intent);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        Intent intent = new Intent(this, MapWidgetActivity.class);
        int mapBrand = 0;
        switch (menuItem.getItemId()) {
            case R.id.here_map:
                mapBrand = 0;
                break;
            case R.id.google_map:
                mapBrand = 1;
                break;
            case R.id.amap:
                mapBrand = 2;
                break;
            case R.id.mapbox:
                mapBrand = 3;
                break;
        }
        intent.putExtra(MapWidgetActivity.MAP_PROVIDER, mapBrand);
        startActivity(intent);
        return false;
    }

    public static boolean isHereMapsSupported() {
        String abi;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            abi = Build.CPU_ABI;
        } else {
            abi = Build.SUPPORTED_ABIS[0];
        }
        DJILog.d(TAG, "abi=" + abi);

        //The possible values are armeabi, armeabi-v7a, arm64-v8a, x86, x86_64, mips, mips64.
        return abi.contains("arm");
    }

    public static boolean isGoogleMapsSupported(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    private void handleBridgeIPTextChange() {
        // the user is done typing.
        final String bridgeIP = bridgeModeEditText.getText().toString();

        if (!TextUtils.isEmpty(bridgeIP)) {
            DJISDKManager.getInstance().enableBridgeModeWithBridgeAppIP(bridgeIP);
            Toast.makeText(getApplicationContext(),"BridgeMode ON!\nIP: " + bridgeIP,Toast.LENGTH_SHORT).show();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(LAST_USED_BRIDGE_IP,bridgeIP).apply();
        }
    }
}
*/