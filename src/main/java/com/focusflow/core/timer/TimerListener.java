package com.focusflow.core.timer;

public interface TimerListener {
    void onTick(long secondsRemaining);

    void onFinish();
}