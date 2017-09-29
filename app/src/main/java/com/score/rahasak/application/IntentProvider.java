package com.score.rahasak.application;

import android.content.IntentFilter;
import android.util.Log;

import com.score.rahasak.enums.IntentType;
import com.score.rahasak.exceptions.InvalidIntentType;

/**
 * This class is responsible to distribute specific or general intents.
 * Please use this wrapper to send out intents inside the app
 */
public class IntentProvider {

    private static final String TAG = IntentProvider.class.getName();

    // intent actions
    public static final String ACTION_SENZ = "com.score.rahasak.SENZ";
    public static final String ACTION_TIMEOUT = "com.score.rahasak.TIMEOUT";
    public static final String ACTION_SMS_REQUEST_ACCEPT = "com.score.rahasak.SMS_REQUEST_ACCEPT";
    public static final String ACTION_SMS_REQUEST_REJECT = "com.score.rahasak.SMS_REQUEST_REJECT";
    public static final String ACTION_SMS_REQUEST_CONFIRM = "com.score.rahasak.SMS_REQUEST_CONFIRM";
    public static final String ACTION_RESTART = "com.score.rahasak.RESTART";
    public static final String ACTION_CONNECTED = "com.score.rahasak.CONNECTED";
    public static final String ACTION_PHONE_STATE = "android.intent.action.PHONE_STATE";
    public static final String ACTION_START_CALL_SERVICE = "com.score.rahasak.START_CALL_SERVICE";
    public static final String ACTION_STOP_CALL_SERVICE = "com.score.rahasak.STOP_CALL_SERVICE";

    /**
     * Return the intent filter for the intent_type.
     *
     * @param type intent type
     * @return
     */
    public static IntentFilter getIntentFilter(IntentType type) {
        try {
            return new IntentFilter(getIntentAction(type));
        } catch (InvalidIntentType ex) {
            Log.e(TAG, "No such intent, " + ex);
        }

        return null;
    }

    /**
     * Intent string generator
     * Get intents from this method, to centralize where intents are generated from for easier customization in the future.
     *
     * @param intentType intent type
     * @return
     */
    public static String getIntentAction(IntentType intentType) throws InvalidIntentType {
        switch (intentType) {
            case SENZ:
                return ACTION_SENZ;
            case TIMEOUT:
                return ACTION_TIMEOUT;
            case SMS_REQUEST_ACCEPT:
                return ACTION_SMS_REQUEST_ACCEPT;
            case SMS_REQUEST_REJECT:
                return ACTION_SMS_REQUEST_REJECT;
            case SMS_REQUEST_CONFIRM:
                return ACTION_SMS_REQUEST_CONFIRM;
            case CONNECTED:
                return ACTION_CONNECTED;
            case PHONE_STATE:
                return ACTION_PHONE_STATE;
            case START_CALL_SERVICE:
                return ACTION_START_CALL_SERVICE;
            case STOP_CALL_SERVICE:
                return ACTION_STOP_CALL_SERVICE;
            default:
                throw new InvalidIntentType();
        }
    }

}
