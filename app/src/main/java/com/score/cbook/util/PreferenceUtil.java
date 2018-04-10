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

    private static final String Z_ADDRESS = "Z_ADDRESS";
    private static final String BANK = "BANK";
    private static final String ACCOUNT_NO = "ACCOUNT_NO";
    private static final String PHONE_NO = "PHONE_NO";
    private static final String PASSWORD = "PASSWORD";
    private static final String STATE = "STATE";

    /**
     * Save user credentials in shared preference
     *
     * @param context application context
     */
    public static void saveSenzeisAddress(Context context, String address) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PreferenceUtil.Z_ADDRESS, address);
        editor.commit();
    }

    /**
     * Get user details from shared preference
     *
     * @param context application context
     * @return user object
     */
    public static String getSenzieAddress(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return preferences.getString(PreferenceUtil.Z_ADDRESS, "");
    }

    /**
     * Save password in shared preference
     *
     * @param context application context
     * @param account
     */
    public static void saveAccount(Context context, Account account) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PreferenceUtil.BANK, account.getBank());
        editor.putString(PreferenceUtil.ACCOUNT_NO, account.getAccountNo());
        editor.putString(PreferenceUtil.PHONE_NO, account.getPhoneNo());
        editor.putString(PreferenceUtil.PASSWORD, account.getPassword());
        editor.commit();
    }

    public static void saveAccountNo(Context context, String accountNo) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PreferenceUtil.ACCOUNT_NO, accountNo);
        editor.commit();
    }

    public static void saveAccountState(Context context, String state) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PreferenceUtil.STATE, state);
        editor.commit();
    }

    /**
     * Get saved password from shared preference
     *
     * @param context
     * @return
     */
    public static Account getAccount(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        Account account = new Account();
        account.setBank(preferences.getString(PreferenceUtil.BANK, ""));
        account.setAccountNo(preferences.getString(PreferenceUtil.ACCOUNT_NO, ""));
        account.setPhoneNo(preferences.getString(PreferenceUtil.PHONE_NO, ""));
        account.setPassword(preferences.getString(PreferenceUtil.PASSWORD, ""));
        account.setState(preferences.getString(PreferenceUtil.STATE, "PENDING"));

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


    /**
     * update password in shared preference
     *
     * @param context application context
     * @param account
     */
    public static void updatePasswordAccount(Context context, Account account) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
          editor.putString(PreferenceUtil.PASSWORD, account.getPassword());
        editor.commit();
    }

    /**
     * update password in shared preference
     *
     * @param context application context
     * @param account
     */
    public static void updateUsernameAccount(Context context, Account account) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PreferenceUtil.PHONE_NO, account.getPhoneNo());
        editor.commit();
    }

}
