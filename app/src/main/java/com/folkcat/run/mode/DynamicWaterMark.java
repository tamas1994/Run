package com.folkcat.run.mode;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.Log;


import com.folkcat.run.R;
import com.folkcat.run.db.mode.GPSPoint;
import com.folkcat.run.util.TamasUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tamas on 2015/8/13.
 */
public class DynamicWaterMark {
    private static final String TAG = "DynamicWaterMark";

    private String mAvgPace;
    private String mCal;
    private String mDateTimeStr;
    private String mCity;
    private String mTimeStr;
    private String mLengthStr;
    private float mTrackWidth, mTrackHeight;
    private double mMaxLat = 0, mMaxLng = 0, mMinLat = 0, mMinLng = 0;
    private List<GPSPoint> mGpsPointList = new ArrayList<>();
    private float mTrackLongEdge;
    private Activity mActivity;//用来获取屏幕信息
    private int mScreenWidth;


    public DynamicWaterMark(Activity activity, String city, String dateTime, String pace, String cal, String timeStr, String lengthStr, List<GPSPoint> gpsPointList) {
        this.mActivity = activity;
        this.mCity = city;
        this.mDateTimeStr = dateTime;
        this.mAvgPace = pace;
        this.mCal = cal;
        this.mTimeStr = timeStr;
        this.mLengthStr = lengthStr;
        long startTime=System.currentTimeMillis();
        mGpsPointList.addAll(gpsPointList);
        Log.i(TAG,"共耗时："+(System.currentTimeMillis()-startTime) );
        this.mScreenWidth = TamasUtils.getScreenWidth(mActivity);
    }

    public Bitmap getBm1() {
        int bmWidth = TamasUtils.getScreenWidth(mActivity); //bitmap宽度为屏幕宽度
        Bitmap trackBm = getGpsTrackBitmap(bmWidth / 3);
        Bitmap newBm = Bitmap.createBitmap(bmWidth, dp(100), Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newBm);
        Paint paint = new Paint();
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);//滤波处理
        paint.setColor(Color.WHITE);
        paint.setTextSize(dp(35));
        cv.drawBitmap(trackBm, 0, 0, paint);
        paint.setStrokeWidth(3);
        cv.drawLine(trackBm.getWidth() + dp(10), 0, trackBm.getWidth() + dp(10), trackBm.getHeight(), paint);
        cv.drawText(mLengthStr, trackBm.getWidth() + dp(20), dp(35), paint);
        float textWidth = paint.measureText(mLengthStr);
        paint.setTextSize(dp(20));
        cv.drawText("KM", trackBm.getWidth() + dp(20) + textWidth, dp(35), paint);
        paint.setTextSize(dp(17));
        cv.drawText(mDateTimeStr, trackBm.getWidth() + dp(20), dp(57), paint);
        mCity="厦门";
        cv.drawText(mCity, trackBm.getWidth() + dp(20), dp(75), paint);
        return newBm;
    }

    //获得GPS轨迹Bitmap
    private Bitmap getGpsTrackBitmap(int bmWidth) {
        if (mGpsPointList == null) {
            mGpsPointList = new ArrayList<>();
        }
        if (mGpsPointList.size() < 2) {
            GPSPoint p1 = new GPSPoint();
            GPSPoint p2 = new GPSPoint();
            p1.setLatitude(100);
            p1.setLongitude(100);
            p2.setLatitude(100);
            p2.setLongitude(100);
            mGpsPointList.add(p1);
            mGpsPointList.add(p2);

        }
        int fixedBitmapWidth = (int) (bmWidth * 0.8);
        Bitmap newBitmap = Bitmap.createBitmap(bmWidth, bmWidth, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.create("宋体", Typeface.BOLD));
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6);
        mMaxLat = mGpsPointList.get(0).getLatitude();
        mMinLat = mGpsPointList.get(0).getLatitude();
        mMaxLng = mGpsPointList.get(0).getLongitude();
        mMinLng = mGpsPointList.get(0).getLongitude();

        int step = 1;//步幅，如果GPS点太多，忽略一些点
        if (mGpsPointList.size() > 75) {
            step = mGpsPointList.size() / 75;
        }
        //获得最大、最小经、纬度
        for (int i = 0; i < mGpsPointList.size(); i = i + step) {
            if (mGpsPointList.get(i).getLatitude() > mMaxLat)
                mMaxLat = mGpsPointList.get(i).getLatitude();
            if (mGpsPointList.get(i).getLatitude() < mMinLat)
                mMinLat = mGpsPointList.get(i).getLatitude();
            if (mGpsPointList.get(i).getLongitude() > mMaxLng)
                mMaxLng = mGpsPointList.get(i).getLongitude();
            if (mGpsPointList.get(i).getLongitude() < mMinLng)
                mMinLng = mGpsPointList.get(i).getLongitude();
        }
        this.mTrackHeight = (float) ((mMaxLat - mMinLat) * 10000 * 8);
        this.mTrackWidth = (float) ((mMaxLng - mMinLng) * 100000);
        if (this.mTrackHeight < this.mTrackWidth) {
            this.mTrackLongEdge = this.mTrackWidth;
        } else {
            this.mTrackLongEdge = this.mTrackHeight;
        }
        double lastY = mGpsPointList.get(0).getLatitude();
        double lastX = mGpsPointList.get(0).getLongitude();
        Path path = new Path();
        float xRate, yRate;
        for (int i = 0; i < mGpsPointList.size(); i = i + step) {
            GPSPoint gpsPoint = mGpsPointList.get(i);
            xRate = (float) (gpsPoint.getLongitude() - mMinLng) * 100000 / mTrackLongEdge;
            yRate = (float) (mMaxLat - gpsPoint.getLatitude()) * 10000 * 8 / mTrackLongEdge;
            float xCoord, yCoord;
            if (mTrackLongEdge == mTrackWidth) {
                xCoord = (float) (xRate + 0.05) * fixedBitmapWidth;
                yCoord = (float) (yRate + 0.05) * fixedBitmapWidth + (fixedBitmapWidth - (mTrackHeight / mTrackLongEdge) * fixedBitmapWidth) / 2;
            } else {
                xCoord = (float) (xRate + 0.05) * fixedBitmapWidth + (fixedBitmapWidth - (mTrackWidth / mTrackLongEdge) * fixedBitmapWidth) / 2;
                yCoord = (float) (yRate + 0.05) * fixedBitmapWidth;
            }
            if (i > 0) {
                //path.moveTo(xCoord, yCoord);
                cv.drawLine((float) lastX, (float) lastY, xCoord, yCoord, paint);
            }
            lastY = yCoord;
            lastX = xCoord;
        }
        return newBitmap;
    }

    //获得跑步数据Bitmap
    public Bitmap getBm2() {
        Bitmap newBitmap = Bitmap.createBitmap(mScreenWidth, dp(100), Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newBitmap);
        Paint paint = new Paint();
        paint.setTypeface(Typeface.create("宋体", Typeface.BOLD));
        paint.setTextSize(dp(15));
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);//滤波处理
        paint.setColor(Color.WHITE);
        String text1 = mActivity.getString(R.string.water_mark_km);
        String text2 = mActivity.getString(R.string.water_mark_time);
        String text3 = mActivity.getString(R.string.water_mark_pace);
        String text4 = mActivity.getString(R.string.water_mark_cal);
        float textLength1 = paint.measureText(text1);
        float textLength2 = paint.measureText(text2);
        float textLength3 = paint.measureText(text3);
        float textLength4 = paint.measureText(text4);
        cv.drawText(text1, mScreenWidth / 8 - textLength1 / 2, dp(50), paint);
        cv.drawText(text2, mScreenWidth * (3 / (float) 8) - textLength2 / 2, dp(50), paint);
        cv.drawText(text3, mScreenWidth * (5 / (float) 8) - textLength3 / 2, dp(50), paint);
        cv.drawText(text4, mScreenWidth * (7 / (float) 8) - textLength4 / 2, dp(50), paint);
        paint.setTextSize(dp(20));
        textLength1 = paint.measureText(mLengthStr);
        textLength2 = paint.measureText(mTimeStr);
        textLength3 = paint.measureText(mAvgPace);
        textLength4 = paint.measureText(mCal);
        cv.drawText(mLengthStr, mScreenWidth / 8 - textLength1 / 2, dp(70), paint);
        cv.drawText(mTimeStr, mScreenWidth * (3 / (float) 8) - textLength2 / 2, dp(70), paint);
        cv.drawText(mAvgPace, mScreenWidth * (5 / (float) 8) - textLength3 / 2, dp(70), paint);
        cv.drawText(mCal, mScreenWidth * (7 / (float) 8) - textLength4 / 2, dp(70), paint);
        return newBitmap;
    }

    public Bitmap getBm3() {
        Bitmap newBitmap = Bitmap.createBitmap(mScreenWidth, dp(100), Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newBitmap);
        Paint paint = new Paint();
        paint.setTypeface(Typeface.create("宋体", Typeface.BOLD));
        paint.setTextSize(dp(30));
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);//滤波处理
        paint.setColor(Color.WHITE);
        Bitmap leftBitmap = BitmapFactory.decodeResource(mActivity.getResources(), R.mipmap.water_mark_2);
        cv.drawBitmap(leftBitmap, 0, 0, paint);
        cv.drawText(mLengthStr + "KM", leftBitmap.getWidth() + dp(20), leftBitmap.getHeight() * ((float) 3 / 4), paint);
        leftBitmap.recycle();
        return newBitmap;
    }

    private int dp(int dpValue) {
        return TamasUtils.dip2px(mActivity, dpValue);
    }


}
