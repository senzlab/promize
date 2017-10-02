package com.score.rahasak.pojo;

import android.os.Parcel;
import android.os.Parcelable;

public class Cheque implements Parcelable {
    private String account;
    private int amount;
    private String date;
    private String img;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
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

    public String getImg() {
        return img;
    }

    public Cheque(String account, int amount) {
        this.account = account;
        this.amount = amount;
    }

    public void setImg(String img) {
        this.img = img;
    }

    protected Cheque(Parcel in) {
        account = in.readString();
        amount = in.readInt();
        date = in.readString();
        img = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(account);
        dest.writeInt(amount);
        dest.writeString(date);
        dest.writeString(img);
    }
}
