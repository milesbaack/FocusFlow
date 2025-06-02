/**
 * Class to debug PomodoroTimer
 * @author Emilio Lopez
 * @version 1.0.0
 */

package com.focusflow.timer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.focusflow.core.timer.PomodoroTimer;
import com.focusflow.core.timer.TimerEventListener;
import com.focusflow.core.timer.TimerState;
import com.focusflow.core.timer.TimerType;

public class TimerTest {
    // Create instances for Pomodoro and listener.
    private PomodoroTimer timer;
    private TestTimerListener listener;

    /**
     * Helper class to track timer events
     */
    private class TestTimerListener implements TimerEventListener {
        public long lastTickValue;
        public boolean finished;

        @Override
        public void onTimerStarted(com.focusflow.core.timer.Timer timer) {
            // Not used in tests
        }

        @Override
        public void onTimerPaused(com.focusflow.core.timer.Timer timer) {
            // Not used in tests
        }

        @Override
        public void onTimerResumed(com.focusflow.core.timer.Timer timer) {
            // Not used in tests
        }

        @Override
        public void onTimerCompleted(com.focusflow.core.timer.Timer timer) {
            finished = true;
        }

        @Override
        public void onTimerStopped(com.focusflow.core.timer.Timer timer) {
            // Not used in tests
        }

        @Override
        public void onTimerReset(com.focusflow.core.timer.Timer timer) {
            // Not used in tests
        }

        @Override
        public void onTimerTick(com.focusflow.core.timer.Timer timer, int secondsRemaining) {
            lastTickValue = secondsRemaining;
        }
    }

    /**
     * Establish setup for each test, initiating PomodoroTimer and Listener.
     */
    @BeforeEach
    public void setup() {
        // Create instance of TestTimerListener
        listener = new TestTimerListener();

        // Create instance of Pomodoro Timer with WORK type
        timer = new PomodoroTimer(TimerType.WORK);
        timer.addListener(listener);
    }

    /**
     * Test initial state of the Timer
     */
    @Test
    public void testInitialState() {
        assertEquals(TimerType.WORK.getDefaultDuration(), timer.getRemainingTime());
        assertEquals(TimerState.INACTIVE, timer.getState());
    }

    /**
     * Test if timer starts correctly.
     */
    @Test
    public void testStartTimer() {
        timer.start();
        assertEquals(TimerState.RUNNING, timer.getState());
    }

    /**
     * Test if timer pauses correctly.
     */
    @Test
    public void testPauseTimer() {
        timer.start();
        timer.pause();
        assertEquals(TimerState.PAUSED, timer.getState());
    }

    /**
     * Test if timer resets correctly.
     */
    @Test
    public void testResetTimer() {
        timer.start();
        try {
            // Delay by 2 seconds
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        timer.reset();
        assertEquals(TimerType.WORK.getDefaultDuration(), timer.getRemainingTime());
        assertEquals(TimerState.INACTIVE, timer.getState());
    }

    /**
     * Test timer tick
     */
    @Test
    public void testTimerTick() {
        timer.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(timer.getRemainingTime() < TimerType.WORK.getDefaultDuration());
    }

    /**
     * Test Listener Notification system
     */
    @Test
    public void testListenerNotification() {
        // Create a short timer for testing
        timer = new PomodoroTimer(TimerType.SHORT_BREAK);
        timer.addListener(listener);
        timer.start();

        try {
            // Let more than the duration pass
            Thread.sleep(TimerType.SHORT_BREAK.getDefaultDuration() * 1000 + 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Check if timer is finished
        assertTrue(listener.finished);
    }
}
