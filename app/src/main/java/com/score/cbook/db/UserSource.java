package com.score.cbook.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.score.cbook.pojo.ChequeUser;

import java.util.LinkedList;

public class UserSource {

    public static boolean isExistingUser(Context context, String username) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        Cursor cursor = db.query(SenzorsDbContract.User.TABLE_NAME, // table
                null, // columns
                SenzorsDbContract.User.COLUMN_NAME_USERNAME + " = ?", // constraint
                new String[]{username}, // prams
                null, // order by
                null, // group by
                null); // join

        return cursor.moveToFirst();
    }

    public static boolean isExistingUserWithPhoneNo(Context context, String phoneNo) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        Cursor cursor = db.query(SenzorsDbContract.User.TABLE_NAME, // table
                null, // columns
                SenzorsDbContract.User.COLUMN_NAME_PHONE + " = ?", // constraint
                new String[]{phoneNo}, // prams
                null, // order by
                null, // group by
                null); // join

        return cursor.moveToFirst();
    }

    public static ChequeUser getExistingUserWithPhoneNo(Context context, String phoneNo) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        Cursor cursor = db.query(SenzorsDbContract.User.TABLE_NAME, // table
                null, // columns
                SenzorsDbContract.User.COLUMN_NAME_PHONE + " = ?", // constraint
                new String[]{phoneNo}, // prams
                null, // order by
                null, // group by
                null); // join

        if (cursor.moveToFirst()) {
            // have matching user
            // so get user data
            // we return id as password since we no storing users password in database
            String username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_USERNAME));
            ChequeUser chequeUser = new ChequeUser(username);
            chequeUser.setPhone(phoneNo);

            return chequeUser;
        }

        cursor.close();

        return null;
    }

    public static void createUser(Context context, ChequeUser chequeUser) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.User.COLUMN_NAME_USERNAME, chequeUser.getUsername());
        if (chequeUser.getSessionKey() != null)
            values.put(SenzorsDbContract.User.COLUMN_NAME_SESSION_KEY, chequeUser.getSessionKey());
        if (chequeUser.getPhone() != null)
            values.put(SenzorsDbContract.User.COLUMN_NAME_PHONE, chequeUser.getPhone());
        if (chequeUser.getPubKey() != null && chequeUser.getPubKey().isEmpty())
            values.put(SenzorsDbContract.User.COLUMN_NAME_PUBKEY, chequeUser.getPubKey());
        if (chequeUser.getPubKeyHash() != null && chequeUser.getPubKeyHash().isEmpty())
            values.put(SenzorsDbContract.User.COLUMN_NAME_PUBKEY_HASH, chequeUser.getPubKeyHash());
        if (chequeUser.getImage() != null && chequeUser.getImage().isEmpty())
            values.put(SenzorsDbContract.User.COLUMN_NAME_IMAGE, chequeUser.getImage());
        values.put(SenzorsDbContract.User.COLUMN_NAME_IS_ACTIVE, chequeUser.isActive() ? 1 : 0);
        values.put(SenzorsDbContract.User.COLUMN_NAME_IS_ADMIN, chequeUser.isAdmin() ? 1 : 0);
        values.put(SenzorsDbContract.User.COLUMN_NAME_IS_SMS_REQUESTER, chequeUser.isSMSRequester() ? 1 : 0);

        // Insert the new row, if fails throw an error
        // fails means user already exists
        db.insertOrThrow(SenzorsDbContract.User.TABLE_NAME, null, values);
    }

    public static void updateUser(Context context, String username, String key, String value) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        if (key.equalsIgnoreCase("phone")) {
            values.put(SenzorsDbContract.User.COLUMN_NAME_PHONE, value);
        } else if (key.equalsIgnoreCase("pubkey")) {
            values.put(SenzorsDbContract.User.COLUMN_NAME_PUBKEY, value);
        } else if (key.equalsIgnoreCase("pubkey_hash")) {
            values.put(SenzorsDbContract.User.COLUMN_NAME_PUBKEY_HASH, value);
        } else if (key.equalsIgnoreCase("image")) {
            values.put(SenzorsDbContract.User.COLUMN_NAME_IMAGE, value);
        } else if (key.equalsIgnoreCase("session_key")) {
            values.put(SenzorsDbContract.User.COLUMN_NAME_SESSION_KEY, value);
        }

        // update
        db.update(SenzorsDbContract.User.TABLE_NAME,
                values,
                SenzorsDbContract.User.COLUMN_NAME_USERNAME + " = ?",
                new String[]{username});
    }

    public static void updateUnreadSecretCount(Context context, String username, int count) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        db.execSQL("UPDATE " + SenzorsDbContract.User.TABLE_NAME +
                        " SET " + SenzorsDbContract.User.COLUMN_NAME_UNREAD_SECRET_COUNT + " = " + SenzorsDbContract.User.COLUMN_NAME_UNREAD_SECRET_COUNT + " + ? " +
                        " WHERE " + SenzorsDbContract.User.COLUMN_NAME_USERNAME + " = ? ",
                new String[]{String.valueOf(count), username});
    }

    public static void resetUnreadSecretCount(Context context, String username) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.User.COLUMN_NAME_UNREAD_SECRET_COUNT, 0);

        // update
        db.update(SenzorsDbContract.User.TABLE_NAME,
                values,
                SenzorsDbContract.User.COLUMN_NAME_USERNAME + " = ?",
                new String[]{username});
    }

    public static void updateUnreadChequeCount(Context context, String username, int count) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        db.execSQL("UPDATE " + SenzorsDbContract.User.TABLE_NAME +
                        " SET " + SenzorsDbContract.User.COLUMN_NAME_UNREAD_CHEQUE_COUNT + " = " + SenzorsDbContract.User.COLUMN_NAME_UNREAD_CHEQUE_COUNT + " + ? " +
                        " WHERE " + SenzorsDbContract.User.COLUMN_NAME_USERNAME + " = ? ",
                new String[]{String.valueOf(count), username});
    }

    public static void resetUnreadChequeCount(Context context, String username) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.User.COLUMN_NAME_UNREAD_CHEQUE_COUNT, 0);

        // update
        db.update(SenzorsDbContract.User.TABLE_NAME,
                values,
                SenzorsDbContract.User.COLUMN_NAME_USERNAME + " = ?",
                new String[]{username});
    }

    public static void activateUser(Context context, String username) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.User.COLUMN_NAME_IS_ACTIVE, 1);

        // update
        db.update(SenzorsDbContract.User.TABLE_NAME,
                values,
                SenzorsDbContract.User.COLUMN_NAME_USERNAME + " = ?",
                new String[]{username});
    }

    public static void deleteUser(Context context, String username) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // delete senz of given user
        db.delete(SenzorsDbContract.User.TABLE_NAME,
                SenzorsDbContract.User.COLUMN_NAME_USERNAME + " = ?",
                new String[]{username});
    }

    public static ChequeUser getUser(Context context, String username) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(SenzorsDbContract.User.TABLE_NAME, // table
                null, // columns
                SenzorsDbContract.User.COLUMN_NAME_USERNAME + " = ?", // constraint
                new String[]{username}, // prams
                null, // order by
                null, // group by
                null); // join

        ChequeUser chequeUser = new ChequeUser(username);
        if (cursor.moveToFirst()) {
            // have matching user
            // so get user data
            // we return id as password since we no storing users password in database
            String phone = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_PHONE));
            String pubKey = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_PUBKEY));
            String pubKeyHash = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_PUBKEY_HASH));
            String image = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_IMAGE));
            String sessionKey = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_SESSION_KEY));
            int isActive = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_IS_ACTIVE));
            int isAdmin = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_IS_ADMIN));
            int isSmsRequester = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_IS_SMS_REQUESTER));
            int unreadSecretCount = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_UNREAD_SECRET_COUNT));
            int unreadChequeCount = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_UNREAD_CHEQUE_COUNT));

            // clear
            chequeUser.setPhone(phone);
            chequeUser.setPubKey(pubKey);
            chequeUser.setPubKeyHash(pubKeyHash);
            chequeUser.setImage(image);
            chequeUser.setActive(isActive == 1);
            chequeUser.setAdmin(isAdmin == 1);
            chequeUser.setSMSRequester(isSmsRequester == 1);
            chequeUser.setSessionKey(sessionKey);
            chequeUser.setUnreadSecretCount(unreadSecretCount);
            chequeUser.setUnreadChequeCount(unreadChequeCount);

            return chequeUser;
        }

        cursor.close();

        return chequeUser;
    }

    public static LinkedList<ChequeUser> getAllUsers(Context context) {
        // get all non admin users
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(SenzorsDbContract.User.TABLE_NAME, // table
                null, // columns
                SenzorsDbContract.User.COLUMN_NAME_IS_ADMIN + " = ?", // constraint
                new String[]{"0"}, // prams,
                null, // order by
                null, // group by
                null); // join

        LinkedList<ChequeUser> chequeUserList = new LinkedList<>();

        while (cursor.moveToNext()) {
            String username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_USERNAME));
            String phone = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_PHONE));
            String pubKey = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_PUBKEY));
            String pubKeyHash = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_PUBKEY_HASH));
            int isActive = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_IS_ACTIVE));
            int isAdmin = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_IS_ADMIN));
            String image = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_IMAGE));
            int isSmsRequester = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_IS_SMS_REQUESTER));

            ChequeUser chequeUser = new ChequeUser(username);
            chequeUser.setPhone(phone);
            chequeUser.setPubKey(pubKey);
            chequeUser.setPubKeyHash(pubKeyHash);
            chequeUser.setImage(image);
            chequeUser.setActive(isActive == 1);
            chequeUser.setAdmin(isAdmin == 1);
            chequeUser.setSMSRequester(isSmsRequester == 1);

            if (chequeUser.isActive()) chequeUserList.addLast(chequeUser);
            else chequeUserList.addFirst(chequeUser);
        }

        cursor.close();

        return chequeUserList;
    }

}
