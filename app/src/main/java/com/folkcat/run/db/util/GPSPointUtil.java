package com.folkcat.run.db.util;


import com.folkcat.run.db.mode.GPSPoint;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Tamas on 2015/10/22.
 */
public class GPSPointUtil {
    private static final String TAG="GPSPointUtil";

    //提交一条记录到数据库，注意数据表中以插入时间作为主键，因此插入间隔至少1毫秒
    public static GPSPoint commitPointToDb(long runningId,double lat,double lng){
        Realm realm= Realm.getDefaultInstance();
        realm.beginTransaction();
        GPSPoint gpsPointRealm =realm.createObject(GPSPoint.class);
        gpsPointRealm.setCreateDate(System.currentTimeMillis());
        gpsPointRealm.setRunningId(runningId);
        gpsPointRealm.setLatitude(lat);
        gpsPointRealm.setLongitude(lng);
        realm.commitTransaction();
        realm.close();
        return gpsPointRealm;
    }



    public static List<GPSPoint> getGointsByRunning(long runningId){
        Realm realm= Realm.getDefaultInstance();
        RealmResults<GPSPoint> gpsPointList = realm.where(GPSPoint.class).equalTo("runningId", runningId).findAll();
        gpsPointList.sort("createDate", RealmResults.SORT_ORDER_DESCENDING);
        return gpsPointList;
    }


    public static void deletePointsByRunningId(long runningId){
        Realm realm= Realm.getDefaultInstance();
        RealmResults<GPSPoint> gpsPointList =realm.where(GPSPoint.class).equalTo("runningId",runningId).findAll();
        realm.beginTransaction();
        if(gpsPointList!=null){
            gpsPointList.removeAll(gpsPointList);
        }
        realm.commitTransaction();
        realm.close();
    }
}
