package com.thomashaertel.regattatimer;

import android.app.Activity;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
    private TextView mTimerView;

    private ImageView mArrowUp;
    private ImageView mArrowDown;
    private ImageView mPlus;
    private ImageView mRepeat;

    private ToneGenerator mToneGenerator;

    private long mCountDownMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);

        if(savedInstanceState != null) {
            mTimerInterval = TimerInterval.valueOf(savedInstanceState.getString(TIMER_INTERVAL, TimerInterval.M5310.name()));
            mTimerMode = TimerMode.valueOf(savedInstanceState.getString(TIMER_MODE, TimerMode.UPDOWN.name()));
        }

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTimerIntervalView = (TextView) stub.findViewById(R.id.timerInterval);
                mRepeat = (ImageView) stub.findViewById(R.id.timerModeRepeat);
                mArrowDown = (ImageView) stub.findViewById(R.id.timerModeDown);
                mPlus = (ImageView) stub.findViewById(R.id.timerModePlus);
                mArrowUp = (ImageView) stub.findViewById(R.id.timerModeUp);
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

                updateTimerSettings();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mToneGenerator.release();
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

        updateTimerSettings();
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
        }

        mCountDownMillis = 0;
        updateTimer(mCountDownMillis);
        updateTimerSettings();
    }

    public void onClearLongClick(View view) {
        mTimerMode = rotate(mTimerMode, 1);
        updateTimerSettings();
    }

    public void onProgramClick(View view) {
        if(mTimer == null) {
            if(mCountDownMillis < TimerInterval.M5310.getMillis() || !(mTimerMode == TimerMode.REPEATING && mTimerInterval == TimerInterval.M5310)) {
                mCountDownMillis += mTimerInterval.getMillis();
                if (mCountDownMillis > 3600000)
                    mCountDownMillis = 0;
            }
        }

        updateTimer(mCountDownMillis / 1000);
    }

    public void onProgramLongClick(View view) {
        mTimerInterval = rotate(mTimerInterval, 1);
        updateTimerSettings();
    }

    private RegattaCountDownTimer createCountDownTimer(long totalMillis) {
        return new RegattaCountDownTimer(totalMillis, 1000) {

            public void onTick(long millisUntilFinished) {
                long secondsUntilFinished = millisUntilFinished / 1000;

                if(secondsUntilFinished % 60 == 0) {
                    mToneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
                } else if(secondsUntilFinished < 60) {
                    if(secondsUntilFinished % 10 == 0) {
                        mToneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
                    } else if (secondsUntilFinished <= 15) {
                        mToneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
                    } else if(secondsUntilFinished < 10) {
                        mToneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2);
                    }
                }

                updateTimer(secondsUntilFinished);
            }

            public void onFinish() {
                updateTimer(0);
                //mTimer = createStopWatch(-1).start();

                updateTimerSettings();
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

    private void updateTimerSettings() {
        mTimerIntervalView.setText(mTimerInterval.getTextRes());

        if(mTimer != null && !mTimer.isCancelled()) {
            updateTimerMode(mTimerMode, mTimer instanceof StopWatch, mTimer instanceof RegattaCountDownTimer);
        } else {
            updateTimerMode(mTimerMode, true, true);
        }
    }

    private void updateTimerMode(TimerMode mode, boolean showUp, boolean showDown) {
        mRepeat.setVisibility(mode == TimerMode.REPEATING ? View.VISIBLE : View.INVISIBLE);
        mArrowUp.setVisibility(mode == TimerMode.UPDOWN && showUp ? View.VISIBLE : View.INVISIBLE);
        mArrowDown.setVisibility(mode == TimerMode.UPDOWN && showDown ? View.VISIBLE : View.INVISIBLE);
        mPlus.setVisibility(mode == TimerMode.UPDOWN && showUp && showDown ? View.VISIBLE : View.INVISIBLE);
    }

    private static <T extends Enum<T>> T rotate(T current, int increment) {
        T[] values = current.getDeclaringClass().getEnumConstants();
        int i = current.ordinal() + increment;
        while (i<0) i+= values.length;
        return values[i%values.length];
    }
}
