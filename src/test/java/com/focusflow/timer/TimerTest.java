/**
 * Class to debug PomodoroTimer
 * @author Emilio Lopez
 * @version 1.0.0
 */

package  com.focusflow.timer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.focusflow.core.timer.PomodoroTimer;
import com.focusflow.core.timer.TimerListener;

public class TimerTest{
    // Create instances for Pomodoro and listener.
    private PomodoroTimer timer;
    private TestTimerListener listener;

    /**
     * Helper class to track timer events
     */
    private class TestTimerListener implements TimerListener{
        public long lastTickValue;
        public boolean finished;

        @Override
        public void onTick(long secondsRemaining){
            lastTickValue = secondsRemaining;
        }

        @Override
        public void onFinish(){
            finished = true;
        }
    }

    /**
     * Establish setup for each test, initiating PomodoroTimer and Listener.
     */
    @BeforeEach
    public void setup(){
        // Create instance of TestTimerListener
        listener = new TestTimerListener();

        // Create instance of Pomodoro Timer with 10 second test
        timer = new PomodoroTimer(10, listener);
    }

    /**
     * Test initial state of the Timer
     */
    @Test
    public void testInitialState(){
        assertEquals(10, timer.getSecondsRemaining());
        assertFalse(timer.isRunning());
    }

    /**
     * Test if timer starts correctly.
     */
    @Test
    public void testStartTimer(){
        timer.start();
        assertTrue(timer.isRunning());
    }

    /**
     * Test if timer pauses correctly.
     */
    @Test
    public void testPauseTimer(){
        timer.start();
        timer.pause();
        assertFalse(timer.isRunning());
    }

    /**
     * Test if timer resets correctly.
     */
    @Test
    public void testResetTimer(){
        timer.start();
        try {
            // Delay by 2 seconds
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        timer.reset();
        assertEquals(10, timer.getSecondsRemaining());
        assertFalse(timer.isRunning());
    }

    /**
     * Test timer tick
     */
    @Test
    public void testTimerTick(){
        timer.start();
        try {
            Thread.sleep(2000);
        } catch ( InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(timer.getSecondsRemaining() < 10);
    }

    /**
     * Test Listener Notification system
     */
    @Test
    public void testListenerNotification(){
        // Create 2 second timer
        timer = new PomodoroTimer(2, listener);
        timer.start();

        // Tick occurs every SECOND i.e. you can establish how you
        // update secondsRemaining if you need greater precision.
        try {
            // Let more than 2 seconds pass
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Check if timer is finsihed ( more than 2 seconds passed. )
        assertTrue(listener.finished);
        
    }
}
