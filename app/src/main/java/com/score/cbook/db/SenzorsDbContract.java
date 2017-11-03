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

    /* Inner class that defines secret table */
    static abstract class Cheque implements BaseColumns {
        static final String TABLE_NAME = "cheque";
        static final String COLUMN_NAME_UID = "uid";
        static final String COLUMN_NAME_USER = "user";

        // is_sender = true -> friends cheque
        // is_sender = false -> my cheque
        static final String COLUMN_NAME_IS_SENDER = "is_sender";

        static final String COLUMN_NAME_IS_VIEWED = "is_viewed";
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
        static final String COLUMN_NAME_UNREAD_SECRET_COUNT = "unread_secret_count";
    }

}
