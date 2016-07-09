package com.folkcat.run.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ViewFlipper;

import com.folkcat.run.R;

/**
 * Created by Tamas on 2016/7/8.
 */
public class TestFlipViewActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewflip_test);

        ViewFlipper flipper;
        flipper = (ViewFlipper) findViewById(R.id.flipper);
        flipper.startFlipping();
    }
}
