package com.thomashaertel.regattatimer;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;

public class MainActivity extends Activity {
    private RegattaCountDownTimer2 mTimer;

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
            mTimer = createTimer(mCountDownMillis);
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
        if(mTimer != null) {
            mTimer.sync();
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
        mCountDownMillis += 60000; // one minute
        updateTimer(mCountDownMillis);
    }

    private RegattaCountDownTimer2 createTimer(long totalMillis) {
        return new RegattaCountDownTimer2(totalMillis, 1000) {

            public void onTick(long millisUntilFinished) {
                long secondsUntilFinished = millisUntilFinished / 1000;
                updateTimer(secondsUntilFinished);
            }

            public void onFinish() {
                mTimerView.setText("GO!");
            }
        };
    }

    private void updateTimer(long millis) {
        mTimerView.setText(String.format("%02d:%02d", millis / 60, millis % 60));
    }
}
