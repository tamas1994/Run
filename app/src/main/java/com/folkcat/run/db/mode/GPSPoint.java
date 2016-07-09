package com.folkcat.run.db.mode;

import io.realm.RealmObject;

/**
 * Created by Tamas on 2016/7/8.
 */
public class GPSPoint extends RealmObject {
    private long runningId;
    private long createDate;
    private double latitude;
    private double longitude;

    public long getRunningId() {
        return runningId;
    }

    public void setRunningId(long runningId) {
        this.runningId = runningId;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
