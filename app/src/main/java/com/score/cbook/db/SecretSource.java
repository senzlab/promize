package com.score.cbook.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.score.cbook.enums.BlobType;
import com.score.cbook.enums.DeliveryState;
import com.score.cbook.pojo.Secret;
import com.score.cbook.util.TimeUtil;

import java.util.ArrayList;

public class SecretSource {

    public static void createSecret(Context context, Secret secret) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();
        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.Secret.COLUMN_UNIQUE_ID, secret.getId());
        values.put(SenzorsDbContract.Secret.COLUMN_TIMESTAMP, secret.getTimeStamp());
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_USER, secret.getUser().getUsername());
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_MY_SECRET, secret.isMySecret() ? 1 : 0);
        values.put(SenzorsDbContract.Secret.COLUMN_BLOB_TYPE, secret.getBlobType().getType());
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_BLOB, secret.getBlob());
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_VIEWED, secret.isViewed() ? 1 : 0);
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_VIEWED_TIMESTAMP, 0);
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_MISSED, secret.isMissed() ? 1 : 0);
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_IN_ORDER, secret.isInOrder() ? 1 : 0);
        values.put(SenzorsDbContract.Secret.DELIVERY_STATE, secret.getDeliveryState().getState());

        // update previous secret in order state
        setSecretInOrder(context, secret);

        // insert the new row, if fails throw an error
        db.insertOrThrow(SenzorsDbContract.Secret.TABLE_NAME, null, values);
    }

    public static void markSecretViewed(Context context, String uid) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_VIEWED, 1);
        long timestamp = System.currentTimeMillis() / 1000;
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_VIEWED_TIMESTAMP, timestamp);

        // update
        db.update(SenzorsDbContract.Secret.TABLE_NAME,
                values,
                SenzorsDbContract.Secret.COLUMN_UNIQUE_ID + " = ?",
                new String[]{uid});
    }

    public static void updateSecretDeliveryState(Context context, String uid, DeliveryState deliveryState) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();
        try {
            db.beginTransaction();

            // content values to inset
            ContentValues values = new ContentValues();
            values.put(SenzorsDbContract.Secret.DELIVERY_STATE, deliveryState.getState());

            // update
            db.update(SenzorsDbContract.Secret.TABLE_NAME,
                    values,
                    SenzorsDbContract.Secret.COLUMN_UNIQUE_ID + " = ?",
                    new String[]{uid});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private static void setSecretInOrder(Context context, Secret secret) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();
        try {
            db.beginTransaction();

            Cursor cursor = db.query(SenzorsDbContract.Secret.TABLE_NAME, // table
                    null, // columns
                    null,
                    null, // selection
                    null, // order by
                    null, // group by
                    null); // join
            if (cursor.moveToLast()) {
                String uid = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_UNIQUE_ID));
                long time = cursor.getLong(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_TIMESTAMP));
                boolean mySecret = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_MY_SECRET)) == 1;
                if ((secret.isMySecret() == mySecret) && TimeUtil.isInOrder(time, secret.getTimeStamp())) {
                    // secret is inline, viewed true
                    ContentValues values = new ContentValues();
                    values.put(SenzorsDbContract.Secret.COLUMN_NAME_IN_ORDER, 1);

                    // update
                    db.update(SenzorsDbContract.Secret.TABLE_NAME,
                            values,
                            SenzorsDbContract.Secret.COLUMN_UNIQUE_ID + " = ?",
                            new String[]{uid});
                }
            }

            cursor.close();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public static ArrayList<Secret> getSecretsOfUser(Context context, String username) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        String query =
                "SELECT _id, " +
                        "uid, " +
                        "blob, " +
                        "blob_type, " +
                        "user, " +
                        "my_secret, " +
                        "viewed, " +
                        "view_timestamp, " +
                        "missed, " +
                        "timestamp, " +
                        "in_order, " +
                        "delivery_state " +
                        "FROM secret " +
                        "WHERE user = ? " +
                        "ORDER BY _id ASC";
        Cursor cursor = db.rawQuery(query, new String[]{username});
        return getSecretsFromCursor(context, cursor);
    }

    public static ArrayList<Secret> getSecretsOfUserByTime(Context context, String username, Long timestamp) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        String query =
                "SELECT _id, " +
                        "uid, " +
                        "blob, " +
                        "blob_type, " +
                        "user, " +
                        "my_secret, " +
                        "viewed, " +
                        "view_timestamp, " +
                        "missed, " +
                        "timestamp, " +
                        "in_order, " +
                        "delivery_state " +
                        "FROM secret " +
                        "WHERE user = ? AND timestamp > ? " +
                        "ORDER BY _id ASC";
        Cursor cursor = db.rawQuery(query, new String[]{username, timestamp.toString()});
        return getSecretsFromCursor(context, cursor);
    }

    public static ArrayList<Secret> getPendingDeliverySecrets(Context context) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        String query =
                "SELECT _id, " +
                        "uid, " +
                        "blob, " +
                        "blob_type, " +
                        "user, " +
                        "my_secret, " +
                        "viewed, " +
                        "view_timestamp, " +
                        "missed, " +
                        "timestamp, " +
                        "in_order, " +
                        "delivery_state " +
                        "FROM secret " +
                        "WHERE delivery_state = ? " +
                        "ORDER BY _id ASC";
        Cursor cursor = db.rawQuery(query, new String[]{Integer.toString(DeliveryState.PENDING.getState())});
        return getSecretsFromCursor(context, cursor);
    }

    public static ArrayList<Secret> getRecentSecrets(Context context) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        String query =
                "SELECT MAX(secret._id), " +
                        "secret._id, " +
                        "secret.uid, " +
                        "secret.blob, " +
                        "secret.blob_type, " +
                        "secret.user, " +
                        "secret.my_secret, " +
                        "secret.viewed, " +
                        "secret.view_timestamp, " +
                        "secret.missed, " +
                        "secret.timestamp, " +
                        "secret.in_order, " +
                        "secret.delivery_state " +
                        "FROM secret " +
                        "INNER JOIN user ON user.username = secret.user " +
                        "WHERE user.is_admin = ? " +
                        "GROUP BY user.username " +
                        "ORDER BY timestamp DESC";
        Cursor cursor = db.rawQuery(query, new String[]{"0"});
        return getSecretsFromCursor(context, cursor);
    }

    public static void deleteSecret(Context context, Secret secret) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // delete senz of given user
        db.delete(SenzorsDbContract.Secret.TABLE_NAME,
                SenzorsDbContract.Secret.COLUMN_UNIQUE_ID + " = ?",
                new String[]{secret.getId()});
    }

    public static void deleteSecretsOfUserExceptLast(Context context, String username) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();
        String sqlDelete =
                "uid in " +
                        "(select uid from secret where " +
                        "_id not in(select _id from secret where user = '" + username + "' order by _id DESC limit 1) and " +
                        "user = '" + username + "')";
        db.delete(SenzorsDbContract.Secret.TABLE_NAME, sqlDelete, null);
    }

    public static void deleteSecretsOfUser(Context context, String username) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // delete senz of given user
        db.delete(SenzorsDbContract.Secret.TABLE_NAME,
                SenzorsDbContract.Secret.COLUMN_NAME_USER + " = ?",
                new String[]{username});
    }

    private static ArrayList<Secret> getSecretsFromCursor(Context context, Cursor cursor) {
        ArrayList<Secret> secrets = new ArrayList<>();

        // secret attr
        String id;
        String username;
        String blob;
        int blobType;
        int mySecret;
        int isViewed;
        int isMissed;
        int isInOrder;
        Long timestamp;
        Long viewTimestamp;
        int deliveryState;

        // extract attributes
        while (cursor.moveToNext()) {
            // get secret attributes
            id = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_UNIQUE_ID));
            username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_USER));
            timestamp = cursor.getLong(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_TIMESTAMP));
            mySecret = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_MY_SECRET));
            blobType = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_BLOB_TYPE));
            blob = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_BLOB));
            isViewed = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_VIEWED));
            viewTimestamp = cursor.getLong(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_VIEWED_TIMESTAMP));
            isMissed = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_MISSED));
            isInOrder = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_IN_ORDER));
            deliveryState = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Secret.DELIVERY_STATE));

            // create secret
            Secret secret = new Secret();
            secret.setId(id);
            secret.setBlob(blob);
            secret.setBlobType(BlobType.valueOfType(blobType));
            secret.setUser(UserSource.getUser(context, username));
            secret.setMySecret(mySecret == 1);
            secret.setViewed(isViewed == 1);
            secret.setMissed(isMissed == 1);
            secret.setInOrder(isInOrder == 1);
            secret.setTimeStamp(timestamp);
            secret.setViewedTimeStamp(viewTimestamp);
            secret.setDeliveryState(DeliveryState.valueOfState(deliveryState));

            secrets.add(secret);
        }

        cursor.close();

        return secrets;
    }

}
