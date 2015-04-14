package com.thomashaertel.regattatimer;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

/**
 * Schedule a counter until a time in the future, with
 * regular notifications on intervals along the way.
 *
 * Example of showing a 30 second countdown in a text field:
 *
 * <pre class="prettyprint">
 * new StopWatch(30000, 1000) {
 *
 *     public void onTick(long millisUntilFinished) {
 *         mTextField.setText("seconds counted: " + millisUntilFinished / 1000);
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
public abstract class StopWatch implements Timer<StopWatch> {

    private long mMillisLeft;
    private long mMillisCounted;

    /**
     * Millis since epoch when alarm should stop.
     */
    private final long mMillisInFuture;

    /**
     * The interval in millis that the user receives callbacks
     */
    private final long mCountInterval;

    private long mStartTime;
    private long mStopTimeInFuture;

    /**
     * boolean representing if the timer was cancelled
     */
    private boolean mCancelled = false;

    /**
     * @param millisInFuture The number of millis in the future from the call
     *   to {@link #start()} until the counter is done and {@link #onFinish()}
     *   is called. set to -1 for infinite counting
     * @param countInterval The interval along the way to receive
     *   {@link #onTick(long)} callbacks.
     */
    public StopWatch(long millisInFuture, long countInterval) {
        mMillisInFuture = millisInFuture;
        mCountInterval = countInterval;

        mMillisCounted = mMillisInFuture;
    }

    /**
     * Cancel the counter.
     */
    public synchronized final void cancel() {
        mCancelled = true;
        mHandler.removeMessages(MSG);
    }

    /**
     * Start the counter.
     */
    public synchronized final StopWatch start() {
        mCancelled = false;
        if (mMillisInFuture != -1 && mMillisLeft <= 0) {
            onFinish();
            return this;
        }

        onTick(0);

        mStartTime = SystemClock.elapsedRealtime();
        mStopTimeInFuture = mMillisInFuture > -1 ? mStartTime + mMillisInFuture :  -1;

        mHandler.sendMessage(mHandler.obtainMessage(MSG));
        return this;
    }

    /**
     * Resume the counter after it was cancelled.
     */
    public synchronized final StopWatch resume() {
        mCancelled = false;
        if (mMillisInFuture != -1) {
            if(mMillisLeft <= 0) {
                onFinish();
                return this;
            }

            mStopTimeInFuture = SystemClock.elapsedRealtime() + mMillisCounted;
        }

        mStartTime = SystemClock.elapsedRealtime() - mMillisCounted;

        mHandler.sendMessage(mHandler.obtainMessage(MSG));
        return this;
    }

    public long getMillisLeft() {
        return mMillisLeft;
    }

    public long getMillisCounted() { return mMillisCounted; }

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


    // handles counting up
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            synchronized (StopWatch.this) {
                if (mCancelled) {
                    return;
                }

                final long millisCounted = SystemClock.elapsedRealtime() - mStartTime;
                mMillisCounted = millisCounted;
                mMillisLeft = mStopTimeInFuture != -1 ? mStopTimeInFuture - millisCounted : -1;

                if (mStopTimeInFuture != -1 && millisCounted >= mStopTimeInFuture) {
                    onFinish();
                } else if (mStopTimeInFuture != -1 && mStopTimeInFuture - millisCounted < mCountInterval) {
                    // no tick, just delay until done
                    sendMessageDelayed(obtainMessage(MSG), mStopTimeInFuture - millisCounted);
                } else {
                    long lastTickStart = SystemClock.elapsedRealtime();
                    onTick(millisCounted);

                    // take into account user's onTick taking time to execute
                    long delay = lastTickStart + mCountInterval - SystemClock.elapsedRealtime();

                    // special case: user's onTick took more than interval to
                    // complete, skip to next interval
                    while (delay < 0) delay += mCountInterval;

                    sendMessageDelayed(obtainMessage(MSG), delay);
                }
            }
        }
    };
}
