package com.folkcat.run.adapter;

import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.folkcat.run.R;
import com.folkcat.run.db.mode.Photo;
import com.folkcat.run.db.mode.RunningRecord;
import com.folkcat.run.db.util.RunningRecordUtil;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Tamas on 2016/4/23.
 */
public class MapThumbGruidRvAdapter extends RecyclerView.Adapter<MapThumbGruidRvAdapter.SimpleItemViewHolder>   {

    private static final String TAG="MapThumbGruidRvAdapter";
    private List<RunningRecord> mRunningRecordList;

    private MyItemClickListener mItemClickListener;



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





    public MapThumbGruidRvAdapter() {
        mRunningRecordList= RunningRecordUtil.getRecordListFromDb();
        Realm.getDefaultInstance().addChangeListener(mRealmListener);
        Log.i(TAG,"mRunningRecordList.size:"+mRunningRecordList.size());

    }
    public List<RunningRecord> getRunningRecordList(){
        return mRunningRecordList;
    }

    @Override
    public SimpleItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_map_grid, viewGroup, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SimpleItemViewHolder viewHolder, int position) {
        RunningRecord recordItem=mRunningRecordList.get(position);
        viewHolder.ivMapThumb.setImageBitmap(BitmapFactory.decodeFile(recordItem.getThumbnailPath()));
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        viewHolder.tvRunningTitle.setText(simpleDateFormat.format(new Date(recordItem.getCreateDate())));
    }

    @Override
    public int getItemCount() {
        return (this.mRunningRecordList != null) ? this.mRunningRecordList.size() : 0;
    }
    //final static
    protected  class SimpleItemViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {
        protected ImageView ivMapThumb;
        protected TextView tvRunningTitle;

        //private MyItemClickListener mListener;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            this.ivMapThumb = (ImageView) itemView.findViewById(R.id.iv_map_thumb);
            this.tvRunningTitle=(TextView)itemView.findViewById(R.id.tv_running_title);
            ivMapThumb.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG,"onClick called");
            if( mItemClickListener!= null){
                mItemClickListener.onItemClick(v,getPosition());
            }
        }

    }

    public void setOnItemClickListener(MyItemClickListener listener){
        this.mItemClickListener = listener;
    }

    public interface MyItemClickListener {
        public void onItemClick(View view,int postion);
    }



}