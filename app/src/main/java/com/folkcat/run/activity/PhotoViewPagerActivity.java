/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.folkcat.run.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;


import com.folkcat.run.R;
import com.folkcat.run.db.mode.Photo;
import com.folkcat.run.db.util.PhotoUtil;

import java.util.List;

import uk.co.senab.photoview.HackyViewPager;
import uk.co.senab.photoview.PhotoView;

/**
 * Created by Tamas on 2015/8/3.
 *
 * 显示一个PhotoList的ViewPagerActivity
 * 默认为逆序显示，如果 getIntent.getBooleanExtra(“is_order”)指定为true，则顺序显示
 * 当前项由 getIntent.getIntExtra("index")指定
 */

public class PhotoViewPagerActivity extends ActionBarActivity {
    private static final String TAG="PhotoViewPagerActivity";

    private static final String ISLOCKED_ARG = "isLocked";
    private int mCurrentPhotoIndex;
    private ActionBar mActionBar;

    private ViewPager mViewPager;
    private int mPosition;

    private static List<Photo> mPhotos;
    private static int mPhotosSize;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_pager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        //setContentView(mViewPager);
        Intent i=getIntent();

        mCurrentPhotoIndex=i.getIntExtra("position",0);
        long runningId=i.getLongExtra("runningId",0);
        mPhotos= PhotoUtil.getPthotosByRunningIdAndUpdateUi(runningId);

        mViewPager.setAdapter(new SamplePagerAdapter(this));
        if (savedInstanceState != null) {
            boolean isLocked = savedInstanceState.getBoolean(ISLOCKED_ARG, false);
            ((HackyViewPager) mViewPager).setLocked(isLocked);
        }
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mPosition = position;

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mViewPager.setCurrentItem(mCurrentPhotoIndex);

    }
    private class SamplePagerAdapter extends PagerAdapter {
        private Activity mActivity;
        public SamplePagerAdapter(Activity activity){
            this.mActivity=activity;
        }
        @Override
        public int getCount() {
            return mPhotos.size();
        }
        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());
//            if(!isOrder){
//                position=mPhotos.size()-1-position;
//                mPosition=position;
//            }else{
//                mPosition=position;
//            }
            Bitmap photoBitmap= BitmapFactory.decodeFile(mPhotos.get(position).getPhotoPath());
            //Bitmap scaledBitmap= TamasUtils.getScaledBitmapByShotEdge(photoBitmap, TamasUtils.getScreenWidth(mActivity));
            //photoBitmap.recycle();
            photoView.setImageBitmap(photoBitmap);
            // Now just add PhotoView to ViewPager and return it
            container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            return photoView;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }
    private boolean isViewPagerActive() {
        return (mViewPager != null && mViewPager instanceof HackyViewPager);
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (isViewPagerActive()) {
            outState.putBoolean(ISLOCKED_ARG, ((HackyViewPager) mViewPager).isLocked());
        }
        super.onSaveInstanceState(outState);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case (android.R.id.home):
                finish();
                break;
            case (R.id.share):
                /*
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mPhotos.get(mPosition).getFile()));
                shareIntent.setType("image/jpeg");
                startActivity(Intent.createChooser(shareIntent,getString(R.string.photo_view_activity_share)));
                break;
                */
            default:
                break;
        }
        return true;
    }
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_photo_view_pager, menu);
        return true;
    }

}
