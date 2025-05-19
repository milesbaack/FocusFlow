package com.focusflow.core.session;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import com.focusflow.core.timer.TimerType;

/**
 * Represents a single focus session for a task.
 * 
 * This class tracks the duration and state of a focus session,
 * including start and end times, pauses, and completion status.
 */
public class FocusSession implements Serializable {
   
    private final UUID id;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lastPauseTime;
    private long durationSeconds;
    private long pausedDurationSeconds;
    private final String associatedTaskId;
    private boolean completed;
    private boolean paused;
    private boolean isConsecutiveSession;
    private TimerType timerType;

    /**
     * Creates a new focus session for a task.
     * 
     * @param associatedTaskId The ID of the task this session is for
     * @throws IllegalArgumentException if the task ID is null or empty
     */
    public FocusSession(String associatedTaskId) {
        if (associatedTaskId == null || associatedTaskId.trim().isEmpty()) {
            throw new IllegalArgumentException("Task ID cannot be null or empty");
        }
        this.id = UUID.randomUUID();
        this.startTime = LocalDateTime.now();
        this.associatedTaskId = associatedTaskId;
        this.completed = false;
        this.paused = false;
        this.pausedDurationSeconds = 0;
        this.isConsecutiveSession = false;
        this.timerType = TimerType.WORK;
    }

    /**
     * Creates a new focus session with specific start and end times.
     * 
     * @param associatedTaskId The ID of the task this session is for
     * @param startTime The start time of the session
     * @param endTime The end time of the session
     * @param timerType The type of timer for this session
     */
    public FocusSession(String associatedTaskId, LocalDateTime startTime, LocalDateTime endTime, TimerType timerType) {
        if (associatedTaskId == null || associatedTaskId.trim().isEmpty()) {
            throw new IllegalArgumentException("Task ID cannot be null or empty");
        }
        this.id = UUID.randomUUID();
        this.startTime = startTime;
        this.endTime = endTime;
        this.associatedTaskId = associatedTaskId;
        this.completed = endTime != null;
        this.paused = false;
        this.pausedDurationSeconds = 0;
        this.isConsecutiveSession = false;
        this.timerType = timerType;
        if (this.completed) {
            this.durationSeconds = Duration.between(startTime, endTime).getSeconds();
        }
    }

    /**
     * Pauses the current session.
     * 
     * @throws IllegalStateException if the session is already paused or completed
     */
    public void pauseSession() {
        if (paused) {
            throw new IllegalStateException("Session is already paused");
        }
        if (completed) {
            throw new IllegalStateException("Cannot pause a completed session");
        }
        lastPauseTime = LocalDateTime.now();
        paused = true;
    }

    /**
     * Resumes a paused session.
     * 
     * @throws IllegalStateException if the session is not paused or is completed
     */
    public void resumeSession() {
        if (!paused) {
            throw new IllegalStateException("Session is not paused");
        }
        if (completed) {
            throw new IllegalStateException("Cannot resume a completed session");
        }
        pausedDurationSeconds += Duration.between(lastPauseTime, LocalDateTime.now()).getSeconds();
        paused = false;
    }

    /**
     * Ends the current session.
     * 
     * @throws IllegalStateException if the session is already completed
     */
    public void endSession() {
        if (completed) {
            throw new IllegalStateException("Session is already completed");
        }
        this.endTime = LocalDateTime.now();
        this.durationSeconds = Duration.between(startTime, endTime).getSeconds() - pausedDurationSeconds;
        this.completed = true;
    }

    /**
     * Gets the unique identifier for this session.
     * 
     * @return The session's UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Gets the time when the session started.
     * 
     * @return The session start time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Gets the time when the session ended.
     * 
     * @return The session end time, or null if the session is still active
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Gets the total duration of the session in seconds.
     * 
     * @return The session duration in seconds
     */
    public long getDurationSeconds() {
        return durationSeconds;
    }

    /**
     * Gets the ID of the task associated with this session.
     * 
     * @return The associated task ID
     */
    public String getAssociatedTaskId() {
        return associatedTaskId;
    }

    /**
     * Checks if the session has been completed.
     * 
     * @return true if the session is completed, false otherwise
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Checks if the session is currently paused.
     * 
     * @return true if the session is paused, false otherwise
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Gets the total time the session has been paused.
     * 
     * @return The paused duration in seconds
     */
    public long getPausedDurationSeconds() {
        return pausedDurationSeconds;
    }

    /**
     * Checks if this session is consecutive to another session.
     * 
     * @return true if this is a consecutive session
     */
    public boolean isConsecutive() {
        return isConsecutiveSession;
    }

    /**
     * Marks this session as consecutive based on the previous session's end time.
     * 
     * @param previousEndTime The end time of the previous session
     * @param breakDuration The duration of the break in minutes
     * @param toleranceMinutes The tolerance for break timing in minutes
     * @return true if the session was marked as consecutive
     */
    public boolean markAsConsecutiveSession(LocalDateTime previousEndTime, int breakDuration, int toleranceMinutes) {
        if (previousEndTime == null) {
            return false;
        }

        Duration timeSinceLastSession = Duration.between(previousEndTime, startTime);
        long breakDurationSeconds = breakDuration * 60;
        long toleranceSeconds = toleranceMinutes * 60;

        // Check if the time between sessions is within the break duration plus tolerance
        isConsecutiveSession = timeSinceLastSession.getSeconds() <= (breakDurationSeconds + toleranceSeconds);
        return isConsecutiveSession;
    }

    /**
     * Gets the timer type for this session.
     * 
     * @return The timer type
     */
    public TimerType getTimerType() {
        return timerType;
    }

    /**
     * Sets the timer type for this session.
     * 
     * @param timerType The new timer type
     */
    public void setTimerType(TimerType timerType) {
        this.timerType = timerType;
    }
}