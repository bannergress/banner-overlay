package com.bannergress.overlay;

import com.bannergress.overlay.api.Banner;

public class State {
    public final Banner banner;

    public final int currentMission;

    public final boolean error;

    public final boolean cooldown;

    private State(Banner banner, int currentMission, boolean error, boolean cooldown) {
        this.banner = banner;
        this.currentMission = currentMission;
        this.error = error;
        this.cooldown = cooldown;
    }

    static State initial() {
        return new State(null, -1, false, false);
    }

    static State error() {
        return new State(null, -1, true, false);
    }

    State previousMission() {
        return new State(this.banner, this.currentMission - 1, false, false);
    }

    State nextMission(boolean cooldown) {
        return new State(this.banner, this.currentMission + 1, false, cooldown);
    }

    State bannerLoaded(Banner banner) {
        return new State(banner, this.currentMission, false, false);
    }

    State bannerLoaded(Banner banner, int currentMission) {
        return new State(banner, currentMission, false, false);
    }

    State cooldownFinished() {
        return new State(this.banner, this.currentMission, this.error, false);
    }
}
