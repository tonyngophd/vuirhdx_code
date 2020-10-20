package com.suas.uxdual;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static com.suas.uxdual.MediaGalleryFragment.currentImageIndex;
import static com.suas.uxdual.MediaGalleryFragment.loadPercent;
import static com.suas.uxdual.MediaGalleryFragment.needToReloadImage0;

public class MediaFileAdapter extends RecyclerView.Adapter<MediaFileAdapter.ViewHolder> {
    private ArrayList<CreateList> galleryList;
    private Context context;
    private ImageView imageView;
    private TextView textView;
    private VideoView videoView;

    public MediaFileAdapter(Context context, ArrayList<CreateList> galleryList, ImageView imageView, TextView textView, VideoView videoView) {
        this.galleryList = galleryList;
        this.context = context;
        this.imageView = imageView;
        this.textView = textView;
        this.videoView = videoView;
    }

    @Override
    public MediaFileAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    public void onBindViewHolder(final MediaFileAdapter.ViewHolder viewHolder, int i) {

        //viewHolder.title.setText(galleryList.get(i).getImage_title());
        viewHolder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //viewHolder.img.setImageResource((galleryList.get(i).getImage_ID()));
        //Picasso.with(context).load(galleryList.get(i).getImage_ID()).resize(240, 120).into(viewHolder.img);
        //Picasso.with(context).load(galleryList.get(i).getImage_Location()).resize(80, 64).into(viewHolder.img);
        Bitmap bmp = null;
        Bitmap thumbbitmap = null;
        File file = new File(galleryList.get(i).getThumb_location());
        if (file.exists()) {
            thumbbitmap = BitmapFactory.decodeFile(galleryList.get(i).getThumb_location());
        } else {
            if (galleryList.get(i).getImage_Location().contains("jpg")) {
                File imagefile = new File(galleryList.get(i).getImage_Location());
                if (imagefile.exists())
                    bmp = BitmapFactory.decodeFile(galleryList.get(i).getImage_Location());
            } else if (galleryList.get(i).getImage_Location().contains("mp4")) {
                File filemp4 = new File(galleryList.get(i).getImage_Location());
                if (filemp4.exists()) {
                    /*FrameGrab grab = null;
                    try {
                        grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(filemp4));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JCodecException e) {
                        e.printStackTrace();
                    }
                    Picture picture = null;
                    try {
                        assert grab != null;
                        for (int j = 0; j < 10; j++) {
                            picture = grab.getNativeFrame();
                            //if (picture != null) break;
                        }
                        if (picture != null)
                            bmp = AndroidUtil.toBitmap(picture);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    try {
                        retriever.setDataSource(galleryList.get(i).getImage_Location());
                        bmp = retriever.getFrameAtTime(1);
                    } catch (Exception ignored){
                    }
                }
            }
            if (bmp != null) {
                thumbbitmap = Bitmap.createScaledBitmap(bmp, 80, 64, false);
                String mfilepath = galleryList.get(i).getImage_Location();
                String mfolderpath = mfilepath.substring(0, mfilepath.lastIndexOf("/")) + "/thumbs";
                Log.i("MFileAdapter", "onBindViewHolder: mfolderpath = " + mfolderpath);
                if (createFolder(mfolderpath)) {
                    ///Saving the thumbnail just created to speed up the next loads
                    File imageFile = new File(galleryList.get(i).getThumb_location());
                    FileOutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(imageFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    int quality = 100;
                    thumbbitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                    try {
                        assert outputStream != null;
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (thumbbitmap == null) return;
        viewHolder.img.setImageBitmap(thumbbitmap);
        viewHolder.index = i;
        loadPercent = i;
        if (galleryList.get(i).getImage_Location().contains("mp4"))
            viewHolder.playButton.setVisibility(View.VISIBLE);
        else viewHolder.playButton.setVisibility(View.GONE);
        //Log.i("MFileAdapter", "onBindViewHolder: percent loadPercent = " + loadPercent);
        //showPercent(i);

        if (i == 0 && needToReloadImage0) { //MediaGalleryFragment.startindex
            needToReloadImage0 = false;
            if (galleryList.get(i).getImage_Location().contains("jpg")) {
                if (imageView != null) {
                    if (videoView != null) videoView.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setZ(10f);
                    if (bmp != null) {
                        imageView.setImageBitmap(bmp);
                    } else {
                        File imagefile = new File(galleryList.get(i).getImage_Location());
                        if (imagefile.exists())
                            imageView.setImageBitmap(BitmapFactory.decodeFile(galleryList.get(i).getImage_Location()));
                    }
                }
            } else if (galleryList.get(i).getImage_Location().contains("mp4")) {
                if (videoView != null) {
                    if (imageView != null) imageView.setVisibility(View.GONE);
                    videoView.setVisibility(View.VISIBLE);
                    videoView.setZ(10f);
                    File imagefile = new File(galleryList.get(i).getImage_Location());
                    if (imagefile.exists()) {
                        Uri uri = Uri.parse(galleryList.get(i).getImage_Location());
                        videoView.setVideoURI(uri);
                        videoView.seekTo(1);
                        //https://stackoverflow.com/questions/17079593/how-to-set-the-preview-image-in-videoview-before-playing
                        MediaGalleryFragment.playbutton.setVisibility(View.VISIBLE);
                        MediaGalleryFragment.videoseekbar.setVisibility(View.VISIBLE);
                        MediaGalleryFragment.vidfulllength.setVisibility(View.VISIBLE);
                        MediaGalleryFragment.vidcurrentpos.setVisibility(View.VISIBLE);
                        MediaGalleryFragment.playbutton.setZ(10f);
                        MediaGalleryFragment.vidfulllength.setZ(11f);
                        MediaGalleryFragment.vidcurrentpos.setZ(11f);
                        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mediaPlayer) {
                                MediaGalleryFragment.vidfulllength.setText(videoDuration(videoView));
                                //enableSound(15, mediaPlayer);
                            }
                        });
                    }
                }
            }
            if (textView != null) {
                textView.setText(galleryList.get(i).getImage_title());
            }
        }

        /*mGestureDetector = new GestureDetector(viewHolder.img.getContext(), new DragListener(viewHolder));
        viewHolder.img.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mGestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });*/
        if (!viewHolder.img.hasOnClickListeners()) {
            viewHolder.img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int index = viewHolder.index;
                    currentImageIndex = viewHolder.index;
                    //Toast.makeText(context, name, Toast.LENGTH_SHORT).show();
                    if (galleryList.get(index).getImage_Location().contains("jpg")) {
                        if (imageView != null) {
                            if (videoView != null) videoView.setVisibility(View.GONE);
                            imageView.setVisibility(View.VISIBLE);
                            imageView.setZ(10f);
                            File imagefile = new File(galleryList.get(index).getImage_Location());
                            if (imagefile.exists())
                                imageView.setImageBitmap(BitmapFactory.decodeFile(galleryList.get(index).getImage_Location()));
                            //Log.i("MFileAdapter", "onClick: index = " + index + " title = " + galleryList.get(index).getImage_title() + " location = " + galleryList.get(index).getImage_Location());
                            MediaGalleryFragment.playbutton.setVisibility(View.GONE);
                            MediaGalleryFragment.videoseekbar.setVisibility(View.GONE);
                            MediaGalleryFragment.vidfulllength.setVisibility(View.GONE);
                            MediaGalleryFragment.vidcurrentpos.setVisibility(View.GONE);
                        }
                    } else if (galleryList.get(index).getImage_Location().contains("mp4")) {
                        if (videoView != null) {
                            if (imageView != null) imageView.setVisibility(View.GONE);
                            //VideoWindow.Horizontal.swapVideoSurface(true);
                            videoView.setVisibility(View.VISIBLE);
                            videoView.setZ(10f);
                            File imagefile = new File(galleryList.get(index).getImage_Location());
                            if (imagefile.exists()) {
                                Uri uri = Uri.parse(galleryList.get(index).getImage_Location());
                                videoView.setVideoURI(uri);
                                videoView.seekTo(1);
                                //https://stackoverflow.com/questions/17079593/how-to-set-the-preview-image-in-videoview-before-playing
                            }
                            MediaGalleryFragment.playbutton.setVisibility(View.VISIBLE);
                            MediaGalleryFragment.videoseekbar.setVisibility(View.VISIBLE);
                            MediaGalleryFragment.vidfulllength.setVisibility(View.VISIBLE);
                            MediaGalleryFragment.vidcurrentpos.setVisibility(View.VISIBLE);
                            MediaGalleryFragment.playbutton.setZ(10f);
                            MediaGalleryFragment.vidfulllength.setZ(11f);
                            MediaGalleryFragment.vidcurrentpos.setZ(11f);
                            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mediaPlayer) {
                                    MediaGalleryFragment.vidfulllength.setText(videoDuration(videoView));
                                }
                            });
                            //Log.i("MFileAdapter", "onClick: index = " + index + " title = " + galleryList.get(index).getImage_title() + " location = " + galleryList.get(index).getImage_Location());
                        }
                    }
                    if (textView != null) {
                        textView.setText(galleryList.get(index).getImage_title());
                    }

                    if (MediaGalleryFragment.selectMode) {
                        viewHolder.selected = !viewHolder.selected;
                        if (viewHolder.selected) {
                            viewHolder.img.setAlpha(0.35f);
                        } else {
                            viewHolder.img.setAlpha(1f);
                        }
                    } else {
                        viewHolder.img.setAlpha(1f);
                    }
                }
            });
        }
    }

    private void enableSound(int sound, MediaPlayer mp) {
        float f = (float) sound;
        Log.e("checkingsounds", "&&&&&   " + f);
        mp.setVolume(f, f);
        //mp.setLooping(true);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        assert audioManager != null;
        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); //Max Volume 15
        audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);  //this will return current volume.
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sound, AudioManager.FLAG_PLAY_SOUND);   //here you can set custom volume.
    }

    @SuppressLint("DefaultLocale")
    private String videoDuration(VideoView videoView) {
        //Log.i("MFileAdapter", "videoDuration: " + videoView.getDuration());
        int duration = videoView.getDuration() / 1000;
        int hours = duration / 3600;
        int minutes = (duration / 60) - (hours * 60);
        int seconds = duration - (hours * 3600) - (minutes * 60);
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public int getItemCount() {
        return galleryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private ImageView img;
        private int index;
        private ImageView playButton;
        boolean selected = false;

        ViewHolder(View view) {
            super(view);

            title = (TextView) view.findViewById(R.id.title);
            img = (ImageView) view.findViewById(R.id.img);
            playButton = (ImageView) view.findViewById(R.id.playButton);
        }
    }

    private boolean createFolder(String mfolderpath) {
        File mFolder = new File(mfolderpath);
        //Log.i("MFileAdapter", "createFolder: takeScreenshot creating folder " + mfolderpath);
        if (!mFolder.exists()) {
            if (!mFolder.mkdir()) {
                //Log.i(ContentValues."MFileAdapter", "takeScreenshot: cannot create folder " + mfolderpath);
                return false; //
            }
        }
        return true;
    }

    private GestureDetector mGestureDetector;

    private class DragListener implements GestureDetector.OnGestureListener {

        ViewHolder viewHolder;

        DragListener(final ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        public boolean onDown(MotionEvent motionEvent) {
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
            Log.i("MFileAdapter", "onLongPress: inded = " + viewHolder.index);
        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }
    }
}