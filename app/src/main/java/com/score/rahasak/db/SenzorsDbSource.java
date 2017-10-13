package com.score.rahasak.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.score.rahasak.enums.DeliveryState;
import com.score.rahasak.pojo.Cheque;
import com.score.rahasak.pojo.ChequeUser;

import java.util.ArrayList;

/**
 * Do all database insertions, updated, deletions from here
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SenzorsDbSource {

    private static final String TAG = SenzorsDbSource.class.getName();

    private static Context context;

    /**
     * Init db helper
     *
     * @param context application context
     */
    public SenzorsDbSource(Context context) {
        this.context = context;
    }

    public boolean isExistingUser(String username) {
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

    public boolean isExistingUserWithPhoneNo(String phoneNo) {
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

    public ChequeUser getExistingUserWithPhoneNo(String phoneNo) {
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
            String _userID = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User._ID));
            String _username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_USERNAME));

            // clear
            cursor.close();

            ChequeUser chequeUser = new ChequeUser(_userID, _username);
            chequeUser.setPhone(phoneNo);

            return chequeUser;
        }

        return null;
    }

    public void createSecretUser(ChequeUser chequeUser) {
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
        values.put(SenzorsDbContract.User.COLUMN_NAME_IS_SMS_REQUESTER, chequeUser.isSMSRequester() ? 1 : 0);

        // Insert the new row, if fails throw an error
        // fails means user already exists
        db.insertOrThrow(SenzorsDbContract.User.TABLE_NAME, null, values);
    }

    public void updateSecretUser(String username, String key, String value) {
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

    public void updateUnreadSecretCount(String username, int count) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        db.execSQL("UPDATE " + SenzorsDbContract.User.TABLE_NAME +
                        " SET " + SenzorsDbContract.User.COLUMN_NAME_UNREAD_SECRET_COUNT + " = " + SenzorsDbContract.User.COLUMN_NAME_UNREAD_SECRET_COUNT + " + ? " +
                        " WHERE " + SenzorsDbContract.User.COLUMN_NAME_USERNAME + " = ? ",
                new String[]{String.valueOf(count), username});
    }

    public void resetUnreadSecretCount(String username) {
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

    public void activateSecretUser(String username, boolean isActive) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.User.COLUMN_NAME_IS_ACTIVE, isActive ? 1 : 0);

        // update
        db.update(SenzorsDbContract.User.TABLE_NAME,
                values,
                SenzorsDbContract.User.COLUMN_NAME_USERNAME + " = ?",
                new String[]{username});
    }

    /**
     * Delete user from database,
     *
     * @param
     */
    public void deleteSecretUser(String username) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // delete senz of given user
        db.delete(SenzorsDbContract.User.TABLE_NAME,
                SenzorsDbContract.User.COLUMN_NAME_USERNAME + " = ?",
                new String[]{username});

        // delete all secrets belongs to user
        deleteAllChequesThatBelongToUser(username);
    }

    public ChequeUser getSecretUser(String username) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(SenzorsDbContract.User.TABLE_NAME, // table
                null, // columns
                SenzorsDbContract.User.COLUMN_NAME_USERNAME + " = ?", // constraint
                new String[]{username}, // prams
                null, // order by
                null, // group by
                null); // join

        if (cursor.moveToFirst()) {
            // have matching user
            // so get user data
            // we return id as password since we no storing users password in database
            String _userID = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User._ID));
            String _username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_USERNAME));
            String _phone = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_PHONE));
            String _pubKey = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_PUBKEY));
            String _pubKeyHash = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_PUBKEY_HASH));
            String _image = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_IMAGE));
            String _sessionKey = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_SESSION_KEY));
            int _isActive = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_IS_ACTIVE));
            int _isSmsRequester = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_IS_SMS_REQUESTER));

            // clear
            cursor.close();

            ChequeUser chequeUser = new ChequeUser(_userID, _username);
            chequeUser.setPhone(_phone);
            chequeUser.setPubKey(_pubKey);
            chequeUser.setPubKeyHash(_pubKeyHash);
            chequeUser.setImage(_image);
            chequeUser.setActive(_isActive == 1);
            chequeUser.setSMSRequester(_isSmsRequester == 1);
            chequeUser.setSessionKey(_sessionKey);

            return chequeUser;
        }

        return null;
    }

    public ArrayList<ChequeUser> getSecretUserList() {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(SenzorsDbContract.User.TABLE_NAME, // table
                null, // columns
                null,
                null, // selection
                null, // order by
                null, // group by
                null); // join

        ArrayList<ChequeUser> chequeUserList = new ArrayList<>();

        while (cursor.moveToNext()) {
            String _userID = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User._ID));
            String _username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_USERNAME));
            String _phone = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_PHONE));
            String _pubKey = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_PUBKEY));
            String _pubKeyHash = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_PUBKEY_HASH));
            int _isActive = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_IS_ACTIVE));
            String _image = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_IMAGE));
            int _isSmsRequester = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_IS_SMS_REQUESTER));

            ChequeUser chequeUser = new ChequeUser(_userID, _username);
            chequeUser.setPhone(_phone);
            chequeUser.setPubKey(_pubKey);
            chequeUser.setPubKeyHash(_pubKeyHash);
            chequeUser.setImage(_image);
            chequeUser.setActive(_isActive == 1);
            chequeUser.setSMSRequester(_isSmsRequester == 1);

            chequeUserList.add(chequeUser);
        }

        cursor.close();

        return chequeUserList;
    }

    /**
     * Create Cheque message or images
     *
     * @param cheque
     */
    public void createCheque(Cheque cheque) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_UID, cheque.getUid());
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_USER, cheque.getUser().getUsername());
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_IS_SENDER, cheque.isSender() ? 1 : 0);
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_IS_VIEWED, cheque.isViewed() ? 1 : 0);
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_TIMESTAMP, cheque.getTimestamp());
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_VIEWED_TIMESTAMP, 0);
        values.put(SenzorsDbContract.Cheque.DELIVERY_NAME_STATE, cheque.getDeliveryState().getState());
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_ID, cheque.getCid());
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_AMOUNT, cheque.getAmount());
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_BLOB, cheque.getBlob());

        // insert the new row, if fails throw an error
        db.insertOrThrow(SenzorsDbContract.Cheque.TABLE_NAME, null, values);
    }

    /**
     * Mark message as viewed
     *
     * @param uid unique identifier of message
     */
    public void markChequeViewed(String uid) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_IS_VIEWED, 1);
        long timestamp = System.currentTimeMillis() / 1000;
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_VIEWED_TIMESTAMP, timestamp);

        // update
        db.update(SenzorsDbContract.Cheque.TABLE_NAME,
                values,
                SenzorsDbContract.Cheque.COLUMN_NAME_UID + " = ?",
                new String[]{uid});
    }

    /**
     * Mark message as delivery state
     *
     * @param deliveryState
     * @param uid
     */
    public void updateDeliveryStatus(DeliveryState deliveryState, String uid) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();
        try {
            db.beginTransaction();

            // content values to inset
            ContentValues values = new ContentValues();
            values.put(SenzorsDbContract.Cheque.DELIVERY_NAME_STATE, deliveryState.getState());

            // update
            db.update(SenzorsDbContract.Cheque.TABLE_NAME,
                    values,
                    SenzorsDbContract.Cheque.COLUMN_NAME_UID + " = ?",
                    new String[]{uid});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Get ALl secrets to be display in chat list
     *
     * @return sensor list
     */
    public ArrayList<Cheque> getCheques(boolean isSender) {
        ArrayList<Cheque> cheques = new ArrayList();

        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        String query =
                "SELECT _id, uid, user, is_sender, is_viewed, timestamp, view_timestamp, delivery_state, cid, amount, blob " +
                        "FROM cheque " +
                        "WHERE is_sender = ? " +
                        "ORDER BY _id DESC";
        Cursor cursor = db.rawQuery(query, new String[]{isSender ? "1" : "0"});

        // secret attr
        String uid;
        String username;
        int isViewed;
        Long timestamp;
        Long viewedTimeStamp;
        int deliveryState;

        String cid;
        int amount;
        String date;
        String blob;

        // extract attributes
        while (cursor.moveToNext()) {
            // get secret attributes
            uid = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_UID));
            username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_USER));
            isViewed = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_IS_VIEWED));
            timestamp = cursor.getLong(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_TIMESTAMP));
            viewedTimeStamp = cursor.getLong(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_VIEWED_TIMESTAMP));
            deliveryState = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Cheque.DELIVERY_NAME_STATE));
            cid = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_ID));
            amount = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_AMOUNT));
            blob = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_BLOB));

            // create cheque
            Cheque cheque = new Cheque();
            cheque.setUid(uid);
            cheque.setUser(getSecretUser(username));
            cheque.setViewed(isViewed == 1);
            cheque.setTimestamp(timestamp);
            cheque.setViewedTimeStamp(viewedTimeStamp);
            cheque.setDeliveryState(DeliveryState.valueOfState(deliveryState));
            cheque.setCid(cid);
            cheque.setAmount(amount);
            cheque.setBlob(blob);

            cheques.add(cheque);
        }

        cursor.close();

        return cheques;
    }

    /**
     * Get secrets from give timestamp, used for lazy loading!!!
     *
     * @param chequeUser
     * @param t
     * @return
     */
    public ArrayList<Cheque> getCheques(ChequeUser chequeUser, Long t) {
        ArrayList<Cheque> cheques = new ArrayList();

        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        String query =
                "SELECT _id, uid, user, is_sender, is_viewed, timestamp, view_timestamp, delivery_state, cid, amount, blob " +
                        "FROM cheque " +
                        "WHERE user = ? AND timestamp > ? " +
                        "ORDER BY _id ASC";
        Cursor cursor = db.rawQuery(query, new String[]{chequeUser.getUsername(), t.toString()});

        // secret attr
        String uid;
        String username;
        int isViewed;
        Long timestamp;
        Long viewedTimeStamp;
        int deliveryState;

        String cid;
        int amount;
        String date;
        String blob;

        // extract attributes
        while (cursor.moveToNext()) {
            // get secret attributes
            uid = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_UID));
            username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_USER));
            isViewed = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_IS_VIEWED));
            timestamp = cursor.getLong(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_TIMESTAMP));
            viewedTimeStamp = cursor.getLong(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_VIEWED_TIMESTAMP));
            deliveryState = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Cheque.DELIVERY_NAME_STATE));
            cid = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_ID));
            amount = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_AMOUNT));
            blob = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_BLOB));

            // create cheque
            Cheque cheque = new Cheque();
            cheque.setUid(uid);
            cheque.setUser(new ChequeUser("_ID", username));
            cheque.setViewed(isViewed == 1);
            cheque.setTimestamp(timestamp);
            cheque.setViewedTimeStamp(viewedTimeStamp);
            cheque.setDeliveryState(DeliveryState.valueOfState(deliveryState));
            cheque.setCid(cid);
            cheque.setAmount(amount);
            cheque.setBlob(blob);

            cheques.add(cheque);
        }

        cursor.close();

        return cheques;
    }

    public ArrayList<Cheque> getUnAckCheques() {
        ArrayList<Cheque> cheques = new ArrayList();

        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        String query =
                "SELECT _id, uid, user, is_sender, is_viewed, timestamp, view_timestamp, delivery_state, cid, amount, blob " +
                        "FROM cheque " +
                        "WHERE delivery_state = ? " +
                        "ORDER BY _id ASC";
        Cursor cursor = db.rawQuery(query, new String[]{Integer.toString(DeliveryState.PENDING.getState())});

        // secret attr
        String uid;
        String username;
        int isViewed;
        Long timestamp;
        Long viewedTimeStamp;
        int deliveryState;

        String cid;
        int amount;
        String date;
        String blob;

        // extract attributes
        while (cursor.moveToNext()) {
            // get secret attributes
            uid = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_UID));
            username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_USER));
            isViewed = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_IS_VIEWED));
            timestamp = cursor.getLong(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_TIMESTAMP));
            viewedTimeStamp = cursor.getLong(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_VIEWED_TIMESTAMP));
            deliveryState = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Cheque.DELIVERY_NAME_STATE));
            cid = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_ID));
            amount = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_AMOUNT));
            blob = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_BLOB));

            // create cheque
            Cheque cheque = new Cheque();
            cheque.setUid(uid);
            cheque.setUser(new ChequeUser("_ID", username));
            cheque.setViewed(isViewed == 1);
            cheque.setTimestamp(timestamp);
            cheque.setViewedTimeStamp(viewedTimeStamp);
            cheque.setDeliveryState(DeliveryState.valueOfState(deliveryState));
            cheque.setCid(cid);
            cheque.setAmount(amount);
            cheque.setBlob(blob);

            cheques.add(cheque);
        }

        cursor.close();

        return cheques;
    }

    /**
     * Delete sec from database,
     *
     * @param
     */
    public void deleteCheque(Cheque cheque) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // delete senz of given user
        db.delete(SenzorsDbContract.Cheque.TABLE_NAME,
                SenzorsDbContract.Cheque.COLUMN_NAME_UID + " = ?",
                new String[]{cheque.getUid()});
    }

    public void deleteAllChequesThatBelongToUser(String username) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // delete senz of given user
        db.delete(SenzorsDbContract.Cheque.TABLE_NAME,
                SenzorsDbContract.Cheque.COLUMN_NAME_USER + " = ?",
                new String[]{username});
    }

}
