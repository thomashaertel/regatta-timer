package com.thomashaertel.regattatimer;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String TIMER_INTERVAL = "tmrint";
    private static final String TIMER_MODE = "tmrmode";

    private TimerInterval mTimerInterval = TimerInterval.M5310;
    private TimerMode mTimerMode = TimerMode.UPDOWN;

    private Timer<?> mTimer;

    private Button mProgramButton;
    private Button mClearButton;

    private TextView mTimerIntervalView;
    private TextView mTimerModeView;
    private TextView mTimerView;


    private long mCountDownMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(savedInstanceState != null) {
            mTimerInterval = TimerInterval.valueOf(savedInstanceState.getString(TIMER_INTERVAL, TimerInterval.M5310.name()));
            mTimerMode = TimerMode.valueOf(savedInstanceState.getString(TIMER_MODE, TimerMode.UPDOWN.name()));
        }

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTimerIntervalView = (TextView) stub.findViewById(R.id.timerInterval);
                mTimerIntervalView.setText(mTimerInterval.getTextRes());
                mTimerModeView = (TextView) stub.findViewById(R.id.timerMode);
                mTimerModeView.setText(mTimerMode.getTextRes());
                mTimerView = (TextView) stub.findViewById(R.id.timer);

                mProgramButton = (Button) stub.findViewById(R.id.programButton);
                mProgramButton.setOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        onProgramLongClick(v);
                        return true;
                    }
                });

                mClearButton = (Button) stub.findViewById(R.id.clearButton);
                mClearButton.setOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        onClearLongClick(v);
                        return true;
                    }
                });
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TIMER_INTERVAL, mTimerInterval.name());
        outState.putString(TIMER_MODE, mTimerMode.name());
    }

    public void onStartStopClick(View view) {
        if(mTimer == null) {
            if(mCountDownMillis > 0) {
                mTimer = createCountDownTimer(mCountDownMillis);
            } else {
                mTimer = createStopWatch(-1);
            }
            mTimer.start();
        } else {
            if (!mTimer.isCancelled()) {
                mTimer.cancel();
            } else {
                mTimer.resume();
            }
        }
    }

    public void onSyncClick(View view) {
        if(mTimer != null && !mTimer.isCancelled()) {
            if(mTimer instanceof RegattaCountDownTimer)
                ((RegattaCountDownTimer) mTimer).sync();
        }
    }

    public void onClearClick(View view) {
        if(mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            mCountDownMillis = 0;

            updateTimer(mCountDownMillis);
        }
    }

    public void onClearLongClick(View view) {
        mTimerMode = rotate(mTimerMode, 1);
        mTimerModeView.setText(mTimerMode.getTextRes());
    }

    public void onProgramClick(View view) {
        if(mTimer == null) {
            mCountDownMillis += mTimerInterval.getMillis(); // one minute
            updateTimer(mCountDownMillis / 1000);
        }
    }

    public void onProgramLongClick(View view) {
        mTimerInterval = rotate(mTimerInterval, 1);
        mTimerIntervalView.setText(mTimerInterval.getTextRes());
    }

    private RegattaCountDownTimer createCountDownTimer(long totalMillis) {
        return new RegattaCountDownTimer(totalMillis, 1000) {

            public void onTick(long millisUntilFinished) {
                long secondsUntilFinished = millisUntilFinished / 1000;
                updateTimer(secondsUntilFinished);
            }

            public void onFinish() {
                updateTimer(0);
                mTimer = createStopWatch(-1).start();
            }
        };
    }

    private StopWatch createStopWatch(long totalMillis) {
        return new StopWatch(totalMillis, 1000) {

            public void onTick(long millisCounted) {
                long secondsCounted = millisCounted / 1000;
                updateTimer(secondsCounted);
            }

            public void onFinish() {
            }
        };
    }

    private void updateTimer(long seconds) {
        mTimerView.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
    }

    private static <T extends Enum<T>> T rotate(T current, int increment) {
        T[] values = current.getDeclaringClass().getEnumConstants();
        int i = current.ordinal() + increment;
        while (i<0) i+= values.length;
        return values[i%values.length];
    }
}
