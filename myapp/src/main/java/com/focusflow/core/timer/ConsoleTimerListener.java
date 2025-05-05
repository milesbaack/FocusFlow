/**
 * A timer event listener that outputs timer events to the console.
 * 
 * This class is primarily used for debugging and testing purposes.
 * It implements the TimerEventListener interface to receive and display
 * timer events in a human-readable format.
 * 
 * @author Miles Baack
 * @version 1.1
 */

package com.focusflow.core.timer;

public class ConsoleTimerListener implements TimerEventListener {
    /**
     * Called when the timer ticks (every second).
     * 
     * @param timer The timer that ticked
     * @param remainingSeconds The number of seconds remaining
     */
    @Override
    public void onTimerTick(Timer timer, int remainingSeconds) {
        System.out.printf("Time remaining: %d seconds%n", remainingSeconds);
    }

    /**
     * Called when the timer completes its countdown.
     * 
     * @param timer The timer that completed
     */
    @Override
    public void onTimerCompleted(Timer timer) {
        System.out.println("Timer completed!");
    }

    /**
     * Called when the timer starts.
     * 
     * @param timer The timer that started
     */
    @Override
    public void onTimerStarted(Timer timer) {
        System.out.println("Timer started!");
    }

    /**
     * Called when the timer is paused.
     * 
     * @param timer The timer that was paused
     */
    @Override
    public void onTimerPaused(Timer timer) {
        System.out.println("Timer paused!");
    }

    /**
     * Called when the timer is resumed after being paused.
     * 
     * @param timer The timer that was resumed
     */
    @Override
    public void onTimerResumed(Timer timer) {
        System.out.println("Timer resumed!");
    }

    /**
     * Called when the timer is stopped before completion.
     * 
     * @param timer The timer that was stopped
     */
    @Override
    public void onTimerStopped(Timer timer) {
        System.out.println("Timer stopped!");
    }

    /**
     * Called when the timer is reset to its initial state.
     * 
     * @param timer The timer that was reset
     */
    @Override
    public void onTimerReset(Timer timer) {
        System.out.println("Timer reset!");
    }
}