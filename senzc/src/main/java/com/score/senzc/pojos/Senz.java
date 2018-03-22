package com.score.senzc.pojos;

import android.os.Parcel;
import android.os.Parcelable;

import com.score.senzc.enums.SenzTypeEnum;

import java.util.HashMap;

/**
 * Keep Senz data here
 *
 * @author eranga herath(erabgaeb@gmail.com)
 */
public class Senz implements Parcelable {
    private String id;
    private String signature;
    private SenzTypeEnum senzType;
    private String sender;
    private String receiver;
    private HashMap<String, String> attributes;

    public Senz() {
    }

    public Senz(String id, String signature, SenzTypeEnum senzType, String sender, String receiver, HashMap<String, String> attributes) {
        this.id = id;
        this.signature = signature;
        this.senzType = senzType;
        this.sender = sender;
        this.receiver = receiver;
        this.attributes = attributes;
    }

    /**
     * Use when reconstructing User object from parcel
     * This will be used only by the 'CREATOR'
     *
     * @param in a parcel to read this object
     */
    public Senz(Parcel in) {
        this.id = in.readString();
        this.signature = in.readString();
        this.senzType = SenzTypeEnum.valueOf(in.readString());
        this.sender = in.readString();
        this.receiver = in.readString();

        // read attribute map from parcel
        this.attributes = new HashMap<>();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            this.attributes.put(in.readString(), in.readString());
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(signature);
        dest.writeString(senzType.name());
        dest.writeString(sender);
        dest.writeString(receiver);

        // write attribute map to parcel
        dest.writeInt(attributes.size());
        for (String s : attributes.keySet()) {
            dest.writeString(s);
            dest.writeString(attributes.get(s));
        }
    }

    /**
     * This field is needed for Android to be able to
     * create new objects, individually or as arrays
     * <p/>
     * If you don’t do that, Android framework will through exception
     * Parcelable protocol requires a Parcelable.Creator object called CREATOR
     */
    public static final Creator<Senz> CREATOR = new Creator<Senz>() {

        public Senz createFromParcel(Parcel in) {
            return new Senz(in);
        }

        public Senz[] newArray(int size) {
            return new Senz[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public SenzTypeEnum getSenzType() {
        return senzType;
    }

    public void setSenzType(SenzTypeEnum senzType) {
        this.senzType = senzType;
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            Senz toCompare = (Senz) obj;
            return (this.id.equalsIgnoreCase(toCompare.getId()));
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (this.getId()).hashCode();
    }
}
