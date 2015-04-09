package com.thomashaertel.regattatimer;

import android.os.CountDownTimer;

/**
 * Created by user on 09.04.15.
 */
public abstract class RegattaCountDownTimer {

    private CountDownTimer mCountDownTimer;

    private long mSyncIntervallMillis = 60000;

    private long mMillisInFuture;
    private long mCountDownInterval;

    private long mTotalMillis;

    private boolean mPaused;

    public RegattaCountDownTimer(long millisInFuture, long countDownInterval) {
        mMillisInFuture = millisInFuture;
        mCountDownInterval = countDownInterval;

        init();
    }

    public synchronized void restart() {
        init();
        start();
    }

    public synchronized void start() {
        mCountDownTimer.start();
        mPaused = false;
    }

    public synchronized void pause() {
        mCountDownTimer.cancel();
        mPaused = true;
    }

    public synchronized void sync() {
        pause();

        mTotalMillis -= mSyncIntervallMillis;

        if(mTotalMillis < 0) {
            mTotalMillis = 0;
        }

        mCountDownTimer = createCountDownTimer(mTotalMillis, mCountDownInterval);

        onTick(mTotalMillis);

        start();
    }

    public boolean isPaused() {
        return mPaused;
    }

    public void setSyncIntervallMillis(long millis) {
        mSyncIntervallMillis = millis;
    }

    public long getSyncIntervallMillis() {
        return mSyncIntervallMillis;
    }

    public long getTotalMillis() {
        return mTotalMillis;
    }

    public abstract void onTick(long millisUntilFinished);
    public abstract void onFinish();

    private void init() {
        mTotalMillis = mMillisInFuture;
        mCountDownTimer = createCountDownTimer(mTotalMillis, mCountDownInterval);
    }

    private CountDownTimer createCountDownTimer(long totalMillis, long countDownInterval) {
        return new CountDownTimer(totalMillis, countDownInterval) {

            public void onTick(long millisUntilFinished) {
                mTotalMillis = millisUntilFinished;
                RegattaCountDownTimer.this.onTick(millisUntilFinished);
            }

            public void onFinish() {
                RegattaCountDownTimer.this.onFinish();
            }
        };
    }
}
