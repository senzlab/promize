package com.score.chatz.remote;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.score.chatz.R;
import com.score.chatz.application.IntentProvider;
import com.score.chatz.application.SenzApplication;
import com.score.chatz.enums.NotificationType;
import com.score.chatz.pojo.SenzNotification;
import com.score.chatz.receivers.NotificationActionReceiver;
import com.score.chatz.ui.ChatActivity;
import com.score.chatz.ui.HomeActivity;
import com.score.chatz.utils.NotificationUtils;

public class SenzNotificationManager {

    private static final String TAG = SenzNotificationManager.class.getName();

    private Context context;
    private static SenzNotificationManager instance;

    private SenzNotificationManager(Context context) {
        this.context = context;
    }

    public static SenzNotificationManager getInstance(Context context) {
        if (instance == null) {
            instance = new SenzNotificationManager(context);
        }

        return instance;
    }

    /**
     * Display notification from here
     *
     * @param senzNotification
     */
    public void showNotification(SenzNotification senzNotification) {
        if (senzNotification.getNotificationType() == NotificationType.NEW_PERMISSION) {
            Notification notification = getNotification(senzNotification);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NotificationUtils.MESSAGE_NOTIFICATION_ID, notification);
        } else if (senzNotification.getNotificationType() == NotificationType.NEW_SECRET) {
            if (SenzApplication.isOnChat() && SenzApplication.getUserOnChat().equalsIgnoreCase(senzNotification.getSender())) {
                // message for currently chatting user
                Log.d(TAG, "Message for chatting user " + senzNotification.getSender());
            } else {
                // display other types of notification when user not on chat
                Notification notification = getNotification(senzNotification);
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NotificationUtils.MESSAGE_NOTIFICATION_ID, notification);
            }
        }else if (senzNotification.getNotificationType() == NotificationType.NEW_SMS_ADD_FRIEND) {
                Notification notification = getSmsNotification(senzNotification);
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NotificationUtils.getNotificationIdForSMS(), notification);
        }
    }

    /**
     * Get notification to create/ update
     * We need to create or update notification in different scenarios
     *
     * @return notification
     */
    private Notification getNotification(SenzNotification senzNotification) {

        // set up pending intent
        Intent intent;
        if (senzNotification.getNotificationType() == NotificationType.NEW_SECRET) {
            intent = new Intent(context, ChatActivity.class);
            intent.putExtra("SENDER", senzNotification.getSender());
        } else {
            intent = new Intent(context, HomeActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(senzNotification.getTitle())
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentText(senzNotification.getMessage())
                .setSmallIcon(senzNotification.getIcon())
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent);

        Uri sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification);
        builder.setSound(sound);

        return builder.build();
    }

    private Notification getSmsNotification(SenzNotification senzNotification) {

        // 1. Sending intent to Custom Receiver is aways with an empty bundle, android reuse itents issue
        // Setup pending intent for accept action
        /*Intent acceptIntent = new Intent(context, NotificationActionReceiver.class);
        acceptIntent.putExtra("NOTIFICATION_ACCEPT", "NOTIFICATION_ACCEPT");
        acceptIntent.putExtra("NOTIFICATION_ID", NotificationUtils.getNotificationId());
        PendingIntent smsAcceptIntent = PendingIntent.getBroadcast(context, 0, acceptIntent, PendingIntent.FLAG_CANCEL_CURRENT);*/

        //2. Sending itent striaght to service did't fire when app was killed, thus not reliable
        /*Intent acceptIntent = IntentProvider.getAddUserIntent();
        acceptIntent.putExtra("USERNAME_TO_ADD", senzNotification.getSender());
        acceptIntent.putExtra("NOTIFICATION_ID", NotificationUtils.getNotificationId());
        PendingIntent smsAcceptIntent = PendingIntent.getBroadcast(context, 0, acceptIntent, PendingIntent.FLAG_CANCEL_CURRENT);*/

        //3. Most reliable way to send extra is to an activity!!!
        Intent acceptIntent = new Intent(context, HomeActivity.class);
        acceptIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        acceptIntent.putExtra("NOTIFICATION_ACCEPT", "NOTIFICATION_ACCEPT");
        acceptIntent.putExtra("SENDER_PHONE_NUMBER", senzNotification.getSenderPhone());
        acceptIntent.putExtra("USERNAME_TO_ADD", senzNotification.getSender());
        acceptIntent.putExtra("NOTIFICATION_ID", NotificationUtils.getNotificationIdForSMS());
        acceptIntent.putExtra("SENDER_UID", senzNotification.getUid());
        acceptIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent smsAcceptIntent = PendingIntent.getActivity(context, 0, acceptIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Setup pending intent for dismiss action
        Intent dismissIntent = new Intent(context, NotificationActionReceiver.class);
        dismissIntent.putExtra("NOTIFICATION_DISMISS", "NOTIFICATION_DISMISS");
        dismissIntent.putExtra("NOTIFICATION_ID", NotificationUtils.getNotificationIdForSMS());
        dismissIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent smsDismissIntent = PendingIntent.getBroadcast(context, 0, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(senzNotification.getTitle())
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentText(senzNotification.getMessage())
                .setSmallIcon(senzNotification.getIcon())
                .setWhen(System.currentTimeMillis())
                .addAction(R.drawable.reject, "Reject", smsDismissIntent)
                .addAction(R.drawable.accept, "Accept", smsAcceptIntent);

        Uri sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification);
        builder.setSound(sound);

        return builder.build();
    }

    /**
     * Create and update notification when query receives from server
     * No we have two notifications regarding Sensor application
     *
     * @param message incoming query
     */
    void updateNotification(Context context, String message) {
        /*Notification notification = getNotification(context, R.drawable.logo_green, context.getString(R.string.new_senz), message);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(MESSAGE_NOTIFICATION_ID, notification);*/
    }

    /**
     * Cancel notification
     * need to cancel when disconnect from web socket
     */
    void cancelNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NotificationUtils.MESSAGE_NOTIFICATION_ID);
    }

}
