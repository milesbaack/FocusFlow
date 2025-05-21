/**
 * Class to debug Task class.
 * @author Emilio Lopez
 * @version 1.0
 */

package com.focusflow.task;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.focusflow.core.task.Task;
import com.focusflow.core.task.TaskCategory;
import com.focusflow.core.task.TaskPriority;
import com.focusflow.core.task.TaskStatus;

/**
 * Test class for the Task class.
 * 
 * This class contains unit tests for verifying the functionality of the Task class,
 * including task creation, status management, and property modifications.
 * 
 * @author Emilio Lopez
 * @version 1.0.0
 * @see com.focusflow.core.task.Task
 */
class TaskTest {
    private Task task;
    private static final String TEST_NAME = "Test Task";
    private static final String TEST_DESCRIPTION = "Test Description";

    @BeforeEach
    void setUp() {
        task = new Task(TEST_NAME, TEST_DESCRIPTION);
    }

    @Test
    void testTaskCreation() {
        assertNotNull(task.getId());
        assertEquals(TEST_NAME, task.getName());
        assertEquals(TEST_DESCRIPTION, task.getDescription());
        assertEquals(TaskStatus.NOT_STARTED, task.getStatus());
        assertNotNull(task.getCreationDateTime());
        assertFalse(task.isComplete());
        assertFalse(task.isInProgress());
    }

    @Test
    void testTaskEquality() {
        Task sameTask = new Task(TEST_NAME, TEST_DESCRIPTION);
        assertNotEquals(task, sameTask); // Different IDs
        assertEquals(task, task); // Same instance
    }

    @Test
    void testTaskHashCode() {
        Task sameTask = new Task(TEST_NAME, TEST_DESCRIPTION);
        assertNotEquals(task.hashCode(), sameTask.hashCode()); // Different IDs
        assertEquals(task.hashCode(), task.hashCode()); // Same instance
    }

    @Test
    void testTaskStatusChanges() {
        // Test completion
        task.markAsCompleted();
        assertTrue(task.isComplete());
        assertEquals(TaskStatus.COMPLETED, task.getStatus());

        // Test uncompletion
        task.markAsIncomplete();
        assertFalse(task.isComplete());
        assertEquals(TaskStatus.NOT_STARTED, task.getStatus());

        // Test in progress
        task.setInProgress(true);
        assertTrue(task.isInProgress());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());

        // Test postponement
        task.setPostponed(true);
        assertTrue(task.isPostponed());
        assertEquals(TaskStatus.POSTPONED, task.getStatus());

        // Reset to NOT_STARTED before testing cancellation
        task.setPostponed(false);
        task.setInProgress(false);
        assertEquals(TaskStatus.NOT_STARTED, task.getStatus());

        // Test cancellation
        task.setCanceled(true);
        assertTrue(task.isCanceled());
        assertEquals(TaskStatus.CANCELED, task.getStatus());
    }

    @Test
    void testPropertyModifications() {
        // Test name modification
        String newName = "New Task Name";
        task.setName(newName);
        assertEquals(newName, task.getName());

        // Test description modification
        String newDescription = "New Description";
        task.setDescription(newDescription);
        assertEquals(newDescription, task.getDescription());

        // Test priority modification
        task.setPriority(TaskPriority.HIGH);
        assertEquals(TaskPriority.HIGH, task.getPriority());

        // Test category modification
        TaskCategory newCategory = new TaskCategory("New Category");
        task.setCategory(newCategory);
        assertEquals(newCategory, task.getCategory());
    }

    @Test
    void testDueDate() {
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
        task.setDueDateTime(dueDate);
        assertEquals(dueDate, task.getDueDateTime());
        assertTrue(task.hasDueDateTime());
    }

    @Test
    void testLastModified() {
        LocalDateTime initialModificationTime = task.getLastModifiedDateTime();
        
        // Wait a bit to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Modify task
        task.setName("Modified Name");
        assertTrue(task.getLastModifiedDateTime().isAfter(initialModificationTime));
    }

    @Test
    void testToString() {
        String toString = task.toString();
        assertTrue(toString.contains(TEST_NAME));
        assertTrue(toString.contains(TEST_DESCRIPTION));
        assertTrue(toString.contains("Status: " + task.getStatus()));
        assertTrue(toString.contains("Priority: " + task.getPriority()));
    }
}