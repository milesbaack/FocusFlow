package com.focusflow.core.task;

/**
 * Represents a category for tasks in the FocusFlow application.
 * 
 * TaskCategory provides a way to organize and group related tasks.
 * Categories help users filter and organize their tasks by area of focus,
 * project, or any other grouping mechanism that makes sense for their workflow.
 * 
 * @author Miles Baack
 * @version 1.0
 * @see com.focusflow.core.task.Task
 */
public class TaskCategory {
    private String category;

    /**
     * Creates a new task category with the specified name.
     * 
     * @param category The name of the category
     */
    public TaskCategory(String category) {
        this.category = category;
    }

    /**
     * Creates a new task category with a default name of "Uncategorized".
     * This constructor is useful when a task needs a category object
     * but no specific category has been assigned.
     */
    public TaskCategory() {
        this.category = "Uncategorized"; // Default value instead of null
    }

    /**
     * Sets the category name.
     * 
     * @param category The new category name
     */
    public void setTaskCategory(String category) {
        this.category = category;
    }

    /**
     * Returns the category name.
     * 
     * @return The category name
     */
    public String getTaskCategory() {
        return category;
    }

    /**
     * Returns a string representation of this category.
     * This method returns the category name itself, allowing TaskCategory
     * objects to be used directly in string contexts.
     * 
     * @return The category name
     */
    @Override
    public String toString() {
        return category;
    }

    /**
     * Determines whether this category is equal to the specified object.
     * Categories are considered equal if they have the same name.
     * This method handles null category names safely.
     * 
     * @param obj The object to compare with this category
     * @return true if the categories have the same name, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        TaskCategory that = (TaskCategory) obj;
        return (category == null) ? that.category == null : category.equals(that.category);
    }

    /**
     * Returns a hash code for this category based on its name.
     * This method handles null category names safely.
     * 
     * @return A hash code value for this category
     */
    @Override
    public int hashCode() {
        return category == null ? 0 : category.hashCode();
    }
}