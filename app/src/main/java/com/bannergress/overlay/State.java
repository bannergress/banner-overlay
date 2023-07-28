package com.bannergress.overlay;

import android.location.Location;

import com.bannergress.overlay.api.Banner;
import com.bannergress.overlay.api.Mission;
import com.bannergress.overlay.api.MissionStep;
import com.bannergress.overlay.api.MissionType;
import com.bannergress.overlay.api.POIType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

public class State {
    public final Banner banner;

    public final int currentMission;

    public final boolean error;

    public final boolean cooldown;

    public final Location currentLocation;

    public final ImmutableSet<Integer> currentMissionVisitedStepIndexes;

    public final boolean locationEnabled;

    private State(Banner banner, int currentMission, boolean error, boolean cooldown, Location currentLocation, ImmutableSet<Integer> currentMissionVisitedStepIndexes, boolean locationEnabled) {
        this.banner = banner;
        this.currentMission = currentMission;
        this.error = error;
        this.cooldown = cooldown;
        this.currentLocation = currentLocation;
        this.currentMissionVisitedStepIndexes = currentMissionVisitedStepIndexes;
        this.locationEnabled = locationEnabled;
    }

    static State initial() {
        return new State(null, -1, false, false, null, ImmutableSet.of(), false);
    }

    static State terminal() {
        return initial();
    }

    static State error() {
        return new State(null, -1, true, false, null, ImmutableSet.of(), false);
    }

    State previousMission() {
        return new State(this.banner, this.currentMission - 1, false, false, this.currentLocation, ImmutableSet.of(), this.locationEnabled)
                .applyLocation();
    }

    State nextMission(boolean cooldown) {
        return new State(this.banner, this.currentMission + 1, false, cooldown, this.currentLocation, ImmutableSet.of(), this.locationEnabled)
                .applyLocation();
    }

    State bannerLoaded(Banner banner) {
        return bannerLoaded(banner, -1);
    }

    State bannerLoaded(Banner banner, int currentMission) {
        return new State(banner, currentMission, false, false, this.currentLocation, ImmutableSet.of(), this.locationEnabled)
                .applyLocation();
    }

    State cooldownFinished() {
        return new State(this.banner, this.currentMission, this.error, false, this.currentLocation, this.currentMissionVisitedStepIndexes, this.locationEnabled);
    }

    State location(Location location) {
        return new State(this.banner, this.currentMission, this.error, this.cooldown, location, this.currentMissionVisitedStepIndexes, this.locationEnabled).applyLocation();
    }

    State locationEnabled() {
        return new State(this.banner, this.currentMission, this.error, this.cooldown, this.currentLocation, this.currentMissionVisitedStepIndexes, true);
    }

    private State applyLocation() {
        ImmutableSet<Integer> currentMissionVisitedStepIndexes = calculateStepIndexesInRange(this.banner, this.currentMission, this.currentMissionVisitedStepIndexes, this.currentLocation);
        return new State(this.banner, this.currentMission, this.error, this.cooldown, this.currentLocation, currentMissionVisitedStepIndexes, this.locationEnabled);
    }

    ImmutableSet<Integer> calculateStepIndexesInRange(Banner banner, int currentMission, ImmutableSet<Integer> finishedSteps, Location location) {
        if (banner == null || currentMission < 0 || currentMission >= banner.missions.size()) {
            return ImmutableSet.of();
        } else if (location == null) {
            return finishedSteps;
        } else {
            Mission mission = banner.missions.get(currentMission);
            assert mission != null;
            List<MissionStep> steps = mission.steps;
            ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
            for (int stepIndex = 0; stepIndex < steps.size(); stepIndex++) {
                MissionStep step = steps.get(stepIndex);
                if (finishedSteps.contains(stepIndex)
                        || step.poi == null
                        || step.poi.type == POIType.unavailable
                        || DistanceCalculation.isInRange(step, location)) {
                    builder.add(stepIndex);
                } else if (mission.type != MissionType.anyOrder) {
                    break;
                }
            }
            return builder.build();
        }
    }
    static ImmutableMap<Integer, MissionStep> getNewStepsInRange(State newState, State oldState) {
        Set<Integer> newStepsInRange;
        if (newState.currentMission != oldState.currentMission) {
            newStepsInRange = newState.currentMissionVisitedStepIndexes;
        } else {
            newStepsInRange = Sets.difference(newState.currentMissionVisitedStepIndexes, oldState.currentMissionVisitedStepIndexes);
        }
        ImmutableMap.Builder<Integer, MissionStep> result = ImmutableMap.builder();
        for (int stepIndex : newStepsInRange) {
            Mission mission = newState.banner.missions.get(newState.currentMission);
            assert mission != null;
            MissionStep step = mission.steps.get(stepIndex);
            result.put(stepIndex, step);
        }
        return result.build();
    }

}
