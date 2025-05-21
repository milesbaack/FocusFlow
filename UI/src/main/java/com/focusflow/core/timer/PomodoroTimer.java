/**
 * Pomodoro Timer class to handle countdown logic, start/pause/reset, and 
 * notification logging.
 * @author Emilio Lopez
 * @version 1.0.1
 */

// TODO: Refactor internal timer reset
package com.focusflow.core.timer;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import com.focusflow.core.session.SessionManager;

/**
 * Implementation of a Pomodoro technique timer.
 * 
 * This class provides a complete implementation of the Pomodoro technique timer,
 * supporting work sessions, short breaks, and long breaks. It includes event-based
 * notification system and full state management.
 * 
 * @author Miles Baack
 * @version 1.0
 * @see com.focusflow.core.timer.Timer
 * @see com.focusflow.core.timer.TimerEventListener
 */
public class PomodoroTimer implements Timer {
    private final List<TimerEventListener> listeners = new ArrayList<>();
    private final TimerType type;
    private final int duration;
    private final AtomicInteger remainingSeconds;
    private TimerState state = TimerState.INACTIVE;
    private java.util.Timer internalTimer;
    private long startTime;
    private long pauseTime;
    private long elapsedTime;
    private final SessionManager sessionManager;
    private String currentTaskId;
    
    /**
     * Creates a new PomodoroTimer with the specified type.
     * 
     * @param type the type of timer to create
     */
    public PomodoroTimer(TimerType type) {
        this(type, type.getDefaultDuration());
    }
    
    /**
     * Creates a new PomodoroTimer with the specified type and duration.
     * 
     * @param type the type of timer to create
     * @param duration the duration in seconds
     */
    public PomodoroTimer(TimerType type, int duration) {
        this.type = type;
        this.duration = duration;
        this.remainingSeconds = new AtomicInteger(duration);
        this.sessionManager = new SessionManager();
    }
    
    /**
     * Sets the current task ID for session tracking.
     * 
     * @param taskId the ID of the current task
     */
    public void setCurrentTaskId(String taskId) {
        this.currentTaskId = taskId;
    }
    
    @Override
    public void start() {
        if (state == TimerState.RUNNING) {
            return;
        }
        
        if (state == TimerState.PAUSED) {
            resume();
            return;
        }
        
        state = TimerState.RUNNING;
        startTime = System.currentTimeMillis();
        internalTimer = new java.util.Timer();
        
        // Start new session
        sessionManager.startSession(currentTaskId);
        
        internalTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int remaining = remainingSeconds.decrementAndGet();
                notifyTick(remaining);
                
                if (remaining <= 0) {
                    complete();
                }
            }
        }, 0, 1000);
        
        notifyStarted();
    }
    
    @Override
    public void pause() {
        if (state != TimerState.RUNNING) {
            return;
        }
        
        state = TimerState.PAUSED;
        pauseTime = System.currentTimeMillis();
        internalTimer.cancel();
        internalTimer = null;
        
        notifyPaused();
    }
    
    @Override
    public void resume() {
        if (state != TimerState.PAUSED) {
            return;
        }
        
        state = TimerState.RUNNING;
        long pauseDuration = System.currentTimeMillis() - pauseTime;
        startTime += pauseDuration;
        
        internalTimer = new java.util.Timer();
        internalTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int remaining = remainingSeconds.decrementAndGet();
                notifyTick(remaining);
                
                if (remaining <= 0) {
                    complete();
                }
            }
        }, 0, 1000);
        
        notifyResumed();
    }
    
    @Override
    public void stop() {
        if (state == TimerState.INACTIVE || state == TimerState.STOPPED) {
            return;
        }
        
        state = TimerState.STOPPED;
        if (internalTimer != null) {
            internalTimer.cancel();
            internalTimer = null;
        }
        
        // End current session
        sessionManager.endCurrentSession();
        
        notifyStopped();
    }
    
    @Override
    public void reset() {
        stop();
        state = TimerState.INACTIVE;
        remainingSeconds.set(duration);
        elapsedTime = 0;
        
        notifyReset();
    }
    
    @Override
    public long getElapsedTime() {
        if (state == TimerState.INACTIVE) {
            return 0;
        }
        
        if (state == TimerState.PAUSED) {
            return pauseTime - startTime;
        }
        
        return System.currentTimeMillis() - startTime;
    }
    
    @Override
    public int getRemainingTime() {
        return remainingSeconds.get();
    }
    
    @Override
    public TimerState getState() {
        return state;
    }
    
    @Override
    public TimerType getType() {
        return type;
    }
    
    @Override
    public void addListener(TimerEventListener listener) {
        listeners.add(listener);
    }
    
    @Override
    public void removeListener(TimerEventListener listener) {
        listeners.remove(listener);
    }
    
    private void complete() {
        state = TimerState.COMPLETED;
        if (internalTimer != null) {
            internalTimer.cancel();
            internalTimer = null;
        }
        elapsedTime = duration * 1000;
        
        // End current session
        sessionManager.endCurrentSession();
        
        notifyCompleted();
    }
    
    private void notifyStarted() {
        for (TimerEventListener listener : listeners) {
            listener.onTimerStarted(this);
        }
    }
    
    private void notifyPaused() {
        for (TimerEventListener listener : listeners) {
            listener.onTimerPaused(this);
        }
    }
    
    private void notifyResumed() {
        for (TimerEventListener listener : listeners) {
            listener.onTimerResumed(this);
        }
    }
    
    private void notifyCompleted() {
        for (TimerEventListener listener : listeners) {
            listener.onTimerCompleted(this);
        }
    }
    
    private void notifyStopped() {
        for (TimerEventListener listener : listeners) {
            listener.onTimerStopped(this);
        }
    }
    
    private void notifyTick(int remainingSeconds) {
        for (TimerEventListener listener : listeners) {
            listener.onTimerTick(this, remainingSeconds);
        }
    }
    
    private void notifyReset() {
        for (TimerEventListener listener : listeners) {
            listener.onTimerReset(this);
        }
    }
}