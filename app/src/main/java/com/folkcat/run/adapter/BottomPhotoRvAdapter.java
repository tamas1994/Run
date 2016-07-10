package com.folkcat.run.adapter;

import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.folkcat.run.R;
import com.folkcat.run.db.mode.Photo;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Tamas on 2016/4/23.
 */
public class BottomPhotoRvAdapter extends RecyclerView.Adapter<BottomPhotoRvAdapter.SimpleItemViewHolder> {

    private static final String TAG="BottomPhotoRecyc**";
    private RealmResults<Photo> mPhotoList;

    /*
    addChangeListener里listener是一个弱引用，极容易被GC。因此为了保证活性，要把listener设为强引用的成员变量
     */
    RealmChangeListener mRealmListener=new RealmChangeListener() {
        @Override
        public void onChange() {
            Log.i(TAG,"RealmListener OnChang");
            notifyDataSetChanged();
        }
    };




    public BottomPhotoRvAdapter(long runningId) {

        Realm realm= Realm.getDefaultInstance();
        mPhotoList = realm.where(Photo.class).equalTo("runningId", runningId).findAll();
        mPhotoList.sort("createDate", RealmResults.SORT_ORDER_DESCENDING);
        realm.addChangeListener(mRealmListener);


        int size=mPhotoList.size();
        for(int i=0;i<size;i++){
            Log.i(TAG,mPhotoList.get(i)+"");
        }
        Log.i(TAG,"photoListSize:"+mPhotoList.size());
    }

    @Override
    public SimpleItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_bottom_photo, viewGroup, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SimpleItemViewHolder viewHolder, int position) {
        viewHolder.imageView.setImageBitmap(BitmapFactory.decodeFile(mPhotoList.get(position).getThumbnailPath()));
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