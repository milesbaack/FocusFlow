/**
 * Class to debug Task class.
 * @author Emilio Lopez
 * @version 1.0
 */

package com.focusflow.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

import com.focusflow.core.task.Task;

public class TaskTest {
    @Test
    public void testTaskCreation() {
        Task task = new Task("Test title");
        assertEquals("Test title", task.getTitle());
        assertFalse(task.isCompleted());
    }
}