/**
 * Session Manager to handle session history and session tasks
 * @author Emilio Lopez
 * @version 1.0.0
 */

package com.focusflow.core.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SessionManager{
    private final List<FocusSession> sessionHistory = new ArrayList<>();
    private FocusSession currentSession;

    /**
     * Start Focus session with Task ID
     * @param associatedTaskId Task id to be associated with Focus session
     * @return FocusSession instance for that task.
     */
    public FocusSession startSession(String associatedTaskId){
        currentSession = new FocusSession(associatedTaskId);

        return currentSession;
    }

    /**
     * End current focus session
     */
    public void endCurrentSession(){
        // Check if current session exists and if current session is incomplete
        if (currentSession != null && !currentSession.isCompleted()){
            currentSession.endSession();
            sessionHistory.add(currentSession);
            currentSession = null;
            
        }
    }

    // TODO: Edit JavaDoc
    /**
     * Get current session
     * @return FocusSession if exists, else, null
     */

    // Using optional container to avoid NullPointerException
    // error if there is not a current session.
    public Optional<FocusSession> getCurrentSession(){
        return Optional.ofNullable(currentSession);
    }

    /**
     * Get session history
     * @return Session history in ArrayList
     */
    public List<FocusSession> getSessionHistory(){
        return new ArrayList<>(sessionHistory);
    }
}