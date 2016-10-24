package com.folkcat.run.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.model.PolylineOptions;
import com.folkcat.run.R;
import com.folkcat.run.adapter.BottomPhotoRvAdapter;
import com.folkcat.run.db.mode.Photo;
import com.folkcat.run.db.util.GPSPointUtil;
import com.folkcat.run.db.util.RunningRecordUtil;
import com.folkcat.run.service.MyService;
import com.folkcat.run.util.GlobalVar;
import com.folkcat.run.mode.DynamicWaterMark;
import com.folkcat.run.util.TamasUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Tamas on 2016/7/6.
 */
public class RunningActivity extends Activity implements LocationSource,
        AMapLocationListener,AMap.OnMapScreenShotListener,BottomPhotoRvAdapter.MyItemClickListener  {
    private MapView mMapView;
    private static final String TAG="RunningActivity";

    private AMap aMap;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private AMapLocation mLastLocation=null;
    private AMapLocation mLastValidLocation=null;
    private boolean isLastValidLocationInit=false;

    private float mDistance=0f;
    private int mSpeed=0;
    private long mRunningId=0;
    private int mTimeMinute=0;
    private int mTimeSecond=0;


    private TextView mTvTime;
    private TextView mTvDistance;
    private TextView mTvSpeed;
    private TextView mTvOver;

    private RecyclerView mRvBottomThumbnail;

    private ImageView mIvTakePhoto;

    private Handler mHandler;
    private List<Photo> mPhotoList;

    private MyService mService;

    private String mDateStr="";

    private int mNumOfGPSPoint=0;


    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected");
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
            //返回一个MsgService对象
            mService=((MyService.MsgBinder)serviceBinder).getService();
            Log.i(TAG, "onServiceConnected");
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);
        mHandler=new Handler();
        initView();

        initMap();
        doSomeOtherThing();
        mMapView.onCreate(savedInstanceState);
    }
    private void initView(){
        /*
        mHandler=new Handler();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        */
        mMapView = (MapView) findViewById(R.id.map);
        mTvDistance=(TextView)findViewById(R.id.tv_distance);
        mTvSpeed=(TextView)findViewById(R.id.tv_speed);
        mTvTime=(TextView)findViewById(R.id.tv_time);
        mIvTakePhoto=(ImageView)findViewById(R.id.iv_take_photo);
        mRvBottomThumbnail=(RecyclerView)findViewById(R.id.rv_bottom_thumbnail);
        mTvOver=(TextView)findViewById(R.id.tv_over);

        mIvTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float kmDis=mDistance/1000f;
                BigDecimal b=new BigDecimal(kmDis);
                kmDis=b.setScale(2,   BigDecimal.ROUND_HALF_UP).floatValue();
                String kmDisStr=kmDis+"";
                DynamicWaterMark dynamicWaterMark = new DynamicWaterMark(RunningActivity.this, "厦门", mDateStr, "20", ((int)(mDistance*50))+"KJ", mTimeMinute+"'"+mTimeSecond+"''", kmDisStr, GPSPointUtil.getGointsByRunning(mRunningId));
                GlobalVar.dynamicWaterMark = dynamicWaterMark;
                Intent toCameraActivity = new Intent(RunningActivity.this, CameraActivity.class);
                toCameraActivity.putExtra("runningId", mRunningId);
                startActivity(toCameraActivity);
            }
        });
        mTvOver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvOver.setOnClickListener(null);
                aMap.getMapScreenShot(RunningActivity.this);
                aMap.invalidate();//刷新地图
            }
        });

    }
    private void doSomeOtherThing(){
        mRunningId=System.currentTimeMillis();
        //RunningRecordUtil.initRecordToDb(mRunningId);
        new Thread(new TimmingRunnable()).start();
        //mRunningId=1468156352458l;
        BottomPhotoRvAdapter rvAdapter=new BottomPhotoRvAdapter(mRunningId);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false);
        mRvBottomThumbnail.setLayoutManager(layoutManager);
        mRvBottomThumbnail.setAdapter(rvAdapter);
        rvAdapter.setOnItemClickListener(this);

        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        mDateStr=sdf.format(new Date());

        Intent intent = new Intent(RunningActivity.this,MyService.class);
        this.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);



    }

    public static double caculateDis(double lat1, double lng1, double lat2, double lng2) {
        double distance=Math.sqrt(Math.pow(lat2 - lat1, 2) + Math.pow(lng2 - lng1, 2));
        return Math.abs(distance)*99847d;
    }

    @Override
    public void onMapScreenShot(Bitmap bitmap) {
        String mapThumbnailDirPath=TamasUtils.getMapThumbnailPath(getApplicationContext()).toString();
        File mapThumbnailFile=new File(mapThumbnailDirPath+ File.separator+"map_"+mRunningId);
        int cal=(int)(mDistance*50);
        if(mNumOfGPSPoint<1){
            GPSPointUtil.commitPointToDb(mRunningId,mLastValidLocation.getLatitude(),mLastValidLocation.getLongitude());
        }

        Bitmap thumbnailBitmap=TamasUtils.getSquareScaledBitmap(bitmap, (int) (dp(180)));
        TamasUtils.saveBitmap(getApplicationContext(), thumbnailBitmap, mapThumbnailFile);
        RunningRecordUtil.commitRecordToDb(mRunningId,mRunningId,System.currentTimeMillis(),cal,mTimeMinute*60+mTimeSecond,(int)mDistance,mapThumbnailFile.getPath());
        Log.i(TAG,"保存跑步记录成功");
        finish();
    }
    private int dp(int dpValue){
        return (int) TamasUtils.dip2px(this, dpValue);
    }


    /*
    初始化地图
     */
    private void initMap() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            // 自定义系统定位小蓝点
            MyLocationStyle myLocationStyle = new MyLocationStyle();
            myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                    .fromResource(R.mipmap.location_marker));// 设置小蓝点的图标
            myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
            myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
            myLocationStyle.strokeWidth(0.0f);// 设置圆形的边框粗细

            aMap.setMyLocationStyle(myLocationStyle);
            aMap.setLocationSource(this);// 设置定位监听
            aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
            aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
            float maxZoom=aMap.getMaxZoomLevel();//最大缩放级别
            float currentZoom=aMap.getCameraPosition().zoom;//当前缩放级别
            CameraUpdate update=CameraUpdateFactory.zoomBy(maxZoom-currentZoom-1);
            aMap.moveCamera(update);

        }
    }

    /*
    监听地理位置变化
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                if(mLastLocation==null){
                    mLastLocation=amapLocation;
                    mLastValidLocation=mLastLocation;
                }
                float stepDis=(float)caculateDis(mLastLocation.getLatitude(),mLastLocation.getLongitude(),amapLocation.getLatitude(),amapLocation.getLongitude());
                if(stepDis>0.05&stepDis<100){
                    mDistance=mDistance+stepDis;
                    PolylineOptions polylineOptions=new PolylineOptions();
                    polylineOptions.add(new LatLng(mLastValidLocation.getLatitude(), mLastValidLocation.getLongitude()), new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude()));
                    aMap.addPolyline(polylineOptions);
                    GPSPointUtil.commitPointToDb(mRunningId, amapLocation.getLatitude(), amapLocation.getLongitude());
                    Log.i(TAG, "提交一条GPSPoint记录到数据库 Lat:" + amapLocation.getLatitude() + " Lng:" + amapLocation.getLongitude());
                    mNumOfGPSPoint++;
                    mLastValidLocation=amapLocation;
                    isLastValidLocationInit=true;

                }
                CameraUpdate update = CameraUpdateFactory.changeLatLng(new LatLng(amapLocation.getLatitude(),amapLocation.getLongitude()));
                aMap.animateCamera(update);
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                mLastLocation=amapLocation;
                if(!isLastValidLocationInit){
                    mLastValidLocation=amapLocation;
                }
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }

    /*
    计时线程
     */
    private class TimmingRunnable implements Runnable{

        @Override
        public void run(){
            while(true){
                try {
                    Thread.sleep(500);
                    mTimeSecond++;
                    if(mTimeSecond>59){
                        mTimeMinute++;
                        mTimeSecond=0;
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mTvTime.setText(mTimeMinute+":"+mTimeSecond);
                            float kmDis=mDistance/1000;
                            BigDecimal b=new BigDecimal(kmDis);
                            kmDis=b.setScale(2,   BigDecimal.ROUND_HALF_UP).floatValue();
                            mTvDistance.setText((kmDis)+"KM");
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy Called");
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //mLocationOption.setGpsFirst(true);
            mLocationOption.setInterval(3000);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);

            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.unRegisterLocationListener(this);
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.i(TAG, "onItemClick called");
        Intent toPhotoViewActivity=new Intent(RunningActivity.this,PhotoViewPagerActivity.class);
        toPhotoViewActivity.putExtra("position", position);
        toPhotoViewActivity.putExtra("runningId",mRunningId);
        startActivity(toPhotoViewActivity);
    }
}
