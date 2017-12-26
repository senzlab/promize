package com.score.cbook.utils;

import com.score.cbook.R;
import com.score.cbook.enums.NotificationType;
import com.score.cbook.pojo.SenzNotification;

/**
 * Utility class for create and update notifications
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class NotificationUtils {

    public static SenzNotification getUserNotification(String user) {
        return new SenzNotification(R.drawable.ic_notification, user, "You have been added as a cheque book customer", user, NotificationType.NEW_USER);
    }

    public static SenzNotification getUserConfirmNotification(String user) {
        return new SenzNotification(R.drawable.ic_notification, user, "Confirmed your request", user, NotificationType.NEW_USER);
    }

    public static SenzNotification getChequeNotification(String title, String message, String user) {
        return new SenzNotification(R.drawable.ic_notification, title, message, user, NotificationType.NEW_CHEQUE);
    }

    public static SenzNotification getSecretNotification(String title, String user, String message) {
        return new SenzNotification(R.drawable.ic_notification, title, message, user, NotificationType.NEW_SECRET);
    }

    public static SenzNotification getSmsNotification(String contactName, String contactPhone, String rahasakUsername) {
        String msg = "Would you to add " + contactName + " as cheque book customer?";
        SenzNotification senzNotification = new SenzNotification(R.drawable.ic_notification, contactName, msg, rahasakUsername, NotificationType.SMS_REQUEST);
        senzNotification.setSenderPhone(contactPhone);

        return senzNotification;
    }

}
