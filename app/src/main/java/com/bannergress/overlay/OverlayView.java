package com.bannergress.overlay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.os.HandlerCompat;

import com.bannergress.overlay.api.Banner;
import com.bannergress.overlay.api.Mission;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

@SuppressLint("ViewConstructor")
class OverlayView extends FrameLayout {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final WindowManager.LayoutParams params;
    private final TextView textMission;
    private final Button buttonMinus;
    private final Button buttonNext;
    private final Button buttonPlus;
    private State state;

    public OverlayView(Context context, String data) {
        super(context);
        LayoutInflater inflater = context.getSystemService(LayoutInflater.class);
        inflater.inflate(R.layout.activity_overlay, this, true);
        textMission = findViewById(R.id.textMission);
        buttonMinus = findViewById(R.id.buttonMinus);
        buttonNext = findViewById(R.id.buttonNext);
        buttonPlus = findViewById(R.id.buttonPlus);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.RGB_888);
        params.gravity = Gravity.START | Gravity.TOP;
        setupListeners();
        applyState(State.initial());
        loadData(data, context);
    }

    public static OverlayView create(Context context, String data) {
        OverlayView overlayView = new OverlayView(context, data);
        context.getSystemService(WindowManager.class).addView(overlayView, overlayView.params);
        return overlayView;
    }

    public WindowManager.LayoutParams getWindowParams() {
        return params;
    }

    private void setupListeners() {
        buttonMinus.setOnClickListener(v -> applyState(state.previousMission()));
        buttonPlus.setOnClickListener(v -> applyState(state.nextMission()));
        buttonNext.setOnClickListener(v -> {
            Optional<Mission> optionalNextMission = state.banner.missions.values().stream().skip(state.currentMission + 1).findFirst();
            optionalNextMission.ifPresent(nextMission -> Ingress.tryLaunchMission(getContext(), nextMission.id));
            applyState(state.nextMission());
        });
        setOnTouchListener(new ViewMoveListener(this));
    }

    public void remove() {
        getContext().getSystemService(WindowManager.class).removeView(this);
    }

    private void loadData(String data, Context context) {
        executorService.submit(() -> {
            try {
                String bannerId = parseBannerId(data);
                Banner banner = loadBanner(bannerId);
                HandlerCompat.createAsync(Looper.getMainLooper()).post(() -> applyState(state.bannerLoaded(banner)));
            } catch (Exception e) {
                HandlerCompat.createAsync(Looper.getMainLooper()).post(() -> applyState(State.error()));
                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Banner loadBanner(String missionId) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url("https://api.bannergress.com/bnrs/" + missionId).build();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ResponseBody responseBody = client.newCall(request).execute().body();
        assert responseBody != null;
        return objectMapper.readValue(responseBody.string(), Banner.class);
    }

    private String parseBannerId(String data) {
        Pattern pattern = Pattern.compile("https://bannergress.com/banner/([^\\s]+)");
        Matcher matcher = pattern.matcher(data);
        if (!matcher.find()) {
            throw new IllegalArgumentException(data);
        }
        return matcher.group(1);
    }

    private void applyState(State state) {
        this.state = state;

        if (state.banner == null) {
            if (state.error) {
                textMission.setText(R.string.overlayError);
            } else {
                textMission.setText(R.string.overlayEllipsis);
            }
            buttonMinus.setEnabled(false);
            buttonPlus.setEnabled(false);
            buttonNext.setEnabled(false);
            buttonNext.setText(R.string.overlayLoading);
        } else {
            int numberOfMissions = state.banner.missions.size();

            if (state.currentMission == -1) {
                textMission.setText(String.valueOf(numberOfMissions));
                buttonNext.setText(R.string.overlayStart);
            } else {
                textMission.setText(String.format(Locale.ROOT, "%d/%d", state.currentMission + 1, numberOfMissions));
                buttonNext.setText(R.string.overlayNext);
            }

            boolean hasPrevious = state.currentMission >= 0;
            boolean hasNext = state.currentMission < numberOfMissions - 1;
            buttonMinus.setEnabled(hasPrevious);
            buttonPlus.setEnabled(hasNext);
            buttonNext.setEnabled(hasNext);
        }
    }
}
