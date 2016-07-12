package com.folkcat.run.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.folkcat.run.R;
import com.folkcat.run.adapter.MapThumbGruidRvAdapter;

/**
 * Created by Tamas on 2016/7/11.
 */
public class HomeActivity extends AppCompatActivity implements MapThumbGruidRvAdapter.MyItemClickListener {
    private static final String TAG="HomeActivity";

    private RecyclerView mRvMapThumbGrid;
    private MapThumbGruidRvAdapter mMapThumbAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_running_grid);
        mRvMapThumbGrid=(RecyclerView)findViewById(R.id.rv_map_thumbnail);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),3);
        mRvMapThumbGrid.setLayoutManager(layoutManager);
        mMapThumbAdapter=new MapThumbGruidRvAdapter();
        mRvMapThumbGrid.setAdapter(mMapThumbAdapter);
        mMapThumbAdapter.setOnItemClickListener(this);


    }

    @Override
    public void onItemClick(View view, int position){
        Log.i(TAG,"onItemClick called");
        long runningId=mMapThumbAdapter.getRunningRecordList().get(position).getRunningId();
        Intent toRunningResultActivity=new Intent(HomeActivity.this,RunningResultActivity.class);
        toRunningResultActivity.putExtra("runningId",runningId);
        startActivity(toRunningResultActivity);
    }
}
