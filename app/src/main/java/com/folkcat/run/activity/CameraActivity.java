package com.folkcat.run.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;



import com.folkcat.run.R;
import com.folkcat.run.adapter.DynamicWaterMarkPagerAdapter;
import com.folkcat.run.db.util.PhotoUtil;
import com.folkcat.run.service.MyService;
import com.folkcat.run.util.GlobalVar;
import com.folkcat.run.util.SPUtil;
import com.folkcat.run.mode.DynamicWaterMark;
import com.folkcat.run.util.TamasUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tamas on 2015/8/5.
 */
public class CameraActivity extends Activity {
    private static final String TAG="CameraActivity";
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private ImageView mIvTakePhoto;
    private ImageView mIvCloseCamera;
    private ImageView mFlipCameraImage;
    private LinearLayout mWaterCameraContainerLayout;
    private FrameLayout mWaterCameraLayout;
    private List<Bitmap> mBmList=new ArrayList<Bitmap>();

    private ViewPager mWaterMarkViewPager;
    private ImageView[] mTips;
    private int mScreenHeight;
    private int mScreenWidth;
    private int mPhotoWidth;
    private DynamicWaterMarkPagerAdapter mWaterMarkPagerAdapter;
    private long mRunningId=0;
    private Handler mHandler;

    private SPUtil mSPUtil;

    private MyService mService;

    private DynamicWaterMark mDynamicWaterMark;

    Bitmap mWaterMark1;
    Bitmap mWaterMark2;
    Bitmap mWaterMark3;
    Bitmap mWaterMark4;
    Bitmap mWaterMarkno;
    Bitmap mSelectedBm;//指向选择的水印

    private boolean isDynamic;//是否动态水印标记

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



    private Camera.Size mPreviewSize,mPhotoSize;
    SurfaceHolder mHolder;
    Camera.Parameters mParameters;
    Camera.Parameters mFrontParameters;

    private int mCameraPosition = 1;// 0 front,1 back

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        //PhotoUtil.commitPhotoToDb(mRunningId, "/storage/emulated/0/Android/data/com.folkcat.run/cache/thumbnail/1468161075055", "/storage/emulated/0/Android/data/com.folkcat.run/cache/photos/1468161075055");
        mSPUtil=SPUtil.getInstance(getApplicationContext());
        Intent intent=getIntent();
        isDynamic=intent.getBooleanExtra("is_dynamic",true);
        mRunningId=intent.getLongExtra("runningId",0);
        initView();
        initValue();
        setListener();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i(TAG,"onDestroy");
        for(int i=0;i<mBmList.size();i++){
            mBmList.get(i).recycle();
        }
    }

    private void initView(){
        mHandler=new Handler();
        mScreenHeight= TamasUtils.getScreenHeight(this);
        mScreenWidth=TamasUtils.getScreenWidth(this);
        if(mScreenWidth<640)
            mPhotoWidth=640;
        else
            mPhotoWidth=mScreenWidth;

        mWaterCameraContainerLayout=(LinearLayout)findViewById(R.id.water_mark_camera_container);
        mWaterCameraLayout=new FrameLayout(getApplicationContext());
        String inflater= Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li;
        li=(LayoutInflater)getApplicationContext().getSystemService(inflater);
        li.inflate(R.layout.frame_layout_water_camera, mWaterCameraLayout, true);
        mWaterCameraContainerLayout.addView(mWaterCameraLayout);
        mSurfaceView=(SurfaceView)mWaterCameraLayout.findViewById(R.id.camera_surfaceview);

        ViewGroup group = (ViewGroup)mWaterCameraLayout.findViewById(R.id.tips_view_group);

        mWaterMarkViewPager=(ViewPager)mWaterCameraLayout.findViewById(R.id.water_mark_viewPager);

        mIvTakePhoto= (ImageView) findViewById(R.id.camera_take_photo);
        mIvCloseCamera = (ImageView) findViewById(R.id.camera_close);
        mFlipCameraImage=(ImageView)findViewById(R.id.camera_flip);

        this.mWaterMarkno=BitmapFactory.decodeResource(getResources(),R.mipmap.water_mark_no);
        mBmList.add(mWaterMarkno);
        if(isDynamic){
            this.mDynamicWaterMark= GlobalVar.dynamicWaterMark;
            this.mWaterMark1=mDynamicWaterMark.getBm1();
            this.mWaterMark2= mDynamicWaterMark.getBm2();
            this.mWaterMark3= mDynamicWaterMark.getBm3();
            this.mWaterMark4 = BitmapFactory.decodeResource(getResources(), R.mipmap.water_mark_1);
            mBmList.add(mWaterMark1);
            mBmList.add(mWaterMark2);
            mBmList.add(mWaterMark3);
            mBmList.add(mWaterMark4);
        }else{
            this.mWaterMark1=BitmapFactory.decodeResource(getResources(),R.mipmap.water_mark_1);
            this.mWaterMark2=BitmapFactory.decodeResource(getResources(),R.mipmap.water_mark_2);
            this.mWaterMark3=BitmapFactory.decodeResource(getResources(),R.mipmap.water_mark_3);
            mBmList.add(mWaterMark1);
            mBmList.add(mWaterMark2);
            mBmList.add(mWaterMark3);
        }

        mSelectedBm=mBmList.get(0);
        mTips = new ImageView[mBmList.size()];
        //将点点加入到ViewGroup中
        for (int i = 0; i < mTips.length; i++) {
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setLayoutParams(new RadioGroup.LayoutParams(10, 10));
            mTips[i] = imageView;
            if (i == 0) {
                mTips[i].setImageResource(R.mipmap.ic_point_white);
            } else {
                mTips[i].setImageResource(R.mipmap.ic_point_dark_gray);
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            layoutParams.leftMargin = 12;
            layoutParams.rightMargin = 12;
            group.addView(imageView, layoutParams);
        }
    }
    private void initValue(){
        this.mDynamicWaterMark=GlobalVar.dynamicWaterMark;
        mHolder=mSurfaceView.getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    private void setListener(){
        mIvCloseCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mFlipCameraImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int cameraCount = 0;
                cameraCount = Camera.getNumberOfCameras();
                //Log.i(TAG,"摄像头个数："+cameraCount);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = Camera.open(mCameraPosition);
                try {
                    mCamera.setPreviewDisplay(mHolder);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (mCameraPosition == 0) {
                    mCamera.setParameters(mParameters);
                }else{
                    setFrontParameters();
                    mCamera.setParameters(mFrontParameters);
                }
                mCamera.setDisplayOrientation(90);
                mCamera.startPreview();
                if (mCameraPosition == 1) {
                    mCameraPosition = 0;
                } else {
                    mCameraPosition = 1;
                }
                //Log.i(TAG, "mCameraPosition: " + mCameraPosition);
            }
        });



        mIvTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCamera != null) {
                    mCamera.takePicture(mShutterCallback, null, mJpegCallBack);
                    mIvTakePhoto.setOnClickListener(null);//取消监听拍照按钮点击事件，防止在响应延迟的情况下多次点击造成的崩溃
                }
            }
        });
        mHolder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder holder) {
                // tell the camera to use this surface as its preview area
                try {
                    if (mCamera != null) {
                        mCamera.setPreviewDisplay(holder);
                    }
                } catch (IOException exception) {
                    //Log.e(TAG, "Error setting up preview display", exception);
                }
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                // we can no longer display on this surface, so stop the preview.
                if (mCamera != null) {
                    mCamera.stopPreview();
                }
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
                //Log.i(TAG, "Surface Changed");
                //Log.i(TAG, "W:" + w + " H:" + h);
                if (mCamera == null)
                    return;
                // the surface has changed size; update the camera preview size
                mParameters = mCamera.getParameters();
                mPreviewSize = getBestPreviewSize(mParameters.getSupportedPreviewSizes());
                //Log.i(TAG, "找到最适合的预览大小:" + s.width + "  Height:" + s.height);
                //================
                float rate=mScreenWidth/(float)mPreviewSize.height;
                Log.i(TAG,"rate:"+rate);
                int viewWidth=(int)(mPreviewSize.width*rate);
                int viewHeight=(int)(mPreviewSize.height*rate);
                LinearLayout.LayoutParams waterMarkContainerParams=new LinearLayout.LayoutParams(mScreenWidth,viewWidth);
                waterMarkContainerParams.setMargins(0, ( viewHeight- viewWidth) / 2, 0, (viewHeight- viewWidth ) / 2);
                mWaterCameraContainerLayout.setLayoutParams(waterMarkContainerParams);

                FrameLayout waterMark=(FrameLayout)mWaterCameraLayout.findViewById(R.id.water_mark);
                FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(mScreenWidth,mScreenWidth);
                params.setMargins(0, ( viewWidth -viewHeight) / 2, 0,( viewWidth -viewHeight) / 2);
                waterMark.setLayoutParams(params);


                mParameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                mPhotoSize = getBestPhotoSize(mParameters.getSupportedPictureSizes(),mPreviewSize);
                mParameters.setPictureSize(mPhotoSize.width, mPhotoSize.height);
                //Log.i(TAG, "找到的最适图片大小：" + s.width + "   " + s.height);
                mCamera.setParameters(mParameters);
                mCamera.setDisplayOrientation(90);
                try {
                    mCamera.startPreview();
                } catch (Exception e) {
                    //Log.e(TAG, "Could not start preview", e);
                    mCamera.release();
                    mCamera = null;
                }
            }
        });
        mWaterMarkPagerAdapter=new DynamicWaterMarkPagerAdapter(this, mBmList,mScreenWidth);
        //Log.i(TAG,"mPreviewHeight:"+mPreviewHeight);
        mWaterMarkViewPager.setAdapter(mWaterMarkPagerAdapter);
        mWaterMarkViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int originPosition = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                setImageForTips(position % mBmList.size());
                mSelectedBm = mBmList.get(position % mBmList.size());
                Log.i(TAG, "position!!!!!!!:" + position % mBmList.size());
                Log.i(TAG, "mSelectedBm  W:" + mSelectedBm.getWidth() + "  H:" + mSelectedBm.getHeight());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void setFrontParameters(){
        mFrontParameters = mCamera.getParameters();
        mPreviewSize=getBestPreviewSize(mFrontParameters.getSupportedPreviewSizes());
        //Log.i(TAG, "找到最适合的预览大小:" + s.width + "  Height:" + s.height);
        mFrontParameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        mPhotoSize = getBestPhotoSize(mFrontParameters.getSupportedPictureSizes(),mPreviewSize);
        mFrontParameters.setPictureSize(mPhotoSize.width, mPhotoSize.height);
    }
    /*
    获得最佳尺寸
     */
    private Camera.Size getBestPreviewSize(List<Camera.Size> sizes) {
        Camera.Size bestSize = sizes.get(0);
        float sub=Math.abs(mScreenWidth-bestSize.height);
        float temp;
        for(Camera.Size s:sizes){
            temp=Math.abs(mScreenWidth-s.height);
            if(temp<sub){
                bestSize=s;
                sub=temp;
            }
        }

        Log.w(TAG, " BestSize  Width:" + bestSize.width + "  Height:" + bestSize.height);
        return bestSize;
    }
    /*
    获得最佳尺寸
     */
    private Camera.Size getBestPhotoSize(List<Camera.Size> sizes,Camera.Size previewSize) {
        Camera.Size bestSize = sizes.get(0);
        float viewRatio=previewSize.height/(float)previewSize.width;
        float sub=Math.abs(viewRatio-bestSize.height/(float)bestSize.width);
        for (Camera.Size s : sizes) {
            //Log.w(TAG," SupportSize  Width:"+s.width+"  Height:"+s.height);
            float temp=Math.abs(viewRatio-s.height/(float)s.width);
            if(temp<sub){
                bestSize=s;
                sub=temp;
            }
        }
        Log.w(TAG," BestSize Photo  Width:"+bestSize.width+"  Height:"+bestSize.height);
        return bestSize;
    }
    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mCamera = Camera.open(0);
        } else {
            mCamera = Camera.open();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
    private Camera.PictureCallback mJpegCallBack = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            final Bitmap bm=mSelectedBm;
            Runnable handlerPhotoRunnable=new HandlerPhotoRunnable(data,camera,bm);
            new Thread(handlerPhotoRunnable).start();
            mSelectedBm=null;
            finish();
        }
    };
    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            //Do nothing
        }
    };
    /**
     * Set the image tip show
     */
    private void setImageForTips(int selectItems) {
        for (int i = 0; i < mTips.length; i++) {
            if (i == selectItems) {
                mTips[i].setImageResource(R.mipmap.ic_point_white);
            } else {
                mTips[i].setImageResource(R.mipmap.ic_point_dark_gray);
            }
        }
    }
    private int dp(int dpValue){
        return (int)TamasUtils.dip2px(this,dpValue);
    }

    private class HandlerPhotoRunnable implements Runnable{
        private byte[] data;
        private Camera camera;
        private  Bitmap selectedBm;
        public HandlerPhotoRunnable(byte[] data,Camera camera,Bitmap selectedBm){
            int shortEdge=selectedBm.getWidth();
            int otherEdge=selectedBm.getHeight();
            if(shortEdge>otherEdge)
                shortEdge=otherEdge;
            long startTime=System.currentTimeMillis();
            this.selectedBm=TamasUtils.getScaledBitmap(selectedBm,shortEdge);
            long endTime=System.currentTimeMillis();
            Log.i(TAG,"构造耗时:"+(endTime-startTime));
            this.data=data;
            this.camera=camera;
        }
        public void run(){
            Bitmap imageBitmap;
            final File imageFile;
            final File thumbnailFile;
            // create a filename
            //String filename = UUID.randomUUID().toString() + ".jpg";
            String filename = ""+System.currentTimeMillis();
            // save the jpeg data to disk
            FileOutputStream os = null;
            boolean success = true;
            String imageDir=TamasUtils.getTakedPicDirPath(getApplicationContext()).toString();
            String thumbnailDir=TamasUtils.getThumbnailPath(getApplicationContext()).toString();
            imageFile=new File(imageDir+File.separator + filename+".jpg");
            thumbnailFile=new File(thumbnailDir+File.separator+filename+".jpg");

            Matrix matrix=new Matrix();
            matrix.reset();
            int rotate=90;
            if(mCameraPosition==0){
                rotate = 270;
                matrix.setScale(1,-1);
            }

            matrix.setRotate(rotate);
            long startTime=System.currentTimeMillis();
            Bitmap rawBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            long endTime=System.currentTimeMillis();
            Log.i(TAG,"decode数据耗时:"+(endTime-startTime));
            Bitmap originBitmap;
            originBitmap= TamasUtils.getSquareScaledBitmap(rawBitmap, mPhotoWidth);
            rawBitmap.recycle();
            Bitmap rotaBitmap = Bitmap.createBitmap(originBitmap, 0, 0,originBitmap.getWidth(),originBitmap.getHeight() ,matrix,true);
            //Log.i(TAG,"rotaBitmap W:"+rotaBitmap.getWidth()+"  H:"+rotaBitmap.getHeight());
            originBitmap.recycle();

            if(mCameraPosition==0){//如果是前置摄像头，照片要旋转
                Matrix mtx=new Matrix();
                mtx.setScale(-1,1);
                Bitmap flipBm= Bitmap.createBitmap(rotaBitmap, 0, 0,originBitmap.getWidth(),originBitmap.getHeight() ,mtx,true);

                imageBitmap= TamasUtils.createBitmapForWatermark(flipBm, selectedBm,mWaterMarkPagerAdapter.getV_left(),mWaterMarkPagerAdapter.getV_top());
            }else{//后置摄像头无须旋转
                imageBitmap= TamasUtils.createBitmapForWatermark(rotaBitmap, selectedBm,mWaterMarkPagerAdapter.getV_left(),mWaterMarkPagerAdapter.getV_top());
            }
            rotaBitmap.recycle();
            TamasUtils.saveBitmap(getApplicationContext(), imageBitmap, imageFile);
            Bitmap tinyBitmap=TamasUtils.getSquareScaledBitmap(imageBitmap, (int) (dp(120)));
            TamasUtils.saveBitmap(getApplicationContext(), tinyBitmap, thumbnailFile);
            PhotoUtil.commitPhotoToDb(mRunningId, thumbnailFile.getPath(), imageFile.getPath());

            imageBitmap.recycle();
            long finalTime=System.currentTimeMillis();
            Log.i(TAG,"最终耗时:"+(finalTime-startTime));
            if (success) {
                // set the photo filename on the result intent
                if (success) {
                    Intent i = new Intent();
                    GlobalVar.bitmapFromCameraActivity=tinyBitmap;
                    i.putExtra("file_path",imageFile.getPath());
                    setResult(Activity.RESULT_OK, i);
                } else {
                    setResult(Activity.RESULT_CANCELED);
                }
            }

        }
    }
}