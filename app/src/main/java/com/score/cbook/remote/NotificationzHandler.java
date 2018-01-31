package com.score.cbook.remote;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.score.cbook.R;
import com.score.cbook.pojo.Notifcationz;
import com.score.cbook.ui.ChatActivity;
import com.score.cbook.ui.ChequeListActivity;
import com.score.cbook.ui.CustomerListActivity;


public class NotificationzHandler {

    private static final int MESSAGE_NOTIFICATION_ID = 1;
    private static final int CHEQUE_NOTIFICATION_ID = 2;
    private static final int CUSTOMER_NOTIFICATION_ID = 3;

    static void notifyMessage(Context context, Notifcationz notifcationz) {
        Intent intent = new Intent(context, ChatActivity.class);

        Notification notification = buildNotification(context, intent, notifcationz);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(MESSAGE_NOTIFICATION_ID, notification);
    }

    static void notifyCheque(Context context, Notifcationz notifcationz) {
        Intent intent = new Intent(context, ChequeListActivity.class);

        Notification notification = buildNotification(context, intent, notifcationz);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(CHEQUE_NOTIFICATION_ID, notification);
    }

    static void notifiyCustomer(Context context, Notifcationz notifcationz) {
        Intent intent = new Intent(context, CustomerListActivity.class);

        Notification notification = buildNotification(context, intent, notifcationz);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(CUSTOMER_NOTIFICATION_ID, notification);
    }

    public static void cancel(Context context, int notificationId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
    }

    private static Notification buildNotification(Context context, Intent intent, Notifcationz notifcationz) {
        intent.putExtra("SENDER", notifcationz.getSender());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(notifcationz.getTitle())
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX | Notification.DEFAULT_VIBRATE)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notifcationz.getMessage()))
                .setSmallIcon(notifcationz.getIcon())
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.eyes))
                .setLights(Color.GREEN, 500, 7000);

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        return notification;
    }
}
