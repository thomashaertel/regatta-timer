package com.thomashaertel.regattatimer;

/**
 * Created by user on 14.04.15.
 */
public enum TimerMode {
    UPDOWN(R.string.mode_updown), INFINITE(R.string.mode_infinite);

    private final int textRes;

    private TimerMode(int textRes) {
        this.textRes = textRes;
    }

    public int getTextRes() {
        return textRes;
    }
}
