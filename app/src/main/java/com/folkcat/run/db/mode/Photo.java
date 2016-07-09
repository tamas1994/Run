package com.folkcat.run.db.mode;

import io.realm.RealmObject;

/**
 * Created by Tamas on 2016/7/10.
 */
public class Photo extends RealmObject {
    private long runningId;
    private String thumbnailPath;//缩略图路径
    private String photoPath;//原图路径
    private long createDate;

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public long getRunningId() {
        return runningId;
    }

    public void setRunningId(long runningId) {
        this.runningId = runningId;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }
}
