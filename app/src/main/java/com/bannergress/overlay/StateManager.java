package com.bannergress.overlay;

import android.util.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class StateManager {
    private static final Set<BiConsumer<State, State>> listeners = new HashSet<>();
    private static State state = State.initial();

    private StateManager() {
    }

    public static BiConsumer<State, State> addListener(BiConsumer<State, State> listener) {
        if (listeners.add(listener)) {
            listener.accept(state, State.initial());
        }
        return listener;
    }

    public static void removeListener(BiConsumer<State, State> listener) {
        if (listeners.remove(listener)) {
            listener.accept(State.terminal(), state);
        }
    }

    public static State getState() {
        return state;
    }

    public static void updateState(Function<State, State> stateFunction) {
        State oldState = state;
        State newState = state = stateFunction.apply(state);
        Log.d("state", String.format("current: %s | visited: %s | location: %s",
                newState.currentMission,
                newState.currentMissionVisitedStepIndexes,
                newState.currentLocation));
        for (BiConsumer<State, State> listener : listeners) {
            listener.accept(newState, oldState);
        }
    }
}
