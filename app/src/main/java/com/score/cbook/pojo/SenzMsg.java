package com.score.cbook.pojo;

import android.os.Parcel;
import android.os.Parcelable;

public class SenzMsg implements Parcelable {
    private String uid;
    private String msg;

    public SenzMsg(String uid, String msg) {
        this.uid = uid;
        this.msg = msg;
    }

    protected SenzMsg(Parcel in) {
        uid = in.readString();
        msg = in.readString();
    }

    public static final Creator<SenzMsg> CREATOR = new Creator<SenzMsg>() {
        @Override
        public SenzMsg createFromParcel(Parcel in) {
            return new SenzMsg(in);
        }

        @Override
        public SenzMsg[] newArray(int size) {
            return new SenzMsg[size];
        }
    };

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(msg);
    }
}
