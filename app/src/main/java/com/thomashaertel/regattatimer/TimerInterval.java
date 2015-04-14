package com.thomashaertel.regattatimer;

/**
 * Created by user on 14.04.15.
 */
public enum TimerInterval {
    M5310(R.string.mode_5410, 300000, false), M5(R.string.mode_5M, 300000, true), M2(R.string.mode_3M, 180000, true), M1(R.string.mode_1M, 60000, true);

    private final int textRes;
    private final long millis;
    private final boolean repeatable;

    private TimerInterval(int textRes, long millis, boolean repeatable) {
        this.textRes = textRes;
        this.millis = millis;
        this.repeatable = repeatable;
    }

    public int getTextRes() {
        return textRes;
    }

    public long getMillis() {
        return millis;
    }

    public boolean isRepeatable() {
        return repeatable;
    }
}
