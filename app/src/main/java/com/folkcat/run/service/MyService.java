package com.folkcat.run.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {
    private static final String TAG="MyService";
    private Handler mHandler;



    @Override
    public void onCreate(){
        Log.i(TAG,"onCreate called");
        mHandler=new Handler();
        //new Thread(new BtSyncRunnable()).start();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand called");
        return START_STICKY;
    }
    @Override
    public void onDestroy(){
        Log.i(TAG, "onDestroy called");
    }

    /**
     * 返回一个Binder对象
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"onBindCalled");
        return new MsgBinder();
    }

    //commit record to db
    public void commitPhotoToDb(){

    }





    public class MsgBinder extends Binder {
        /**
         * 获取当前Service的实例
         * @return
         */
        public MyService getService(){
            return MyService.this;
        }
    }





}