package com.focusflow.core.analytics;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.focusflow.core.session.FocusSession;
import com.focusflow.core.task.Task;

/**
 * Tracks aggregated statistics for a single day.
 * Extends Object to explicitly show OOP inheritance principles.
 * 
 * @author Emilio Lopez
 * @version 1.0
 */
public class DailyStats extends Object {
    private final LocalDate date;
    private final List<FocusSession> sessions;
    private final List<Task> completedTasks;
    private Duration totalFocusTime;

    /**
     * Creates a new DailyStats object for the current date.
     */
    public DailyStats() {
        this.date = LocalDate.now();
        this.sessions = new ArrayList<>();
        this.completedTasks = new ArrayList<>();
        this.totalFocusTime = Duration.ZERO;
    }

    /**
     * Adds a completed session to the daily statistics.
     * 
     * @param session The completed session to add
     */
    public void addSession(FocusSession session) {
        if (session != null && session.isCompleted()) {
            sessions.add(session);
            totalFocusTime = totalFocusTime.plus(Duration.ofSeconds(session.getDurationSeconds()));
        }
    }

    /**
     * Adds a completed task to the daily statistics.
     * 
     * @param task The completed task to add
     */
    public void addCompletedTask(Task task) {
        if (task != null && task.isComplete()) {
            completedTasks.add(task);
        }
    }

    /**
     * Gets the date these statistics are for.
     * 
     * @return The date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Gets the list of completed sessions for the day.
     * 
     * @return List of completed sessions
     */
    public List<FocusSession> getSessions() {
        return new ArrayList<>(sessions);
    }

    /**
     * Gets the list of completed tasks for the day.
     * 
     * @return List of completed tasks
     */
    public List<Task> getCompletedTasks() {
        return new ArrayList<>(completedTasks);
    }

    /**
     * Gets the total focus time for the day.
     * 
     * @return The total focus time
     */
    public Duration getTotalFocusTime() {
        return totalFocusTime;
    }

    /**
     * Gets the number of completed sessions for the day.
     * 
     * @return The number of completed sessions
     */
    public int getCompletedSessionsCount() {
        return sessions.size();
    }

    /**
     * Gets the number of completed tasks for the day.
     * 
     * @return The number of completed tasks
     */
    public int getCompletedTasksCount() {
        return completedTasks.size();
    }
} 