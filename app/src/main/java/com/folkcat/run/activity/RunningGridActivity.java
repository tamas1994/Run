package com.folkcat.run.activity;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.folkcat.run.R;
import com.folkcat.run.adapter.MapThumbGruidRvAdapter;

public class RunningGridActivity extends AppCompatActivity  implements MapThumbGruidRvAdapter.MyItemClickListener {
    private static final String TAG="RunningGridActivity";

    private RecyclerView mRvMapThumbGrid;
    private MapThumbGruidRvAdapter mMapThumbAdapter;
    private TextView mTvWarning;



    private void initView(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        mTvWarning=(TextView)findViewById(R.id.tv_warning);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            private String snackbarMessage = "请开启GPS";

            @Override
            public void onClick(View view) {

                LocationManager locationManager
                        = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
                boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
                boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (gps) {
                    mTvWarning.setVisibility(View.GONE);
                    Intent toRunningActivity=new Intent(RunningGridActivity.this,RunningActivity.class);
                    startActivity(toRunningActivity);
                }else{
                    Snackbar.make(view, snackbarMessage, Snackbar.LENGTH_LONG).setAction("去设置", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent toOpenGPS = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(toOpenGPS, 0);
                        }
                    }).show();
                }


            }
        });

        mRvMapThumbGrid=(RecyclerView)findViewById(R.id.rv_map_thumbnail);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),3);
        mRvMapThumbGrid.setLayoutManager(layoutManager);
        mMapThumbAdapter=new MapThumbGruidRvAdapter();
        mRvMapThumbGrid.setAdapter(mMapThumbAdapter);
        mMapThumbAdapter.setOnItemClickListener(this);

        if(mMapThumbAdapter.getRunningRecordList().size()>0){
            mTvWarning.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_grid);



        initView();

        //绑定Service
//        Intent intent = new Intent(HomeActivity.this,HealthService.class);
//        this.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onItemClick(View view, int position) {
        Log.i(TAG, "onItemClick called");
        long runningId=mMapThumbAdapter.getRunningRecordList().get(position).getRunningId();
        Intent toRunningResultActivity=new Intent(RunningGridActivity.this,RunningResultActivity.class);
        toRunningResultActivity.putExtra("runningId", runningId);
        startActivity(toRunningResultActivity);

    }
}
