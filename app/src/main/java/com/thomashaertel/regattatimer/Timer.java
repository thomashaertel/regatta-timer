package com.thomashaertel.regattatimer;

public interface Timer<T> {
    T start();
    T resume();

    boolean isCancelled();
    void cancel();

    long getMillisLeft();


    void onTick(long millisUntilFinished);
    void onFinish();}
