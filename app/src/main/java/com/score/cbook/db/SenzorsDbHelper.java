package com.score.cbook.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Perform creating tables here
 *
 * @author erangaeb@gmail.com(eranga herath)
 */
class SenzorsDbHelper extends SQLiteOpenHelper {

    private static final String TAG = SenzorsDbHelper.class.getName();

    // we use singleton database
    private static SenzorsDbHelper senzorsDbHelper;

    // If you change the database schema, you must increment the database version
    private static final int DATABASE_VERSION = 6;
    private static final String DATABASE_NAME = "Cheque.db";

    // data types, keywords and queries
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";

    private static final String SQL_CREATE_USER =
            "CREATE TABLE IF NOT EXISTS " + SenzorsDbContract.User.TABLE_NAME + " (" +
                    SenzorsDbContract.User._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                    SenzorsDbContract.User.COLUMN_NAME_USERNAME + TEXT_TYPE + " UNIQUE NOT NULL" + "," +
                    SenzorsDbContract.User.COLUMN_NAME_IS_SMS_REQUESTER + INT_TYPE + "," +
                    SenzorsDbContract.User.COLUMN_NAME_SESSION_KEY + TEXT_TYPE + ", " +
                    SenzorsDbContract.User.COLUMN_NAME_PHONE + TEXT_TYPE + "," +
                    SenzorsDbContract.User.COLUMN_NAME_PUBKEY + TEXT_TYPE + "," +
                    SenzorsDbContract.User.COLUMN_NAME_PUBKEY_HASH + TEXT_TYPE + "," +
                    SenzorsDbContract.User.COLUMN_NAME_IS_ACTIVE + INT_TYPE + "," +
                    SenzorsDbContract.User.COLUMN_NAME_IS_ADMIN + INT_TYPE + "," +
                    SenzorsDbContract.User.COLUMN_NAME_IMAGE + TEXT_TYPE + "," +
                    SenzorsDbContract.User.COLUMN_NAME_UNREAD_CHEQUE_COUNT + INT_TYPE + " DEFAULT 0" + "," +
                    SenzorsDbContract.User.COLUMN_NAME_UNREAD_SECRET_COUNT + INT_TYPE + " DEFAULT 0" +
                    " )";

    private static final String SQL_CREATE_CHEQUE =
            "CREATE TABLE IF NOT EXISTS " + SenzorsDbContract.Cheque.TABLE_NAME + " (" +
                    SenzorsDbContract.Cheque._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + ", " +
                    SenzorsDbContract.Cheque.COLUMN_NAME_UID + TEXT_TYPE + " UNIQUE NOT NULL" + ", " +
                    SenzorsDbContract.Cheque.COLUMN_NAME_TIMESTAMP + INT_TYPE + ", " +
                    SenzorsDbContract.Cheque.COLUMN_NAME_USER + TEXT_TYPE + ", " +
                    SenzorsDbContract.Cheque.COLUMN_NAME_MY_CHEQUE + INT_TYPE + ", " +
                    SenzorsDbContract.Cheque.COLUMN_NAME_VIEWED + INT_TYPE + ", " +
                    SenzorsDbContract.Cheque.COLUMN_NAME_VIEWED_TIMESTAMP + INT_TYPE + ", " +
                    SenzorsDbContract.Cheque.COLUMN_NAME_DELIVERY_STATE + INT_TYPE + ", " +
                    SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_STATE + INT_TYPE + ", " +
                    SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_ID + TEXT_TYPE + ", " +
                    SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_AMOUNT + INT_TYPE + ", " +
                    SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_DATE + TEXT_TYPE + ", " +
                    SenzorsDbContract.Cheque.COLUMN_NAME_CHEQUE_BLOB + TEXT_TYPE +
                    " )";

    private static final String SQL_CREATE_SECRET =
            "CREATE TABLE IF NOT EXISTS " + SenzorsDbContract.Secret.TABLE_NAME + " (" +
                    SenzorsDbContract.Secret._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + ", " +
                    SenzorsDbContract.Secret.COLUMN_UNIQUE_ID + TEXT_TYPE + " UNIQUE NOT NULL" + ", " +
                    SenzorsDbContract.Secret.COLUMN_TIMESTAMP + INT_TYPE + ", " +
                    SenzorsDbContract.Secret.COLUMN_NAME_USER + TEXT_TYPE + ", " +
                    SenzorsDbContract.Secret.COLUMN_NAME_MY_SECRET + INT_TYPE + ", " +
                    SenzorsDbContract.Secret.COLUMN_BLOB_TYPE + INT_TYPE + ", " +
                    SenzorsDbContract.Secret.COLUMN_NAME_BLOB + TEXT_TYPE + ", " +
                    SenzorsDbContract.Secret.COLUMN_NAME_VIEWED + INT_TYPE + ", " +
                    SenzorsDbContract.Secret.COLUMN_NAME_VIEWED_TIMESTAMP + INT_TYPE + ", " +
                    SenzorsDbContract.Secret.COLUMN_NAME_MISSED + INT_TYPE + ", " +
                    SenzorsDbContract.Secret.COLUMN_NAME_IN_ORDER + INT_TYPE + ", " +
                    SenzorsDbContract.Secret.DELIVERY_STATE + INT_TYPE +
                    " )";

    /**
     * Init context
     * Init database
     *
     * @param context application context
     */
    private SenzorsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * We are reusing one database instance in all over the app for better memory usage
     *
     * @param context application context
     * @return db helper instance
     */
    synchronized static SenzorsDbHelper getInstance(Context context) {
        if (senzorsDbHelper == null) {
            senzorsDbHelper = new SenzorsDbHelper(context.getApplicationContext());
        }
        return (senzorsDbHelper);
    }

    /**
     * {@inheritDoc}
     */
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "OnCreate: creating db helper, db version - " + DATABASE_VERSION);
        Log.d(TAG, SQL_CREATE_USER);
        Log.d(TAG, SQL_CREATE_CHEQUE);
        Log.d(TAG, SQL_CREATE_SECRET);

        db.execSQL(SQL_CREATE_USER);
        db.execSQL(SQL_CREATE_CHEQUE);
        db.execSQL(SQL_CREATE_SECRET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);

        // enable foreign key constraint here
        Log.d(TAG, "OnConfigure: Enable foreign key constraint");
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "OnUpgrade: updating db helper, db version - " + DATABASE_VERSION);

        db.execSQL("DROP TABLE USER;");
        db.execSQL("DROP TABLE CHEQUE;");
        db.execSQL("DROP TABLE SECRET;");
        onCreate(db);
    }

    /**
     * {@inheritDoc}
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
