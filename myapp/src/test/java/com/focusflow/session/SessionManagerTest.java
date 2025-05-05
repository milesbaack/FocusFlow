package com.focusflow.session;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.focusflow.core.session.FocusSession;
import com.focusflow.core.session.SessionEventListener;
import com.focusflow.core.session.SessionManager;
import com.focusflow.core.task.Task;

/**
 * Test class for the SessionManager class.
 * 
 * This class contains unit tests for verifying the functionality of the SessionManager,
 * including session creation, tracking, and history management.
 * 
 * @author Emilio Lopez
 * @version 1.0.0
 * @see com.focusflow.core.session.SessionManager
 */
class SessionManagerTest {
    private SessionManager sessionManager;
    private Task testTask;
    private TestSessionListener listener;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager();
        testTask = new Task("Test Task", "Test Description");
        listener = new TestSessionListener();
        sessionManager.addListener(listener);
    }

    @Test
    void testStartSession() {
        FocusSession session = sessionManager.startSession(testTask.getId().toString());
        assertNotNull(session);
        assertTrue(sessionManager.getCurrentSession().isPresent());
        assertEquals(testTask.getId().toString(), sessionManager.getCurrentSession().get().getAssociatedTaskId());
    }

    @Test
    void testPauseAndResumeSession() throws InterruptedException {
        FocusSession session = sessionManager.startSession(testTask.getId().toString());
        Thread.sleep(100); // Let it run briefly
        
        sessionManager.pauseCurrentSession();
        assertTrue(sessionManager.getCurrentSession().isPresent());
        assertTrue(sessionManager.getCurrentSession().get().isPaused());
        
        Thread.sleep(100);
        sessionManager.resumeCurrentSession();
        assertTrue(sessionManager.getCurrentSession().isPresent());
        assertFalse(sessionManager.getCurrentSession().get().isPaused());
    }


    @Test
    void testSessionHistory() throws InterruptedException {
        // Start and end first session
        sessionManager.startSession(testTask.getId().toString());
        Thread.sleep(100);
        sessionManager.endCurrentSession();
        
        // Start and end second session
        sessionManager.startSession(testTask.getId().toString());
        Thread.sleep(100);
        sessionManager.endCurrentSession();
        
        List<FocusSession> history = sessionManager.getSessionHistory();
        assertEquals(2, history.size());
        assertTrue(history.get(0).getStartTime().isBefore(history.get(1).getStartTime()));
    }

    @Test
    void testGetSessionsForTask() throws InterruptedException {
        Task anotherTask = new Task("Another Task", "Another Description");
        
        // Create sessions for both tasks
        sessionManager.startSession(testTask.getId().toString());
        Thread.sleep(100);
        sessionManager.endCurrentSession();
        
        sessionManager.startSession(anotherTask.getId().toString());
        Thread.sleep(100);
        sessionManager.endCurrentSession();
        
        sessionManager.startSession(testTask.getId().toString());
        Thread.sleep(100);
        sessionManager.endCurrentSession();
        
        List<FocusSession> taskSessions = sessionManager.getSessionsForTask(testTask.getId().toString());
        assertEquals(2, taskSessions.size());
        assertTrue(taskSessions.stream().allMatch(session -> 
            session.getAssociatedTaskId().equals(testTask.getId().toString())));
    }

    @Test
    void testGetTotalTimeForTask() throws InterruptedException {
        // Create a session and let it run for a while
        sessionManager.startSession(testTask.getId().toString());
        Thread.sleep(1000); // Let it run for 1 second
        sessionManager.endCurrentSession();

        // Create another session for the same task
        sessionManager.startSession(testTask.getId().toString());
        Thread.sleep(1000); // Let it run for 1 second
        sessionManager.endCurrentSession();

        // Total time should be at least 2 seconds
        long totalTime = sessionManager.getTotalTimeForTask(testTask.getId().toString());
        assertTrue(totalTime >= 2, "Total time should be at least 2 seconds, but was " + totalTime);
    }

    @Test
    void testSessionEvents() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(1);
        
        listener.setOnStartCallback(() -> startLatch.countDown());
        listener.setOnEndCallback(() -> endLatch.countDown());
        
        sessionManager.startSession(testTask.getId().toString());
        assertTrue(startLatch.await(1, TimeUnit.SECONDS));
        
        sessionManager.endCurrentSession();
        assertTrue(endLatch.await(1, TimeUnit.SECONDS));
    }

    private static class TestSessionListener implements SessionEventListener {
        private Runnable onStartCallback;
        private Runnable onPauseCallback;
        private Runnable onResumeCallback;
        private Runnable onEndCallback;
        private Runnable onHistoryChangedCallback;

        void setOnStartCallback(Runnable callback) { this.onStartCallback = callback; }
        void setOnPauseCallback(Runnable callback) { this.onPauseCallback = callback; }
        void setOnResumeCallback(Runnable callback) { this.onResumeCallback = callback; }
        void setOnEndCallback(Runnable callback) { this.onEndCallback = callback; }
        void setOnHistoryChangedCallback(Runnable callback) { this.onHistoryChangedCallback = callback; }

        @Override
        public void onSessionStarted(FocusSession session) {
            if (onStartCallback != null) onStartCallback.run();
        }

        @Override
        public void onSessionPaused(FocusSession session) {
            if (onPauseCallback != null) onPauseCallback.run();
        }

        @Override
        public void onSessionResumed(FocusSession session) {
            if (onResumeCallback != null) onResumeCallback.run();
        }

        @Override
        public void onSessionEnded(FocusSession session) {
            if (onEndCallback != null) onEndCallback.run();
        }

        @Override
        public void onSessionHistoryChanged(List<FocusSession> history) {
            if (onHistoryChangedCallback != null) onHistoryChangedCallback.run();
        }
    }
} 