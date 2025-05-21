/**
 * Represents an item in a task's checklist.
 * 
 * This class implements Serializable to support persistence and implements
 * proper equals/hashCode for collection operations.
 * 
 * @author Miles Baack
 * @version 1.1
 */

package com.focusflow.core.task;

import java.io.Serializable;
import java.util.Objects;

public class ChecklistItem implements Serializable {
    private String text;
    private boolean checked;

    /**
     * Creates a new checklist item with the specified text.
     * 
     * @param text The text content of the checklist item
     */
    public ChecklistItem(String text) {
        this.text = text;
        this.checked = false;
    }

    /**
     * Toggles the checked state of this item.
     * If checked, becomes unchecked and vice versa.
     */
    public void toggleChecked() {
        this.checked = !this.checked;
    }

    /**
     * Gets the text content of this checklist item.
     * 
     * @return The text content
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text content of this checklist item.
     * 
     * @param text The new text content
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Checks if this item is marked as completed.
     * 
     * @return true if the item is checked, false otherwise
     */
    public boolean isChecked() {
        return checked;
    }

    /**
     * Sets the checked state of this item.
     * 
     * @param checked The new checked state
     */
    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChecklistItem that = (ChecklistItem) obj;
        return checked == that.checked && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, checked);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", checked ? "x" : " ", text);
    }
}