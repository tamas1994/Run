package com.folkcat.run.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


/**
 * Created by Tamas on 2015/9/15.
 * SharedPreferences工具类
 */

public class SPUtil {
    public static String SP_KEY_IS_GET_CROUSE_SCHEDULE="isGetCS";//标记是否从教务管理系统导入过课程表
    public static String SP_KEY_CS_ROW_DATA="CSRawData";//课程表原始数据
    public static String SP_KEY_CS_JSON_DATA="CSJsonData";//课程表原始数据


    private Context mContext;
    private  final String SHARED_PATH = "sp_seed";
    private static SPUtil sSPUtil;
    private SharedPreferences mSP;
    private Editor mEditor;

    private SPUtil(Context ctx){
        this.mContext=ctx;
        this.mSP=mContext.getSharedPreferences(SHARED_PATH,Context.MODE_PRIVATE);
        this.mEditor=mSP.edit();
    }
    public static SPUtil getInstance(Context ctx){
        if(sSPUtil==null){
            sSPUtil=new SPUtil(ctx);
        }
        return sSPUtil;
    }


    public  void remove(String key){
        mEditor.remove(key);
        mEditor.commit();
    }

    public void putInt(String key, int value) {
        mEditor.putInt(key, value);
        mEditor.commit();
    }

    public int getInt(String key,int defValue) {
        return mSP.getInt(key, defValue);
    }

    public  void putString(String key, String value) {
        mEditor.putString(key, value);
        mEditor.commit();
    }

    public String getString(String key,String defStr) {
        return mSP.getString(key,defStr);
    }

    public void putBoolean(String key, boolean value) {
        mEditor.putBoolean(key, value);
        mEditor.commit();
    }

    public boolean getBoolean(String key,boolean defValue) {
        return mSP.getBoolean(key,defValue);
    }
}
