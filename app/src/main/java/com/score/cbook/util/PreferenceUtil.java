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
    public static final String BANK = "BANK";
    public static final String ACCOUNT_NO = "ACCOUNT_NO";
    public static final String ACCOUNT_STATE = "ACCOUNT_STATE";
    public static final String PHONE_NO = "PHONE_NO";
    public static final String PASSWORD = "PASSWORD";
    public static final String QUESTION1 = "QUESTION1";
    public static final String QUESTION2 = "QUESTION2";
    public static final String QUESTION3 = "QUESTION3";

    public static Account getAccount(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        Account account = new Account();
        account.setBank(preferences.getString(PreferenceUtil.BANK, ""));
        account.setAccountNo(preferences.getString(PreferenceUtil.ACCOUNT_NO, ""));
        account.setPhoneNo(preferences.getString(PreferenceUtil.PHONE_NO, ""));
        account.setPassword(preferences.getString(PreferenceUtil.PASSWORD, ""));
        account.setState(preferences.getString(PreferenceUtil.ACCOUNT_STATE, "PENDING"));

        return account;
    }

    /**
     * Save public/private keys in shared preference,
     *
     * @param context application context
     * @param key     public/private keys(encoded key string)
     * @param keyType public_key, private_key, server_key
     */
    static void saveRsaKey(Context context, String key, String keyType) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(keyType, key);
        editor.commit();
    }

    /**
     * Get saved RSA key string from shared preference
     *
     * @param context application context
     * @param keyType public_key, private_key, server_key
     * @return key string
     */
    static String getRsaKey(Context context, String keyType) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return preferences.getString(keyType, "");
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

}
