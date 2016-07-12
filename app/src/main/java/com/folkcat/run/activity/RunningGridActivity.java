package com.folkcat.run.activity;

import android.content.Intent;
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

import com.folkcat.run.R;
import com.folkcat.run.adapter.MapThumbGruidRvAdapter;

public class RunningGridActivity extends AppCompatActivity  implements MapThumbGruidRvAdapter.MyItemClickListener {
    private static final String TAG="RunningGridActivity";

    private RecyclerView mRvMapThumbGrid;
    private MapThumbGruidRvAdapter mMapThumbAdapter;



    private void initView(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            private String snackbarMessage = "";

            @Override
            public void onClick(View view) {
                boolean isToShowBtDeviceList = true;

                Snackbar.make(view, snackbarMessage, Snackbar.LENGTH_LONG).setAction("Action", null).show();

            }
        });

        mRvMapThumbGrid=(RecyclerView)findViewById(R.id.rv_map_thumbnail);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),3);
        mRvMapThumbGrid.setLayoutManager(layoutManager);
        mMapThumbAdapter=new MapThumbGruidRvAdapter();
        mRvMapThumbGrid.setAdapter(mMapThumbAdapter);
        mMapThumbAdapter.setOnItemClickListener(this);
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
        toRunningResultActivity.putExtra("runningId",runningId);
        startActivity(toRunningResultActivity);
    }
}
