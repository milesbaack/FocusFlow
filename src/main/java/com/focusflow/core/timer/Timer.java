package com.focusflow.core.timer;

/**
 * Interface for timer functionality in the FocusFlow application.
 * 
 * This interface defines the core functionality required for any timer
 * implementation,
 * including starting, pausing, resuming, stopping, and resetting the timer. It
 * also
 * provides methods to query the timer's state and remaining time.
 * 
 * @author Miles Baack
 * @version 1.0
 * @see com.focusflow.core.timer.PomodoroTimer
 * @see com.focusflow.core.timer.TimerEventListener
 */
public interface Timer {
    /**
     * Starts the timer countdown.
     */
    void start();

    /**
     * Pauses the timer at its current time.
     */
    void pause();

    /**
     * Resumes the timer from its paused state.
     */
    void resume();

    /**
     * Stops the timer before completion.
     */
    void stop();

    /**
     * Resets the timer to its initial state.
     */
    void reset();

    /**
     * Returns the elapsed time in milliseconds.
     * 
     * @return the elapsed time in milliseconds
     */
    long getElapsedTime();

    /**
     * Returns the remaining time in seconds.
     * 
     * @return the remaining time in seconds
     */
    int getRemainingTime();

    /**
     * Returns the current state of the timer.
     * 
     * @return the current timer state
     */
    TimerState getState();

    /**
     * Returns the type of the timer.
     * 
     * @return the timer type
     */
    TimerType getType();

    /**
     * Adds a listener to be notified of timer events.
     * 
     * @param listener the listener to add
     */
    void addListener(TimerEventListener listener);

    /**
     * Removes a listener from the timer.
     * 
     * @param listener the listener to remove
     */
    void removeListener(TimerEventListener listener);
}
