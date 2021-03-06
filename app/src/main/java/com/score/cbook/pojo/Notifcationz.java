package com.score.cbook.pojo;

public class Notifcationz {
    private int icon;
    private String title;
    private String message;
    private String sender;
    private String senderPhone;
    private boolean addActions;

    public Notifcationz(int icon, String title, String message, String sender) {
        this.icon = icon;
        this.title = title;
        this.message = message;
        this.sender = sender;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public boolean addActions() {
        return addActions;
    }

    public void setAddActions(boolean addActions) {
        this.addActions = addActions;
    }
}

