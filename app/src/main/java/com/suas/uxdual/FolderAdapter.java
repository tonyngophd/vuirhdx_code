package com.suas.uxdual;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import static com.suas.uxdual.MediaGalleryFragment.needToClick;
import static com.suas.uxdual.MediaGalleryFragment.resetFolderIcons;


public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {
    private ArrayList<CreateList> galleryListFolder;
    private Context context;
    private int previousclick;
    private String folderClicked = "";
    private FolderAdapter.ViewHolder mviewHolder;

    public FolderAdapter(Context context, ArrayList<CreateList> galleryListFolder) {
        this.galleryListFolder = galleryListFolder;
        this.context = context;
    }

    @Override
    public FolderAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.folder_cell_layout, viewGroup, false);
        FolderAdapter.ViewHolder viewholder = new ViewHolder(view);
        //viewholder.img.setImageResource(R.drawable.ic_folder_open_black_48dp);
        return viewholder;
    }

    @Override
    public void onBindViewHolder(final FolderAdapter.ViewHolder viewHolder, int i) {
        viewHolder.title.setText(galleryListFolder.get(i).getImage_title());
        Log.i("MediaActivity", "onBindViewHolder: title = " + galleryListFolder.get(i).getImage_title());
        viewHolder.img.setImageResource(R.drawable.ic_folder_black_48dp);
        final String name = galleryListFolder.get(i).getImage_title();
        viewHolder.index = i;
        if(i == 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    viewHolder.img.performClick();
                }
            },100);
        }

        if(!viewHolder.img.hasOnClickListeners()) {
            viewHolder.img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!viewHolder.currentFolder || needToClick) {
                        MediaGalleryFragment.needToReloadImage0 = true;
                        viewHolder.currentFolder = true;
                        //Toast.makeText(context, name, Toast.LENGTH_SHORT).show();
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                viewHolder.img.setImageResource(R.drawable.ic_folder_open_black_48dp);
                                viewHolder.title.setText(galleryListFolder.get(viewHolder.index).getImage_title());
                                Log.i("MediaActivity", "onBindViewHolder: title = " + galleryListFolder.get(viewHolder.index).getImage_title());
                            }
                        });
                        previousclick = viewHolder.index;
                        folderClicked = name;
                        MediaGalleryFragment.folderClicked = name;
                        mviewHolder = viewHolder;
                        resetFolderIcons(viewHolder.index);
                        MediaGalleryFragment.currentFolderIndex = viewHolder.index;
                    } else {
                        Log.d("MediaActivity", "onClick: Current folder, do nothing");
                    }
                }
            });
        }
    }

    String getfolderClicked(){
        return folderClicked;
    }

    public FolderAdapter.ViewHolder getViewHolder(){
        return  mviewHolder;
    }
    @Override
    public int getItemCount() {
        return galleryListFolder.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView img;
        private int index;
        boolean currentFolder = false;

        ViewHolder(View view) {
            super(view);

            title = (TextView) view.findViewById(R.id.title);
            img = (ImageView) view.findViewById(R.id.img);
        }
    }
}