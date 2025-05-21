package com.focusflow.timer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.focusflow.core.timer.PomodoroTimer;
import com.focusflow.core.timer.Timer;
import com.focusflow.core.timer.TimerEventListener;
import com.focusflow.core.timer.TimerState;
import com.focusflow.core.timer.TimerType;

/**
 * Test class for the PomodoroTimer class.
 * 
 * This class contains unit tests for verifying the functionality of the PomodoroTimer,
 * including timer state management, duration tracking, and event notifications.
 * 
 * @author Emilio Lopez
 * @version 1.0.0
 * @see com.focusflow.core.timer.PomodoroTimer
 */
class PomodoroTimerTest {
    private PomodoroTimer timer;
    private TestTimerListener listener;
    private static final String TEST_TASK_ID = "test-task-id";

    @BeforeEach
    void setUp() {
        timer = new PomodoroTimer(TimerType.WORK);
        listener = new TestTimerListener();
        timer.addListener(listener);
        timer.setCurrentTaskId(TEST_TASK_ID);
    }

    @Test
    void testInitialState() {
        assertEquals(TimerState.INACTIVE, timer.getState());
        assertEquals(TimerType.WORK.getDefaultDuration(), timer.getRemainingTime());
        assertEquals(0, timer.getElapsedTime());
    }

    @Test
    void testStartTimer() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        listener.setOnStartCallback(() -> startLatch.countDown());

        timer.start();
        assertTrue(startLatch.await(1, TimeUnit.SECONDS));
        assertEquals(TimerState.RUNNING, timer.getState());
    }

    @Test
    void testPauseTimer() throws InterruptedException {
        CountDownLatch pauseLatch = new CountDownLatch(1);
        listener.setOnPauseCallback(() -> pauseLatch.countDown());

        timer.start();
        Thread.sleep(100); // Let it run briefly
        timer.pause();
        
        assertTrue(pauseLatch.await(1, TimeUnit.SECONDS));
        assertEquals(TimerState.PAUSED, timer.getState());
        assertTrue(timer.getElapsedTime() > 0);
    }

    @Test
    void testResumeTimer() throws InterruptedException {
        CountDownLatch resumeLatch = new CountDownLatch(1);
        listener.setOnResumeCallback(() -> resumeLatch.countDown());

        timer.start();
        Thread.sleep(100);
        timer.pause();
        Thread.sleep(100);
        timer.resume();
        
        assertTrue(resumeLatch.await(1, TimeUnit.SECONDS));
        assertEquals(TimerState.RUNNING, timer.getState());
    }

    @Test
    void testStopTimer() throws InterruptedException {
        CountDownLatch stopLatch = new CountDownLatch(1);
        listener.setOnStopCallback(() -> stopLatch.countDown());

        timer.start();
        Thread.sleep(100);
        timer.stop();
        
        assertTrue(stopLatch.await(1, TimeUnit.SECONDS));
        assertEquals(TimerState.STOPPED, timer.getState());
    }

    @Test
    void testResetTimer() throws InterruptedException {
        CountDownLatch resetLatch = new CountDownLatch(1);
        listener.setOnResetCallback(() -> resetLatch.countDown());

        timer.start();
        Thread.sleep(100);
        timer.reset();
        
        assertTrue(resetLatch.await(1, TimeUnit.SECONDS));
        assertEquals(TimerState.INACTIVE, timer.getState());
        assertEquals(0, timer.getElapsedTime());
        assertEquals(TimerType.WORK.getDefaultDuration(), timer.getRemainingTime());
    }

    @Test
    void testTimerCompletion() throws InterruptedException {
        CountDownLatch completeLatch = new CountDownLatch(1);
        listener.setOnCompleteCallback(() -> completeLatch.countDown());

        // Create a test timer with 2 seconds duration
        PomodoroTimer testTimer = new PomodoroTimer(TimerType.SHORT_BREAK, 2); // 2 seconds for testing
        testTimer.addListener(listener);
        testTimer.setCurrentTaskId(TEST_TASK_ID);
        testTimer.start();
        
        // Wait for completion (2 seconds + buffer)
        assertTrue(completeLatch.await(3, TimeUnit.SECONDS));
        assertEquals(TimerState.COMPLETED, testTimer.getState());
    }

    @Test
    void testTimerTickEvents() throws InterruptedException {
        CountDownLatch tickLatch = new CountDownLatch(3);
        listener.setOnTickCallback(() -> tickLatch.countDown());

        timer.start();
        assertTrue(tickLatch.await(4, TimeUnit.SECONDS));
    }

    @Test
    void testTimerTypeChanges() {
        timer = new PomodoroTimer(TimerType.LONG_BREAK);
        timer.setCurrentTaskId(TEST_TASK_ID);
        assertEquals(TimerType.LONG_BREAK.getDefaultDuration(), timer.getRemainingTime());
        
        timer = new PomodoroTimer(TimerType.SHORT_BREAK);
        timer.setCurrentTaskId(TEST_TASK_ID);
        assertEquals(TimerType.SHORT_BREAK.getDefaultDuration(), timer.getRemainingTime());
    }

    private static class TestTimerListener implements TimerEventListener {
        private Runnable onStartCallback;
        private Runnable onPauseCallback;
        private Runnable onResumeCallback;
        private Runnable onStopCallback;
        private Runnable onResetCallback;
        private Runnable onCompleteCallback;
        private Runnable onTickCallback;

        void setOnStartCallback(Runnable callback) { this.onStartCallback = callback; }
        void setOnPauseCallback(Runnable callback) { this.onPauseCallback = callback; }
        void setOnResumeCallback(Runnable callback) { this.onResumeCallback = callback; }
        void setOnStopCallback(Runnable callback) { this.onStopCallback = callback; }
        void setOnResetCallback(Runnable callback) { this.onResetCallback = callback; }
        void setOnCompleteCallback(Runnable callback) { this.onCompleteCallback = callback; }
        void setOnTickCallback(Runnable callback) { this.onTickCallback = callback; }

        @Override
        public void onTimerStarted(Timer timer) {
            if (onStartCallback != null) onStartCallback.run();
        }

        @Override
        public void onTimerPaused(Timer timer) {
            if (onPauseCallback != null) onPauseCallback.run();
        }

        @Override
        public void onTimerResumed(Timer timer) {
            if (onResumeCallback != null) onResumeCallback.run();
        }

        @Override
        public void onTimerStopped(Timer timer) {
            if (onStopCallback != null) onStopCallback.run();
        }

        @Override
        public void onTimerReset(Timer timer) {
            if (onResetCallback != null) onResetCallback.run();
        }

        @Override
        public void onTimerCompleted(Timer timer) {
            if (onCompleteCallback != null) onCompleteCallback.run();
        }

        @Override
        public void onTimerTick(Timer timer, int remainingSeconds) {
            if (onTickCallback != null) onTickCallback.run();
        }
    }
} 
