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
        return new SenzNotification(R.drawable.notification_icon, user, "You have been invited to share secrets", user, NotificationType.NEW_PERMISSION);
    }

    public static SenzNotification getUserConfirmNotification(String user) {
        return new SenzNotification(R.drawable.notification_icon, user, "Confirmed your secret request", user, NotificationType.NEW_PERMISSION);
    }

    public static SenzNotification getChequeNotification(String title, String message, String user) {
        return new SenzNotification(R.drawable.notification_icon, title, message, user, NotificationType.NEW_SECRET);
    }

    public static SenzNotification getSmsNotification(String contactName, String contactPhone, String rahasakUsername) {
        String msg = "Would you like share secrets?";

        SenzNotification senzNotification = new SenzNotification(R.drawable.notification_icon, contactName, msg, rahasakUsername, NotificationType.SMS_REQUEST);
        senzNotification.setSenderPhone(contactPhone);

        return senzNotification;
    }

}
