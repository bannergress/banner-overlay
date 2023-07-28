package com.bannergress.overlay;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.preference.PreferenceManager;

import com.bannergress.overlay.api.MissionStep;
import com.bannergress.overlay.api.Objective;

import java.util.Map;

final class ClipboardHandler {
    static void updateClipboard(Context context, State newState, State oldState) {
        Map<Integer, MissionStep> newStepsInRange = State.getNewStepsInRange(newState, oldState);
        for (Map.Entry<Integer, MissionStep> entry : newStepsInRange.entrySet()) {
            MissionStep step = entry.getValue();
            if (step.poi != null && step.objective == Objective.enterPassphrase && isClipboardEnabled(context)) {
                clipMissionNumber(context, newState.currentMission);
            }
        }
    }

    private static void clipMissionNumber(Context context, int currentMission) {
        ClipData clip = ClipData.newPlainText(context.getString(R.string.clipDataLabel), String.valueOf(currentMission + 1));
        ClipboardManager clipboardManager = context.getSystemService(ClipboardManager.class);
        clipboardManager.setPrimaryClip(clip);
    }

    private static boolean isClipboardEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.clipboard_enable), false);
    }
}
