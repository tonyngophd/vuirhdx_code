package com.suas.uxdual;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private ArrayList<CreateList> galleryList;
    private Context context;
    private ImageView imageView;

    public MyAdapter(Context context, ArrayList<CreateList> galleryList, ImageView imageView) {
        this.galleryList = galleryList;
        this.context = context;
        this.imageView = imageView;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyAdapter.ViewHolder viewHolder, int i) {

        viewHolder.title.setText(galleryList.get(i).getImage_title());
        viewHolder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //viewHolder.img.setImageResource((galleryList.get(i).getImage_ID()));
        //Picasso.with(context).load(galleryList.get(i).getImage_ID()).resize(240, 120).into(viewHolder.img);
        //Picasso.with(context).load(galleryList.get(i).getImage_Location()).resize(400, 300).into(viewHolder.img);
        final Bitmap bmp = BitmapFactory.decodeFile(galleryList.get(i).getImage_Location());
        viewHolder.img.setImageBitmap(Bitmap.createScaledBitmap(bmp, 80, 64, false));
        final String name = galleryList.get(i).getImage_title();
        viewHolder.img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, name, Toast.LENGTH_SHORT).show();
                if (imageView != null) {
                    imageView.setImageBitmap(bmp);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return galleryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private ImageView img;

        public ViewHolder(View view) {
            super(view);

            title = (TextView) view.findViewById(R.id.title);
            img = (ImageView) view.findViewById(R.id.img);
        }
    }

}