package com.thomashaertel.regattatimer;

/**
 * Created by user on 14.04.15.
 */
public enum TimerMode {
    UPDOWN(R.string.mode_updown), REPEATING(R.string.mode_repeat);

    private final int textRes;

    private TimerMode(int textRes) {
        this.textRes = textRes;
    }

    public int getTextRes() {
        return textRes;
    }
}
