package com.folkcat.run.adapter;

import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.folkcat.run.R;
import com.folkcat.run.db.mode.Photo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tamas on 2016/4/23.
 */
public class BottomPhotoRecyclerViewAdapter extends RecyclerView.Adapter<BottomPhotoRecyclerViewAdapter.SimpleItemViewHolder> {

    private static final String TAG="MyRecyclerViewAdapter";
    private List<Photo> mPhotoList;

    public BottomPhotoRecyclerViewAdapter(@NonNull List<Photo> photoList) {
        this.mPhotoList = (photoList != null ? photoList : new ArrayList<Photo>());
        int size=mPhotoList.size();
        for(int i=0;i<size;i++){
            Log.i(TAG,mPhotoList.get(i)+"");
        }

    }

    @Override
    public SimpleItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_bottom_photo, viewGroup, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SimpleItemViewHolder viewHolder, int position) {
        viewHolder.imageView.setImageBitmap(BitmapFactory.decodeFile(""));
    }

    @Override
    public int getItemCount() {
        return (this.mPhotoList != null) ? this.mPhotoList.size() : 0;
    }

    protected final static class SimpleItemViewHolder extends RecyclerView.ViewHolder {
        protected ImageView imageView;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            this.imageView = (ImageView) itemView.findViewById(R.id.iv_bottom_photo);
        }
    }
}