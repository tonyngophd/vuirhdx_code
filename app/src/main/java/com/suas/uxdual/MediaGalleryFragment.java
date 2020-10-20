package com.suas.uxdual;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MediaGalleryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MediaGalleryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MediaGalleryFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    static Window window;
    RecyclerView recyclerView;
    ImageView imageView;
    VideoView videoView;
    private final int maxpics = 999;
    static int maxFileNumbers = 0;
    protected static int startindex = 0;
    private int endindex = maxpics;
    ArrayList<CreateList> createLists;
    static ArrayList<CreateList> createListsFolder;
    MediaFileAdapter adapter;
    FolderAdapter adapterFolder;
    static String TAG = "gallery";
    private long scrollupMillis = System.currentTimeMillis();
    private long scrolldownMillis = System.currentTimeMillis();
    private static RecyclerView recyclerViewFolder;
    static String folderClicked = "";
    RecyclerView.LayoutManager layoutManager;
    static RadioGroup radioGroup;
    private RelativeLayout imagelayout;
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;
    private TextView textviewNoContent;
    private TextView textviewMediaName;
    private TextView textviewLoading;
    private ProgressBar progressBarloading;
    private View rootView;
    static int loadPercent = 0;
    private static int loadPercent_pre = 0;
    private Button buttonreloadfolder;
    private long zoommillis = System.currentTimeMillis();
    private long dragmillis = System.currentTimeMillis();
    static SeekBar videoseekbar;
    static TextView vidfulllength;
    static TextView vidcurrentpos;
    static ImageView playbutton;
    private ImageButton deletebutton;
    private ImageButton sharebutton;
    private ToggleButton selecttoggle;
    public static boolean selectMode = false;
    static int currentFolderIndex = 0;
    static boolean needToClick = false;
    static int currentImageIndex = 0;
    static boolean needToReloadImage0 = true;
    private ConstraintLayout gallerycontainerfrag;


    public MediaGalleryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MediaGalleryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MediaGalleryFragment newInstance(String param1, String param2) {
        MediaGalleryFragment fragment = new MediaGalleryFragment();
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_media_gallery, container, false);
        window = getActivity().getWindow();


        recyclerView = (RecyclerView) rootView.findViewById(R.id.mediagallery);
        recyclerViewFolder = (RecyclerView) rootView.findViewById(R.id.mediafolder);
        recyclerView.setHasFixedSize(true);
        recyclerViewFolder.setHasFixedSize(true);
        imageView = (ImageView) rootView.findViewById(R.id.imageviewbigger);
        videoView = (VideoView) rootView.findViewById(R.id.videoviewbigger);
        radioGroup = (RadioGroup) rootView.findViewById(R.id.rg1);
        imagelayout = (RelativeLayout) rootView.findViewById(R.id.imagelayout);
        textviewNoContent = (TextView) rootView.findViewById(R.id.textviewNoContent);
        textviewMediaName = (TextView) rootView.findViewById(R.id.textviewMediaName);
        textviewLoading = (TextView) rootView.findViewById(R.id.textviewLoading);
        progressBarloading = (ProgressBar) rootView.findViewById(R.id.progressBarloading);
        buttonreloadfolder = (Button) rootView.findViewById(R.id.buttonreloadfolder);
        videoseekbar = (SeekBar) rootView.findViewById(R.id.videoseekbar);
        vidfulllength = (TextView) rootView.findViewById(R.id.vidfulllength);
        vidcurrentpos = (TextView) rootView.findViewById(R.id.vidcurrentpos);
        playbutton = (ImageView) rootView.findViewById(R.id.playbutton);
        deletebutton = (ImageButton) rootView.findViewById(R.id.deletebutton);
        sharebutton = (ImageButton) rootView.findViewById(R.id.sharebutton);
        selecttoggle = (ToggleButton) rootView.findViewById(R.id.selecttoggle);
        gallerycontainerfrag = rootView.findViewById(R.id.gallerycontainerfrag);


        string = getResources().getString(R.string.loading_thermal_media);

        int spanCount = 4;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (displayMetrics.widthPixels > 1080) spanCount = 6;
        layoutManager = new GridLayoutManager(getContext(), spanCount);
        recyclerViewFolder.setLayoutManager(layoutManager);

        createListsFolder = prepareDataFolder();
        adapterFolder = new FolderAdapter(getContext(), createListsFolder);
        adapterFolder.setHasStableIds(true);
        recyclerViewFolder.setAdapter(adapterFolder);
        Log.i(TAG, "onCreate: resetFolderIcons adapterFolder.hasStableIds = " + adapterFolder.hasStableIds());

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                //Log.i(TAG, "onCheckedChanged: onClick " + "openFolderforImageView");
                openFolderforImageView();
            }
        });

        mScaleGestureDetector = new ScaleGestureDetector(window.getContext(), new MediaScaleListener());
        mGestureDetector = new GestureDetector(rootView.getContext(), new DragListener());

        imagelayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int pointerCount = motionEvent.getPointerCount();
                Log.i(TAG, "onTouch: pointerCount = " + pointerCount);
                if (pointerCount > 1) {
                    zoommillis = System.currentTimeMillis();
                    //if(System.currentTimeMillis() - dragmillis > 500) { // This is to avoid accidental pan&tilt when finishing zoom (1 finger briefly becomes 2 fingers)
                    isZooming = true;
                    mScaleGestureDetector.onTouchEvent(motionEvent);
                    //}
                } else {
                    dragmillis = System.currentTimeMillis();
                    if (System.currentTimeMillis() - zoommillis > 250) { // This is to avoid accidental pan&tilt when finishing zoom (2 fingers briefly become 1 finger)
                        mGestureDetector.onTouchEvent(motionEvent);
                    }
                }
                //https://stackoverflow.com/questions/10484188/motionevent-getpointercount-is-always-1
                //The problem was that I was returning false in onTouch, therefore new touch events have not been generated.
                return true;
            }
        });

        videoseekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mHandler.removeCallbacks(updateTimeTask);
                    videoView.seekTo(videoseekbar.getProgress());
                    updateVidandPosition();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(updateTimeTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(updateTimeTask);
                videoView.seekTo(videoseekbar.getProgress());
                updateProgressBar();
            }
        });
        /*imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mScaleGestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });

        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mScaleGestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });*/

        buttonreloadfolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createListsFolder = prepareDataFolder();
                adapterFolder = new FolderAdapter(getContext(), createListsFolder);
                adapterFolder.setHasStableIds(true);
                recyclerViewFolder.setAdapter(adapterFolder);
            }
        });


        selecttoggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                selectMode = isChecked;
                if (!isChecked) {
                    FolderAdapter.ViewHolder viewHolder = (FolderAdapter.ViewHolder) recyclerViewFolder.findViewHolderForPosition(currentFolderIndex);
                    if (viewHolder != null) {
                        needToClick = true;
                        viewHolder.img.performClick();
                    }
                    needToClick = false;
                }
            }
        });

        deletebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
                //Toast.makeText(getContext(), "Feature will be available soon", Toast.LENGTH_SHORT).show();
            }
        });

        sharebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = currentImageIndex; //Todo need to find index
                String type = "unknown/unknown";
                if (createLists.get(index).getImage_Location().contains("jpg")) {
                    type = "image/jpg";
                } else if (createLists.get(index).getImage_Location().contains("mp4")) {
                    type = "video/mp4"; //Todo: need to select mp4 or jpg
                }
                File imagefile = new File(createLists.get(index).getImage_Location());
                Log.i(TAG, "onLongPress: file = " + createLists.get(index).getImage_Location());
                if (imagefile.exists()) {
                    Context context = getContext();
                    if (context != null) {
                        Uri contentUri = Uri.parse(createLists.get(index).getImage_Location());//FileProvider.getUriForFile(context, "com.suas.vuirhd2", imagefile);
                        Log.i(TAG, "onClick: uri = " + contentUri);
                        if (contentUri != null)
                            share(contentUri, type);
                    }
                }
            }
        });

        gallerycontainerfrag.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                HideAndroidBottomNavigationBarforTrueFullScreenView();
                return false;
            }
        });

        /*recyclerViewFolder.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                resetFolderIcons();
            }
        });*/
        return rootView;
    }

    private void HideAndroidBottomNavigationBarforTrueFullScreenView() {
        //https://stackoverflow.com/questions/16713845/permanently-hide-navigation-bar-in-an-activity/26013850
        View decorView = getActivity().getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void share(Uri contentUri, String type) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType(type);
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share with"));

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

    static String string = "";
    private Handler handlerUI = new Handler();
    private long Millis = System.currentTimeMillis();

    private void showPercent() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (loadPercent <= maxFileNumbers - 1) {
                    if (loadPercent != loadPercent_pre) {
                        Log.i(TAG, "run: percent loadPercent = " + loadPercent + " maxFileNumbers = " + maxFileNumbers);
                        loadPercent_pre = loadPercent;
                        final int percent = (int) ((loadPercent + 1) * 100f / maxFileNumbers);
                        Log.i(TAG, "run: percent = " + percent);
                        handlerUI.post(new Runnable() {
                            @Override
                            public void run() {
                                textviewLoading.setText(string + percent + "%");
                                progressBarloading.setProgress(percent);
                            }
                        });
                    }
                }
            }
        }).start();

    }

    private Handler mHandler = new Handler();

    private void updateProgressBar() {
        mHandler.postDelayed(updateTimeTask, 100);
    }

    private Runnable updateTimeTask = new Runnable() {
        public void run() {
            videoseekbar.setProgress(videoView.getCurrentPosition());
            videoseekbar.setMax(videoView.getDuration());
            vidcurrentpos.setText(videoCurrentPosition(videoView));
            int leftMargin = (int) ((videoView.getCurrentPosition() * 1f / videoView.getDuration()) * (videoseekbar.getWidth() - 80)) - vidcurrentpos.getWidth() / 2 + 40;
            if ((leftMargin < 20)) {
                leftMargin = 20;
            } else if (leftMargin > videoseekbar.getWidth() - 2 * vidcurrentpos.getWidth() - 40) {
                leftMargin = videoseekbar.getWidth() - 2 * vidcurrentpos.getWidth() - 40;
            }
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) vidcurrentpos.getLayoutParams();
            params.setMarginStart(leftMargin);
            vidcurrentpos.setLayoutParams(params);
            mHandler.postDelayed(this, 100);
        }
    };

    private void updateVidandPosition() {
        mHandler.postDelayed(updateTimeTaskPosition, 100);
    }

    private Runnable updateTimeTaskPosition = new Runnable() {
        public void run() {
            vidcurrentpos.setText(videoCurrentPosition(videoView));
            int leftMargin = (int) ((videoView.getCurrentPosition() * 1f / videoView.getDuration()) * (videoseekbar.getWidth() - 80)) - vidcurrentpos.getWidth() / 2 + 40;
            if ((leftMargin < 20)) {
                leftMargin = 20;
            } else if (leftMargin > videoseekbar.getWidth() - 2 * vidcurrentpos.getWidth() - 40) {
                leftMargin = videoseekbar.getWidth() - 2 * vidcurrentpos.getWidth() - 40;
            }
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) vidcurrentpos.getLayoutParams();
            params.setMarginStart(leftMargin);
            vidcurrentpos.setLayoutParams(params);
            mHandler.postDelayed(this, 100);
        }
    };

    @SuppressLint("DefaultLocale")
    private String videoCurrentPosition(VideoView videoView) {
        int duration = videoView.getCurrentPosition() / 1000;
        int hours = duration / 3600;
        int minutes = (duration / 60) - (hours * 60);
        int seconds = duration - (hours * 3600) - (minutes * 60);
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

    private class DragListener implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            if (videoView != null) {
                //VideoWindow.Horizontal.swapVideoSurface(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isZooming) {
                            //videoView.requestFocus();
                            if (videoView.isPlaying()) videoView.pause();
                            else {
                                videoView.start();
                                updateProgressBar();
                                playbutton.setVisibility(View.GONE);
                            }
                        }
                    }
                }, 200);
            }
            return true;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {
        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }
    }

    private float mScaleFactor = 1.0f, mScaleFactor_pre = 1.0f;
    private boolean isZooming = false;

    private class MediaScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mScaleFactor_pre = mScaleFactor;
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, 8.0f));
            //Log.i(TAG, "onScale: mScaleFactor = " + mScaleFactor);
            if (imageView.getVisibility() == View.VISIBLE) {
                imageView.setPivotX(scaleGestureDetector.getFocusX());
                imageView.setPivotY(scaleGestureDetector.getFocusY());
                imageView.setScaleX(mScaleFactor);
                imageView.setScaleY(mScaleFactor);
            }
            if (videoView.getVisibility() == View.VISIBLE) {
                videoView.setPivotX(scaleGestureDetector.getFocusX());
                videoView.setPivotY(scaleGestureDetector.getFocusY());
                videoView.setScaleX(mScaleFactor);
                videoView.setScaleY(mScaleFactor);
            }
            isZooming = true;
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            super.onScaleBegin(scaleGestureDetector);
            isZooming = true;
            Log.d(TAG, "ScaleListener onScaleBegin: ");
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            Log.d(TAG, "ScaleListener onScaleEnd: ");
            isZooming = false;
            super.onScaleEnd(detector);
        }
    }

    private void openFolderforImageView() {
        layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);
        String folder = adapterFolder.getfolderClicked();
        Log.i(TAG, "openFolderforImageView: folder = " + folder);
        createLists = prepareData(folder);
        loadPercent = 0;
        //showPercent();
        adapter = new MediaFileAdapter(getContext(), createLists, imageView, textviewMediaName, videoView);
        recyclerView.setAdapter(adapter);
        //loadPercent = maxFileNumbers;

        /*if (!recyclerView.hasOnClickListeners()) {
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    //Log.i("gallery", "onScrollStateChanged: newState = " + newState);
                    //https://stackoverflow.com/questions/36127734/detect-when-recyclerview-reaches-the-bottom-most-position-while-scrolling
                    //direction integers: -1 for up, 1 for down, 0 will always return false
                    //TODO make this scroll much smoother without having to load too many images at once
                    if (!recyclerView.canScrollVertically(1)) {
                        scrolldownMillis = System.currentTimeMillis();
                        if (System.currentTimeMillis() - scrollupMillis > 1000) {
                            endindex += 3;
                            if (endindex > maxpics)
                                startindex += 3;
                            if(endindex <= maxFileNumbers) {
                                createLists = prepareData(folderClicked);
                                adapter = new MediaFileAdapter(getContext(), createLists, imageView, textviewMediaName, videoView);
                                //adapter.setHasStableIds(true);
                                recyclerView.setAdapter(adapter);
                                recyclerView.scrollToPosition(endindex);
                                Log.i("gallery", "onScrollStateChanged: can't move Down further start = " + startindex + " end = " + endindex);
                                Log.i(TAG, "onScrollStateChanged: size = " + createLists.size());
                            }
                        }
                    } else if (!recyclerView.canScrollVertically(-1)) {
                        scrollupMillis = System.currentTimeMillis();
                        if (System.currentTimeMillis() - scrolldownMillis > 1000) {
                            startindex -= 3;
                            if (startindex >= 0) {
                                endindex -= 3;
                                createLists = prepareData(folderClicked);
                                adapter = new MediaFileAdapter(getContext(), createLists, imageView, textviewMediaName, videoView);
                                //adapter.setHasStableIds(true);
                                recyclerView.setAdapter(adapter);
                                recyclerView.scrollToPosition(startindex);
                                //Log.i("gallery", "onScrollStateChanged: can't move UP further start = " + startindex + " end = " + endindex);
                            } else {
                                startindex = 0;
                            }
                        }
                    }
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    //Log.i("", "onScrolled: dx = " + dx + " dy = " + dy);
                }
            });
        }*/
    }

    private void deleteFiles() {
        int count = adapter.getItemCount();
        for (int i = 0; i < count; i++) {
            MediaFileAdapter.ViewHolder viewHolder = (MediaFileAdapter.ViewHolder) recyclerView.findViewHolderForPosition(i);
            if (viewHolder != null) {
                if (viewHolder.selected) {
                    String imagepath = createLists.get(i).getImage_Location();
                    File file = new File(imagepath);
                    if (file.exists()) {
                        if (file.delete()) {
                            Log.i(TAG, "deleteFiles: onClick deleted file " + imagepath);
                        }
                        String nameWithoutJPGorMP4 = imagepath.substring(imagepath.lastIndexOf("/"), imagepath.lastIndexOf("."));
                        String thumbpath = imagepath.substring(0, imagepath.lastIndexOf("/")) + "/thumbs" + nameWithoutJPGorMP4 + "_thumb.jpg";
                        File thumbFile = new File(thumbpath);
                        if (thumbFile.exists()) {
                            if (thumbFile.delete()) {
                                Log.i(TAG, "deleteFiles: onClick deleted file " + thumbpath);
                            }
                        }
                    }
                }
            }
        }

        //Reload the folder
        FolderAdapter.ViewHolder viewHolder = (FolderAdapter.ViewHolder) recyclerViewFolder.findViewHolderForPosition(currentFolderIndex);
        if (viewHolder != null) {
            needToClick = true;
            viewHolder.img.performClick();
        }
        needToClick = false;

    }

    protected static int checked = 1;

    protected static void resetFolderIcons(int exludeindex) {
        if (checked == 0) {
            checked = 1;
            radioGroup.check(R.id.rb1);
        } else {
            checked = 0;
            radioGroup.check(R.id.rb0);
        }
        int itemCount = Objects.requireNonNull(recyclerViewFolder.getAdapter()).getItemCount();
        Log.i(TAG, "resetFolderIcons: itemCount = " + itemCount);
        for (int i = 0; i < itemCount; i++) {
            if (i != exludeindex) {
                FolderAdapter.ViewHolder viewHolder = (FolderAdapter.ViewHolder) recyclerViewFolder.findViewHolderForPosition(i);
                Log.i(TAG, "resetFolderIcons: i = " + i + " viewHolder = " + viewHolder);
                if (viewHolder != null) {
                    viewHolder.img.setImageResource(R.drawable.ic_folder_black_48dp);
                    viewHolder.title.setText(createListsFolder.get(i).getImage_title());
                    Log.i(TAG, "onBindViewHolder: title = " + createListsFolder.get(i).getImage_title());
                    viewHolder.currentFolder = false;
                }
            }
        }
    }

    private void confirm() {
        //https://stackoverflow.com/questions/13675822/android-alertdialog-builder with my own modifications
        //https://stackoverflow.com/questions/18346920/change-the-background-color-of-a-pop-up-dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext(), R.style.MyDialogTheme);//AlertDialog.THEME_HOLO_LIGHT);

        // set title
        alertDialogBuilder.setTitle("Media Gallery");
        alertDialogBuilder.setIcon(R.drawable.ic_delete_black_24dp);

        // set dialog message
        alertDialogBuilder
                .setMessage("Delete all selected files?")
                .setCancelable(false)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                        HideAndroidBottomNavigationBarforTrueFullScreenView();
                        //selecttoggle.setChecked(false);
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        deleteFiles();
                        HideAndroidBottomNavigationBarforTrueFullScreenView();
                        selecttoggle.setChecked(false);
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
        //https://stackoverflow.com/questions/4406804/how-to-control-the-width-and-height-of-the-default-alert-dialog-in-android
        //Objects.requireNonNull(alertDialog.getWindow()).setLayout(400, 200);
    }


    private ArrayList<CreateList> prepareData(String folder) {

        ArrayList<CreateList> theimage = new ArrayList<>();

        String path = "";//Environment.getExternalStorageDirectory() + "/sUAS.com/VuIR_Media/" + folder;//Environment.getRootDirectory().toString();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            path = Environment.getExternalStorageDirectory() + "/sUAS.com/VuIR_Media/" + folder;
        } else {
            path = Objects.requireNonNull(getActivity()).getExternalFilesDir(null) + "/sUAS.com/VuIR_Media/" + folder;
        }

        Log.i(TAG, "prepareData: path = " + path);
        File f = new File(path);
        File[] file = f.listFiles();
        if (file != null) {
            //https://stackoverflow.com/questions/203030/best-way-to-list-files-in-java-sorted-by-date-modified
            Arrays.sort(file, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    //return Long.compare(f2.lastModified(), f1.lastModified());
                    //https://stackoverflow.com/questions/18751468/how-can-i-sort-my-listview-by-z-a-alphabetical-instead-of-a-z
                    return -f1.getName().compareTo(f2.getName());
                }
            });

            /*if (file.length < endindex) endindex = file.length;
            if (file.length < startindex) startindex = file.length - maxpics;
            if (startindex >= endindex) startindex = endindex - 3;
            if (startindex < 0) startindex = 0;*/
            maxFileNumbers = 0;
            for (int i = 0; i < file.length; i++) {
                if (file[i].getName().contains("jpg") || file[i].getName().contains("mp4")) {
                    int file_size_in_kB = Integer.parseInt(String.valueOf(file[i].length() / 1024));
                    if (file_size_in_kB < 10)
                        continue; //Discard corrupted or too small (< 10kB) files so it won't crash the app
                    if (file_size_in_kB < 300 && file[i].getName().contains("mp4"))
                        continue; //Discard corrupted or too small (< 10kB) files so it won't crash the app
                    maxFileNumbers++;
                    //if (i >= startindex && i < endindex) {
                    CreateList createList = new CreateList();
                    createList.setImage_title(file[i].getName());
                    createList.setImage_Location(path + "/" + file[i].getName());
                    createList.setThumb_location(path + "/thumbs/" + file[i].getName().substring(0, file[i].getName().lastIndexOf(".")) + "_thumb.jpg");
                    //Log.i(TAG, "prepareData: file name = " + file[i].getName());

                    theimage.add(createList);
                    //}
                }
            }
        }

        return theimage;
    }

    private ArrayList<CreateList> prepareDataFolder() {

        ArrayList<CreateList> theimage = new ArrayList<>();
        //https://stackoverflow.com/questions/12780446/check-if-a-path-represents-a-file-or-a-folder
        String path = "";//Environment.getExternalStorageDirectory() + "/sUAS.com/VuIR_Media";//Environment.getRootDirectory().toString();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            path = Environment.getExternalStorageDirectory() + "/sUAS.com/VuIR_Media";//Environment.getRootDirectory().toString();
        } else {
            path = Objects.requireNonNull(getActivity()).getExternalFilesDir(null) + "/sUAS.com/VuIR_Media";
        }
        Log.d(TAG, "prepareData Folder: path = " + path);
        File f = new File(path);
        File[] files = f.listFiles();
        boolean foundNocontent = true;
        if (files != null) {
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    //return Long.compare(f2.lastModified(), f1.lastModified());
                    return -f1.getName().compareTo(f2.getName());
                }
            });
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    CreateList createList = new CreateList();
                    createList.setImage_title(files[i].getName());
                    //createList.setImage_Location(path + "/" + file[i].getName());
                    theimage.add(createList);
                    foundNocontent = false;
                }
            }
        }
        if (foundNocontent) textviewNoContent.setVisibility(View.VISIBLE);
        else textviewNoContent.setVisibility(View.GONE);
        return theimage;
    }
}