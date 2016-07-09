package com.folkcat.run.db.util;

import com.folkcat.run.db.mode.Photo;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Tamas on 2016/7/10.
 */
public class PhotoUtil {
    public static Photo commitPhotoToDb(long runningId,String nailPath,String photoPath){
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

    public static List<Photo> getPthotosByRunningId(long runningId){
        Realm realm= Realm.getDefaultInstance();
        RealmResults<Photo> photoList = realm.where(Photo.class).equalTo("runningId", runningId).findAll();
        photoList.sort("createDate", RealmResults.SORT_ORDER_DESCENDING);
        return photoList;
    }
}
