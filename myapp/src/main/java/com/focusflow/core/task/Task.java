/**
 * Task class that implements Serializable and TaskStatus.TaskWithStatus interfaces
 * for managing tasks in the FocusFlow application.
 * 
 * @author Miles Baack
 * @version 1.1
 */

package com.focusflow.core.task;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.focusflow.core.task.TaskStatus.TaskWithStatus;

/**
 * Represents a task within the FocusFlow application.
 * 
 * This class focuses on core task properties and behaviors, with support for
 * additional status tracking and property change notifications.
 * 
 * @author Miles Baack
 * @version 1.0
 * @see com.focusflow.core.task.TaskManager
 * @see com.focusflow.core.task.TaskStatus
 */
public class Task implements Serializable, TaskWithStatus {
    // Core properties of a task
    private String name;
    private String description;
    private final LocalDateTime creationDateTime;
    private LocalDateTime lastModifiedDateTime;
    private LocalDateTime dueDateTime;
    private boolean isComplete;
    private TaskPriority priority;
    private TaskCategory category;
    
    // Additional status properties
    private boolean isInProgress;
    private boolean isPostponed;
    private boolean isCanceled;
    
    // Using UUID for thread-safe unique identification
    private final UUID id;
    private List<Task> subtasks;
    
    /**
     * Constructs a new Task with the specified name and description.
     * 
     * @param name The name of the task
     * @param description The detailed description of the task
     */
    public Task(String name, String description) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.creationDateTime = LocalDateTime.now();
        this.lastModifiedDateTime = LocalDateTime.now();
        this.dueDateTime = null;
        this.isComplete = false;
        this.isInProgress = false;
        this.isPostponed = false;
        this.isCanceled = false;
        this.priority = TaskPriority.MEDIUM;
        this.category = new TaskCategory(); // Default uncategorized
        this.subtasks = new ArrayList<>();
    }

    /**
     * Gets the unique identifier for this task.
     * 
     * @return The task's UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Gets the name of this task.
     * 
     * @return The task name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this task.
     * 
     * @param name The new task name
     */
    public void setName(String name) {
        this.name = name;
        this.lastModifiedDateTime = LocalDateTime.now();
    }

    /**
     * Gets the description of this task.
     * 
     * @return The task description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this task.
     * 
     * @param description The new task description
     */
    public void setDescription(String description) {
        this.description = description;
        this.lastModifiedDateTime = LocalDateTime.now();
    }

    /**
     * Gets the category of this task.
     * 
     * @return The task category
     */
    public TaskCategory getCategory() {
        return category;
    }

    /**
     * Sets the category of this task.
     * 
     * @param category The new task category
     */
    public void setCategory(TaskCategory category) {
        this.category = category;
        this.lastModifiedDateTime = LocalDateTime.now();
    }

    /**
     * Gets the priority of this task.
     * 
     * @return The task priority
     */
    public TaskPriority getPriority() {
        return priority;
    }

    /**
     * Sets the priority of this task.
     * 
     * @param priority The new task priority
     */
    public void setPriority(TaskPriority priority) {
        this.priority = priority;
        this.lastModifiedDateTime = LocalDateTime.now();
    }

    /**
     * Gets the creation date and time of this task.
     * 
     * @return The creation date and time
     */
    public LocalDateTime getCreationDateTime() {
        return creationDateTime;
    }

    /**
     * Gets the last modification date and time of this task.
     * 
     * @return The last modification date and time
     */
    public LocalDateTime getLastModifiedDateTime() {
        return lastModifiedDateTime;
    }

    /**
     * Gets the due date and time of this task.
     * 
     * @return The due date and time, or null if not set
     */
    public LocalDateTime getDueDateTime() {
        return dueDateTime;
    }

    /**
     * Sets the due date and time of this task.
     * 
     * @param dueDateTime The new due date and time
     */
    public void setDueDateTime(LocalDateTime dueDateTime) {
        this.dueDateTime = dueDateTime;
        this.lastModifiedDateTime = LocalDateTime.now();
    }

    /**
     * Checks if this task has a due date and time set.
     * 
     * @return true if a due date and time is set, false otherwise
     */
    public boolean hasDueDateTime() {
        return dueDateTime != null;
    }

    /**
     * Checks if this task is complete.
     * 
     * @return true if the task is complete, false otherwise
     */
    public boolean isComplete() {
        return isComplete;
    }

    /**
     * Marks this task as completed.
     */
    public void markAsCompleted() {
        this.isComplete = true;
        this.isInProgress = false;
        this.lastModifiedDateTime = LocalDateTime.now();
    }

    /**
     * Marks this task as incomplete.
     */
    public void markAsIncomplete() {
        this.isComplete = false;
        this.lastModifiedDateTime = LocalDateTime.now();
    }

    @Override
    public boolean isInProgress() {
        return isInProgress;
    }

    /**
     * Sets whether this task is in progress.
     * 
     * @param inProgress true if the task is in progress, false otherwise
     */
    public void setInProgress(boolean inProgress) {
        this.isInProgress = inProgress;
        this.lastModifiedDateTime = LocalDateTime.now();
    }

    @Override
    public boolean isPostponed() {
        return isPostponed;
    }

    /**
     * Sets whether this task is postponed.
     * 
     * @param postponed true if the task is postponed, false otherwise
     */
    public void setPostponed(boolean postponed) {
        this.isPostponed = postponed;
        this.lastModifiedDateTime = LocalDateTime.now();
    }

    @Override
    public boolean isCanceled() {
        return isCanceled;
    }

    /**
     * Sets whether this task is canceled.
     * 
     * @param canceled true if the task is canceled, false otherwise
     */
    public void setCanceled(boolean canceled) {
        this.isCanceled = canceled;
        this.lastModifiedDateTime = LocalDateTime.now();
    }

    /**
     * Gets the current status of this task.
     * 
     * @return The TaskStatus representing the current state of this task
     */
    public TaskStatus getStatus() {
        return TaskStatus.fromTask(this);
    }

    /**
     * Gets the list of subtasks for this task.
     * 
     * @return List of subtasks
     */
    public List<Task> getSubtasks() {
        return new ArrayList<>(subtasks);
    }

    /**
     * Adds a subtask to this task.
     * 
     * @param subtask The subtask to add
     */
    public void addSubtask(Task subtask) {
        subtasks.add(subtask);
        this.lastModifiedDateTime = LocalDateTime.now();
    }

    /**
     * Removes a subtask from this task.
     * 
     * @param subtask The subtask to remove
     * @return true if the subtask was removed, false otherwise
     */
    public boolean removeSubtask(Task subtask) {
        boolean removed = subtasks.remove(subtask);
        if (removed) {
            this.lastModifiedDateTime = LocalDateTime.now();
        }
        return removed;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task other = (Task) obj;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append("Task: ").append(name)
            .append("\nDescription: ").append(description)
            .append("\nStatus: ").append(TaskStatus.fromTask(this))
            .append("\nPriority: ").append(priority)
            .append("\nCategory: ").append(category)
            .append("\nDue: ").append(dueDateTime != null ? dueDateTime : "Not set")
            .toString();
    }
}
