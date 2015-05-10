/*
 * Copyright (c) 2015. Thomas Haertel
 *
 * Licensed under Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.thomashaertel.regattatimer;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

/**
 * Schedule a countdown until a time in the future, with
 * regular notifications on intervals along the way.
 *
 * Example of showing a 30 second countdown in a text field:
 *
 * <pre class="prettyprint">
 * new RegattaCountDownTimer(30000, 1000) {
 *
 *     public void onTick(long millisUntilFinished) {
 *         mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
 *     }
 *
 *     public void onFinish() {
 *         mTextField.setText("done!");
 *     }
 *  }.start();
 * </pre>
 *
 * The calls to {@link #onTick(long)} are synchronized to this object so that
 * one call to {@link #onTick(long)} won't ever occur before the previous
 * callback is complete.  This is only relevant when the implementation of
 * {@link #onTick(long)} takes an amount of time to execute that is significant
 * compared to the countdown interval.
 */
public abstract class RegattaCountDownTimer implements Timer<RegattaCountDownTimer> {

    private long mMillisLeft;

    private long mSyncIntervalMillis = 60000;

    /**
     * Millis since epoch when alarm should stop.
     */
    private final long mMillisInFuture;

    /**
     * The interval in millis that the user receives callbacks
     */
    private final long mCountdownInterval;

    private long mStopTimeInFuture;

    /**
     * boolean representing if the timer was cancelled
     */
    private boolean mCancelled = false;

    /**
     * @param millisInFuture The number of millis in the future from the call
     *   to {@link #start()} until the countdown is done and {@link #onFinish()}
     *   is called.
     * @param countDownInterval The interval along the way to receive
     *   {@link #onTick(long)} callbacks.
     */
    public RegattaCountDownTimer(long millisInFuture, long countDownInterval) {
        mMillisInFuture = millisInFuture;
        mCountdownInterval = countDownInterval;

        mMillisLeft = mMillisInFuture;
    }

    /**
     * Cancel the countdown.
     */
    public synchronized final void cancel() {
        mCancelled = true;
        mHandler.removeMessages(MSG);
    }

    /**
     * Start the countdown.
     */
    public synchronized final RegattaCountDownTimer start() {
        mCancelled = false;
        if (mMillisInFuture <= 0) {
            onFinish();
            return this;
        }

        onTick(mMillisInFuture);

        mStopTimeInFuture = SystemClock.elapsedRealtime() + mMillisInFuture;
        mHandler.sendMessage(mHandler.obtainMessage(MSG));
        return this;
    }

    /**
     * Resume the countdown after it was cancelled.
     */
    public synchronized final RegattaCountDownTimer resume() {
        if (mMillisLeft <= 0) {
            onFinish();
            return this;
        }

        mStopTimeInFuture = SystemClock.elapsedRealtime() + mMillisLeft;

        if(mCancelled) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG));
        }

        mCancelled = false;

        return this;
    }

    /**
     * Reduces the countdown with SyncIntervalMillis .
     */
    public synchronized final RegattaCountDownTimer sync() {
        cancel();

        final long syncIntervals = mMillisLeft / mSyncIntervalMillis;
        mMillisLeft = syncIntervals * mSyncIntervalMillis + 100; // add 100ms latency for displaying synced time tick

        onTick(mMillisLeft);

        return resume();
    }

    public void setSyncIntervalMillis(long millis) {
        mSyncIntervalMillis = millis;
    }

    public long getSyncIntervalMillis() {
        return mSyncIntervalMillis;
    }

    public long getMillisLeft() {
        return mMillisLeft;
    }

    public boolean isCancelled() {
        return mCancelled;
    }

    /**
     * Callback fired on regular interval.
     * @param millisUntilFinished The amount of time until finished.
     */
    public abstract void onTick(long millisUntilFinished);

    /**
     * Callback fired when the time is up.
     */
    public abstract void onFinish();


    private static final int MSG = 1;


    // handles counting down
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            synchronized (RegattaCountDownTimer.this) {
                if (mCancelled) {
                    return;
                }

                final long lastTickStart = SystemClock.elapsedRealtime();
                final long millisLeft = mStopTimeInFuture - lastTickStart;

                mMillisLeft = millisLeft;

                if (millisLeft <= 0) {
                    onFinish();
                } else if (millisLeft < mCountdownInterval) {
                    // no tick, just delay until done
                    sendMessageDelayed(obtainMessage(MSG), millisLeft);
                } else {
                    onTick(millisLeft);

                    // take into account user's onTick taking time to execute
                    long delay = lastTickStart + mCountdownInterval - SystemClock.elapsedRealtime();

                    // special case: user's onTick took more than interval to
                    // complete, skip to next interval
                    while (delay < 0) {
                        Log.d("HANDLER", "onTick took to long: " + delay + "ms");
                        delay += mCountdownInterval;

                    }

                    sendMessageDelayed(obtainMessage(MSG), delay);
                }
            }
        }
    };
}