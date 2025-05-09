package com.focusflow.core.timer;

/**
 * Represents the different types of timers available in the FocusFlow application.
 * 
 * This enum defines the various timer types that can be used, including
 * work sessions, short breaks, and long breaks. Each type has a default
 * duration associated with it.
 * 
 * @author Miles Baack
 * @version 1.0
 * @see com.focusflow.core.timer.Timer
 */
public enum TimerType {
    /**
     * A work session timer, typically 25 minutes.
     */
    WORK(25 * 60),
    
    /**
     * A short break timer, typically 5 minutes.
     */
    SHORT_BREAK(5 * 60),
    
    /**
     * A long break timer, typically 15 minutes.
     */
    LONG_BREAK(15 * 60),
    
    /**
     * A custom timer with user-defined duration.
     */
    CUSTOM(0),
    
    /**
     * A break timer, typically 5 minutes.
     */
    BREAK(5 * 60);
    
    private final int defaultDuration;
    
    /**
     * Creates a new TimerType with the specified default duration.
     * 
     * @param defaultDuration the default duration in seconds
     */
    TimerType(int defaultDuration) {
        this.defaultDuration = defaultDuration;
    }
    
    /**
     * Returns the default duration for this timer type in seconds.
     * 
     * @return the default duration in seconds
     */
    public int getDefaultDuration() {
        return defaultDuration;
    }
} 