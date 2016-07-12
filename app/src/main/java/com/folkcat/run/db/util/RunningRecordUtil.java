package com.folkcat.run.db.util;

import android.util.Log;
import android.widget.BaseAdapter;


import com.folkcat.run.db.mode.RunningRecord;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Tamas on 2015/10/22.
 */
public class RunningRecordUtil {
    private static final String TAG="RunningRecordUtil";

    //提交一条记录到数据库，注意数据表中以插入时间作为主键，因此插入间隔至少1毫秒
    public static RunningRecord commitRecordToDb(long runnintId,long createDate,long finishDate,String mapThumpnaiPath){
        Realm realm= Realm.getDefaultInstance();
        realm.beginTransaction();
        RunningRecord runningRecordRealm =realm.createObject(RunningRecord.class);
        runningRecordRealm.setRunningId(runnintId);
        runningRecordRealm.setCreateDate(createDate);
        runningRecordRealm.setFinishDate(finishDate);
        runningRecordRealm.setThumbnailPath(mapThumpnaiPath);

        realm.commitTransaction();
        realm.close();
        return runningRecordRealm;
    }



    //获取习惯列表
    public static List<RunningRecord> getRecordListFromDb(){
        Realm realm= Realm.getDefaultInstance();
        RealmResults<RunningRecord> runningRecordList = realm.where(RunningRecord.class).findAll();
        runningRecordList.sort("createDate", RealmResults.SORT_ORDER_DESCENDING);
        return runningRecordList;
    }

    public static RunningRecord getRecordById(long id){
        Realm realm= Realm.getDefaultInstance();
        RunningRecord runningRecord = realm.where(RunningRecord.class).equalTo("runningId", id).findFirst();
        return runningRecord;
    }

    public static void deleteRecordById(long id){
        Realm realm= Realm.getDefaultInstance();
        RunningRecord runningRecord =realm.where(RunningRecord.class).equalTo("runningId",id).findFirst();
        realm.beginTransaction();
        if(runningRecord !=null){
            runningRecord.removeFromRealm();
        }
        realm.commitTransaction();
        realm.close();
    }
}
