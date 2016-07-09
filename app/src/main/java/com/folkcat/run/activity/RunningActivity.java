package com.folkcat.run.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.folkcat.run.db.util.GPSPointUtil;
import com.folkcat.run.util.GlobalVar;
import com.folkcat.run.db.util.RunningRecordUtil;
import com.folkcat.run.mode.DynamicWaterMark;

/**
 * Created by Tamas on 2016/7/6.
 */
public class RunningActivity extends Activity implements LocationSource,
        AMapLocationListener{
    private MapView mMapView;
    private static final String TAG="RunningActivity";

    private AMap aMap;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private AMapLocation mLastLocation=null;

    private float mDistance=0f;
    private int mSpeed=0;
    private long mRunningId=0;
    private int mTimeMinute=0;
    private int mTimeSecond=0;


    private TextView mTvTime;
    private TextView mTvDistance;
    private TextView mTvSpeed;

    private ImageView mIvTakePhoto;

    private Handler mHandler;



    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);
        initView();

        initMap();
        doSomeOtherThing();
        mMapView.onCreate(savedInstanceState);
    }
    private void initView(){
        mHandler=new Handler();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        mMapView = (MapView) findViewById(R.id.map);
        mTvDistance=(TextView)findViewById(R.id.tv_distance);
        mTvSpeed=(TextView)findViewById(R.id.tv_speed);
        mTvTime=(TextView)findViewById(R.id.tv_time);
        mIvTakePhoto=(ImageView)findViewById(R.id.iv_take_photo);

        mIvTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DynamicWaterMark dynamicWaterMark = new DynamicWaterMark(RunningActivity.this, "厦门", "2016-7-9", "20", "20KJ", "60'20''", "3KM", GPSPointUtil.getGointsByRunning(mRunningId));
                GlobalVar.dynamicWaterMark = dynamicWaterMark;
                Intent toCameraActivity=new Intent(RunningActivity.this,CameraActivity.class);
                startActivity(toCameraActivity);
            }
        });
    }
    private void doSomeOtherThing(){
        mRunningId=System.currentTimeMillis();
        RunningRecordUtil.initRecordToDb(mRunningId);
        new Thread(new TimmingRunnable()).start();

    }


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
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                if(mLastLocation==null)
                    mLastLocation=amapLocation;
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                PolylineOptions polylineOptions=new PolylineOptions();
                polylineOptions.add(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()),new LatLng(amapLocation.getLatitude(),amapLocation.getLongitude()));
                mLastLocation=amapLocation;
                aMap.addPolyline(polylineOptions);
                CameraUpdate update = CameraUpdateFactory.changeLatLng(new LatLng(amapLocation.getLatitude(),amapLocation.getLongitude()));
                //aMap.moveCamera(update);
                aMap.animateCamera(update);
                GPSPointUtil.commitPointToDb(mRunningId, amapLocation.getLatitude(), amapLocation.getLongitude());

                Log.i(TAG, "提交一条GPSPoint记录到数据库 Lat:"+amapLocation.getLatitude()+" Lng:"+amapLocation.getLongitude());

            } else {
                String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }
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
            mLocationOption.setGpsFirst(true);
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
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }
}
