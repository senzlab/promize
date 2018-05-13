package com.score.cbook.pojo;

public class SenzMsg {
    String uid;
    String msg;

    public SenzMsg(String uid, String msg) {
        this.uid = uid;
        this.msg = msg;
    }

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
}
