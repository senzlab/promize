package com.score.cbook.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.score.senzc.pojos.User;

/**
 * Created by eranga on 11/11/16
 */
public class ChequeUser extends User implements Parcelable {
    private String phone;
    private String image;
    private String pubKey;
    private String pubKeyHash;
    private String sessionKey;
    private boolean isSMSRequester;
    private boolean isActive;
    private boolean selected;
    private int unreadSecretCount;

    public ChequeUser(String id, String username) {
        super(id, username);
    }

    /**
     * Use when reconstructing User object from parcel
     * This will be used only by the 'CREATOR'
     *
     * @param in a parcel to read this object
     */
    public ChequeUser(Parcel in) {
        super(in);
        this.phone = in.readString();
        this.image = in.readString();
        this.pubKey = in.readString();
        this.pubKeyHash = in.readString();
        this.sessionKey = in.readString();
        this.isActive = in.readByte() != 0;
        this.isSMSRequester = in.readByte() != 0;
        this.selected = in.readByte() != 0;
        this.unreadSecretCount = in.readInt();
    }

    /**
     * Define the kind of object that you gonna parcel,
     * You can use hashCode() here
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Actual object serialization happens here, Write object content
     * to parcel one by one, reading should be done according to this write order
     *
     * @param dest  parcel
     * @param flags Additional flags about how the object should be written
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(phone);
        dest.writeString(image);
        dest.writeString(pubKey);
        dest.writeString(pubKeyHash);
        dest.writeString(sessionKey);
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeByte((byte) (isSMSRequester ? 1 : 0));
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeInt(unreadSecretCount);
    }

    /**
     * This field is needed for Android to be able to
     * create new objects, individually or as arrays
     * <p>
     * If you don’t do that, Android framework will through exception
     * Parcelable protocol requires a Parcelable.Creator object called CREATOR
     */
    public static final Creator<ChequeUser> CREATOR = new Creator<ChequeUser>() {
        public ChequeUser createFromParcel(Parcel in) {
            return new ChequeUser(in);
        }

        public ChequeUser[] newArray(int size) {
            return new ChequeUser[size];
        }
    };

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getPubKeyHash() {
        return pubKeyHash;
    }

    public void setPubKeyHash(String pubKeyHash) {
        this.pubKeyHash = pubKeyHash;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isSMSRequester() {
        return isSMSRequester;
    }

    public void setSMSRequester(boolean SMSRequester) {
        isSMSRequester = SMSRequester;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getUnreadSecretCount() {
        return unreadSecretCount;
    }

    public void setUnreadSecretCount(int unreadSecretCount) {
        this.unreadSecretCount = unreadSecretCount;
    }
}
