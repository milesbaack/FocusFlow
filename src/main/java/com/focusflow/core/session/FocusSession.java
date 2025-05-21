/**
 * Focus session manager for Pomodoro Timer
 * @author Emilio Lopez
 * @version 1.1.0
 */

package  com.focusflow.core.session;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import com.focusflow.core.timer.TimerType;

/**
 * Represents a single focus session for a task.
 * 
 * This class tracks the duration and state of a focus session,
 * including start and end times, pauses, and completion status.
 */
public class FocusSession implements Serializable{
    private final UUID id;
    private final String taskId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final int durationSeconds;
    private final TimerType type;
    private final boolean completed;
    private final boolean paused;
    private final int pausedDurationSeconds;

    /**
     * Creates a new focus session for a task.
     * 
     * @param taskId The ID of the task this session is for
     * @throws IllegalArgumentException if the task ID is null or empty
     */
    public FocusSession(String taskId, LocalDateTime startTime, LocalDateTime endTime, TimerType type) {
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new IllegalArgumentException("Task ID cannot be null or empty");
        }
        this.id = UUID.randomUUID();
        this.taskId = taskId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationSeconds = endTime != null ? 
            (int) java.time.Duration.between(startTime, endTime).getSeconds() : 0;
        this.type = type;
        this.completed = false;
        this.paused = false;
        this.pausedDurationSeconds = 0;
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
     * Gets the ID of the task associated with this session.
     * 
     * @return The associated task ID
     */
    public String getTaskId(){
        return taskId;
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
    public int getDurationSeconds(){
        return durationSeconds;
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
    public int getPausedDurationSeconds() {
        return pausedDurationSeconds;
    }

    public TimerType getType() {
        return type;
    }
}