package com.bannergress.overlay;

import android.Manifest;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;

import java.util.function.BiConsumer;

public class OverlayService extends Service {
    private static final int NOTIFICATION_ID = 1;

    private OverlayView overlayView;

    private BiConsumer<State, State> stateListener;

    private LocationListener locationListener;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, ServiceNotification.createNotification(this));
        addListener();
        addOverlay(intent);
        addLocationListening();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeListener();
        removeOverlay();
        removeLocationListening();
    }

    private void addOverlay(Intent intent) {
        removeOverlay();
        overlayView = OverlayView.create(this, intent.getStringExtra(Intent.EXTRA_TEXT));
    }

    private void removeOverlay() {
        if (overlayView != null) {
            overlayView.remove();
            overlayView = null;
        }
    }

    private void addListener() {
        removeListener();
        stateListener = StateManager.addListener((newState, oldState) -> getSystemService(NotificationManager.class).notify(NOTIFICATION_ID, ServiceNotification.createNotification(this)));
    }

    private void removeListener() {
        if (stateListener != null) {
            StateManager.removeListener(stateListener);
            stateListener = null;
        }
    }

    private void addLocationListening() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = getSystemService(LocationManager.class);
            locationListener = location -> StateManager.updateState(state -> state.location(location));
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, locationListener);
        }
    }

    private void removeLocationListening() {
        if (locationListener != null) {
            LocationManager locationManager = getSystemService(LocationManager.class);
            locationManager.removeUpdates(locationListener);
            locationListener = null;
        }
    }
}
