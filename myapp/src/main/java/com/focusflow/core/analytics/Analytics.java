package com.focusflow.core.analytics;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.focusflow.core.session.FocusSession;
import com.focusflow.core.task.Task;

/**
 * Main analytics class for tracking and analyzing user productivity data.
 * This class extends Object to explicitly show OOP inheritance principles.
 * 
 * @author Emilio Lopez
 * @version 1.0
 */
public class Analytics extends Object {
    // Core data structures for analytics
    private final Map<String, SessionStats> sessionStats;
    private final Map<LocalDate, DailyStats> dailyStats;
    private final Map<String, TaskStats> taskStats;
    private double productivityScore;
    private Duration focusTimeTotal;

    /**
     * Constructs a new Analytics instance with empty data structures.
     */
    public Analytics() {
        this.sessionStats = new HashMap<>();
        this.dailyStats = new HashMap<>();
        this.taskStats = new HashMap<>();
        this.productivityScore = 0.0;
        this.focusTimeTotal = Duration.ZERO;
    }

    /**
     * Tracks a completed focus session and updates relevant statistics.
     * 
     * @param session The completed focus session to track
     */
    public void trackSession(FocusSession session) {
        if (session == null || !session.isCompleted()) {
            return;
        }

        // Update session statistics
        SessionStats stats = new SessionStats(session);
        sessionStats.put(session.getId().toString(), stats);

        // Update daily statistics
        LocalDate sessionDate = session.getStartTime().toLocalDate();
        dailyStats.computeIfAbsent(sessionDate, k -> new DailyStats())
                 .addSession(session);

        // Update total focus time
        focusTimeTotal = focusTimeTotal.plus(Duration.ofSeconds(session.getDurationSeconds()));

        // Recalculate productivity score
        updateProductivityScore();
    }

    /**
     * Tracks a completed task and updates relevant statistics.
     * 
     * @param task The completed task to track
     */
    public void trackTaskCompletion(Task task) {
        if (task == null || !task.isComplete()) {
            return;
        }

        // Update task statistics
        TaskStats stats = new TaskStats(task);
        taskStats.put(task.getId().toString(), stats);

        // Update daily statistics
        LocalDate completionDate = LocalDate.now();
        dailyStats.computeIfAbsent(completionDate, k -> new DailyStats())
                 .addCompletedTask(task);

        // Recalculate productivity score
        updateProductivityScore();
    }

    /**
     * Gets the current productivity score.
     * 
     * @return The current productivity score (0.0 to 100.0)
     */
    public double getProductivityScore() {
        return productivityScore;
    }

    /**
     * Gets the daily statistics for a specific date.
     * 
     * @param date The date to get statistics for
     * @return The DailyStats object for the specified date
     */
    public DailyStats getDailyStats(LocalDate date) {
        return dailyStats.getOrDefault(date, new DailyStats());
    }

    /**
     * Gets the statistics for a specific task.
     * 
     * @param taskId The ID of the task to get statistics for
     * @return The TaskStats object for the specified task
     */
    public TaskStats getTaskStats(String taskId) {
        return taskStats.getOrDefault(taskId, new TaskStats());
    }

    /**
     * Gets the total focus time accumulated.
     * 
     * @return The total focus time as a Duration
     */
    public Duration getFocusTimeTotal() {
        return focusTimeTotal;
    }

    /**
     * Updates the productivity score based on current statistics.
     * This is a simplified implementation that can be enhanced.
     */
    private void updateProductivityScore() {
        // Simple productivity score calculation based on completed tasks and focus time
        int totalTasks = taskStats.size();
        long totalFocusMinutes = focusTimeTotal.toMinutes();
        
        if (totalTasks == 0 || totalFocusMinutes == 0) {
            productivityScore = 0.0;
            return;
        }

        // Basic formula: (tasks completed / focus time) * 100
        productivityScore = (totalTasks / (double)totalFocusMinutes) * 100.0;
        
        // Cap the score at 100
        productivityScore = Math.min(productivityScore, 100.0);
    }
} 