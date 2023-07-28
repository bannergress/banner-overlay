package com.bannergress.overlay;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;

import androidx.preference.PreferenceManager;

import java.util.function.BiConsumer;

public class OverlayService extends Service {
    private OverlayView overlayView;

    private BiConsumer<State, State> stateNotificationListener;

    private BiConsumer<State, State> stateLocationListener;

    private LocationListener locationListener;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        stateNotificationListener = StateManager.addListener(this::handleStateNotification);
        stateLocationListener = StateManager.addListener(this::handleStateLocation);
        addOverlay(intent);
        initPreferences();
        return START_NOT_STICKY;
    }

    private void initPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        applyPreferences(preferences);
        preferences.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> applyPreferences(sharedPreferences));
    }

    private void applyPreferences(SharedPreferences preferences) {
        StateManager.updateState(State::locationEnabled);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        StateManager.removeListener(stateLocationListener);
        StateManager.removeListener(stateNotificationListener);
        removeOverlay();
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

    private void handleStateNotification(State newState, State oldState) {
        ServiceNotification.createNotificationChannels(this);
        ServiceNotification.updateDefaultNotification(this, newState);
        ServiceNotification.updateStepReachedNotification(this, newState, oldState);
        ClipboardHandler.updateClipboard(this, newState, oldState);
    }

    private void handleStateLocation(State newState, State oldState) {
        if (newState.locationEnabled) {
            addLocationListening();
        } else {
            removeLocationListening();
        }
    }

    private void addLocationListening() {
        if (locationListener == null && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = getSystemService(LocationManager.class);
            locationListener = location -> StateManager.updateState(state -> state.location(location));
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
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
