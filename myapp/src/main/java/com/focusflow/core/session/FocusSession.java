/**
 * Focus session manager for Pomodoro Timer
 * @author Emilio Lopez
 * @version 1.1.0
 */

package  com.focusflow.core.session;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a single focus session for a task.
 * 
 * This class tracks the duration and state of a focus session,
 * including start and end times, pauses, and completion status.
 */
public class FocusSession implements Serializable{
    private final UUID id;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lastPauseTime;
    private long durationSeconds;
    private long pausedDurationSeconds;
    private final String associatedTaskId;
    private boolean completed;
    private boolean paused;

    /**
     * Creates a new focus session for a task.
     * 
     * @param associatedTaskId The ID of the task this session is for
     * @throws IllegalArgumentException if the task ID is null or empty
     */
    public FocusSession(String associatedTaskId){
        if (associatedTaskId == null || associatedTaskId.trim().isEmpty()) {
            throw new IllegalArgumentException("Task ID cannot be null or empty");
        }
        this.id = UUID.randomUUID();
        this.startTime = LocalDateTime.now();
        this.associatedTaskId = associatedTaskId;
        this.completed = false;
        this.paused = false;
        this.pausedDurationSeconds = 0;
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
        pausedDurationSeconds += java.time.Duration.between(lastPauseTime, LocalDateTime.now()).getSeconds();
        paused = false;
    }

    /**
     * Ends the current session.
     * 
     * @throws IllegalStateException if the session is already completed
     */
    public void endSession(){
        if (completed) {
            throw new IllegalStateException("Session is already completed");
        }
        this.endTime = LocalDateTime.now();
        this.durationSeconds = java.time.Duration.between(startTime, endTime).getSeconds() - pausedDurationSeconds;
        this.completed = true;
    }

    /**
     * Gets the unique identifier for this session.
     * 
     * @return The session's UUID
     */
    public UUID getId(){
        return id;
    }

    /**
     * Gets the time when the session started.
     * 
     * @return The session start time
     */
    public LocalDateTime getStartTime(){
        return startTime;
    }

    /**
     * Gets the time when the session ended.
     * 
     * @return The session end time, or null if the session is still active
     */
    public LocalDateTime getEndTime(){
        return endTime;
    }

    /**
     * Gets the total duration of the session in seconds.
     * 
     * @return The session duration in seconds
     */
    public long getDurationSeconds(){
        return durationSeconds;
    }

    /**
     * Gets the ID of the task associated with this session.
     * 
     * @return The associated task ID
     */
    public String getAssociatedTaskId(){
        return associatedTaskId;
    }

    /**
     * Checks if the session has been completed.
     * 
     * @return true if the session is completed, false otherwise
     */
    public boolean isCompleted(){
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
}