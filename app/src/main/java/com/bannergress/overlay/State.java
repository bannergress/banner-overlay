package com.bannergress.overlay;

import android.location.Location;

import com.bannergress.overlay.api.Banner;
import com.bannergress.overlay.api.Mission;
import com.bannergress.overlay.api.MissionStep;
import com.bannergress.overlay.api.MissionType;
import com.bannergress.overlay.api.POIType;
import com.google.common.collect.ImmutableSet;

import java.util.List;

public class State {
    public final Banner banner;

    public final int currentMission;

    public final boolean error;

    public final boolean cooldown;

    public final Location currentLocation;

    public final ImmutableSet<Integer> currentMissionVisitedStepIndexes;

    private State(Banner banner, int currentMission, boolean error, boolean cooldown, Location currentLocation, ImmutableSet<Integer> currentMissionVisitedStepIndexes) {
        this.banner = banner;
        this.currentMission = currentMission;
        this.error = error;
        this.cooldown = cooldown;
        this.currentLocation = currentLocation;
        this.currentMissionVisitedStepIndexes = currentMissionVisitedStepIndexes;
    }

    static State initial() {
        return new State(null, -1, false, false, null, ImmutableSet.of());
    }

    static State error() {
        return new State(null, -1, true, false, null, ImmutableSet.of());
    }

    State previousMission() {
        return new State(this.banner, this.currentMission - 1, false, false, this.currentLocation, ImmutableSet.of())
                .applyLocation();
    }

    State nextMission(boolean cooldown) {
        return new State(this.banner, this.currentMission + 1, false, cooldown, this.currentLocation, ImmutableSet.of())
                .applyLocation();
    }

    State bannerLoaded(Banner banner) {
        return bannerLoaded(banner, -1);
    }

    State bannerLoaded(Banner banner, int currentMission) {
        return new State(banner, currentMission, false, false, this.currentLocation, ImmutableSet.of())
                .applyLocation();
    }

    State cooldownFinished() {
        return new State(this.banner, this.currentMission, this.error, false, this.currentLocation, this.currentMissionVisitedStepIndexes);
    }

    State location(Location location) {
        return new State(this.banner, this.currentMission, this.error, this.cooldown, location, this.currentMissionVisitedStepIndexes)
                .applyLocation();
    }

    private State applyLocation() {
        ImmutableSet<Integer> currentMissionVisitedStepIndexes = calculateStepIndexesInRange(this.banner, this.currentMission, this.currentMissionVisitedStepIndexes, this.currentLocation);
        return new State(this.banner, this.currentMission, this.error, this.cooldown, this.currentLocation, currentMissionVisitedStepIndexes);
    }

    ImmutableSet<Integer> calculateStepIndexesInRange(Banner banner, int currentMission, ImmutableSet<Integer> finishedSteps, Location location) {
        if (banner == null || currentMission < 0 || currentMission >= banner.missions.size()) {
            return ImmutableSet.of();
        } else if (location == null) {
            return finishedSteps;
        } else {
            Mission mission = banner.missions.get(currentMission);
            List<MissionStep> steps = mission.steps;
            ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
            for (int stepIndex = 0; stepIndex < steps.size(); stepIndex++) {
                MissionStep step = steps.get(stepIndex);
                if (finishedSteps.contains(stepIndex)
                        || step.poi == null
                        || step.poi.type == POIType.unavailable
                        || isInRange(step, location)) {
                    builder.add(stepIndex);
                } else if (mission.type != MissionType.anyOrder) {
                    break;
                }
            }
            return builder.build();
        }
    }

    private boolean isInRange(MissionStep step, Location location) {
        return DistanceCalculation.distanceMeters(location.getLatitude(), location.getLongitude(), step.poi.latitude, step.poi.longitude) <= 40;
    }
}
