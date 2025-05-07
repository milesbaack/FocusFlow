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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SessionManager{
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
     * @param associatedTaskId The ID of the task to associate with the session
     * @return The newly created focus session
     * @throws IllegalArgumentException if the task ID is null or empty
     */
    public FocusSession startSession(String associatedTaskId) {
        if (associatedTaskId == null || associatedTaskId.trim().isEmpty()) {
            throw new IllegalArgumentException("Task ID cannot be null or empty");
        }

        currentSession = new FocusSession(associatedTaskId);
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
        currentSession.pauseSession();
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
        currentSession.resumeSession();
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
        currentSession.endSession();
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
    public Optional<FocusSession> getCurrentSession(){
        return Optional.ofNullable(currentSession);
    }

    /**
     * Gets the complete session history.
     * 
     * @return A list of all sessions, ordered by start time
     */
    public List<FocusSession> getSessionHistory(){
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
            .filter(session -> session.getAssociatedTaskId().equals(taskId))
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