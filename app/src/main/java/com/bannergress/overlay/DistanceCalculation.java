package com.bannergress.overlay;

import android.location.Location;

import androidx.annotation.NonNull;

import com.bannergress.overlay.api.Banner;
import com.bannergress.overlay.api.MissionStep;
import com.bannergress.overlay.api.POI;
import com.bannergress.overlay.api.POIType;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DistanceCalculation {
    private static final LoadingCache<Banner, Map<Integer, PartialResult>> remainingDistanceCache = CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<Banner, Map<Integer, PartialResult>>() {
        @NonNull
        @Override
        public Map<Integer, PartialResult> load(@NonNull Banner banner) {
            Map<Integer, PartialResult> result = new HashMap<>();
            PartialResult partialResult = PartialResult.create();
            for (int missionIndex = banner.missions.size() - 1; missionIndex >= 0; missionIndex--) {
                List<MissionStep> steps = Objects.requireNonNull(banner.missions.get(missionIndex)).steps;
                for (int stepIndex = steps.size() - 1; stepIndex >= 0; stepIndex--) {
                    POI poi = steps.get(stepIndex).poi;
                    partialResult = partialResult.plus(poi);
                }
                result.put(missionIndex, partialResult);
            }
            return result;
        }
    });

    public static double getTotalDistance(Banner banner) {
        return remainingDistanceCache.getUnchecked(banner).get(0).distance;
    }

    public static double getRemainingDistance(Banner banner, int firstMissionIndex, Set<Integer> excludedStepIndexesFromFirstMission, Location currentLocation) {
        PartialResult result = firstMissionIndex < banner.missions.size() - 1
                ? remainingDistanceCache.getUnchecked(banner).get(firstMissionIndex + 1)
                : PartialResult.create();
        if (firstMissionIndex >= 0) {
            List<MissionStep> steps = Objects.requireNonNull(banner.missions.get(firstMissionIndex)).steps;
            for (int stepIndex = steps.size() - 1; stepIndex >= 0; stepIndex--) {
                if (!excludedStepIndexesFromFirstMission.contains(stepIndex)) {
                    POI poi = steps.get(stepIndex).poi;
                    result = result.plus(poi);
                }
            }
        }
        if (currentLocation != null) {
            result = result.plus(currentLocation.getLatitude(), currentLocation.getLongitude());
        }
        return result.distance;
    }

    public static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final int radius_meters = 6_371_000;
        double latDistance = toRad(lat2 - lat1);
        double lonDistance = toRad(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return radius_meters * c;
    }

    private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }

    private static class PartialResult {
        private final double distance;

        private final Double latitude;

        private final Double longitude;

        private PartialResult(double distance, Double latitude, Double longitude) {
            this.distance = distance;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public static PartialResult create() {
            return new PartialResult(0, null, null);
        }

        public PartialResult plus(double otherLatitude, double otherLongitude) {
            if (this.latitude == null) {
                return new PartialResult(distance, otherLatitude, otherLongitude);
            } else {
                return new PartialResult(distance + distanceMeters(this.latitude, this.longitude, otherLatitude, otherLongitude), otherLatitude, otherLongitude);
            }
        }

        public PartialResult plus(POI poi) {
            if (poi == null || poi.type == POIType.unavailable) {
                return this;
            } else {
                return plus(poi.latitude, poi.longitude);
            }
        }
    }
}
