package com.focusflow.app;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;

import com.focusflow.core.notification.Notification;
import com.focusflow.core.notification.NotificationListener;
import com.focusflow.core.notification.NotificationManager;
import com.focusflow.core.notification.NotificationType;
import com.focusflow.core.session.FocusSession;
import com.focusflow.core.session.SessionManager;
import com.focusflow.core.task.Task;
import com.focusflow.core.timer.PomodoroTimer;
import com.focusflow.core.timer.Timer;
import com.focusflow.core.timer.TimerEventListener;
import com.focusflow.core.timer.TimerState;
import com.focusflow.core.timer.TimerType;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Main application class for FocusFlow.
 * 
 * This class implements the main user interface for the FocusFlow application,
 * integrating the timer, task management, and session tracking features.
 * 
 * @author Emilio Lopez
 * @version 1.0.0
 */
public class App extends Application {
    private volatile PomodoroTimer timer;
    private SessionManager sessionManager;
    private NotificationManager notificationManager;
    private Task currentTask;
    private ListView<Task> taskListView;
    private TextField taskNameField;
    private TextArea taskDescriptionField;
    private Label timerLabel;
    private TextArea sessionHistoryArea;
    private Button startButton;
    private Button pauseButton;
    private Button resetButton;
    private Button createTaskButton;
    private ComboBox<TimerType> timerTypeComboBox;
    private CheckBox taskComplete;
    private Label currentTaskLabel;
    private Label sessionStatusLabel;
    private ListView<Notification> notificationListView;
    private TitledPane notificationPane;
    private TitledPane sessionHistoryPane;
    private final AtomicReference<TimerType> currentTimerType = new AtomicReference<>(TimerType.WORK);

    @Override
    public void start(Stage stage) {
        // Initialize components
        sessionManager = new SessionManager();
        notificationManager = new NotificationManager();
        timer = new PomodoroTimer(TimerType.WORK);
        currentTask = null;
        
        // Send initial notification
        notificationManager.sendNotification(new Notification(
            "System Ready",
            "FocusFlow is ready for testing with 5-second sessions",
            NotificationType.INFO,
            null
        ));

        // Create UI components
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // Notification panel at the top
        notificationListView = new ListView<>();
        notificationListView.setPrefHeight(100);
        Button clearNotificationsButton = new Button("Clear All");
        clearNotificationsButton.setOnAction(e -> notificationManager.clearAllNotifications());
        
        HBox notificationControls = new HBox(10, clearNotificationsButton);
        notificationControls.setAlignment(Pos.CENTER_RIGHT);
        
        VBox notificationBox = new VBox(5, notificationListView, notificationControls);
        notificationBox.setPadding(new Insets(5));
        
        notificationPane = new TitledPane("Notifications", notificationBox);
        notificationPane.setCollapsible(true);
        notificationPane.setExpanded(false);
        
        // Add notification listener
        notificationManager.addListener(new NotificationListener() {
            @Override
            public void onNotificationCreated(Notification notification) {
                Platform.runLater(() -> {
                    notificationListView.getItems().add(0, notification);
                    if (notificationListView.getItems().size() > 50) {
                        notificationListView.getItems().remove(notificationListView.getItems().size() - 1);
                    }
                    // Expand notification pane when new notification arrives
                    notificationPane.setExpanded(true);
                });
            }
            
            @Override
            public void onNotificationRead(Notification notification) {
                Platform.runLater(() -> {
                    int index = notificationListView.getItems().indexOf(notification);
                    if (index >= 0) {
                        notificationListView.getItems().set(index, notification);
                    }
                });
            }
            
            @Override
            public void onNotificationsCleared() {
                Platform.runLater(() -> {
                    notificationListView.getItems().clear();
                });
            }
        });

        // Task creation section
        HBox taskInputBox = new HBox(10);
        taskNameField = new TextField();
        taskNameField.setPromptText("Task Name");
        taskDescriptionField = new TextArea();
        taskDescriptionField.setPromptText("Task Description");
        taskDescriptionField.setPrefRowCount(2);
        createTaskButton = new Button("Create Task");
        createTaskButton.setOnAction(e -> createNewTask());
        taskInputBox.getChildren().addAll(taskNameField, taskDescriptionField, createTaskButton);
        HBox.setHgrow(taskDescriptionField, Priority.ALWAYS);

        // Task list section
        taskListView = new ListView<>();
        taskListView.setPrefHeight(150);
        taskListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectTask(newVal);
            }
        });

        // Current task display
        HBox currentTaskBox = new HBox(10);
        currentTaskLabel = new Label("No task selected");
        currentTaskLabel.setStyle("-fx-font-weight: bold;");
        taskComplete = new CheckBox("Complete");
        taskComplete.setOnAction(e -> {
            if (currentTask != null) {
                if (taskComplete.isSelected()) {
                    currentTask.markAsCompleted();
                    notificationManager.sendNotification(new Notification(
                        "Task Completed",
                        "Task '" + currentTask.getName() + "' has been completed",
                        NotificationType.TASK_COMPLETED,
                        currentTask.getId().toString()
                    ));
                    endCurrentSession();
                } else {
                    currentTask.markAsIncomplete();
                    notificationManager.sendNotification(new Notification(
                        "Task Updated",
                        "Task '" + currentTask.getName() + "' has been marked as incomplete",
                        NotificationType.TASK_UPDATED,
                        currentTask.getId().toString()
                    ));
                }
                updateTaskDisplay();
            }
        });
        currentTaskBox.getChildren().addAll(currentTaskLabel, taskComplete);
        currentTaskBox.setAlignment(Pos.CENTER_LEFT);

        // Timer type selection
        timerTypeComboBox = new ComboBox<>();
        timerTypeComboBox.getItems().addAll(TimerType.values());
        timerTypeComboBox.setValue(TimerType.WORK);
        timerTypeComboBox.setOnAction(e -> {
            if (currentTask != null) {
                updateTimer(timerTypeComboBox.getValue());
                notificationManager.sendNotification(new Notification(
                    "Timer Type Changed",
                    "Timer type changed to " + timerTypeComboBox.getValue(),
                    NotificationType.TIMER_UPDATED,
                    currentTask.getId().toString()
                ));
            }
        });

        // Timer section
        HBox timerBox = new HBox(10);
        timerLabel = new Label("25:00");
        timerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        sessionStatusLabel = new Label("No active session");
        timerBox.getChildren().addAll(timerLabel, sessionStatusLabel);
        timerBox.setAlignment(Pos.CENTER);

        // Timer controls
        startButton = new Button("Start");
        pauseButton = new Button("Pause");
        resetButton = new Button("Reset");

        startButton.setOnAction(e -> startNewSession());
        pauseButton.setOnAction(e -> pauseCurrentSession());
        resetButton.setOnAction(e -> resetTimer());

        HBox timerControls = new HBox(10);
        timerControls.getChildren().addAll(startButton, pauseButton, resetButton);
        timerControls.setAlignment(Pos.CENTER);

        // Session history
        sessionHistoryArea = new TextArea();
        sessionHistoryArea.setEditable(false);
        sessionHistoryArea.setPrefRowCount(5);
        sessionHistoryArea.setPrefHeight(150);
        
        sessionHistoryPane = new TitledPane("Session History", sessionHistoryArea);
        sessionHistoryPane.setCollapsible(true);
        sessionHistoryPane.setExpanded(false);

        // Add components to root
        root.getChildren().addAll(
            notificationPane,
            new Separator(),
            new Label("Create New Task:"),
            taskInputBox,
            new Separator(),
            new Label("Task List:"),
            taskListView,
            new Separator(),
            new Label("Current Task:"),
            currentTaskBox,
            new Separator(),
            new Label("Timer Type:"),
            timerTypeComboBox,
            new Separator(),
            timerBox,
            timerControls,
            new Separator(),
            sessionHistoryPane
        );

        // Create scene
        Scene scene = new Scene(root, 500, 700);
        stage.setTitle("FocusFlow");
        stage.setScene(scene);
        stage.show();

        // Initialize UI state
        updateUIState();
    }

    private void setupTimerListeners() {
        timer.addListener(new TimerEventListener() {
            @Override
            public void onTimerTick(Timer timer, int remainingSeconds) {
                Platform.runLater(() -> {
                    int minutes = remainingSeconds / 60;
                    int seconds = remainingSeconds % 60;
                    timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
                    
                    // Send warning notification at 3 seconds remaining
                    if (remainingSeconds == 3) {
                        notificationManager.sendNotification(new Notification(
                            "Almost Done!",
                            "3 seconds remaining in your " + (currentTimerType.get() == TimerType.WORK ? "work" : "break") + " session",
                            NotificationType.WARNING,
                            currentTask != null ? currentTask.getId().toString() : null
                        ));
                    }
                });
            }

            @Override
            public void onTimerCompleted(Timer timer) {
                final TimerType currentType = currentTimerType.get();
                final TimerType newType = (currentType == TimerType.WORK) ? TimerType.BREAK : TimerType.WORK;
                
                Platform.runLater(() -> {
                    timerLabel.setText("Done!");
                    notificationManager.sendNotification(new Notification(
                        "Session Ended",
                        "Your " + (currentType == TimerType.WORK ? "work" : "break") + " session has ended",
                        NotificationType.SESSION_ENDED,
                        currentTask != null ? currentTask.getId().toString() : null
                    ));
                    
                    // Switch timer type
                    currentTimerType.set(newType);
                    updateTimer(newType);
                    
                    if (newType == TimerType.BREAK) {
                        notificationManager.sendNotification(new Notification(
                            "Break Time!",
                            "Take a 3-second break",
                            NotificationType.BREAK_REMINDER,
                            null
                        ));
                    } else {
                        notificationManager.sendNotification(new Notification(
                            "Work Time!",
                            "Ready for your next work session",
                            NotificationType.SESSION_STARTED,
                            currentTask != null ? currentTask.getId().toString() : null
                        ));
                    }
                    
                    endCurrentSession();
                    updateSessionHistory();
                    updateUIState();
                });
            }

            @Override
            public void onTimerStarted(Timer timer) {
                Platform.runLater(() -> {
                    sessionStatusLabel.setText((currentTimerType.get() == TimerType.WORK ? "Work" : "Break") + " session in progress");
                    notificationManager.sendNotification(new Notification(
                        "Session Started",
                        "Your " + (currentTimerType.get() == TimerType.WORK ? "work" : "break") + " session has begun",
                        NotificationType.SESSION_STARTED,
                        currentTask != null ? currentTask.getId().toString() : null
                    ));
                    updateUIState();
                });
            }

            @Override
            public void onTimerPaused(Timer timer) {
                Platform.runLater(() -> {
                    sessionStatusLabel.setText((currentTimerType.get() == TimerType.WORK ? "Work" : "Break") + " session paused");
                    notificationManager.sendNotification(new Notification(
                        "Session Paused",
                        "Your " + (currentTimerType.get() == TimerType.WORK ? "work" : "break") + " session has been paused",
                        NotificationType.SESSION_PAUSED,
                        currentTask != null ? currentTask.getId().toString() : null
                    ));
                    updateUIState();
                });
            }

            @Override
            public void onTimerResumed(Timer timer) {
                Platform.runLater(() -> {
                    sessionStatusLabel.setText((currentTimerType.get() == TimerType.WORK ? "Work" : "Break") + " session in progress");
                    notificationManager.sendNotification(new Notification(
                        "Session Resumed",
                        "Your " + (currentTimerType.get() == TimerType.WORK ? "work" : "break") + " session has resumed",
                        NotificationType.SESSION_RESUMED,
                        currentTask != null ? currentTask.getId().toString() : null
                    ));
                    updateUIState();
                });
            }

            @Override
            public void onTimerStopped(Timer timer) {
                Platform.runLater(() -> {
                    sessionStatusLabel.setText((currentTimerType.get() == TimerType.WORK ? "Work" : "Break") + " session stopped");
                    notificationManager.sendNotification(new Notification(
                        "Session Stopped",
                        "Your " + (currentTimerType.get() == TimerType.WORK ? "work" : "break") + " session has been stopped",
                        NotificationType.SESSION_STOPPED,
                        currentTask != null ? currentTask.getId().toString() : null
                    ));
                    updateUIState();
                });
            }

            @Override
            public void onTimerReset(Timer timer) {
                Platform.runLater(() -> {
                    timerLabel.setText(String.format("%02d:00", timer.getType().getDefaultDuration() / 60));
                    sessionStatusLabel.setText("No active session");
                    notificationManager.sendNotification(new Notification(
                        "Timer Reset",
                        "Timer has been reset to initial state",
                        NotificationType.TIMER_RESET,
                        currentTask != null ? currentTask.getId().toString() : null
                    ));
                    updateUIState();
                });
            }
        });
    }

    private void createNewTask() {
        String name = taskNameField.getText().trim();
        String description = taskDescriptionField.getText().trim();
        
        if (!name.isEmpty()) {
            Task newTask = new Task(name, description);
            taskListView.getItems().add(newTask);
            taskNameField.clear();
            taskDescriptionField.clear();
            
            notificationManager.sendNotification(new Notification(
                "Task Created",
                "New task '" + name + "' has been created",
                NotificationType.TASK_CREATED,
                newTask.getId().toString()
            ));
        }
    }

    private void selectTask(Task task) {
        if (task != null) {
            currentTask = task;
            updateTaskDisplay();
            updateUIState();
            timer.setCurrentTaskId(task.getId().toString());
            
            notificationManager.sendNotification(new Notification(
                "Task Selected",
                "Task '" + task.getName() + "' has been selected",
                NotificationType.TASK_SELECTED,
                task.getId().toString()
            ));
        }
    }

    private void updateTaskDisplay() {
        if (currentTask != null) {
            currentTaskLabel.setText("Current Task: " + currentTask.getName());
            taskComplete.setSelected(currentTask.isComplete());
        } else {
            currentTaskLabel.setText("No task selected");
            taskComplete.setSelected(false);
        }
    }

    private void updateUIState() {
        boolean hasTask = currentTask != null;
        boolean isSessionActive = timer.getState() == TimerState.RUNNING;
        boolean isSessionPaused = timer.getState() == TimerState.PAUSED;

        createTaskButton.setDisable(false);
        startButton.setDisable(!hasTask || isSessionActive);
        pauseButton.setDisable(!hasTask || !isSessionActive || isSessionPaused);
        resetButton.setDisable(!hasTask || isSessionActive);
        taskComplete.setDisable(!hasTask);
        timerTypeComboBox.setDisable(!hasTask || isSessionActive);
        
        // Update start button text based on timer type
        startButton.setText(currentTimerType.get() == TimerType.WORK ? "Start Work" : "Start Break");
    }

    private void startNewSession() {
        if (currentTask != null) {
            try {
                sessionManager.startSession(currentTask.getId().toString());
                timer = new PomodoroTimer(currentTimerType.get(), 5); // 5 seconds for testing
                timer.setCurrentTaskId(currentTask.getId().toString());
                setupTimerListeners();
                timer.start();
                updateUIState();
                
                notificationManager.sendNotification(new Notification(
                    "Session Started",
                    "Started 5-second " + (currentTimerType.get() == TimerType.WORK ? "work" : "break") + " session for task: " + currentTask.getName(),
                    NotificationType.SESSION_STARTED,
                    currentTask.getId().toString()
                ));
            } catch (IllegalArgumentException e) {
                sessionStatusLabel.setText("Error: " + e.getMessage());
                
                notificationManager.sendNotification(new Notification(
                    "Session Error",
                    e.getMessage(),
                    NotificationType.ERROR,
                    null
                ));
            }
        } else {
            sessionStatusLabel.setText("Please select a task first");
            
            notificationManager.sendNotification(new Notification(
                "Task Required",
                "Please select a task before starting a session",
                NotificationType.WARNING,
                null
            ));
        }
    }

    private void pauseCurrentSession() {
        if (timer.getState() == TimerState.RUNNING) {
            timer.pause();
            sessionManager.pauseCurrentSession();
        } else if (timer.getState() == TimerState.PAUSED) {
            timer.resume();
            sessionManager.resumeCurrentSession();
        }
        updateUIState();
    }

    private void resetTimer() {
        timer.reset();
        sessionManager.endCurrentSession();
        updateSessionHistory();
        updateUIState();
    }

    private void endCurrentSession() {
        timer.stop();
        sessionManager.endCurrentSession();
        updateSessionHistory();
        updateUIState();
    }

    private void updateSessionHistory() {
        StringBuilder history = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (FocusSession session : sessionManager.getSessionHistory()) {
            history.append(String.format(
                "[%s] %s - Duration: %d seconds\n",
                session.getStartTime().format(formatter),
                session.getType() == TimerType.WORK ? "Work Session" : "Break Session",
                session.getDurationSeconds()
            ));
        }
        sessionHistoryArea.setText(history.toString());
        sessionHistoryPane.setExpanded(true); // Auto-expand when updated
    }

    private void updateTimer(TimerType type) {
        final PomodoroTimer newTimer = new PomodoroTimer(type, 5); // 5 seconds for testing
        newTimer.setCurrentTaskId(currentTask != null ? currentTask.getId().toString() : null);
        setupTimerListeners();
        this.timer = newTimer;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
