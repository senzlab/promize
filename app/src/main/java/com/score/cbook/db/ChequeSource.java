package com.score.cbook.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.score.cbook.enums.ChequeState;
import com.score.cbook.enums.DeliveryState;
import com.score.cbook.pojo.Cheque;

import java.util.ArrayList;

public class ChequeSource {

    public static void createCheque(Context context, Cheque cheque) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_UID, cheque.getUid());
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_USER, cheque.getUser().getUsername());
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_MY_CHEQUE, cheque.isMyCheque() ? 1 : 0);
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_VIEWED, cheque.isViewed() ? 1 : 0);
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_TIMESTAMP, cheque.getTimestamp());
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_VIEWED_TIMESTAMP, 0);
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_DELIVERY_STATE, cheque.getDeliveryState().getState());
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_STATE, cheque.getChequeState().getState());
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_ID, cheque.getCid());
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_AMOUNT, cheque.getAmount());
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_DATE, cheque.getDate());
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_BLOB, cheque.getBlob());

        // insert the new row, if fails throw an error
        db.insertOrThrow(SenzorsDbContract.Cheque.TABLE_NAME, null, values);
    }

    public static void markChequeViewed(Context context, String uid) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_VIEWED, 1);
        long timestamp = System.currentTimeMillis() / 1000;
        values.put(SenzorsDbContract.Cheque.COLUMN_NAME_VIEWED_TIMESTAMP, timestamp);

        // update
        db.update(SenzorsDbContract.Cheque.TABLE_NAME,
                values,
                SenzorsDbContract.Cheque.COLUMN_NAME_UID + " = ?",
                new String[]{uid});
    }

    public static void updateChequeDeliveryState(Context context, String uid, DeliveryState deliveryState) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();
        try {
            db.beginTransaction();

            // content values to inset
            ContentValues values = new ContentValues();
            values.put(SenzorsDbContract.Cheque.COLUMN_NAME_DELIVERY_STATE, deliveryState.getState());

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

    public static void updateChequeState(Context context, String uid, ChequeState chequeState) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();
        try {
            db.beginTransaction();

            // content values to inset
            ContentValues values = new ContentValues();
            values.put(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_STATE, chequeState.getState());

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

    public static ArrayList<Cheque> getCheques(Context context, boolean myCheques) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        String query =
                "SELECT _id, " +
                        "uid, " +
                        "user, " +
                        "my_cheque, " +
                        "viewed, " +
                        "timestamp, " +
                        "view_timestamp, " +
                        "delivery_state, " +
                        "cheque_state, " +
                        "cid, " +
                        "amount, " +
                        "date, " +
                        "blob " +
                        "FROM cheque " +
                        "WHERE my_cheque = ? " +
                        "ORDER BY _id DESC";
        Cursor cursor = db.rawQuery(query, new String[]{myCheques ? "1" : "0"});
        return getChequesFromCursor(context, cursor);
    }

    public static ArrayList<Cheque> getChequesOfUserByTime(Context context, String username, Long t) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        String query =
                "SELECT _id, " +
                        "uid, " +
                        "user, " +
                        "my_cheque, " +
                        "viewed, " +
                        "timestamp, " +
                        "view_timestamp, " +
                        "delivery_state, " +
                        "cheque_state, " +
                        "cid, " +
                        "amount, " +
                        "date, " +
                        "blob " +
                        "FROM cheque " +
                        "WHERE user = ? AND timestamp > ? " +
                        "ORDER BY _id ASC";
        Cursor cursor = db.rawQuery(query, new String[]{username, t.toString()});
        return getChequesFromCursor(context, cursor);
    }

    public static ArrayList<Cheque> getPendingDeliveryCheques(Context context) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        String query =
                "SELECT _id, " +
                        "uid, " +
                        "user, " +
                        "my_cheque, " +
                        "viewed, " +
                        "timestamp, " +
                        "view_timestamp, " +
                        "delivery_state, " +
                        "cheque_state, " +
                        "cid, " +
                        "amount, " +
                        "date, " +
                        "blob " +
                        "FROM cheque " +
                        "WHERE delivery_state = ? " +
                        "ORDER BY _id ASC";
        Cursor cursor = db.rawQuery(query, new String[]{Integer.toString(DeliveryState.PENDING.getState())});
        return getChequesFromCursor(context, cursor);
    }

    public static void deleteCheque(Context context, Cheque cheque) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // delete senz of given user
        db.delete(SenzorsDbContract.Cheque.TABLE_NAME,
                SenzorsDbContract.Cheque.COLUMN_NAME_UID + " = ?",
                new String[]{cheque.getUid()});
    }

    public static void deleteChequesOfUser(Context context, String username) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // delete senz of given user
        db.delete(SenzorsDbContract.Cheque.TABLE_NAME,
                SenzorsDbContract.Cheque.COLUMN_NAME_USER + " = ?",
                new String[]{username});
    }

    private static ArrayList<Cheque> getChequesFromCursor(Context context, Cursor cursor) {
        ArrayList<Cheque> cheques = new ArrayList<>();

        // cheque attr
        String uid;
        String username;
        int isMyCheque;
        int isViewed;
        Long timestamp;
        Long viewedTimeStamp;
        int deliveryState;
        int chequeState;
        String cid;
        int amount;
        String date;
        String blob;

        // extract attributes
        while (cursor.moveToNext()) {
            // get secret attributes
            uid = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_UID));
            username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_USER));
            isMyCheque = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_MY_CHEQUE));
            isViewed = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_VIEWED));
            timestamp = cursor.getLong(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_TIMESTAMP));
            viewedTimeStamp = cursor.getLong(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_VIEWED_TIMESTAMP));
            deliveryState = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_DELIVERY_STATE));
            chequeState = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_STATE));
            cid = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_ID));
            amount = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_AMOUNT));
            date = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_DATE));
            blob = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_BLOB));

            // create cheque
            Cheque cheque = new Cheque();
            cheque.setUid(uid);
            cheque.setUser(UserSource.getUser(context, username));
            cheque.setMyCheque(isMyCheque == 1);
            cheque.setViewed(isViewed == 1);
            cheque.setTimestamp(timestamp);
            cheque.setViewedTimeStamp(viewedTimeStamp);
            cheque.setDeliveryState(DeliveryState.valueOfState(deliveryState));
            cheque.setChequeState(ChequeState.valueOfState(chequeState));
            cheque.setCid(cid);
            cheque.setAmount(amount);
            cheque.setDate(date);
            cheque.setBlob(blob);

            cheques.add(cheque);
        }

        cursor.close();

        return cheques;
    }

}
