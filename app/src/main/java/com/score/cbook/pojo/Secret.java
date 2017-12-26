package com.score.cbook.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.score.cbook.enums.BlobType;
import com.score.cbook.enums.DeliveryState;

public class Secret implements Parcelable {
    private String id;
    private String blob;
    private BlobType blobType;
    private ChequeUser user;
    private boolean isMySecret;
    private boolean isViewed;
    private boolean isSelected;
    private Long timeStamp;
    private Long viewedTimeStamp;
    private boolean isMissed;
    private boolean inOrder;
    private DeliveryState deliveryState;

    public Secret() {
    }

    protected Secret(Parcel in) {
        id = in.readString();
        blob = in.readString();
        user = in.readParcelable(ChequeUser.class.getClassLoader());
        isMySecret = in.readByte() != 0;
        isViewed = in.readByte() != 0;
        isSelected = in.readByte() != 0;
        if (in.readByte() == 0) {
            timeStamp = null;
        } else {
            timeStamp = in.readLong();
        }
        if (in.readByte() == 0) {
            viewedTimeStamp = null;
        } else {
            viewedTimeStamp = in.readLong();
        }
        isMissed = in.readByte() != 0;
        inOrder = in.readByte() != 0;
    }

    public static final Creator<Secret> CREATOR = new Creator<Secret>() {
        @Override
        public Secret createFromParcel(Parcel in) {
            return new Secret(in);
        }

        @Override
        public Secret[] newArray(int size) {
            return new Secret[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBlob() {
        return blob;
    }

    public void setBlob(String blob) {
        this.blob = blob;
    }

    public BlobType getBlobType() {
        return blobType;
    }

    public void setBlobType(BlobType blobType) {
        this.blobType = blobType;
    }

    public ChequeUser getUser() {
        return user;
    }

    public void setUser(ChequeUser user) {
        this.user = user;
    }

    public boolean isMySecret() {
        return isMySecret;
    }

    public void setMySecret(boolean mySecret) {
        isMySecret = mySecret;
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

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Long getViewedTimeStamp() {
        return viewedTimeStamp;
    }

    public void setViewedTimeStamp(Long viewedTimeStamp) {
        this.viewedTimeStamp = viewedTimeStamp;
    }

    public boolean isMissed() {
        return isMissed;
    }

    public void setMissed(boolean missed) {
        isMissed = missed;
    }

    public boolean isInOrder() {
        return inOrder;
    }

    public void setInOrder(boolean inOrder) {
        this.inOrder = inOrder;
    }

    public DeliveryState getDeliveryState() {
        return deliveryState;
    }

    public void setDeliveryState(DeliveryState deliveryState) {
        this.deliveryState = deliveryState;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(blob);
        dest.writeParcelable(user, flags);
        dest.writeByte((byte) (isMySecret ? 1 : 0));
        dest.writeByte((byte) (isViewed ? 1 : 0));
        dest.writeByte((byte) (isSelected ? 1 : 0));
        if (timeStamp == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(timeStamp);
        }
        if (viewedTimeStamp == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(viewedTimeStamp);
        }
        dest.writeByte((byte) (isMissed ? 1 : 0));
        dest.writeByte((byte) (inOrder ? 1 : 0));
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Secret)) {
            return false;
        }

        Secret that = (Secret) other;
        return this.id.equalsIgnoreCase(that.id);
    }
}
