package com.thomashaertel.regattatimer;

/**
 * Created by user on 14.04.15.
 */
public enum TimerInterval {
    M5310(R.string.mode_5310, 50000, false), M5(R.string.mode_5M, 50000, true), M2(R.string.mode_2M, 20000, true), M1(R.string.mode_1M, 10000, true);

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
