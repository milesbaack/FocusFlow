/**
 * Manages focus sessions and their history.
 * 
 * This class is responsible for creating, tracking, and managing focus sessions.
 * It maintains a history of all sessions and provides methods to query and filter
 * sessions based on various criteria.
 * 
 * @author Emilio Lopez
 * @version 1.1.0
 */

package com.focusflow.core.session;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.focusflow.core.timer.TimerType;

public class SessionManager {
    private final List<FocusSession> sessionHistory = new ArrayList<>();
    private final List<SessionEventListener> listeners = new ArrayList<>();
    private FocusSession currentSession;

    /**
     * Adds a listener to receive session events.
     * 
     * @param listener The listener to add
     */
    public void addListener(SessionEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener from receiving session events.
     * 
     * @param listener The listener to remove
     */
    public void removeListener(SessionEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Starts a new focus session associated with a task.
     * 
     * @param taskId The ID of the task to associate with the session
     * @return The newly created focus session
     * @throws IllegalArgumentException if the task ID is null or empty
     */
    public FocusSession startSession(String taskId) {
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new IllegalArgumentException("Task ID cannot be null or empty");
        }

        if (currentSession != null) {
            throw new IllegalStateException("A session is already in progress");
        }

        currentSession = new FocusSession(taskId, LocalDateTime.now(), null, TimerType.WORK);
        notifySessionStarted(currentSession);
        return currentSession;
    }

    /**
     * Pauses the current focus session.
     * 
     * @throws IllegalStateException if there is no active session
     */
    public void pauseCurrentSession() {
        if (currentSession == null) {
            throw new IllegalStateException("No active session to pause");
        }
        // Create a new session with paused state
        currentSession = new FocusSession(
            currentSession.getTaskId(),
            currentSession.getStartTime(),
            LocalDateTime.now(),
            currentSession.getType()
        );
        notifySessionPaused(currentSession);
    }

    /**
     * Resumes the current focus session.
     * 
     * @throws IllegalStateException if there is no active session
     */
    public void resumeCurrentSession() {
        if (currentSession == null) {
            throw new IllegalStateException("No active session to resume");
        }
        // Create a new session with resumed state
        currentSession = new FocusSession(
            currentSession.getTaskId(),
            LocalDateTime.now(),
            null,
            currentSession.getType()
        );
        notifySessionResumed(currentSession);
    }

    /**
     * Ends the current focus session.
     * 
     * @throws IllegalStateException if there is no active session
     */
    public void endCurrentSession() {
        if (currentSession == null) {
            throw new IllegalStateException("No active session to end");
        }
        currentSession = new FocusSession(
            currentSession.getTaskId(),
            currentSession.getStartTime(),
            LocalDateTime.now(),
            currentSession.getType()
        );
        sessionHistory.add(currentSession);
        notifySessionEnded(currentSession);
        notifySessionHistoryChanged(sessionHistory);
        currentSession = null;
    }

    /**
     * Gets the current active session.
     * 
     * @return An Optional containing the current session if one exists
     */
    public Optional<FocusSession> getCurrentSession() {
        return Optional.ofNullable(currentSession);
    }

    /**
     * Gets the complete session history.
     * 
     * @return A list of all sessions, ordered by start time
     */
    public List<FocusSession> getSessionHistory() {
        return new ArrayList<>(sessionHistory);
    }

    /**
     * Gets sessions associated with a specific task.
     * 
     * @param taskId The task ID to filter by
     * @return A list of sessions associated with the task
     */
    public List<FocusSession> getSessionsForTask(String taskId) {
        return sessionHistory.stream()
            .filter(session -> session.getTaskId().equals(taskId))
            .collect(Collectors.toList());
    }

    /**
     * Gets the total time spent on a task across all sessions.
     * 
     * @param taskId The task ID to calculate time for
     * @return The total duration in seconds
     */
    public long getTotalTimeForTask(String taskId) {
        return getSessionsForTask(taskId).stream()
            .mapToLong(FocusSession::getDurationSeconds)
            .sum();
    }

    private void notifySessionStarted(FocusSession session) {
        listeners.forEach(listener -> listener.onSessionStarted(session));
    }

    private void notifySessionPaused(FocusSession session) {
        listeners.forEach(listener -> listener.onSessionPaused(session));
    }

    private void notifySessionResumed(FocusSession session) {
        listeners.forEach(listener -> listener.onSessionResumed(session));
    }

    private void notifySessionEnded(FocusSession session) {
        listeners.forEach(listener -> listener.onSessionEnded(session));
    }

    private void notifySessionHistoryChanged(List<FocusSession> history) {
        listeners.forEach(listener -> listener.onSessionHistoryChanged(history));
    }
}