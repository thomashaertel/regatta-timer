package com.thomashaertel.regattatimer;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    private Timer<?> mTimer;

    private Button mStartStopButton;
    private TextView mTimerView;

    private long mCountDownMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mStartStopButton = (Button) stub.findViewById(R.id.startStopButton);
                mTimerView = (TextView) stub.findViewById(R.id.timer);
            }
        });
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
            if(mTimer instanceof RegattaCountDownTimer2)
                ((RegattaCountDownTimer2) mTimer).sync();
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

    public void onProgramClick(View view) {
        if(mTimer == null) {
            mCountDownMillis += 60000; // one minute
            updateTimer(mCountDownMillis / 1000);
        }
    }

    private RegattaCountDownTimer2 createCountDownTimer(long totalMillis) {
        return new RegattaCountDownTimer2(totalMillis, 1000) {

            public void onTick(long millisUntilFinished) {
                long secondsUntilFinished = millisUntilFinished / 1000;
                updateTimer(secondsUntilFinished);
            }

            public void onFinish() {
                updateTimer(0);
                mTimer = createStopWatch(-1);
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
}
