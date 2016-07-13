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
    private long finishDate;
    private int numOfGPSPoint;
    private float avageSpeed;
    private int cal;
    private int numOfSecond;

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    private int distance;
    private String thumbnailPath;

    public int getNumOfSecond() {
        return numOfSecond;
    }

    public void setNumOfSecond(int numOfSecond) {
        this.numOfSecond = numOfSecond;
    }

    public int getCal() {
        return cal;
    }

    public void setCal(int cal) {
        this.cal = cal;
    }

    public long getRunningId() {
        return runningId;
    }

    public void setRunningId(long id) {
        this.runningId = id;
    }

    public long getCreateDate() {
        return createDate;
    }

    public long getFinishDate(){
        return finishDate;
    }
    public String getThumbnailPath(){
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath){
        this.thumbnailPath=thumbnailPath;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public void setFinishDate(long finishDate){
        this.finishDate=finishDate;
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
