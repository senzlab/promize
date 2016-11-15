package com.score.chatz.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.score.chatz.pojo.Permission;
import com.score.chatz.pojo.Secret;
import com.score.chatz.pojo.SecretUser;
import com.score.senzc.pojos.User;

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
        Log.d(TAG, "Init: db source");
        this.context = context;
    }

    public boolean isExistingUser(String username) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        Cursor cursor = db.query(SenzorsDbContract.User.TABLE_NAME, // table
                null, // columns
                SenzorsDbContract.User.COLUMN_NAME_USERNAME + "=?", // constraint
                new String[]{username}, // prams
                null, // order by
                null, // group by
                null); // join

        return cursor.moveToFirst();
    }

    public void createSecretUser(SecretUser secretUser) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // create two empty permissions
        String givenPermId = createPermission(new Permission("id", false));
        String recvPermId = createPermission(new Permission("id", true));

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.User.COLUMN_NAME_USERNAME, secretUser.getUsername());
        if (secretUser.getPhone() != null)
            values.put(SenzorsDbContract.User.COLUMN_NAME_PHONE, secretUser.getPhone());
        if (secretUser.getPubKey() != null && secretUser.getPubKey().isEmpty())
            values.put(SenzorsDbContract.User.COLUMN_NAME_PUBKEY, secretUser.getPubKey());
        if (secretUser.getPubKeyHash() != null && secretUser.getPubKeyHash().isEmpty())
            values.put(SenzorsDbContract.User.COLUMN_NAME_PUBKEY_HASH, secretUser.getPubKeyHash());
        if (secretUser.getImage() != null && secretUser.getImage().isEmpty())
            values.put(SenzorsDbContract.User.COLUMN_NAME_IMAGE, secretUser.getImage());
        if (secretUser.getUid() != null)
            values.put(SenzorsDbContract.User.COLUMN_UNIQUE_ID, secretUser.getUid());
        values.put(SenzorsDbContract.User.COLUMN_NAME_IS_ACTIVE, secretUser.isActive() ? 1 : 0);
        values.put(SenzorsDbContract.User.COLUMN_NAME_GIVEN_PERM, givenPermId);
        values.put(SenzorsDbContract.User.COLUMN_NAME_RECV_PERM, recvPermId);

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
        }

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

    public void activateSecretUserFromUid(String uid, boolean isActive) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.User.COLUMN_NAME_IS_ACTIVE, isActive ? 1 : 0);

        // update
        db.update(SenzorsDbContract.User.TABLE_NAME,
                values,
                SenzorsDbContract.User.COLUMN_UNIQUE_ID + " = ?",
                new String[]{uid});
    }

    public SecretUser getSecretUser(String username) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(SenzorsDbContract.User.TABLE_NAME, // table
                null, // columns
                SenzorsDbContract.User.COLUMN_NAME_USERNAME + "=?", // constraint
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
            int _isActive = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_IS_ACTIVE));
            String _image = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_IMAGE));
            String _givenPermId = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_GIVEN_PERM));
            String _recvPermId = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_RECV_PERM));

            Permission givenPerm = getPermission(_givenPermId, true);
            Permission recvPerm = getPermission(_recvPermId, false);

            // clear
            cursor.close();

            SecretUser secretUser = new SecretUser(_userID, _username);
            secretUser.setPhone(_phone);
            secretUser.setPhone(_pubKey);
            secretUser.setPhone(_pubKeyHash);
            secretUser.setImage(_image);
            secretUser.setActive(_isActive == 1);
            secretUser.setGivenPermission(givenPerm);
            secretUser.setRecvPermission(recvPerm);

            return secretUser;
        }

        return null;
    }

    public ArrayList<SecretUser> getSecretUserList() {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(SenzorsDbContract.User.TABLE_NAME, // table
                null, // columns
                null,
                null, // selection
                null, // order by
                null, // group by
                null); // join

        ArrayList<SecretUser> secretUserList = new ArrayList<>();

        while (cursor.moveToNext()) {
            String _userID = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User._ID));
            String _userUid = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_UNIQUE_ID));
            String _username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_USERNAME));
            String _phone = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_PHONE));
            String _pubKey = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_PUBKEY));
            String _pubKeyHash = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_PUBKEY_HASH));
            int _isActive = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_IS_ACTIVE));
            String _image = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_IMAGE));
            String _givenPermId = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_GIVEN_PERM));
            String _recvPermId = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_RECV_PERM));

            // get permission
            Permission givenPerm = getPermission(_givenPermId, true);
            Permission recvPerm = getPermission(_recvPermId, false);

            SecretUser secretUser = new SecretUser(_userID, _username);
            secretUser.setUid(_userUid);
            secretUser.setPhone(_phone);
            secretUser.setPubKey(_pubKey);
            secretUser.setPubKeyHash(_pubKeyHash);
            secretUser.setImage(_image);
            secretUser.setActive(_isActive == 1);
            secretUser.setGivenPermission(givenPerm);
            secretUser.setRecvPermission(recvPerm);

            // Add created User to list
            secretUserList.add(secretUser);

            cursor.moveToNext();
        }

        cursor.close();

        return secretUserList;
    }

    public String createPermission(Permission permission) {
        Log.d(TAG, "Add new permission with isGiven=" + permission.isGiven());
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.Permission.COLUMN_NAME_CAMERA, 0);
        values.put(SenzorsDbContract.Permission.COLUMN_NAME_LOCATION, 0);
        values.put(SenzorsDbContract.Permission.COLUMN_NAME_MIC, 0);
        values.put(SenzorsDbContract.Permission.COLUMN_NAME_IS_GIVEN, permission.isGiven() ? 1 : 0);

        // Insert the new row, if fails throw an error
        long id = db.insertOrThrow(SenzorsDbContract.Permission.TABLE_NAME, null, values);
        return Long.toString(id);
    }

    public void updatePermission(String id, String permName, boolean permVal, boolean isGiven) {
        Log.d(TAG, "Update permission with isGiven=" + isGiven);
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(permName, permVal ? 1 : 0);

        // update
        db.update(SenzorsDbContract.Permission.TABLE_NAME,
                values,
                SenzorsDbContract.Permission._ID + " = ? AND is_given = ?",
                new String[]{id, isGiven ? "1" : "0"});
    }

    public Permission getPermission(String id, boolean isGiven) {
        // get matching user if exists
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();
        Cursor cursor = db.query(SenzorsDbContract.Permission.TABLE_NAME, // table
                null, // columns
                SenzorsDbContract.Permission._ID + "=? AND " + SenzorsDbContract.Permission.COLUMN_NAME_IS_GIVEN + "=?", // constraint
                new String[]{id, isGiven ? "1" : "0"}, // prams
                null, // order by
                null, // group by
                null); // join

        if (cursor.moveToFirst()) {
            boolean _location = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Permission.COLUMN_NAME_LOCATION)) == 1;
            boolean _cam = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Permission.COLUMN_NAME_CAMERA)) == 1;
            boolean _mic = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Permission.COLUMN_NAME_MIC)) == 1;

            // clear
            cursor.close();

            Permission permission = new Permission(id, isGiven);
            permission.setLoc(_location);
            permission.setCam(_cam);
            permission.setMic(_mic);

            return permission;
        }

        return null;
    }

    /**
     * Create Secret message or images
     *
     * @param secret
     */
    public void createSecret(Secret secret) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();
        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.Secret.COLUMN_UNIQUE_ID, secret.getId());
        values.put(SenzorsDbContract.Secret.COLUMN_TIMESTAMP, secret.getTimeStamp());
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_USER, secret.getUser().getUsername());
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_IS_SENDER, secret.isSender() ? 1 : 0);
        values.put(SenzorsDbContract.Secret.COLUMN_BLOB_TYPE, secret.getType());
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_BLOB, secret.getBlob());
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_VIEWED, secret.isViewed() ? 1 : 0);
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_VIEWED_TIMESTAMP, 0);
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_MISSED, secret.isMissed() ? 1 : 0);
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_DELIVERED, 0);
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_DISPATCHED, 0);

        // Insert the new row, if fails throw an error
        db.insertOrThrow(SenzorsDbContract.Secret.TABLE_NAME, null, values);

        insertRecordIntoLatestChat(secret);
    }

    private void insertRecordIntoLatestChat(Secret secret) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.LatestChat.COLUMN_USER, secret.getUser().getUsername());
        values.put(SenzorsDbContract.LatestChat.COLUMN_BLOB, secret.getBlob());
            values.put(SenzorsDbContract.LatestChat.COLUMN_TYPE, secret.getType());
        values.put(SenzorsDbContract.LatestChat.COLUMN_NAME_IS_SENDER, secret.isSender());
        values.put(SenzorsDbContract.LatestChat.COLUMN_TIMESTAMP, secret.getTimeStamp());

        //First update the table
        int rowCount = db.update(SenzorsDbContract.LatestChat.TABLE_NAME,
                values,
                SenzorsDbContract.LatestChat.COLUMN_USER + " =?",
                new String[]{secret.getUser().getUsername()});

        //If not rows were affected!!then insert
        if (rowCount == 0) {
            db.insert(SenzorsDbContract.LatestChat.TABLE_NAME, null, values);
        }
    }

    /**
     * Mark message as viewed
     *
     * @param uid unique identifier of message
     */
    public void markSecretViewed(String uid) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_VIEWED, 1);
        long timestamp = System.currentTimeMillis() / 1000;
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_VIEWED_TIMESTAMP, timestamp);

        // update
        db.update(SenzorsDbContract.Secret.TABLE_NAME,
                values,
                SenzorsDbContract.Secret.COLUMN_UNIQUE_ID + " =?",
                new String[]{uid});
    }

    /**
     * Mark message as delivered
     *
     * @param uid unique identifier of message
     */
    public void markSecretDelivered(String uid) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();
        try {
            db.beginTransaction();

            // content values to inset
            ContentValues values = new ContentValues();
            values.put(SenzorsDbContract.Secret.COLUMN_NAME_DELIVERED, 1);

            // update
            db.update(SenzorsDbContract.Secret.TABLE_NAME,
                    values,
                    SenzorsDbContract.Secret.COLUMN_UNIQUE_ID + " =?",
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
    public ArrayList<Secret> getSecrets(SecretUser secretUser) {
        ArrayList<Secret> secretList = new ArrayList();

        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        String query = "SELECT _id, uid, blob, type, user, is_sender, viewed, view_timestamp, missed, delivered, dispatched, timestamp " +
                "FROM secret WHERE user = ? ORDER BY _id ASC";
        Cursor cursor = db.rawQuery(query, new String[]{secretUser.getUsername()});

        // secret attr
        String _secretId;
        String _secretBlob;
        String _secretBlobType;
        int _secretIsSender;
        int _isViewed;
        int _isMissed;
        int _secretIsDelivered;
        int _secretIsDispatched;
        Long _secretTimestamp;
        Long _secretViewTimestamp;

        // extract attributes
        while (cursor.moveToNext()) {
            // get secret attributes
            _secretId = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_UNIQUE_ID));
            _secretTimestamp = cursor.getLong(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_TIMESTAMP));
            _secretIsSender = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_IS_SENDER));
            _secretBlobType = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_BLOB_TYPE));
            _secretBlob = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_BLOB));
            _isViewed = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_VIEWED));
            _secretViewTimestamp = cursor.getLong(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_VIEWED_TIMESTAMP));
            _isMissed = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_MISSED));
            _secretIsDelivered = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_DELIVERED));
            _secretIsDispatched = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_DISPATCHED));

            // create secret
            Secret secret = new Secret(_secretBlob, _secretBlobType, secretUser, _secretIsSender == 1);
            secret.setId(_secretId);
            secret.setViewed(_isViewed == 1);
            secret.setMissed(_isMissed == 1);
            secret.setDelivered(_secretIsDelivered == 1);
            secret.setDispatched(_secretIsDispatched == 1);
            secret.setTimeStamp(_secretTimestamp);
            secret.setViewedTimeStamp(_secretViewTimestamp);

            // fill secret list
            secretList.add(secret);
        }

        // clean
        cursor.close();

        Log.d(TAG, "GetSecretz: secrets count " + secretList.size());
        return secretList;
    }

    /**
     * Get secrets from give timestamp, used for lazy loading!!!
     *
     * @param secretUser
     * @param timestamp
     * @return
     */
    public ArrayList<Secret> getSecrets(SecretUser secretUser, Long timestamp) {
        ArrayList secretList = new ArrayList();

        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        String query = "SELECT _id, uid, blob, type, user, is_sender, viewed, view_timestamp, missed, delivered, dispatched, timestamp " +
                "FROM secret WHERE user = ? AND timestamp > ? ORDER BY _id ASC";
        Cursor cursor = db.rawQuery(query, new String[]{secretUser.getUsername(), timestamp.toString()});

        // secret attr
        String _secretId;
        String _secretBlob;
        String _secretBlobType;
        String _secretUser;
        int _secretIsSender;
        int _isViewed;
        int _isMissed;
        int _secretIsDelivered;
        int _secretIsDispatched;
        Long _secretTimestamp;
        Long _secretViewTimestamp;

        // extract attributes
        while (cursor.moveToNext()) {
            // get secret attributes
            _secretId = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_UNIQUE_ID));
            _secretTimestamp = cursor.getLong(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_TIMESTAMP));
            _secretUser = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_USER));
            _secretIsSender = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_IS_SENDER));
            _secretBlobType = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_BLOB_TYPE));
            _secretBlob = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_BLOB));
            _isViewed = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_VIEWED));
            _secretViewTimestamp = cursor.getLong(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_VIEWED_TIMESTAMP));
            _isMissed = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_MISSED));
            _secretIsDelivered = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_DELIVERED));
            _secretIsDispatched = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_DISPATCHED));

            // create secret
            Secret secret = new Secret(_secretBlob, _secretBlobType, secretUser, _secretIsSender == 1);
            secret.setId(_secretId);
            secret.setViewed(_isViewed == 1);
            secret.setMissed(_isMissed == 1);
            secret.setDelivered(_secretIsDelivered == 1);
            secret.setDispatched(_secretIsDispatched == 1);
            secret.setTimeStamp(_secretTimestamp);
            secret.setViewedTimeStamp(_secretViewTimestamp);

            // fill secret list
            secretList.add(secret);
        }

        // clean
        cursor.close();

        Log.d(TAG, "GetSecretz: secrets count " + secretList.size());
        return secretList;
    }

    /**
     * Delete sec from database,
     *
     * @param
     */
    public void deleteSecret(Secret secret) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // delete senz of given user
        db.delete(SenzorsDbContract.Secret.TABLE_NAME,
                SenzorsDbContract.Secret.COLUMN_UNIQUE_ID + "=?",
                new String[]{secret.getId()});
    }

    public void deleteAllSecretsExceptLast(String username) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();
        //String sqlDelete = "delete from secret where uid in (select uid from secret where _id not in (select _id from secret where user = '" + username + "' order by _id DESC limit 1) and user = '" + username + "')";

//        String sqlDelete =
//                "delete from secret where " +
//                        "uid in " +
//                        "(select uid from secret where " +
//                        "_id not in(select _id from secret where user = '" + username + "' order by _id DESC limit 1) and " +
//                        "user = '" + username + "' and " +
//                        "missed = 0)";

        String sqlDelete =
                "uid in " +
                        "(select uid from secret where " +
                        "_id not in(select _id from secret where user = '" + username + "' order by _id DESC limit 7) and " +
                        "user = '" + username + "')";

        // TODO refactor/optimize this
        //String sqlDelete = "uid in (select uid from secret where _id not in(select _id from secret where user = '" + username + "' order by _id DESC limit 7) and user = '" + username + "')";
        db.delete(SenzorsDbContract.Secret.TABLE_NAME,
                sqlDelete,
                null);
    }

    public void deleteAllSecretsThatBelongToUser(User user) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // delete senz of given user
        db.delete(SenzorsDbContract.Secret.TABLE_NAME,
                SenzorsDbContract.Secret.COLUMN_NAME_USER + "=?",
                new String[]{user.getUsername()});

        // delete last secret
        db.delete(SenzorsDbContract.LatestChat.TABLE_NAME,
                SenzorsDbContract.Secret.COLUMN_NAME_USER + "=?",
                new String[]{user.getUsername()});
    }


    /**
     * GEt list of the lates chat messages!!!!
     *
     * @return
     */
    public ArrayList<Secret> getLatestChatMessages() {
        ArrayList<Secret> secretList = new ArrayList();

        // TODO JOIN with user to get user image
        //SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        //String query = "SELECT MAX(_id), _id, blob, type, user, is_sender, timestamp FROM secret " +
        //        "GROUP BY user ORDER BY timestamp DESC";

        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        String query = "SELECT * " +
                "FROM latest_chat " +
                "INNER JOIN user " +
                "ON user.username = latest_chat.user GROUP BY user.username ORDER BY timestamp DESC";

        Cursor cursor = db.rawQuery(query, null);

        // secret attr
        String _userID;
        String _secretBlob;
        String _secretBlobType;
        String _secretUser;
        Long _secretTimestamp;
        String _image;
        int _secretIsSender;
        int _isActive;

        // extract attributes
        while (cursor.moveToNext()) {
            // get secret attributes
            _userID = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User._ID));
            _secretBlob = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.LatestChat.COLUMN_BLOB));
            _secretBlobType = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.LatestChat.COLUMN_TYPE));
            _secretUser = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.LatestChat.COLUMN_USER));
            _secretTimestamp = cursor.getLong(cursor.getColumnIndex(SenzorsDbContract.LatestChat.COLUMN_TIMESTAMP));
            _secretIsSender = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_IS_SENDER));

            // get user attributes
            _image = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_IMAGE));
            _isActive = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_IS_ACTIVE));

            SecretUser secretUser = new SecretUser(_userID, _secretUser);
            secretUser.setImage(_image);
            secretUser.setActive(_isActive == 1);

            Secret secret = new Secret(_secretBlob, _secretBlobType, secretUser, _secretIsSender == 1);
            secret.setTimeStamp(_secretTimestamp);
            // fill secret list
            secretList.add(secret);
        }

        // clean
        cursor.close();

        Log.d(TAG, "GetSecretz: secrets count " + secretList.size());
        return secretList;
    }

}
