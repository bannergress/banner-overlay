package com.bannergress.overlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;

import com.bannergress.overlay.api.Mission;
import com.bannergress.overlay.api.MissionStep;
import com.bannergress.overlay.api.Objective;
import com.bannergress.overlay.api.POIType;
import com.google.common.collect.Sets;

import java.util.Set;

final class ServiceNotification {
    static final String DEFAULT_CHANNEL_ID = "default";
    static final String STEP_IN_RANGE_CHANNEL_ID = "step";

    private static final int DEFAULT_NOTIFICATION_ID = 1;
    private static final int STEP_IN_RANGE_NOTIFICATION_ID_BASE = 2;
    private static final int STEP_IN_RANGE_NOTIFICATION_ID_MULTIPLIER = 100;

    static void createNotificationChannels(Context context) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        NotificationChannel channelDefault = new NotificationChannel(DEFAULT_CHANNEL_ID, context.getString(R.string.notificationChannelDefaultTitle), NotificationManager.IMPORTANCE_DEFAULT);
        channelDefault.setImportance(NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(channelDefault);

        NotificationChannel channelStepInRange = new NotificationChannel(STEP_IN_RANGE_CHANNEL_ID, context.getString(R.string.notificationChannelStepInRangeTitle), NotificationManager.IMPORTANCE_DEFAULT);
        channelStepInRange.setImportance(NotificationManager.IMPORTANCE_NONE);
        notificationManager.createNotificationChannel(channelStepInRange);
    }

    static void updateDefaultNotification(Service context, State state) {
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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(), DEFAULT_CHANNEL_ID);
        if (state.banner == null) {
            builder
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(context.getString(R.string.notificationTitle))
                    .setProgress(0, 0, false);
        } else {
            double totalDistance = DistanceCalculation.getTotalDistance(state.banner);
            double remainingDistance = DistanceCalculation.getRemainingDistance(state.banner, state.currentMission, state.currentMissionVisitedStepIndexes, state.currentLocation);
            builder
                    .setContentTitle(state.banner.title)
                    .setContentText(context.getString(R.string.notificationRemaining, remainingDistance / 1_000, totalDistance / 1_000))
                    .setProgress((int) totalDistance, Math.max((int) (totalDistance - remainingDistance), 0), false);
        }
        Notification notification = builder
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .addAction(action)
                .setOnlyAlertOnce(true)
                .setAllowSystemGeneratedContextualActions(false)
                .setOngoing(true)
                .build();

        context.startForeground(DEFAULT_NOTIFICATION_ID, notification);
    }

    static void updateStepReachedNotification(Context context, State newState, State oldState) {
        Set<Integer> newStepsInRange;
        if (newState.currentMission != oldState.currentMission) {
            newStepsInRange = newState.currentMissionVisitedStepIndexes;
        } else {
            newStepsInRange = Sets.difference(newState.currentMissionVisitedStepIndexes, oldState.currentMissionVisitedStepIndexes);
        }
        for (int stepIndex : newStepsInRange) {
            Mission mission = newState.banner.missions.get(newState.currentMission);
            assert mission != null;
            MissionStep step = mission.steps.get(stepIndex);
            if (step.poi != null && step.objective != null && step.poi.type != POIType.unavailable) {
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(), STEP_IN_RANGE_CHANNEL_ID)
                        .setContentTitle(context.getString(getObjectiveName(step.objective)))
                        .setContentText(step.poi.title)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setAllowSystemGeneratedContextualActions(false)
                        .setTimeoutAfter(8_000);
                Ingress.createLaunchIngressIntent(context).ifPresent(builder::setContentIntent);
                int notificationId = STEP_IN_RANGE_NOTIFICATION_ID_BASE
                        + STEP_IN_RANGE_NOTIFICATION_ID_MULTIPLIER * newState.currentMission
                        + stepIndex;
                notificationManager.notify(notificationId, builder.build());
            }
        }
    }

    private static @StringRes
    int getObjectiveName(Objective objective) {
        switch (objective) {
            case hack:
                return R.string.objectiveHack;
            case captureOrUpgrade:
                return R.string.objectiveCaptureOrUpgrade;
            case createLink:
                return R.string.objectiveCreateLink;
            case createField:
                return R.string.objectiveCreateField;
            case installMod:
                return R.string.objectiveInstallMod;
            case takePhoto:
                return R.string.objectiveTakePhoto;
            case viewWaypoint:
                return R.string.objectiveViewWaypoint;
            case enterPassphrase:
                return R.string.objectiveEnterPassphrase;
            default:
                throw new IllegalArgumentException(objective.toString());
        }
    }
}
