package com.score.cbook.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.score.cbook.R;
import com.score.cbook.pojo.Account;

/**
 * Utility class to deal with Share Preferences
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class PreferenceUtil {

    public static final String Z_ADDRESS = "Z_ADDRESS";
    public static final String PUBLIC_KEY = "PUBLIC_KEY";
    public static final String PRIVATE_KEY = "PRIVATE_KEY";
    public static final String ZWITCH_KEY = "ZWITCH_KEY";
    public static final String CHAINZ_KEY = "CHAINZ_KEY";
    public static final String ACCOUNT_BANK = "ACCOUNT_BANK";
    public static final String ACCOUNT_NO = "ACCOUNT_NO";
    public static final String ACCOUNT_STATE = "ACCOUNT_STATE";
    public static final String USERNAME = "USERNAME";
    public static final String PASSWORD = "PASSWORD";
    public static final String QUESTION1 = "QUESTION1";
    public static final String QUESTION2 = "QUESTION2";
    public static final String QUESTION3 = "QUESTION3";
    public static final String TODAY = "TODAY";
    public static final String TODAY_AMOUNT = "TODAY_COUNT";

    public static Account getAccount(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        Account account = new Account();
        account.setBank(preferences.getString(PreferenceUtil.ACCOUNT_BANK, ""));
        account.setAccountNo(preferences.getString(PreferenceUtil.ACCOUNT_NO, ""));
        account.setUsername(preferences.getString(PreferenceUtil.USERNAME, ""));
        account.setPassword(preferences.getString(PreferenceUtil.PASSWORD, ""));
        account.setState(preferences.getString(PreferenceUtil.ACCOUNT_STATE, "PENDING"));

        return account;
    }

    public static void put(Context context, String key, String value) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String get(Context context, String key) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return preferences.getString(key, "");
    }

    public static void put(Context context, String key, int value) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int get(Context context, String key, int defVal) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return preferences.getInt(key, defVal);
    }

}
