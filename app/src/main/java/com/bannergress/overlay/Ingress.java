package com.bannergress.overlay;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.text.MessageFormat;

public class Ingress {
    private static final String URL_TEMPLATE = "https://link.ingress.com/?link=https%3a%2f%2fintel.ingress.com%2fmission%2f{0}&apn=com.nianticproject.ingress&isi=576505181&ibi=com.google.ingress&ifl=https%3a%2f%2fapps.apple.com%2fapp%2fingress%2fid576505181&ofl=https%3a%2f%2fintel.ingress.com%2fmission%2f{0}";

    public static void tryLaunchMission(Context context, String missionId) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MessageFormat.format(URL_TEMPLATE, missionId)));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, MessageFormat.format("Failed to launch mission: {0}", e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }
}
