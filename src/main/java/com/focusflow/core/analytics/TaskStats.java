package com.focusflow.core.analytics;

import com.focusflow.core.task.Task;
import com.focusflow.core.task.TaskCategory;
import com.focusflow.core.task.TaskPriority;

/**
 * Tracks statistics for individual tasks.
 * Extends Object to explicitly show OOP inheritance principles.
 * 
 * @author Emilio Lopez
 * @version 1.0
 */
public class TaskStats extends Object {
    private final String taskId;
    private final TaskPriority priority;
    private final TaskCategory category;

    /**
     * Creates a new TaskStats object from a completed task.
     * 
     * @param task The completed task to create statistics from
     */
    public TaskStats(Task task) {
        if (task == null || !task.isComplete()) {
            throw new IllegalArgumentException("Task must be completed to create TaskStats");
        }
        
        this.taskId = task.getId().toString();
        this.priority = task.getPriority();
        this.category = task.getCategory();
    }

    /**
     * Creates an empty TaskStats object.
     */
    public TaskStats() {
        this.taskId = "";
        this.priority = TaskPriority.MEDIUM;
        this.category = new TaskCategory(); // Default uncategorized
    }

    /**
     * Gets the task ID.
     * 
     * @return The task ID
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * Gets the task priority.
     * 
     * @return The priority
     */
    public TaskPriority getPriority() {
        return priority;
    }

    /**
     * Gets the task category.
     * 
     * @return The category
     */
    public TaskCategory getCategory() {
        return category;
    }
} 