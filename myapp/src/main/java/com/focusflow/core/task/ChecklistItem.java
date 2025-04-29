/**
 * Checklist class to store subtasks  / checklist
 * @author Emilio Lopez
 * @version 1.0
 */

package com.focusflow.core.task;

import java.io.Serializable;

public class ChecklistItem implements Serializable{
    private String text;
    private boolean isChecked;

    /**
     * Public constructor for ChecklistItem
     * @param text String containing subtask
     */
    public ChecklistItem(String text){
        this.text = text;
        this.isChecked = false;
    }

    // TODO: Fix description
    /**
     * Toggle subtask checkmark ( negates checkmark )
     */
    public void toggleChecked(){
        this.isChecked = !this.isChecked;
    }

    /**
     * Get text
     */
    public String getText(){
        return text;
    }

    /**
     * Set text
     * @param text Text to be added
     */
    public void setText(String text){
        this.text = text;
    }

    // TODO: Fix comments
    /**
     * Get boolean value of isChecked
     * @return {@code true} if subtask is checked; {@code false} otherwise
     */
    public boolean isChecked(){
        return isChecked;
    }
}