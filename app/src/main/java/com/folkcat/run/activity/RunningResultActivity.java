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
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.folkcat.run.db.mode.GPSPoint;
import com.folkcat.run.db.mode.Photo;
import com.folkcat.run.db.mode.RunningRecord;
import com.folkcat.run.db.util.GPSPointUtil;
import com.folkcat.run.db.util.RunningRecordUtil;
import com.folkcat.run.mode.DynamicWaterMark;
import com.folkcat.run.service.MyService;
import com.folkcat.run.util.GlobalVar;
import com.folkcat.run.util.TamasUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Tamas on 2016/7/6.
 */
public class RunningResultActivity extends AppCompatActivity implements BottomPhotoRvAdapter.MyItemClickListener {
    private MapView mMapView;
    private static final String TAG="RunningResultActivity";

    private AMap aMap;



    private long mRunningId=0;


    private RecyclerView mRvBottomThumbnail;

    private ImageView mIvTakePhoto;

    private Handler mHandler;
    private List<Photo> mPhotoList;

    private MyService mService;

    private List<GPSPoint> mGPSPointList;


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
        setContentView(R.layout.activity_running_result);

        if(savedInstanceState==null){
            mRunningId=getIntent().getLongExtra("runningId",0);
        }else{
            mRunningId=savedInstanceState.getLong("runningId", 0);
        }
        mGPSPointList=GPSPointUtil.getGointsByRunning(mRunningId);
        initView();
        initMap();
        doSomeOtherThing();
        mMapView.onCreate(savedInstanceState);
    }
    private void initView(){
        SimpleDateFormat sdf=new SimpleDateFormat("MM-dd HH:mm");
        RunningRecord runningRecord=RunningRecordUtil.getRecordById(mRunningId);
        String titleStr=sdf.format(new Date(runningRecord.getCreateDate()));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        toolbar.setTitle(titleStr);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mHandler=new Handler();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        mMapView = (MapView) findViewById(R.id.map);

        mIvTakePhoto=(ImageView)findViewById(R.id.iv_take_photo);
        mRvBottomThumbnail=(RecyclerView)findViewById(R.id.rv_bottom_thumbnail);


        mIvTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DynamicWaterMark dynamicWaterMark = new DynamicWaterMark(RunningResultActivity.this, "厦门", "2016-7-9", "20", "20KJ", "60'20''", "3KM", GPSPointUtil.getGointsByRunning(mRunningId));
                GlobalVar.dynamicWaterMark = dynamicWaterMark;
                Intent toCameraActivity = new Intent(RunningResultActivity.this, CameraActivity.class);
                toCameraActivity.putExtra("runningId", mRunningId);
                startActivity(toCameraActivity);
            }
        });


    }
    private void doSomeOtherThing(){
        BottomPhotoRvAdapter rvAdapter=new BottomPhotoRvAdapter(mRunningId);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false);
        mRvBottomThumbnail.setLayoutManager(layoutManager);
        mRvBottomThumbnail.setAdapter(rvAdapter);
        rvAdapter.setOnItemClickListener(this);

        Intent intent = new Intent(RunningResultActivity.this,MyService.class);
        this.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

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
            aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
            aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
            float maxZoom=aMap.getMaxZoomLevel();//最大缩放级别
            float currentZoom=aMap.getCameraPosition().zoom;//当前缩放级别

            //寻地图中心点坐标
            double lat=116.403622,lng=39.919694;//天安门坐标
            double maxLat=lat;
            double minLat=lat;
            double maxLng=lng;
            double minLng=lng;
            double lastLat=lat;
            double lastLng=lng;
            if(mGPSPointList.size()>0){
                PolylineOptions polylineOptions=new PolylineOptions();
                lat=mGPSPointList.get(0).getLatitude();
                lng=mGPSPointList.get(0).getLongitude();
                lastLat=lat;
                lastLng=lng;
                maxLat=lat;
                maxLng=lng;
                minLat=lat;
                minLng=lng;

                int size=mGPSPointList.size();
                for(int i=0;i<size;i++){
                    lat=mGPSPointList.get(i).getLatitude();
                    lng=mGPSPointList.get(i).getLongitude();

                    polylineOptions.add(new LatLng(lastLat, lastLng), new LatLng(lat, lng));

                    if(lat>maxLat)
                        maxLat=lat;
                    if(lng>maxLng)
                        maxLng=lng;
                    if(lat<minLat)
                        minLat=lat;
                    if(lng<minLng)
                        minLng=lng;
                    lastLat=lat;
                    lastLng=lng;
                }
                aMap.addPolyline(polylineOptions);

            }
            lat=(maxLat+minLat)/2;
            lng=(maxLng+minLng)/2;
            CameraUpdate update=CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),(maxZoom-3));
            aMap.moveCamera(update);
        }
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
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
        outState.putLong("runningId", mRunningId);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.i(TAG, "onItemClick called");
        Intent toRunningResultActivity=new Intent(RunningResultActivity.this,PhotoViewPagerActivity.class);
        toRunningResultActivity.putExtra("position",position);
        toRunningResultActivity.putExtra("runningId",mRunningId);
        startActivity(toRunningResultActivity);
    }
}
