package com.score.rahasak.utils;

import com.score.rahasak.R;
import com.score.rahasak.enums.NotificationType;
import com.score.rahasak.pojo.SenzNotification;

/**
 * Utility class for create and update notifications
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class NotificationUtils {

    public static SenzNotification getUserNotification(String user) {
        return new SenzNotification(R.drawable.ic_notification, user, "You have been invited to share cheques", user, NotificationType.NEW_USER);
    }

    public static SenzNotification getUserConfirmNotification(String user) {
        return new SenzNotification(R.drawable.ic_notification, user, "Confirmed your request", user, NotificationType.NEW_USER);
    }

    public static SenzNotification getChequeNotification(String title, String message, String user) {
        return new SenzNotification(R.drawable.ic_notification, title, message, user, NotificationType.NEW_SECRET);
    }

    public static SenzNotification getSmsNotification(String contactName, String contactPhone, String rahasakUsername) {
        String msg = "Would you like share cheques?";
        SenzNotification senzNotification = new SenzNotification(R.drawable.ic_notification, contactName, msg, rahasakUsername, NotificationType.SMS_REQUEST);
        senzNotification.setSenderPhone(contactPhone);

        return senzNotification;
    }

}
