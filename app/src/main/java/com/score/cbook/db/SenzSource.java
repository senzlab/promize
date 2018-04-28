package com.score.cbook.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class SenzSource {
    public static void enqueue(Context context, String uid, String msg) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();
        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.Senz.COLUMN_UNIQUE_ID, uid);
        values.put(SenzorsDbContract.Senz.COLUMN_MSG, msg);

        // insert the new row, if fails throw an error
        db.insertOrThrow(SenzorsDbContract.Senz.TABLE_NAME, null, values);
    }

    public static void dequeue(Context context, String uid) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // delete senz of given user
        db.delete(SenzorsDbContract.Senz.TABLE_NAME,
                SenzorsDbContract.Senz.COLUMN_UNIQUE_ID + " = ?",
                new String[]{uid});
    }

    public static ArrayList<String> all(Context context) {
        // get all senzes
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(SenzorsDbContract.Senz.TABLE_NAME, // table
                null,
                null,
                null,
                null,
                null,
                null);

        ArrayList<String> senzes = new ArrayList<>();
        while (cursor.moveToNext()) {
            String msg = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Senz.COLUMN_MSG));
            senzes.add(msg);
        }

        cursor.close();
        return senzes;
    }
}
