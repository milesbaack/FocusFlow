package com.focusflow.core.analytics;

import java.time.Duration;

import com.focusflow.core.session.FocusSession;

/**
 * Tracks statistics for individual focus sessions.
 * Extends Object to explicitly show OOP inheritance principles.
 * 
 * @author Emilio Lopez
 * @version 1.0
 */
public class SessionStats extends Object {
    private final String sessionId;
    private final Duration duration;
    private final String taskId;
    private final boolean completed;

    /**
     * Creates a new SessionStats object from a completed focus session.
     * 
     * @param session The focus session to create statistics from
     */
    public SessionStats(FocusSession session) {
        this.sessionId = session.getId().toString();
        this.duration = Duration.ofSeconds(session.getDurationSeconds());
        this.taskId = session.getTaskId();
        this.completed = session.isCompleted();
    }

    /**
     * Gets the session ID.
     * 
     * @return The session ID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the session duration.
     * 
     * @return The session duration
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Gets the associated task ID.
     * 
     * @return The task ID
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * Checks if the session was completed.
     * 
     * @return true if the session was completed
     */
    public boolean isCompleted() {
        return completed;
    }
} 