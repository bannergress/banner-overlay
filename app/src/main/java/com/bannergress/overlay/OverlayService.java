package com.bannergress.overlay;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class OverlayService extends Service {
    private OverlayView overlayView;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = ServiceNotification.createNotification(this);
        startForeground(1, notification);
        addOverlay(intent);
        return START_NOT_STICKY;
    }

    private void addOverlay(Intent intent) {
        removeOverlay();
        overlayView = OverlayView.create(this, intent.getStringExtra(Intent.EXTRA_TEXT));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeOverlay();
    }

    private void removeOverlay() {
        if (overlayView != null) {
            overlayView.remove();
            overlayView = null;
        }
    }
}
