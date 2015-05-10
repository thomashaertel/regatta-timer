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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;

public class MainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String PREF_INTERVAL_MODE ="interval_mode";
    private static final String PREF_TIMER_MODE ="timer_mode";

    private TimerInterval mTimerInterval = TimerInterval.M5410;
    private TimerMode mTimerMode = TimerMode.UPDOWN;

    private Timer<?> mTimer;

    private SharedPreferences mPrefs;

    private Button mProgramButton;
    private Button mClearButton;
    private Button mSyncButton;

    private TextView mTimerIntervalView;
    private TextView mTimerView;

    private ImageView mArrowUp;
    private ImageView mArrowDown;
    private ImageView mPlus;
    private ImageView mRepeat;

    private Vibrator mVibrator;

    private long mCountDownMillis = 0;

    private final long[] mVibrationPatternCountdownFinished = {0, 1000};
    private final long[] mVibrationPatternLastMinuteLastSeconds = {0, 125, 50, 125, 50, 125};
    private final long[] mVibrationPatternMinuteLastSeconds = {0, 125};
    private final long[] mVibrationPatternMinute = {0, 125, 50, 125, 50, 125, 50, 125};
    private final long[] mVibrationPatternLastMinute = {0, 200, 50, 200};
    private final long[] mVibrationPatternLastSeconds = {0, 250, 50, 250, 50, 250};
    //-1 - don't repeat
    private final int mIndexInPatternToRepeat = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // prepare access to shared preferences
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // initialize vibrator
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // initialize ui binding
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

                mSyncButton = (Button) stub.findViewById(R.id.syncButton);
                mProgramButton = (Button) stub.findViewById(R.id.programButton);
                mClearButton = (Button) stub.findViewById(R.id.clearButton);

                updateButtonState(false);
                onSharedPreferenceChanged(mPrefs, null);
            }
        });

        // register preference listener after initialization of ui state
        mPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);

        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        try {
            mTimerInterval = TimerInterval.valueOf(sharedPreferences.getString(PREF_INTERVAL_MODE, TimerInterval.M5410.name()));
        } catch(IllegalArgumentException e) {
            mTimerInterval = TimerInterval.M5410;
            sharedPreferences.edit().putString(PREF_INTERVAL_MODE, mTimerInterval.name()).commit();
        }

        try {
            mTimerMode = TimerMode.valueOf(sharedPreferences.getString(PREF_TIMER_MODE, TimerMode.UPDOWN.name()));
        } catch(IllegalArgumentException e) {
            mTimerMode = TimerMode.UPDOWN;
            sharedPreferences.edit().putString(PREF_TIMER_MODE, mTimerMode.name()).commit();
        }

        updateTimerSettings();
    }

    public void onStartStopClick(View view) {
        if(mTimer == null) {
            if(mCountDownMillis > 0) {
                mTimer = createCountDownTimer(mCountDownMillis);
            } else {
                mTimer = createStopWatch(-1);
            }
            mTimer.start();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            if (!mTimer.isCancelled()) {
                mTimer.cancel();
            } else {
                mTimer.resume();
            }
        }

        updateButtonState(!mTimer.isCancelled());
        updateTimerSettings();
    }

    public void onSyncClick(View view) {
        if(mTimer != null && !mTimer.isCancelled()) {
            if(mTimer instanceof RegattaCountDownTimer)
                ((RegattaCountDownTimer) mTimer).sync();
        }
    }

    public void onClearClick(View view) {
        if(mTimer != null && mTimer.isCancelled()) {
            mTimer = null;
        }

        if(mTimer == null) {
            mCountDownMillis = 0;

            updateTimer(mCountDownMillis);
            updateTimerSettings();

            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

/*    public void onClearLongClick(View view) {
        mTimerMode = rotate(mTimerMode, 1);
        updateTimerSettings();
    }

    public void onProgramLongClick(View view) {
        mTimerInterval = rotate(mTimerInterval, 1);
        updateTimerSettings();
    }*/

    public void onProgramClick(View view) {
        if(mTimer == null) {
            if(mCountDownMillis < TimerInterval.M5410.getMillis() || !(mTimerMode == TimerMode.REPEATING && mTimerInterval == TimerInterval.M5410)) {
                mCountDownMillis += mTimerInterval.getMillis();
                if (mCountDownMillis > 3600000)
                    mCountDownMillis = 0;
            }

            updateTimer(mCountDownMillis / 1000);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public void onTimerClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private RegattaCountDownTimer createCountDownTimer(long totalMillis) {
        return new RegattaCountDownTimer(totalMillis, 1000) {

            public void onTick(long millisUntilFinished) {
                final long secondsUntilFinished = millisUntilFinished / 1000;
                final long secondsUntilNextMinute = secondsUntilFinished % 60;

                if(secondsUntilFinished < 60) { // Last Minute
                    if(secondsUntilFinished < 6) {
                        mVibrator.vibrate(mVibrationPatternLastMinuteLastSeconds, mIndexInPatternToRepeat);
                    } else if(secondsUntilFinished % 10 == 0 || secondsUntilFinished < 10) {
                        mVibrator.vibrate(mVibrationPatternLastMinute, mIndexInPatternToRepeat);
                    } else if (secondsUntilFinished <= 15) {
                        mVibrator.vibrate(mVibrationPatternMinuteLastSeconds, mIndexInPatternToRepeat);
                    }
                } else {
                    if(secondsUntilNextMinute < 6 && secondsUntilNextMinute > 0) {
                        mVibrator.vibrate(mVibrationPatternMinuteLastSeconds, mIndexInPatternToRepeat);
                    } else if(secondsUntilNextMinute == 0) {
                        mVibrator.vibrate(mVibrationPatternMinute, mIndexInPatternToRepeat);
                    }
                }

                updateTimer(secondsUntilFinished);
            }

            public void onFinish() {
                mVibrator.vibrate(mVibrationPatternCountdownFinished, mIndexInPatternToRepeat);

                updateTimer(0);

                if(mTimerMode == TimerMode.REPEATING) {
                    mTimer.start();
                } else {
                    mTimer = createStopWatch(-1).start();
                }

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

    private void updateButtonState(boolean timerRunning) {
        mSyncButton.setVisibility(timerRunning ? View.VISIBLE : View.INVISIBLE);
        mProgramButton.setVisibility(timerRunning ? View.INVISIBLE : View.VISIBLE);
        mClearButton.setVisibility(timerRunning ? View.INVISIBLE : View.VISIBLE);
    }

    private static <T extends Enum<T>> T rotate(T current, int increment) {
        T[] values = current.getDeclaringClass().getEnumConstants();
        int i = current.ordinal() + increment;
        while (i<0) i+= values.length;
        return values[i%values.length];
    }
}
