package com.score.cbook.db;

import android.provider.BaseColumns;

/**
 * Keep database table attributes
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
class SenzorsDbContract {

    public SenzorsDbContract() {
    }

    /* Inner class that defines the user table contents */
    static abstract class User implements BaseColumns {
        static final String TABLE_NAME = "user";
        static final String COLUMN_NAME_USERNAME = "username";
        static final String COLUMN_NAME_IS_SMS_REQUESTER = "is_sms_requester";
        static final String COLUMN_NAME_SESSION_KEY = "session_key";
        static final String COLUMN_NAME_PHONE = "phone";
        static final String COLUMN_NAME_PUBKEY = "pubkey";
        static final String COLUMN_NAME_PUBKEY_HASH = "pubkey_hash";
        static final String COLUMN_NAME_IS_ACTIVE = "is_active";
        static final String COLUMN_NAME_IMAGE = "image";
        static final String COLUMN_NAME_UNREAD_CHEQUE_COUNT = "unread_cheque_count";
        static final String COLUMN_NAME_UNREAD_SECRET_COUNT = "unread_secret_count";
    }

    /* Inner class that defines secret table */
    static abstract class Cheque implements BaseColumns {
        static final String TABLE_NAME = "cheque";
        static final String COLUMN_NAME_UID = "uid";
        static final String COLUMN_NAME_USER = "user";
        static final String COLUMN_NAME_MY_CHEQUE = "my_cheque";
        static final String COLUMN_NAME_VIEWED = "viewed";
        static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        static final String COLUMN_NAME_VIEWED_TIMESTAMP = "view_timestamp";
        static final String COLUMN_NAME_DELIVERY_STATE = "delivery_state";

        // cheque info
        static final String COLUMN_NAME_CHEQUE_ID = "cid";
        static final String COLUMN_NAME_CHEQUE_STATE = "state";
        static final String COLUMN_NAME_CHEQUE_AMOUNT = "amount";
        static final String COLUMN_NAME_CHEQUE_DATE = "date";
        static final String COLUMN_NAME_CHEQUE_BLOB = "blob";
    }

    /* Inner class that defines secret table */
    static abstract class Secret implements BaseColumns {
        static final String TABLE_NAME = "secret";
        static final String COLUMN_UNIQUE_ID = "uid";
        static final String COLUMN_TIMESTAMP = "timestamp";
        static final String COLUMN_NAME_USER = "user";
        static final String COLUMN_NAME_MY_SECRET = "my_secret";
        static final String COLUMN_NAME_BLOB = "blob";
        static final String COLUMN_BLOB_TYPE = "blob_type";
        static final String COLUMN_NAME_VIEWED = "viewed";
        static final String COLUMN_NAME_VIEWED_TIMESTAMP = "view_timestamp";
        static final String COLUMN_NAME_MISSED = "missed";
        static final String COLUMN_NAME_IN_ORDER = "in_order";
        static final String DELIVERY_STATE = "delivery_state";
    }

}
