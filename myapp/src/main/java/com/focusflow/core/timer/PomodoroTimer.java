/**
 * Pomodoro Timer class to handle countdown logic, start/pause/reset, and 
 * notification logging.
 * @author Emilio Lopez
 * @version 1.0.1
 */

// TODO: Refactor internal timer reset
package com.focusflow.core.timer;

import java.util.Timer;
import java.util.TimerTask;

import com.focusflow.core.session.SessionManager;

public class PomodoroTimer {
    private final SessionManager sessionManager;
    private long totalSeconds;
    private long secondsRemaining;
    private boolean running = false;
    private TimerListener listener;
    private Timer timer;

    /**
     * Default constructor
     */
    public PomodoroTimer(long totalSeconds, TimerListener listener){
        this.totalSeconds = totalSeconds;
        this.secondsRemaining = totalSeconds;
        this.listener = listener;
        this.sessionManager = new SessionManager();
    }

    /**
     * Start or resume timer countdown. Contains anonymous function to handle countdown logic.
     */
    public void start(){
        // If already running, do nothing
        // TODO: Fix redundant return
        if(running){
            return;
        }

        // Else, start running
        running = true;

        // TODO: Session ID when starting new session i.e. integrate with current task.
        // Start new session
        sessionManager.startSession(null);

        // Create new Timer instance
        timer = new Timer();

        // Start running
        // Used Timer documentation from Oracle
        // Anonymous function allows access to timer's private fields ( also just one task :p)
        timer.scheduleAtFixedRate(new TimerTask() {
            // Setup anonymous TimerTask for countdown logic
            @Override
            public void run(){
                if(secondsRemaining > 0){
                    // Decrease time by 1 second.
                    secondsRemaining--;

                    // Notify listener of new time value
                    if(listener != null){
                        listener.onTick(secondsRemaining);
                    }
                }

                else{
                    // Stop timer
                    running = false;
                    if(timer != null){
                        timer.cancel();
                        timer = null;
                    }

                    // Update listener
                    if (listener != null){
                        listener.onFinish();
                    }
                }
            }
        }, 1000, 1000); // Delay by 1 seconds and repeat every second.
    }

    /**
     * Pause timer to current time
     */
    public void pause(){
        // If already paused, exit
        if(!running){
            return;
        }
        // Change timer status
        running = false;

        if(timer != null){
            timer.cancel();
        }
    }

    /**
     * Reset timer to intial duration
     */
    public void reset(){
        // Pause clock
        pause();

        // Reset seconds remaining
        secondsRemaining = totalSeconds;

        // Reset listener
        if(listener != null){
            listener.onTick(secondsRemaining);
        }
    }

    /**
     * Check if timer is running
     * @return {@code true} if timer is running; {@code false} otherwise
     */
    public boolean isRunning(){
        return running;
    }

    /**
     * Get remaining time in seconds
     * @return number of seconds remaining
     */
    public long getSecondsRemaining(){
        return secondsRemaining;
    }

    /**
     * Stop timer and reset timer instance.
     */
    private void stop(){
        running = false;
        if ( timer != null){
            timer.cancel();
            timer = null;
        }
        // End current session
        sessionManager.endCurrentSession();
    }
}