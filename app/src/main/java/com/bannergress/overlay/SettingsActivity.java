package com.bannergress.overlay;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import java.util.function.Function;

public class SettingsActivity extends AppCompatActivity {
    private final ActivityResultLauncher<String> locationEnabler = registerSwitchEnabler(R.string.location_enable);
    private final ActivityResultLauncher<String> notificationsEnabler = registerSwitchEnabler(R.string.notifications_enable);

    private ActivityResultLauncher<String> registerSwitchEnabler(@StringRes int key) {
        return registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                SettingsFragment settingsFragment = ((SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.settings));
                assert settingsFragment != null;
                SwitchPreferenceCompat preference = settingsFragment.findPreference(getString(key));
                assert preference != null;
                preference.setChecked(true);
                preference.setEnabled(false);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.settings, new SettingsFragment()).commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            ServiceNotification.createNotificationChannels(requireContext());
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            updatePreferences();
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        private void updatePreferences() {
            SwitchPreferenceCompat locationEnablePreference = findPreference(getString(R.string.location_enable));
            assert locationEnablePreference != null;
            addPermissionPreference(locationEnablePreference, Manifest.permission.ACCESS_FINE_LOCATION, z -> z.locationEnabler);

            SwitchPreferenceCompat notificationsEnablePreference = findPreference(getString(R.string.notifications_enable));
            assert notificationsEnablePreference != null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                addPermissionPreference(notificationsEnablePreference, Manifest.permission.POST_NOTIFICATIONS, z -> z.notificationsEnabler);
            } else {
                notificationsEnablePreference.setChecked(true);
                notificationsEnablePreference.setEnabled(false);
            }

            Preference notificationProgressPreference = findPreference(getString(R.string.notification_progress));
            assert notificationProgressPreference != null;
            addChannelPreference(notificationProgressPreference, ServiceNotification.DEFAULT_CHANNEL_ID);

            Preference notificationStepInRangePreference = findPreference(getString(R.string.notification_step_in_range));
            assert notificationStepInRangePreference != null;
            addChannelPreference(notificationStepInRangePreference, ServiceNotification.STEP_IN_RANGE_CHANNEL_ID);
        }

        private void addChannelPreference(Preference preference, String channelId) {
            preference.setOnPreferenceClickListener(p -> {
                Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelId);
                startActivity(intent);
                return true;
            });
        }

        private void addPermissionPreference(SwitchPreferenceCompat preference, String permission, Function<SettingsActivity, ActivityResultLauncher<String>> launcherFunction) {
            boolean hasPermission = requireContext().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
            preference.setChecked(hasPermission);
            preference.setEnabled(!hasPermission);
            preference.setOnPreferenceChangeListener((x, newValue) -> {
                if (shouldShowRequestPermissionRationale(permission)) {
                    Toast.makeText(getContext(), "Permission has been permanently denied.", Toast.LENGTH_SHORT).show();
                } else {
                    launcherFunction.apply((SettingsActivity) getActivity()).launch(permission);
                }
                return false;
            });
        }
    }
}
