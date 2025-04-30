/**
 * Focus session manager for Pomodoro Timer
 * @author Emilio Lopez
 * @version 1.0.0
 */

package  com.focusflow.core.session;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class FocusSession implements Serializable{
    private final UUID id;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    
    private long durationSeconds;
    private String associatedTaskId;
    private boolean completed;

    // Default construction
    public FocusSession(String associatedTaskId){
        this.id = UUID.randomUUID();
        this.startTime = LocalDateTime.now();
        this.associatedTaskId = associatedTaskId;
        this.completed = false;
    }

    /**
     * End Focus session
     */
    public void endSession(){
        this.endTime = LocalDateTime.now();
        this.durationSeconds = java.time.Duration.between(startTime, endTime).getSeconds();
        this.completed = true;
    }

    // TODO: Javadoc for (Get/Set)'s
    public UUID getId(){
        return id;
    }

    public LocalDateTime getStartTime(){
        return startTime;
    }

    public LocalDateTime getEndTime(){
        return endTime;
    }

    public long getDurationSeconds(){
        return durationSeconds;
    }

    public String getAssociatedTaskId(){
        return associatedTaskId;
    }

    public boolean isCompleted(){
        return completed;
    }
}