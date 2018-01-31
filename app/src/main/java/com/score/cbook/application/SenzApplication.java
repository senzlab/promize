package com.score.cbook.application;

import android.app.Application;

/**
 * Application class to hold shared attributes
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SenzApplication extends Application {

    private static boolean onChat = false;

    private static String chatUser = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static boolean isOnChat() {
        return onChat;
    }

    public static void setOnChat(boolean onChat) {
        SenzApplication.onChat = onChat;
    }

    public static String getChatUser() {
        return chatUser;
    }

    public static void setChatUser(String chatUser) {
        SenzApplication.chatUser = chatUser;
    }

}
