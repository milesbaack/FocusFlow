/**
 * Pomodoro Timer class to handle countdown logic, start/pause/reset, and 
 * notification logging.
 * @author Emilio Lopez & Miles Baack
 * @version 1.0.2 - Fixed timer logic and synchronization
 */

package com.focusflow.core.timer;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import com.focusflow.core.session.SessionManager;

/**
 * Implementation of a Pomodoro technique timer.
 * 
 * This class provides a complete implementation of the Pomodoro technique
 * timer,
 * supporting work sessions, short breaks, and long breaks. It includes
 * event-based
 * notification system and full state management.
 * 
 * @author Miles Baack
 * @version 1.0.2
 * @see com.focusflow.core.timer.Timer
 * @see com.focusflow.core.timer.TimerEventListener
 */
public class PomodoroTimer implements Timer {
    private final List<TimerEventListener> listeners = new ArrayList<>();
    private final TimerType type;
    private final int duration;
    private final AtomicInteger remainingSeconds;
    private volatile TimerState state = TimerState.INACTIVE;
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
     * @param type     the type of timer to create
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
    public synchronized void start() {
        if (state == TimerState.RUNNING) {
            return;
        }

        if (state == TimerState.PAUSED) {
            resume();
            return;
        }

        // Only start session for work timers with valid task ID
        if (type == TimerType.WORK && currentTaskId != null && !currentTaskId.trim().isEmpty()) {
            try {
                sessionManager.startSession(currentTaskId);
            } catch (Exception e) {
                System.err.println("Error starting session: " + e.getMessage());
            }
        }

        state = TimerState.RUNNING;
        startTime = System.currentTimeMillis();
        internalTimer = new java.util.Timer("PomodoroTimer-" + type.name(), true);

        internalTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int remaining = remainingSeconds.get();
                
                if (remaining <= 0) {
                    // Timer completed
                    complete();
                    return;
                }
                
                // Decrement and notify
                remaining = remainingSeconds.decrementAndGet();
                notifyTick(Math.max(0, remaining));
                
                // Check again after decrement
                if (remaining <= 0) {
                    complete();
                }
            }
        }, 1000, 1000); // Start after 1 second, repeat every 1 second

        notifyStarted();
    }

    @Override
    public synchronized void pause() {
        if (state != TimerState.RUNNING) {
            return;
        }

        state = TimerState.PAUSED;
        pauseTime = System.currentTimeMillis();
        
        if (internalTimer != null) {
            internalTimer.cancel();
            internalTimer = null;
        }

        // Pause session if it exists
        try {
            sessionManager.pauseCurrentSession();
        } catch (Exception e) {
            // Session might not exist, ignore
        }

        notifyPaused();
    }

    @Override
    public synchronized void resume() {
        if (state != TimerState.PAUSED) {
            return;
        }

        state = TimerState.RUNNING;
        long pauseDuration = System.currentTimeMillis() - pauseTime;
        startTime += pauseDuration;

        internalTimer = new java.util.Timer("PomodoroTimer-" + type.name(), true);
        internalTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int remaining = remainingSeconds.get();
                
                if (remaining <= 0) {
                    complete();
                    return;
                }
                
                remaining = remainingSeconds.decrementAndGet();
                notifyTick(Math.max(0, remaining));
                
                if (remaining <= 0) {
                    complete();
                }
            }
        }, 1000, 1000);

        // Resume session if it exists
        try {
            sessionManager.resumeCurrentSession();
        } catch (Exception e) {
            // Session might not exist, ignore
        }

        notifyResumed();
    }

    @Override
    public synchronized void stop() {
        if (state == TimerState.INACTIVE || state == TimerState.STOPPED) {
            return;
        }

        state = TimerState.STOPPED;
        if (internalTimer != null) {
            internalTimer.cancel();
            internalTimer = null;
        }

        // End current session if it exists
        try {
            sessionManager.endCurrentSession();
        } catch (Exception e) {
            // Session might not exist, ignore
        }

        notifyStopped();
    }

    @Override
    public synchronized void reset() {
        stop();
        state = TimerState.INACTIVE;
        remainingSeconds.set(duration);
        elapsedTime = 0;
        startTime = 0;
        pauseTime = 0;

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

        if (state == TimerState.RUNNING) {
            return System.currentTimeMillis() - startTime;
        }

        return elapsedTime;
    }

    @Override
    public int getRemainingTime() {
        return Math.max(0, remainingSeconds.get());
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
        if (listener != null) {
            synchronized (listeners) {
                listeners.add(listener);
            }
        }
    }

    @Override
    public void removeListener(TimerEventListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private synchronized void complete() {
        if (state != TimerState.RUNNING) {
            return; // Already completed or stopped
        }
        
        state = TimerState.COMPLETED;
        if (internalTimer != null) {
            internalTimer.cancel();
            internalTimer = null;
        }
        elapsedTime = duration * 1000L;
        remainingSeconds.set(0);

        // End current session if it exists
        try {
            sessionManager.endCurrentSession();
        } catch (Exception e) {
            // Session might not exist, ignore
        }

        notifyCompleted();
    }

    private void notifyStarted() {
        synchronized (listeners) {
            for (TimerEventListener listener : listeners) {
                try {
                    listener.onTimerStarted(this);
                } catch (Exception e) {
                    System.err.println("Error notifying timer started: " + e.getMessage());
                }
            }
        }
    }

    private void notifyPaused() {
        synchronized (listeners) {
            for (TimerEventListener listener : listeners) {
                try {
                    listener.onTimerPaused(this);
                } catch (Exception e) {
                    System.err.println("Error notifying timer paused: " + e.getMessage());
                }
            }
        }
    }

    private void notifyResumed() {
        synchronized (listeners) {
            for (TimerEventListener listener : listeners) {
                try {
                    listener.onTimerResumed(this);
                } catch (Exception e) {
                    System.err.println("Error notifying timer resumed: " + e.getMessage());
                }
            }
        }
    }

    private void notifyCompleted() {
        synchronized (listeners) {
            for (TimerEventListener listener : listeners) {
                try {
                    listener.onTimerCompleted(this);
                } catch (Exception e) {
                    System.err.println("Error notifying timer completed: " + e.getMessage());
                }
            }
        }
    }

    private void notifyStopped() {
        synchronized (listeners) {
            for (TimerEventListener listener : listeners) {
                try {
                    listener.onTimerStopped(this);
                } catch (Exception e) {
                    System.err.println("Error notifying timer stopped: " + e.getMessage());
                }
            }
        }
    }

    private void notifyTick(int remainingSeconds) {
        synchronized (listeners) {
            for (TimerEventListener listener : listeners) {
                try {
                    listener.onTimerTick(this, remainingSeconds);
                } catch (Exception e) {
                    System.err.println("Error notifying timer tick: " + e.getMessage());
                }
            }
        }
    }

    private void notifyReset() {
        synchronized (listeners) {
            for (TimerEventListener listener : listeners) {
                try {
                    listener.onTimerReset(this);
                } catch (Exception e) {
                    System.err.println("Error notifying timer reset: " + e.getMessage());
                }
            }
        }
    }
}