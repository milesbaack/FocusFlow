package com.focusflow.core.task;

/**
 * Interface for objects that need to be notified when tasks are updated.
 * Follows the Observer pattern to allow objects to react to task state changes.
 * 
 * Implementers of this interface will receive notifications about various
 * task lifecycle events such as completion, progress updates, and subtask
 * management.
 * Default implementations are provided for optional callbacks.
 * 
 * @author Miles Baack
 * @version 1.1
 */
public interface TaskListener {

    /**
     * Called when a task is completed.
     * 
     * @param task The task that was completed
     */
    void onTaskCompleted(Task task);

    /**
     * Called when a task's progress is updated.
     * 
     * @param task The task that was updated
     */
    void onTaskProgressUpdated(Task task);

    /**
     * Called when a subtask is added to a task.
     * 
     * @param parentTask The parent task
     * @param subtask    The subtask that was added
     */
    default void onSubtaskAdded(Task parentTask, Task subtask) {
        // Default empty implementation
    }

    /**
     * Called when a subtask is removed from a task.
     * 
     * @param parentTask The parent task
     * @param subtask    The subtask that was removed
     */
    default void onSubtaskRemoved(Task parentTask, Task subtask) {
        // Default empty implementation
    }

    /**
     * Called when a task is deleted.
     * 
     * @param task The task that was deleted
     */
    default void onTaskDeleted(Task task) {
        // Default empty implementation
    }

    /**
     * Called when a task is postponed.
     * 
     * @param task The task that was postponed
     */
    default void onTaskPostponed(Task task) {
        // Default empty implementation
    }

}