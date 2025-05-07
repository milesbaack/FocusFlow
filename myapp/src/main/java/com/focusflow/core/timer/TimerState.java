package com.focusflow.core.timer;

/**
 * Represents the possible states of a timer in the FocusFlow application.
 * 
 * This enum defines all possible states that a timer can be in, including
 * inactive, running, paused, and completed states. Each state represents
 * a specific phase in the timer's lifecycle.
 * 
 * @author Miles Baack
 * @version 1.0
 * @see com.focusflow.core.timer.Timer
 */
public enum TimerState {
    /**
     * Timer is not running and has not been started.
     */
    INACTIVE,
    
    /**
     * Timer is currently running and counting down.
     */
    RUNNING,
    
    /**
     * Timer is paused and can be resumed.
     */
    PAUSED,
    
    /**
     * Timer has completed its countdown.
     */
    COMPLETED,
    
    /**
     * Timer has been stopped before completion.
     */
    STOPPED
} 