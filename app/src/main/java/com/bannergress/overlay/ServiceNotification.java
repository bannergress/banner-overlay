package com.bannergress.overlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

final class ServiceNotification {
    private static final String CHANNEL_ID = "default";

    private static void createNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.notificationChannelTitle), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Default");
        channel.setImportance(NotificationManager.IMPORTANCE_LOW);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    static Notification createNotification(Context context) {
        createNotificationChannel(context);
        Intent deleteIntent = new Intent(context, StopDetectionReceiver.class);
        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(
                context,
                0,
                deleteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                R.drawable.ic_launcher_foreground,
                context.getString(R.string.notificationAction),
                pendingIntentCancel
        ).build();
        return new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.notificationTitle))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .addAction(action)
                .build();
    }
}
