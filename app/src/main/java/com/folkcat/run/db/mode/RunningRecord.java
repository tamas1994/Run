package com.folkcat.run.db.mode;


import io.realm.RealmObject;

/**
 * Created by Tamas on 2016/7/2.
 */
public class RunningRecord extends RealmObject {
    public static final int STATE_NOT_UP=0;//未上传
    public static final int STATE_UP=1;//已上传
    public static final int STATE_DOWN=2;//已下载

    private long runningId;
    private long createDate;
    private int numOfGPSPoint;
    private float avageSpeed;

    public long getRunningId() {
        return runningId;
    }

    public void setRunningId(long id) {
        this.runningId = id;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public int getNumOfGPSPoint() {
        return numOfGPSPoint;
    }

    public void setNumOfGPSPoint(int numOfGPSPoint) {
        this.numOfGPSPoint = numOfGPSPoint;
    }

    public float getAvageSpeed() {
        return avageSpeed;
    }

    public void setAvageSpeed(float avageSpeed) {
        this.avageSpeed = avageSpeed;
    }
}
