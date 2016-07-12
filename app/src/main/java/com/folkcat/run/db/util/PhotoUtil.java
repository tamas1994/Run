package com.folkcat.run.db.util;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.folkcat.run.db.mode.Photo;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Tamas on 2016/7/10.
 */
public class PhotoUtil {
    private static final String TAG="PhotoUtil";
    public static Photo commitPhotoToDb(long runningId,String nailPath,String photoPath){
        Log.i(TAG, "a photo has been commit to db nailPath:" + nailPath + "  photoPath:" + photoPath);
        Realm realm= Realm.getDefaultInstance();
        realm.beginTransaction();
        Photo photoRealm =realm.createObject(Photo.class);
        photoRealm.setCreateDate(System.currentTimeMillis());
        photoRealm.setRunningId(runningId);
        photoRealm.setPhotoPath(photoPath);
        photoRealm.setThumbnailPath(nailPath);
        realm.commitTransaction();
        realm.close();
        return photoRealm;
    }

    public static List<Photo> getPthotosByRunningIdAndUpdateUi(long runningId){
        Realm realm= Realm.getDefaultInstance();
        RealmResults<Photo> photoList = realm.where(Photo.class).equalTo("runningId", runningId).findAll();
        photoList.sort("createDate", RealmResults.SORT_ORDER_DESCENDING);
        return photoList;
    }
}
