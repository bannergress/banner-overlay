package com.bannergress.overlay;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
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
import com.bannergress.overlay.api.BannerApi;
import com.bannergress.overlay.api.Mission;
import com.google.common.collect.Iterables;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Function;

@SuppressLint("ViewConstructor")
class OverlayView extends FrameLayout {
    public static final int COOLDOWN_MILLIS = 1_000;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final WindowManager.LayoutParams params;
    private final TextView textMission;
    private final Button buttonMinus;
    private final Button buttonNext;
    private final Button buttonPlus;
    private BiConsumer<State, State> stateListener;

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
        applyState(StateManager.getState());
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
        stateListener = StateManager.addListener((newState, oldState) -> applyState(newState));
        buttonMinus.setOnClickListener(v -> StateManager.updateState(State::previousMission));
        buttonPlus.setOnClickListener(v -> StateManager.updateState(state -> state.nextMission(false)));
        buttonNext.setOnClickListener(v -> {
            Optional<Mission> optionalNextMission = StateManager.getState().banner.missions.values().stream().skip(StateManager.getState().currentMission + 1).findFirst();
            if (optionalNextMission.isPresent()) {
                Ingress.tryLaunchMission(getContext(), optionalNextMission.get().id);
                StateManager.updateState(state -> state.nextMission(true));
                new Handler().postDelayed(() -> StateManager.updateState(State::cooldownFinished), COOLDOWN_MILLIS);
            } else {
                Intent serviceIntent = new Intent();
                serviceIntent.setComponent(new ComponentName(getContext(), OverlayService.class));
                getContext().stopService(serviceIntent);
            }
        });
        setOnTouchListener(new ViewMoveListener(this));
    }

    public void remove() {
        StateManager.removeListener(stateListener);
        getContext().getSystemService(WindowManager.class).removeView(this);
    }

    private void loadData(String data, Context context) {
        executorService.submit(() -> {
            try {
                SharedDataParser.ParsedData parsedData = SharedDataParser.parse(data);
                Function<State, State> stateFunction;
                switch (parsedData.type) {
                    case mission: {
                        String missionId = parsedData.id;
                        List<Banner> banners = BannerApi.findBanners(missionId);
                        if (banners.size() == 1) {
                            String bannerId = banners.get(0).id;
                            Banner banner = BannerApi.getBanner(bannerId);
                            int currentMission = Iterables.indexOf(banner.missions.values(), mission -> {
                                assert mission != null;
                                return mission.id.equals(missionId);
                            });
                            stateFunction = state -> state.bannerLoaded(banner, currentMission);
                        } else {
                            stateFunction = state -> State.error();
                        }
                        break;
                    }
                    case banner: {
                        String bannerId = parsedData.id;
                        Banner banner = BannerApi.getBanner(bannerId);
                        stateFunction = state -> state.bannerLoaded(banner);
                        break;
                    }
                    case invalid: {
                        stateFunction = state -> State.error();
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException(parsedData.type.toString());
                    }
                }
                HandlerCompat.createAsync(Looper.getMainLooper()).post(() -> StateManager.updateState(stateFunction));
            } catch (Exception e) {
                HandlerCompat.createAsync(Looper.getMainLooper()).post(() -> StateManager.updateState(state -> State.error()));
                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyState(State state) {
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
                textMission.setText(String.format(Locale.ROOT, "—/%s", numberOfMissions));
                buttonNext.setText(R.string.overlayStart);
            } else {
                textMission.setText(String.format(Locale.ROOT, "%s/%s", state.currentMission + 1, numberOfMissions));
                if (state.currentMission + 1 == numberOfMissions) {
                    buttonNext.setText(R.string.overlayClose);
                } else {
                    buttonNext.setText(R.string.overlayNext);
                }
            }

            boolean hasPrevious = state.currentMission >= 0;
            boolean hasNext = state.currentMission < numberOfMissions - 1;
            buttonMinus.setEnabled(hasPrevious);
            buttonPlus.setEnabled(hasNext);
            buttonNext.setEnabled(!state.cooldown);
        }
    }
}
