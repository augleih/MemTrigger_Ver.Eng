package com.google.gachon.software.reminderapp_eng;

/**
 * Created by 이상원 on 2017-07-27.
 */

public class Playlist {
    String numStr;
    String content;
    String alarmTime;
    int resId;
    String strColor;
    boolean isCheckedState = false;

    public Playlist(String numStr, String content, String alarmTime, int resId, String strColor) {
        this.numStr = numStr;
        this.content = content;
        this.alarmTime = alarmTime;
        this.resId = resId;
        this.strColor = strColor;
    }

    public String getNumStr() {
        return numStr;
    }

    public String getContent() {
        return content;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public int getResId() {
        return resId;
    }

    public String getStrColor() {
        return strColor;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public void setStrColor(String strColor) {
        this.strColor = strColor;
    }

    public void setCheckedState() {this.isCheckedState = true;}

    public void setUnCheckedState() {this.isCheckedState = false;}

}