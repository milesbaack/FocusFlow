package com.focusflow.core.timer;

/**
 * Interface for objects that want to be notified of timer events.
 * 
 * This interface defines methods that will be called when various timer events occur,
 * such as starting, pausing, completing, or ticking. Implementers can use these
 * events to update the UI or perform other actions in response to timer state changes.
 * 
 * @author Miles Baack
 * @version 1.0
 * @see com.focusflow.core.timer.Timer
 */
public interface TimerEventListener {
    /**
     * Called when the timer starts.
     * 
     * @param timer the timer that started
     */
    void onTimerStarted(Timer timer);
    
    /**
     * Called when the timer is paused.
     * 
     * @param timer the timer that was paused
     */
    void onTimerPaused(Timer timer);
    
    /**
     * Called when the timer is resumed after being paused.
     * 
     * @param timer the timer that was resumed
     */
    void onTimerResumed(Timer timer);
    
    /**
     * Called when the timer completes its countdown.
     * 
     * @param timer the timer that completed
     */
    void onTimerCompleted(Timer timer);
    
    /**
     * Called when the timer is stopped before completion.
     * 
     * @param timer the timer that was stopped
     */
    void onTimerStopped(Timer timer);
    
    /**
     * Called when the timer ticks (every second).
     * 
     * @param timer the timer that ticked
     * @param remainingSeconds the number of seconds remaining
     */
    void onTimerTick(Timer timer, int remainingSeconds);
    
    /**
     * Called when the timer is reset to its initial state.
     * 
     * @param timer the timer that was reset
     */
    void onTimerReset(Timer timer);
    
    /**
     * Base implementation that does nothing for all events.
     * Extend this class to override only the methods you need.
     */
    class Adapter implements TimerEventListener {
        @Override
        public void onTimerStarted(Timer timer) {}
        
        @Override
        public void onTimerPaused(Timer timer) {}
        
        @Override
        public void onTimerResumed(Timer timer) {}
        
        @Override
        public void onTimerCompleted(Timer timer) {}
        
        @Override
        public void onTimerStopped(Timer timer) {}
        
        @Override
        public void onTimerTick(Timer timer, int remainingSeconds) {}
        
        @Override
        public void onTimerReset(Timer timer) {}
    }
} 