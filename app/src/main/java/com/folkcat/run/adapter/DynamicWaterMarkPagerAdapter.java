package com.folkcat.run.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.folkcat.run.R;
import com.folkcat.run.util.TamasUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Tamas on 2015/8/6.
 */
public class DynamicWaterMarkPagerAdapter extends PagerAdapter {
    private int mPreiviewHeight;
    private int toLeft,toTop,toLef1,toTop1;
    private int mScreenWidth;

    private void setPosition(int x,int y){
        toLeft=toLef1;
        toTop=toTop1;
        toLef1=x;
        toTop1=y;
    }

    private Activity mActivity;
    private List<Bitmap> mWMBmList=new ArrayList<Bitmap>();
    public DynamicWaterMarkPagerAdapter(Activity activity, List<Bitmap> bmList, int height){
        this.mWMBmList=bmList;
        this.mActivity=activity;
        this.mPreiviewHeight=height;
        this.mScreenWidth= TamasUtils.getScreenWidth(mActivity);
    }
    @Override
    public void destroyItem(View container, int position, Object object) {
        ((ViewGroup) container).removeView((View) object);
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }
    /**
     * 载入图片，用当前的position 除以 图片数组长度取余数
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final int fPosition=position;
        LinearLayout waterMarkLinearLayout=new LinearLayout(mActivity);
        String inflater= Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li;
        li=(LayoutInflater)mActivity.getSystemService(inflater);
        li.inflate(R.layout.item_water_mark, waterMarkLinearLayout, true);
        final ImageView waterMarkImage=(ImageView)waterMarkLinearLayout.findViewById(R.id.water_mark_image);

        int screenWidth=TamasUtils.getScreenWidth(mActivity);
        final LinearLayout.LayoutParams watermarkLayoutParmas=new LinearLayout.LayoutParams((int)(screenWidth/2),(int)(screenWidth/2));
        Bitmap waterMarkBm=mWMBmList.get(position % mWMBmList.size());
        setPosition(dp(10),TamasUtils.getScreenWidth(mActivity)-waterMarkBm.getHeight()-dp(10));
//        Log.i(TAG,"ToLeft:"+toLeft+"  ToTop:"+toTop);
//        Log.i(TAG,"WaterMarkBm  width:"+waterMarkBm.getWidth()+"  height:"+waterMarkBm.getHeight());
        waterMarkImage.setImageBitmap(waterMarkBm);
        container.addView(waterMarkLinearLayout);
        waterMarkImage.setOnTouchListener(new View.OnTouchListener() {
            Boolean onMove = false;
            float touch_x, touch_y;
            private int v_top,v_left;
            int mx, my;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!onMove) {
                            touch_x = event.getRawX();
                            touch_y = event.getRawY();
                            v_top = v.getTop();
                            v_left = v.getLeft();
                            onMove = true;
                        }
                        mx = (int) (event.getRawX() - touch_x);
                        my = (int) (event.getRawY() - touch_y);
                        v.layout(mx + v_left, my + v_top, mx + v_left + v.getWidth(), my + v_top + v.getHeight());
                        break;
                    case MotionEvent.ACTION_UP:
                        if (onMove) {
                            onMove = false;
                        }
                        toLeft=v.getLeft();
                        toTop=v.getTop();
                        break;
                }
                return true;
            }
        });
        return waterMarkLinearLayout;
    }
    public int getV_top(){
        return this.toTop;
    }
    public int getV_left(){
        return this.toLeft;
    }
    private int dp(float dpValue){
        return TamasUtils.dip2px(mActivity,dpValue);
    }
}