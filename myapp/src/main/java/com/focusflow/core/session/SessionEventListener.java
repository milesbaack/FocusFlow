package com.focusflow.core.session;

import java.util.List;

/**
 * Interface for handling session-related events.
 * 
 * This interface defines the contract for classes that need to be notified
 * about various session events such as start, pause, resume, and end.
 * 
 * @author Emilio Lopez
 * @version 1.1.0
 */
public interface SessionEventListener {
    /**
     * Called when a new focus session starts.
     * 
     * @param session The session that has started
     */
    void onSessionStarted(FocusSession session);

    /**
     * Called when a focus session is paused.
     * 
     * @param session The session that has been paused
     */
    void onSessionPaused(FocusSession session);

    /**
     * Called when a paused focus session is resumed.
     * 
     * @param session The session that has been resumed
     */
    void onSessionResumed(FocusSession session);

    /**
     * Called when a focus session ends.
     * 
     * @param session The session that has ended
     */
    void onSessionEnded(FocusSession session);

    /**
     * Called when the session history changes.
     * 
     * @param history The updated list of all sessions
     */
    void onSessionHistoryChanged(List<FocusSession> history);
}
