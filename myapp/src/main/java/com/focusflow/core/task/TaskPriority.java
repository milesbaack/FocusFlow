package com.focusflow.core.task;

/**
 * Represents the priority level of a task.
 * 
 * @author Miles Baack
 * @version 1.0
 */
public enum TaskPriority {
    URGENT(3),
    HIGH(2),
    MEDIUM(1),
    LOW(0);

    private int value;

    private TaskPriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return name() + " (" + value + ")";
    }
} 