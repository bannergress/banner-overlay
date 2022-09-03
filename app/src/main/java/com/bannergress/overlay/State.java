package com.bannergress.overlay;

import com.bannergress.overlay.api.Banner;

public class State {
    public final Banner banner;

    public final int currentMission;

    public final boolean error;

    private State(Banner banner, int currentMission, boolean error) {
        this.banner = banner;
        this.currentMission = currentMission;
        this.error = error;
    }

    static State initial() {
        return new State(null, -1, false);
    }

    static State error() {
        return new State(null, -1, true);
    }

    State previousMission() {
        return new State(this.banner, this.currentMission - 1, false);
    }

    State nextMission() {
        return new State(this.banner, this.currentMission + 1, false);
    }

    State bannerLoaded(Banner banner) {
        return new State(banner, this.currentMission, false);
    }

    State bannerLoaded(Banner banner, int currentMission) {
        return new State(banner, currentMission, false);
    }
}
