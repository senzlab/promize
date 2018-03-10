package com.score.cbook.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.score.cbook.R;
import com.score.cbook.exceptions.NoUserException;
import com.score.cbook.pojo.Account;
import com.score.senzc.pojos.User;

/**
 * Utility class to deal with Share Preferences
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class PreferenceUtil {

    private static final String Z_ADDRESS = "Z_ADDRESS";
    private static final String BANK = "BANK";
    private static final String ACCOUNT_NO = "ACCOUNT_NO";
    private static final String PASSWORD = "PASSWORD";

    /**
     * Save user credentials in shared preference
     *
     * @param context application context
     * @param user    logged-in user
     */
    public static void saveUser(Context context, User user) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PreferenceUtil.Z_ADDRESS, user.getUsername());
        editor.commit();
    }

    /**
     * Get user details from shared preference
     *
     * @param context application context
     * @return user object
     */
    public static User getUser(Context context) throws NoUserException {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String senzieAddress = preferences.getString(PreferenceUtil.Z_ADDRESS, "");

        if (senzieAddress.isEmpty())
            throw new NoUserException();

        return new User(senzieAddress, senzieAddress);
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
        editor.putString(PreferenceUtil.PASSWORD, account.getPassword());
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
        account.setPassword(preferences.getString(PreferenceUtil.PASSWORD, ""));

        return account;
    }

    /**
     * Save public/private keys in shared preference,
     *
     * @param context application context
     * @param key     public/private keys(encoded key string)
     * @param keyType public_key, private_key, server_key
     */
    public static void saveRsaKey(Context context, String key, String keyType) {
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
    public static String getRsaKey(Context context, String keyType) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return preferences.getString(keyType, "");
    }

}
