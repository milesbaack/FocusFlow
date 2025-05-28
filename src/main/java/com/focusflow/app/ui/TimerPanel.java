package com.focusflow.app.ui;

import java.util.function.Consumer;

import com.focusflow.core.task.Task;
import com.focusflow.core.timer.PomodoroTimer;
import com.focusflow.core.timer.TimerState;
import com.focusflow.core.timer.TimerType;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * FIXED Timer Panel that handles all timer GUI logic and layout.
 * Fixed issues with button interactions and timer state management.
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

    // UI Components
    private Label currentTaskLabel;
    private Label timerTypeLabel;
    private Label timerLabel;
    private Label remainingLabel;
    private Button selectTaskButton;
    private Button startButton;
    private HBox controlsContainer;
    private VBox timerDisplay;

    // Responsive sizing properties
    private double baseWidth = 400;
    private double baseHeight = 200;
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

        // Add listeners for timer events
        workTimer.addListener(new com.focusflow.core.timer.TimerEventListener() {
            @Override
            public void onTimerStarted(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> updateButtonStates());
            }

            @Override
            public void onTimerPaused(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> updateButtonStates());
            }

            @Override
            public void onTimerResumed(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> updateButtonStates());
            }

            @Override
            public void onTimerCompleted(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> {
                    switchToBreakMode();
                    breakTimer.start();
                    updateButtonStates();
                    updateTimerDisplay();
                });
            }

            @Override
            public void onTimerStopped(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> updateButtonStates());
            }

            @Override
            public void onTimerTick(com.focusflow.core.timer.Timer timer, int remainingSeconds) {
                Platform.runLater(() -> updateTimeDisplay(remainingSeconds));
            }

            @Override
            public void onTimerReset(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> updateButtonStates());
            }
        });

        breakTimer.addListener(new com.focusflow.core.timer.TimerEventListener() {
            @Override
            public void onTimerStarted(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> updateButtonStates());
            }

            @Override
            public void onTimerPaused(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> updateButtonStates());
            }

            @Override
            public void onTimerResumed(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> updateButtonStates());
            }

            @Override
            public void onTimerCompleted(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> {
                    switchToWorkMode();
                    updateButtonStates();
                    updateTimerDisplay();
                });
            }

            @Override
            public void onTimerStopped(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> updateButtonStates());
            }

            @Override
            public void onTimerTick(com.focusflow.core.timer.Timer timer, int remainingSeconds) {
                Platform.runLater(() -> updateTimeDisplay(remainingSeconds));
            }

            @Override
            public void onTimerReset(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> updateButtonStates());
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

        // FIXED: Control buttons with basic JavaFX button creation
        controlsContainer = new HBox(15);
        controlsContainer.setAlignment(Pos.CENTER);

        // Create buttons with minimal styling first
        selectTaskButton = new Button("Select Task");
        startButton = new Button("START");

        // Set basic styles that we know work
        selectTaskButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px;");
        startButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px;");

        // Add event handlers AFTER style setting
        selectTaskButton.setOnAction(e -> {
            System.out.println("[TimerPanel] SELECT TASK CLICKED!");
            if (onSelectTask != null) {
                System.out.println("[TimerPanel] Calling onSelectTask callback");
                onSelectTask.accept(null);
            } else {
                System.out.println("[TimerPanel] onSelectTask callback is null!");
            }
        });

        startButton.setOnAction(e -> {
            System.out.println("[TimerPanel] START BUTTON CLICKED!");
            handleStartButtonClick();
        });

        controlsContainer.getChildren().addAll(selectTaskButton, startButton);

        getChildren().addAll(currentTaskLabel, timerDisplay, controlsContainer);

        // Add debug output
        System.out.println("[TimerPanel] Buttons created with handlers");
        System.out.println("[TimerPanel] TimerPanel children count: " + getChildren().size());
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

        // Calculate scale factor more aggressively based on available space
        double widthScale = availableWidth / 600.0; // Base width for scaling
        double heightScale = availableHeight / 400.0; // Base height for scaling

        // Use the smaller scale to maintain proportions
        scaleFactor = Math.min(widthScale, heightScale);

        // Clamp the scale factor to reasonable bounds
        scaleFactor = Math.max(0.3, Math.min(3.0, scaleFactor));

        // Apply responsive font sizes
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

        double buttonFontSize = Math.max(18, 32 * scaleFactor);
        selectTaskButton.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, buttonFontSize));
        startButton.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, buttonFontSize));
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

        double controlSpacing = Math.max(15, 35 * scaleFactor);
        controlsContainer.setSpacing(controlSpacing);
    }

    // FIXED: Simplified button click handler
    private void handleStartButtonClick() {
        System.out.println(
                "[TimerPanel] handleStartButtonClick: isOnBreak=" + isOnBreak + ", currentTask=" + currentTask);

        PomodoroTimer currentTimer = getCurrentTimer();
        System.out.println("[TimerPanel] currentTimer state: " + currentTimer.getState());

        if (currentTimer.getState() == TimerState.RUNNING) {
            // Timer is running, pause it
            currentTimer.pause();
            System.out.println("[TimerPanel] Timer paused");
        } else {
            // Timer is paused or stopped, try to start/resume it
            if (!isOnBreak && currentTask == null) {
                // Can't start work timer without a task - show task selection
                System.out.println("[TimerPanel] No task selected, showing task selection");
                if (onSelectTask != null) {
                    onSelectTask.accept(null);
                }
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

    private PomodoroTimer getCurrentTimer() {
        return isOnBreak ? breakTimer : workTimer;
    }

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

    // FIXED: Clearer button state management
    private void updateButtonStates() {
        PomodoroTimer currentTimer = getCurrentTimer();
        TimerState state = currentTimer.getState();

        System.out.println("[TimerPanel] updateButtonStates: state=" + state + ", isOnBreak=" + isOnBreak
                + ", currentTask=" + (currentTask != null ? currentTask.getName() : "null"));

        // Update start button text and state
        switch (state) {
            case RUNNING:
                startButton.setText("PAUSE");
                startButton.setDisable(false);
                break;
            case PAUSED:
                startButton.setText("RESUME");
                startButton.setDisable(false);
                break;
            default:
                startButton.setText("START");
                // Only disable if we're in work mode and have no task
                boolean shouldDisable = !isOnBreak && currentTask == null;
                startButton.setDisable(shouldDisable);
                System.out.println("[TimerPanel] Start button disabled: " + shouldDisable);
                break;
        }

        // Update select task button state
        // Disable during timer running or break mode
        boolean selectTaskDisabled = isOnBreak || state == TimerState.RUNNING;
        selectTaskButton.setDisable(selectTaskDisabled);
        System.out.println("[TimerPanel] Select task button disabled: " + selectTaskDisabled);

        updateButtonStyles();
    }

    private void updateButtonStyles() {
        double buttonPadding = Math.max(12, 20 * scaleFactor);
        double buttonMinWidth = Math.max(120, 200 * scaleFactor);

        // FIXED: Better button styling with proper disabled states
        String selectTaskStyle = String.format(
                "-fx-background-color: %s; -fx-text-fill: white; " +
                        "-fx-background-radius: 15; -fx-padding: %.0f 20; " +
                        "-fx-font-weight: bold; -fx-min-width: %.0f; -fx-cursor: hand;",
                selectTaskButton.isDisabled() ? "#CCCCCC" : WORK_TIMER_COLOR,
                buttonPadding, buttonMinWidth);

        String startButtonStyle = String.format(
                "-fx-background-color: %s; -fx-text-fill: %s; " +
                        "-fx-background-radius: 15; -fx-padding: %.0f 20; " +
                        "-fx-font-weight: bold; -fx-min-width: %.0f; " +
                        "-fx-border-color: #DDD; -fx-border-radius: 15; -fx-cursor: hand;",
                startButton.isDisabled() ? "#F5F5F5" : "white",
                startButton.isDisabled() ? "#999999" : "black",
                buttonPadding, buttonMinWidth);

        selectTaskButton.setStyle(selectTaskStyle);
        startButton.setStyle(startButtonStyle);

        // Add hover effects only for enabled buttons
        addButtonHoverEffects();
    }

    private void addButtonHoverEffects() {
        // Select Task button hover
        selectTaskButton.setOnMouseEntered(e -> {
            if (!selectTaskButton.isDisabled()) {
                selectTaskButton.setStyle(selectTaskButton.getStyle() +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
            }
        });

        selectTaskButton.setOnMouseExited(e -> {
            selectTaskButton.setStyle(selectTaskButton.getStyle().replace(
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);", ""));
        });

        // Start button hover
        startButton.setOnMouseEntered(e -> {
            if (!startButton.isDisabled()) {
                startButton.setStyle(startButton.getStyle() +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
            }
        });

        startButton.setOnMouseExited(e -> {
            startButton.setStyle(startButton.getStyle().replace(
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);", ""));
        });
    }

    private void updateTimerDisplay() {
        PomodoroTimer currentTimer = getCurrentTimer();
        int remainingTime = currentTimer.getRemainingTime();

        updateTimeDisplay(remainingTime);
        updateButtonStates();

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

    // FIXED: Simplified timer control methods
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
        updateButtonStyles();
    }

    public double getScaleFactor() {
        return scaleFactor;
    }
}