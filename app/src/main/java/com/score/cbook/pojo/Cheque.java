package com.score.cbook.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.score.cbook.enums.DeliveryState;

public class Cheque implements Parcelable {
    private String uid;
    private ChequeUser user;
    private boolean isSender;
    private boolean isViewed;
    private boolean isSelected;
    private Long timestamp;
    private Long viewedTimeStamp;
    private DeliveryState deliveryState;

    private String cid;
    private String state;
    private int amount;
    private String date;
    private String blob;

    public Cheque() {
    }

    public Cheque(Parcel in) {
        uid = in.readString();
        user = in.readParcelable(ChequeUser.class.getClassLoader());
        isSender = in.readByte() != 0;
        isViewed = in.readByte() != 0;
        isSelected = in.readByte() != 0;
        cid = in.readString();
        state = in.readString();
        amount = in.readInt();
        date = in.readString();
        blob = in.readString();
    }

    public static final Creator<Cheque> CREATOR = new Creator<Cheque>() {
        @Override
        public Cheque createFromParcel(Parcel in) {
            return new Cheque(in);
        }

        @Override
        public Cheque[] newArray(int size) {
            return new Cheque[size];
        }
    };

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public ChequeUser getUser() {
        return user;
    }

    public void setUser(ChequeUser user) {
        this.user = user;
    }

    public boolean isSender() {
        return isSender;
    }

    public void setSender(boolean sender) {
        isSender = sender;
    }

    public boolean isViewed() {
        return isViewed;
    }

    public void setViewed(boolean viewed) {
        isViewed = viewed;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Long getViewedTimeStamp() {
        return viewedTimeStamp;
    }

    public void setViewedTimeStamp(Long viewedTimeStamp) {
        this.viewedTimeStamp = viewedTimeStamp;
    }

    public DeliveryState getDeliveryState() {
        return deliveryState;
    }

    public void setDeliveryState(DeliveryState deliveryState) {
        this.deliveryState = deliveryState;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBlob() {
        return blob;
    }

    public void setBlob(String blob) {
        this.blob = blob;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeParcelable(user, flags);
        dest.writeByte((byte) (isSender ? 1 : 0));
        dest.writeByte((byte) (isViewed ? 1 : 0));
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeString(cid);
        dest.writeString(state);
        dest.writeInt(amount);
        dest.writeString(date);
        dest.writeString(blob);
    }
}
