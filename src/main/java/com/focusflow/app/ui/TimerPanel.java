package com.focusflow.app.ui;

import java.util.function.Consumer;

import com.focusflow.core.task.Task;
import com.focusflow.core.timer.PomodoroTimer;
import com.focusflow.core.timer.TimerState;
import com.focusflow.core.timer.TimerType;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * FIXED Timer Panel that handles timer display without control buttons.
 * Control buttons are now managed by App.java in the bottom area.
 */
public class TimerPanel extends VBox {

    // Dependencies
    private final Font pixelFont;
    private final Consumer<Void> onSelectTask;
    private final Consumer<Void> onToggleTimer;

    // Timer components
    private PomodoroTimer workTimer;
    private PomodoroTimer breakTimer;
    private Task currentTask;
    private boolean isOnBreak = false;

    // UI Components - ONLY display elements, no buttons
    private Label currentTaskLabel;
    private Label timerTypeLabel;
    private Label timerLabel;
    private Label remainingLabel;
    private VBox timerDisplay;

    // Responsive sizing properties
    private double scaleFactor = 1.0;

    // Styling constants
    private static final String TIMER_CONTAINER_STYLE = "-fx-background-color: rgba(255,255,255,0.1); " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);";

    private static final String WORK_TIMER_COLOR = "#4CAF50";
    private static final String BREAK_TIMER_COLOR = "#FF9800";
    private static final String URGENT_TIMER_COLOR = "#F44336";
    private static final String WARNING_TIMER_COLOR = "#FF9800";

    public TimerPanel(Font pixelFont, Consumer<Void> onSelectTask, Consumer<Void> onToggleTimer) {
        this.pixelFont = pixelFont;
        this.onSelectTask = onSelectTask;
        this.onToggleTimer = onToggleTimer;

        initializeTimers();
        createTimerInterface();
        setupResponsiveLayout();
        updateTimerDisplay();
    }

    private void initializeTimers() {
        workTimer = new PomodoroTimer(TimerType.WORK, 25 * 60);
        breakTimer = new PomodoroTimer(TimerType.SHORT_BREAK, 5 * 60);

        // Add listeners for timer events - NO button updates, just state logging
        workTimer.addListener(new com.focusflow.core.timer.TimerEventListener() {
            @Override
            public void onTimerStarted(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> logTimerState("Work timer started"));
            }

            @Override
            public void onTimerPaused(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> logTimerState("Work timer paused"));
            }

            @Override
            public void onTimerResumed(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> logTimerState("Work timer resumed"));
            }

            @Override
            public void onTimerCompleted(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> {
                    switchToBreakMode();
                    breakTimer.start();
                    updateTimerDisplay();
                    logTimerState("Work timer completed, break started");
                });
            }

            @Override
            public void onTimerStopped(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> logTimerState("Work timer stopped"));
            }

            @Override
            public void onTimerTick(com.focusflow.core.timer.Timer timer, int remainingSeconds) {
                Platform.runLater(() -> updateTimeDisplay(remainingSeconds));
            }

            @Override
            public void onTimerReset(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> logTimerState("Work timer reset"));
            }
        });

        breakTimer.addListener(new com.focusflow.core.timer.TimerEventListener() {
            @Override
            public void onTimerStarted(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> logTimerState("Break timer started"));
            }

            @Override
            public void onTimerPaused(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> logTimerState("Break timer paused"));
            }

            @Override
            public void onTimerResumed(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> logTimerState("Break timer resumed"));
            }

            @Override
            public void onTimerCompleted(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> {
                    switchToWorkMode();
                    updateTimerDisplay();
                    logTimerState("Break timer completed, switched to work mode");
                });
            }

            @Override
            public void onTimerStopped(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> logTimerState("Break timer stopped"));
            }

            @Override
            public void onTimerTick(com.focusflow.core.timer.Timer timer, int remainingSeconds) {
                Platform.runLater(() -> updateTimeDisplay(remainingSeconds));
            }

            @Override
            public void onTimerReset(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> logTimerState("Break timer reset"));
            }
        });
    }

    private void createTimerInterface() {
        setAlignment(Pos.CENTER);
        setSpacing(15);
        setStyle(TIMER_CONTAINER_STYLE);

        // Current task indicator
        currentTaskLabel = new Label("No task selected");
        currentTaskLabel.setTextFill(Color.DARKSLATEGRAY);
        currentTaskLabel.setWrapText(true);
        currentTaskLabel.setAlignment(Pos.CENTER);

        // Timer display section
        timerDisplay = new VBox(5);
        timerDisplay.setAlignment(Pos.CENTER);

        timerTypeLabel = new Label("WORK SESSION");
        timerTypeLabel.setTextFill(Color.BLACK);
        timerTypeLabel.setAlignment(Pos.CENTER);

        timerLabel = new Label("25:00");
        timerLabel.setTextFill(Color.BLACK);
        timerLabel.setAlignment(Pos.CENTER);

        remainingLabel = new Label("REMAINING...");
        remainingLabel.setTextFill(Color.DARKSLATEGRAY);
        remainingLabel.setAlignment(Pos.CENTER);

        timerDisplay.getChildren().addAll(timerTypeLabel, timerLabel, remainingLabel);

        // Only timer display - no control buttons
        getChildren().addAll(currentTaskLabel, timerDisplay);

        System.out.println("[TimerPanel] Timer display created without control buttons");
    }

    private void setupResponsiveLayout() {
        // Listen for size changes to adjust scaling
        this.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            if (newWidth.doubleValue() > 0) {
                updateScaling(newWidth.doubleValue(), getHeight());
            }
        });

        this.heightProperty().addListener((obs, oldHeight, newHeight) -> {
            if (newHeight.doubleValue() > 0) {
                updateScaling(getWidth(), newHeight.doubleValue());
            }
        });

        // Set to use available space but respect height constraints
        setMaxHeight(Region.USE_PREF_SIZE);
        setPrefHeight(Region.USE_COMPUTED_SIZE);
    }

    private void updateScaling(double availableWidth, double availableHeight) {
        if (availableWidth <= 0 || availableHeight <= 0)
            return;

        // Calculate scale factor based on available space
        double widthScale = availableWidth / 600.0;
        double heightScale = availableHeight / 400.0;

        // Use the smaller scale to maintain proportions
        scaleFactor = Math.min(widthScale, heightScale);

        // Clamp the scale factor to reasonable bounds
        scaleFactor = Math.max(0.3, Math.min(3.0, scaleFactor));

        // Apply responsive sizing
        updateFontSizes();
        updatePadding();
        updateSpacing();
    }

    private void updateFontSizes() {
        double taskFontSize = Math.max(18, 32 * scaleFactor);
        currentTaskLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, taskFontSize));

        double typeFontSize = Math.max(24, 40 * scaleFactor);
        timerTypeLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, typeFontSize));

        double timerFontSize = Math.max(48, 120 * scaleFactor);
        timerLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, timerFontSize));

        double remainingFontSize = Math.max(16, 28 * scaleFactor);
        remainingLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, remainingFontSize));
    }

    private void updatePadding() {
        double paddingValue = Math.max(20, 60 * scaleFactor);
        setPadding(new Insets(paddingValue));

        double displayPadding = Math.max(15, 30 * scaleFactor);
        timerDisplay.setPadding(new Insets(displayPadding));
    }

    private void updateSpacing() {
        double mainSpacing = Math.max(15, 45 * scaleFactor);
        setSpacing(mainSpacing);

        double displaySpacing = Math.max(8, 25 * scaleFactor);
        timerDisplay.setSpacing(displaySpacing);
    }

    // Helper method for logging timer state changes
    private void logTimerState(String message) {
        PomodoroTimer currentTimer = getCurrentTimer();
        TimerState state = currentTimer.getState();
        System.out.println("[TimerPanel] " + message + " - state=" + state + 
                          ", isOnBreak=" + isOnBreak + 
                          ", currentTask=" + (currentTask != null ? currentTask.getName() : "null"));
    }

    // Timer mode switching methods
    private void switchToWorkMode() {
        isOnBreak = false;
        timerTypeLabel.setText("WORK SESSION");
        timerTypeLabel.setTextFill(Color.web(WORK_TIMER_COLOR));
        updateTimeDisplay(25 * 60);
    }

    private void switchToBreakMode() {
        isOnBreak = true;
        timerTypeLabel.setText("BREAK TIME");
        timerTypeLabel.setTextFill(Color.web(BREAK_TIMER_COLOR));
        updateTimeDisplay(5 * 60);
    }

    private void updateTimeDisplay(int remainingSeconds) {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        String timeText = String.format("%02d:%02d", minutes, seconds);

        timerLabel.setText(timeText);

        // Apply color coding based on remaining time
        Color timerColor;
        if (remainingSeconds <= 60) {
            timerColor = Color.web(URGENT_TIMER_COLOR);
        } else if (remainingSeconds <= 300) {
            timerColor = Color.web(WARNING_TIMER_COLOR);
        } else {
            timerColor = Color.web(isOnBreak ? BREAK_TIMER_COLOR : WORK_TIMER_COLOR);
        }

        timerLabel.setTextFill(timerColor);
    }

    private void updateTimerDisplay() {
        PomodoroTimer currentTimer = getCurrentTimer();
        int remainingTime = currentTimer.getRemainingTime();

        updateTimeDisplay(remainingTime);

        // Update task label
        if (currentTask != null) {
            String taskText = "Working on: " + currentTask.getName();
            if (taskText.length() > 50) {
                taskText = taskText.substring(0, 47) + "...";
            }
            currentTaskLabel.setText(taskText);
            currentTaskLabel.setTextFill(Color.web("#2E7D32"));
        } else {
            currentTaskLabel.setText(isOnBreak ? "Break time! Relax and recharge" : "No task selected");
            currentTaskLabel.setTextFill(isOnBreak ? Color.web(BREAK_TIMER_COLOR) : Color.DARKSLATEGRAY);
        }
    }

    // Public API methods for external control

    /**
     * Handles start/pause/resume logic externally controlled
     */
    public void handleStartPauseAction() {
        System.out.println("[TimerPanel] handleStartPauseAction called from external control");

        PomodoroTimer currentTimer = getCurrentTimer();
        System.out.println("[TimerPanel] currentTimer state: " + currentTimer.getState());

        if (currentTimer.getState() == TimerState.RUNNING) {
            // Timer is running, pause it
            currentTimer.pause();
            System.out.println("[TimerPanel] Timer paused");
        } else {
            // Timer is paused or stopped, try to start/resume it
            if (!isOnBreak && currentTask == null) {
                // Can't start work timer without a task
                System.out.println("[TimerPanel] Cannot start - no task selected");
                return;
            }

            if (currentTimer.getState() == TimerState.PAUSED) {
                currentTimer.resume();
                System.out.println("[TimerPanel] Timer resumed");
            } else {
                currentTimer.start();
                System.out.println("[TimerPanel] Timer started");
            }
        }

        // Notify parent component if needed
        if (onToggleTimer != null) {
            onToggleTimer.accept(null);
        }
    }

    /**
     * Gets the current button text that should be displayed
     */
    public String getStartButtonText() {
        PomodoroTimer currentTimer = getCurrentTimer();
        TimerState state = currentTimer.getState();

        switch (state) {
            case RUNNING:
                return "PAUSE";
            case PAUSED:
                return "RESUME";
            default:
                return "START";
        }
    }

    /**
     * Checks if the start button should be enabled
     */
    public boolean isStartButtonEnabled() {
        // Only disable if we're in work mode and have no task
        return isOnBreak || currentTask != null;
    }

    /**
     * Checks if task selection should be enabled
     */
    public boolean isTaskSelectionEnabled() {
        // Disable during timer running or break mode
        TimerState state = getCurrentTimer().getState();
        return !isOnBreak && state != TimerState.RUNNING;
    }

    public void setCurrentTask(Task task) {
        System.out.println("[TimerPanel] setCurrentTask: " + (task != null ? task.getName() : "null"));

        this.currentTask = task;

        // Set task ID for timers
        if (task != null) {
            workTimer.setCurrentTaskId(task.getId().toString());
        }

        updateTimerDisplay();
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void startTimer() {
        System.out.println("[TimerPanel] startTimer called");
        PomodoroTimer currentTimer = getCurrentTimer();

        if (currentTimer.getState() != TimerState.RUNNING) {
            if (!isOnBreak && currentTask == null) {
                System.out.println("[TimerPanel] Cannot start work timer without task");
                return;
            }
            currentTimer.start();
            System.out.println("[TimerPanel] Timer started successfully");
        }
    }

    public void pauseTimer() {
        PomodoroTimer currentTimer = getCurrentTimer();
        if (currentTimer.getState() == TimerState.RUNNING) {
            currentTimer.pause();
        }
    }

    public void stopTimer() {
        getCurrentTimer().stop();
    }

    public void resetTimer() {
        getCurrentTimer().reset();
        updateTimerDisplay();
    }

    public TimerState getTimerState() {
        return getCurrentTimer().getState();
    }

    public boolean isOnBreak() {
        return isOnBreak;
    }

    public void forceWorkMode() {
        if (isOnBreak) {
            breakTimer.stop();
            switchToWorkMode();
            updateTimerDisplay();
        }
    }

    public void forceBreakMode() {
        if (!isOnBreak) {
            workTimer.stop();
            switchToBreakMode();
            updateTimerDisplay();
        }
    }

    public PomodoroTimer getWorkTimer() {
        return workTimer;
    }

    public PomodoroTimer getBreakTimer() {
        return breakTimer;
    }

    public void setScaleFactor(double newScaleFactor) {
        this.scaleFactor = Math.max(0.5, Math.min(2.0, newScaleFactor));
        updateFontSizes();
        updatePadding();
        updateSpacing();
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    private PomodoroTimer getCurrentTimer() {
        return isOnBreak ? breakTimer : workTimer;
    }
}