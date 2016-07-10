package com.folkcat.run.util;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.WindowManager;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Tamas on 2015/8/4.
 */
public class TamasUtils {
    private static final String TAG="APPUtils";
    /*
    get the screen width of phone
     */
    public static int getScreenWidth(Activity activity){
        WindowManager wm = activity.getWindowManager();
        return wm.getDefaultDisplay().getWidth();
    }
    public static int getScreenHeight(Activity activity){
        WindowManager wm = activity.getWindowManager();
        return wm.getDefaultDisplay().getHeight();
    }
    /*
    get the directory of pictures
     */
    public static File getTakedPicDirPath(Context ctx){
        String cachePath;
        //Preferred to use sd card
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = ctx.getExternalCacheDir().getPath()+File.separator+"photos";
        } else {
            cachePath = ctx.getCacheDir().getPath()+File.separator+"photos";
        }
        File cacheDir=new File(cachePath);
        if(!cacheDir.exists()){
            cacheDir.mkdirs();
        }
        return cacheDir;
    }
    /*
    get the directory of pictures
     */
    public static File getThumbnailPath(Context ctx){
        String cachePath;
        //Preferred to use sd card
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = ctx.getExternalCacheDir().getPath()+File.separator+"thumbnail";
        } else {
            cachePath = ctx.getCacheDir().getPath()+File.separator+"thumbnail";
        }
        File cacheDir=new File(cachePath);
        if(!cacheDir.exists()){
            cacheDir.mkdirs();
        }
        return cacheDir;
    }
    /*
    save Bitmap to file
     */
    public static void saveBitmap(Context ctx,Bitmap bm,File file) {
        try {
            if(file.exists()){
                file.delete();
            }

            String filePath = file.getPath();
            FileOutputStream out = new FileOutputStream(file);
            Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
            bm.compress(format, 80, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
    Remix the origin bitmap and watermark bitmap
     */
    public static Bitmap createBitmapForWatermark(Bitmap src, Bitmap watermark,int toLeft,int toTop) {
        Log.i(TAG,"toLeft:"+toLeft+"  toTop:"+toTop);
        Log.i(TAG,"src W:"+src.getWidth()+" H:"+src.getHeight());
        Log.i(TAG,"watermark w:"+watermark.getWidth()+" H:"+watermark.getHeight());

        if (src == null) {
            return null;
        }
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        // create the new blank bitmap
        Bitmap newBitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);//
        Canvas cv = new Canvas(newBitmap);
        // draw src into
        cv.drawBitmap(src, 0, 0, null);
        // draw watermark into
        cv.drawBitmap(watermark, toLeft, toTop , null);
        // save all clip
        cv.save(Canvas.ALL_SAVE_FLAG);
        // store
        cv.restore();
        return newBitmap;
    }

    /*
    get file path from Uri
     */
    public static String getFilePathFromUri(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver()
                .query(uri, null, null,
                        null, null);
        int index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(index);
        cursor.close();
        cursor = null;
        return path;
    }
    /*
    生成正方形位图
    裁取位图中间部分
     */
    public static Bitmap getSquareScaledBitmap(Bitmap oriBm,int px){
        //获得oriBm的最短边
        int shortEdge=oriBm.getHeight();
        if(shortEdge>oriBm.getWidth()){
            shortEdge=oriBm.getWidth();
        }
        //判断最短边是否小于传入的正方形边长
        if(shortEdge>px){
            shortEdge=px;
        }
        Bitmap bm= ThumbnailUtils.extractThumbnail(oriBm,shortEdge-1,shortEdge-1);
        return bm;
    }
    /*
    缩放图片
     */
    public static Bitmap getScaledBitmap(Bitmap oriBm,int px){
        //获得oriBm的最短边
        float shortEdge=oriBm.getHeight();
        float rate=oriBm.getWidth()/(float)shortEdge;
        if(shortEdge>px){
            shortEdge=px;
        }
        Log.i(TAG,"shortEdge:"+shortEdge+"   rate:"+rate);
        Log.i(TAG,"oriBm  W:"+oriBm.getWidth()+"  H:"+oriBm.getHeight());
        Bitmap bm= ThumbnailUtils.extractThumbnail(oriBm,(int)(shortEdge*rate-1),(int)shortEdge-1);//减一防止越界
        return bm;
    }
    /*
    缩放图片
     */
    public static Bitmap getScaledBitmapByWidth(Bitmap oriBm,int px){
        //获得oriBm的最短边
        float width=oriBm.getWidth();
        float rate=oriBm.getHeight()/(float)width;
        if(width>px){
            width=px;
        }
        Log.i(TAG,"oriBm  W:"+oriBm.getWidth()+"  H:"+oriBm.getHeight());
        Bitmap bm= ThumbnailUtils.extractThumbnail(oriBm,(int)(width-1),(int)(width*rate-1));//减一防止越界
        return bm;
    }
    /*
    缩放图片
     */
    public static Bitmap getScaledBitmapFromSize(Bitmap oriBm,int px,Camera.Size s){
        //获得oriBm的最短边
        float shortEdge=oriBm.getHeight();
        float rate=s.width/(float)s.height;
        if(shortEdge>px){
            shortEdge=px;
        }
        Bitmap bm= ThumbnailUtils.extractThumbnail(oriBm,(int)(shortEdge*rate),(int)shortEdge);
        return bm;
    }
    public static Bitmap getScaledBitmapByShotEdge(Bitmap oriBm,int px){
        //获得oriBm的最短边
        int shortEdge=oriBm.getHeight();
        float rate=oriBm.getWidth()/(float)shortEdge;
        if(shortEdge>px){
            shortEdge=px;
        }
        Log.i(TAG,"rate:"+rate);
        Bitmap bm= ThumbnailUtils.extractThumbnail(oriBm,(int)(shortEdge*rate),(int)shortEdge);
        return bm;
    }
    /*
    transform dip to px
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /*
    针对切换摄像头后前置摄像头只显示上方的问题
     */
    public static Bitmap getTopSquareBm(Bitmap oriBm,int width){
        Bitmap scaledBm=getScaledBitmap(oriBm, width);
        Bitmap newBm=Bitmap.createBitmap(scaledBm,scaledBm.getWidth()-scaledBm.getHeight(),0,width,width);
        scaledBm.recycle();
        return newBm;
    }

    /*
    获取系统当前时间
     */
    public static String getCurrentTimeStr(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd HH:mm:ss");
        long time = System.currentTimeMillis();
        return sdf.format(time);
    }


}
