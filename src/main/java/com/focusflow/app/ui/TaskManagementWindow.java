package com.focusflow.app.ui;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.focusflow.core.gameify.Achievement;
import com.focusflow.core.gameify.Quest;
import com.focusflow.core.gameify.QuestManager;
import com.focusflow.core.task.Task;
import com.focusflow.core.task.TaskPriority;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Comprehensive Task and Quest Management Window
 * Provides full CRUD operations for tasks and quests with tabbed interface
 */
public class TaskManagementWindow {
    private Stage stage;
    private List<Task> tasks;
    private QuestManager questManager;
    private Font pixelFont;
    private Runnable onTasksUpdated;
    private BorderPane root;

    // UI Components
    private ListView<TaskItem> taskListView;
    private ListView<QuestItem> questListView;
    private ObservableList<TaskItem> taskItems;
    private ObservableList<QuestItem> questItems;

    public TaskManagementWindow(List<Task> tasks, QuestManager questManager, Font pixelFont) {
        this.tasks = tasks;
        this.questManager = questManager;
        this.pixelFont = pixelFont;
        this.taskItems = FXCollections.observableArrayList();
        this.questItems = FXCollections.observableArrayList();
        initializeWindow();
    }

    public void setOnTasksUpdated(Runnable callback) {
        this.onTasksUpdated = callback;
    }

    private void initializeWindow() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Task & Quest Management");
        stage.setWidth(800);
        stage.setHeight(600);

        // Create main layout
        root = new BorderPane();

        // Create tabbed interface
        TabPane tabPane = new TabPane();

        // Tasks tab
        Tab tasksTab = new Tab("Tasks");
        tasksTab.setClosable(false);
        tasksTab.setContent(createTasksPanel());

        // Quests tab
        Tab questsTab = new Tab("Quests");
        questsTab.setClosable(false);
        questsTab.setContent(createQuestsPanel());

        tabPane.getTabs().addAll(tasksTab, questsTab);

        root.setCenter(tabPane);

        Scene scene = new Scene(root);
        stage.setScene(scene);

        refreshTaskList();
        refreshQuestList();
    }

    private VBox createTasksPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        // Title
        Label title = new Label("Task Management");
        title.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 24));

        // Buttons
        HBox buttonBox = new HBox(10);
        Button createTaskBtn = new Button("Create Task");
        Button editTaskBtn = new Button("Edit Task");
        Button deleteTaskBtn = new Button("Delete Task");
        Button markCompleteBtn = new Button("Mark Complete");
        Button assignToQuestBtn = new Button("Assign to Quest");

        createTaskBtn.setOnAction(e -> showCreateTaskDialog());
        editTaskBtn.setOnAction(e -> editSelectedTask());
        deleteTaskBtn.setOnAction(e -> deleteSelectedTask());
        markCompleteBtn.setOnAction(e -> markTaskComplete());
        assignToQuestBtn.setOnAction(e -> assignTasksToQuest());

        buttonBox.getChildren().addAll(createTaskBtn, editTaskBtn, deleteTaskBtn, markCompleteBtn, assignToQuestBtn);

        // Task list
        taskListView = new ListView<>();
        taskListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        taskListView.setPrefHeight(400);
        taskListView.setItems(taskItems);

        // Filter options
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        Label filterLabel = new Label("Filter:");
        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All Tasks", "Incomplete Tasks", "Complete Tasks", "Overdue Tasks");
        filterCombo.setValue("All Tasks");
        filterCombo.setOnAction(e -> applyTaskFilter(filterCombo.getValue()));

        filterBox.getChildren().addAll(filterLabel, filterCombo);

        panel.getChildren().addAll(title, buttonBox, filterBox, taskListView);
        return panel;
    }

    private VBox createQuestsPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        // Title
        Label title = new Label("Quest Management");
        title.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 24));

        // Buttons
        HBox buttonBox = new HBox(10);
        Button createQuestBtn = new Button("Create Quest");
        Button editQuestBtn = new Button("Edit Quest");
        Button deleteQuestBtn = new Button("Delete Quest");
        Button viewQuestDetailsBtn = new Button("View Details");

        createQuestBtn.setOnAction(e -> showCreateQuestDialog());
        editQuestBtn.setOnAction(e -> editSelectedQuest());
        deleteQuestBtn.setOnAction(e -> deleteSelectedQuest());
        viewQuestDetailsBtn.setOnAction(e -> viewQuestDetails());

        buttonBox.getChildren().addAll(createQuestBtn, editQuestBtn, deleteQuestBtn, viewQuestDetailsBtn);

        // Quest list
        questListView = new ListView<>();
        questListView.setPrefHeight(400);
        questListView.setItems(questItems);

        panel.getChildren().addAll(title, buttonBox, questListView);
        return panel;
    }

    private void showCreateTaskDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Create New Task");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        // Task name
        Label nameLabel = new Label("Task Name:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter task name");

        // Task description
        Label descLabel = new Label("Description:");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Enter task description (optional)");
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);

        // Priority
        Label priorityLabel = new Label("Priority:");
        ComboBox<TaskPriority> priorityCombo = new ComboBox<>();
        priorityCombo.setItems(FXCollections.observableArrayList(TaskPriority.values()));
        priorityCombo.setValue(TaskPriority.MEDIUM);

        // Due date
        Label dueDateLabel = new Label("Due Date:");
        DatePicker dueDatePicker = new DatePicker();

        // Quest assignment
        Label questLabel = new Label("Assign to Quest:");
        ComboBox<String> questCombo = new ComboBox<>();
        updateQuestComboBox(questCombo);

        // Buttons
        Button saveButton = new Button("Save Task");
        Button cancelButton = new Button("Cancel");

        saveButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showAlert("Error", "Task name cannot be empty!");
                return;
            }

            Task newTask = new Task(name, descArea.getText().trim());
            newTask.setPriority(priorityCombo.getValue());

            if (dueDatePicker.getValue() != null) {
                newTask.setDueDateTime(dueDatePicker.getValue().atStartOfDay());
            }

            tasks.add(newTask);

            // Add to quest if selected
            String selectedQuest = questCombo.getValue();
            if (selectedQuest != null && !selectedQuest.equals("No Quest")) {
                questManager.getAllQuests().values().stream()
                        .filter(quest -> quest.getTitle().equals(selectedQuest))
                        .findFirst()
                        .ifPresent(quest -> questManager.addTaskToQuest(quest.getId(), newTask));
            }

            refreshTaskList();
            refreshQuestList();
            notifyTasksUpdated();
            dialog.close();
        });

        cancelButton.setOnAction(e -> dialog.close());

        // Layout
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(descLabel, 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(priorityLabel, 0, 2);
        grid.add(priorityCombo, 1, 2);
        grid.add(dueDateLabel, 0, 3);
        grid.add(dueDatePicker, 1, 3);
        grid.add(questLabel, 0, 4);
        grid.add(questCombo, 1, 4);

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        grid.add(buttonBox, 1, 5);

        Scene scene = new Scene(grid, 400, 350);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showCreateQuestDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Create New Quest");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        // Quest title
        Label titleLabel = new Label("Quest Title:");
        TextField titleField = new TextField();
        titleField.setPromptText("Enter quest title");

        // Quest description
        Label descLabel = new Label("Description:");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Enter quest description");
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);

        // Base XP reward
        Label xpLabel = new Label("Base XP Reward:");
        TextField xpField = new TextField();
        xpField.setPromptText("Enter base XP (e.g., 100)");
        xpField.setText("100");

        // Achievement reward
        Label achievementLabel = new Label("Achievement Reward:");
        ComboBox<String> achievementCombo = new ComboBox<>();
        achievementCombo.getItems().addAll("None", "Quest Completion Achievement");
        achievementCombo.setValue("None");

        // Buttons
        Button saveButton = new Button("Create Quest");
        Button cancelButton = new Button("Cancel");

        saveButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                showAlert("Error", "Quest title cannot be empty!");
                return;
            }

            int baseXp;
            try {
                baseXp = Integer.parseInt(xpField.getText().trim());
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid XP value!");
                return;
            }

            Achievement reward = null;
            if (!"None".equals(achievementCombo.getValue())) {
                reward = Achievement.COMPLETED_FIRST_QUEST; // Default achievement
            }

            Quest newQuest = new Quest(title, descArea.getText().trim(), reward, baseXp);
            questManager.addQuest(newQuest);

            refreshQuestList();
            notifyTasksUpdated();
            dialog.close();
        });

        cancelButton.setOnAction(e -> dialog.close());

        // Layout
        grid.add(titleLabel, 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(descLabel, 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(xpLabel, 0, 2);
        grid.add(xpField, 1, 2);
        grid.add(achievementLabel, 0, 3);
        grid.add(achievementCombo, 1, 3);

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        grid.add(buttonBox, 1, 4);

        Scene scene = new Scene(grid, 400, 300);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void editSelectedTask() {
        TaskItem selectedItem = taskListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("No Selection", "Please select a task to edit.");
            return;
        }

        Task task = selectedItem.getTask();

        // Create edit dialog (similar to create dialog but pre-filled)
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Task");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        // Pre-fill fields with current values
        TextField nameField = new TextField(task.getName());
        TextArea descArea = new TextArea(task.getDescription());
        ComboBox<TaskPriority> priorityCombo = new ComboBox<>();
        priorityCombo.setItems(FXCollections.observableArrayList(TaskPriority.values()));
        priorityCombo.setValue(task.getPriority());

        DatePicker dueDatePicker = new DatePicker();
        if (task.hasDueDateTime()) {
            dueDatePicker.setValue(task.getDueDateTime().toLocalDate());
        }

        Button saveButton = new Button("Save Changes");
        Button cancelButton = new Button("Cancel");

        saveButton.setOnAction(e -> {
            task.setName(nameField.getText().trim());
            task.setDescription(descArea.getText().trim());
            task.setPriority(priorityCombo.getValue());

            if (dueDatePicker.getValue() != null) {
                task.setDueDateTime(dueDatePicker.getValue().atStartOfDay());
            } else {
                task.setDueDateTime(null);
            }

            refreshTaskList();
            refreshQuestList();
            notifyTasksUpdated();
            dialog.close();
        });

        cancelButton.setOnAction(e -> dialog.close());

        // Layout
        grid.add(new Label("Task Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Priority:"), 0, 2);
        grid.add(priorityCombo, 1, 2);
        grid.add(new Label("Due Date:"), 0, 3);
        grid.add(dueDatePicker, 1, 3);

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        grid.add(buttonBox, 1, 4);

        Scene scene = new Scene(grid, 400, 300);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void deleteSelectedTask() {
        TaskItem selectedItem = taskListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("No Selection", "Please select a task to delete.");
            return;
        }

        Task task = selectedItem.getTask();

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to delete the task: " + task.getName() + "?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                tasks.remove(task);

                // Remove from any quests
                questManager.getAllQuests().values().forEach(quest -> quest.removeTask(task));

                refreshTaskList();
                refreshQuestList();
                notifyTasksUpdated();
            }
        });
    }

    private void markTaskComplete() {
        List<TaskItem> selectedItems = taskListView.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            showAlert("No Selection", "Please select task(s) to mark as complete.");
            return;
        }

        for (TaskItem item : selectedItems) {
            Task task = item.getTask();
            if (!task.isComplete()) {
                task.markAsCompleted();
            }
        }

        refreshTaskList();
        refreshQuestList();
        notifyTasksUpdated();
    }

    private void assignTasksToQuest() {
        List<TaskItem> selectedItems = taskListView.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            showAlert("No Selection", "Please select task(s) to assign to a quest.");
            return;
        }

        // Show quest selection dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Assign Tasks to Quest");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label titleLabel = new Label("Select a quest:");
        ComboBox<String> questCombo = new ComboBox<>();
        updateQuestComboBox(questCombo);

        Button assignButton = new Button("Assign");
        Button cancelButton = new Button("Cancel");

        assignButton.setOnAction(e -> {
            String selectedQuest = questCombo.getValue();
            if (selectedQuest != null && !selectedQuest.equals("No Quest")) {
                Optional<Quest> quest = questManager.getAllQuests().values().stream()
                        .filter(q -> q.getTitle().equals(selectedQuest))
                        .findFirst();

                if (quest.isPresent()) {
                    for (TaskItem item : selectedItems) {
                        questManager.addTaskToQuest(quest.get().getId(), item.getTask());
                    }
                    refreshQuestList();
                    notifyTasksUpdated();
                }
            }
            dialog.close();
        });

        cancelButton.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(assignButton, cancelButton);

        layout.getChildren().addAll(titleLabel, questCombo, buttonBox);

        Scene scene = new Scene(layout, 300, 150);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void editSelectedQuest() {
        QuestItem selectedItem = questListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("No Selection", "Please select a quest to edit.");
            return;
        }

        Quest quest = selectedItem.getQuest();

        // Create edit dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Quest");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        TextField titleField = new TextField(quest.getTitle());
        TextArea descArea = new TextArea(quest.getDescription());
        TextField xpField = new TextField(String.valueOf(quest.getBaseXpReward()));

        Button saveButton = new Button("Save Changes");
        Button cancelButton = new Button("Cancel");

        saveButton.setOnAction(e -> {
            quest.setTitle(titleField.getText().trim());
            quest.setDescription(descArea.getText().trim());

            try {
                quest.setBaseXpReward(Integer.parseInt(xpField.getText().trim()));
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid XP value!");
                return;
            }

            refreshQuestList();
            notifyTasksUpdated();
            dialog.close();
        });

        cancelButton.setOnAction(e -> dialog.close());

        grid.add(new Label("Quest Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Base XP:"), 0, 2);
        grid.add(xpField, 1, 2);

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        grid.add(buttonBox, 1, 3);

        Scene scene = new Scene(grid, 400, 250);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void deleteSelectedQuest() {
        QuestItem selectedItem = questListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("No Selection", "Please select a quest to delete.");
            return;
        }

        Quest quest = selectedItem.getQuest();

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to delete the quest: " + quest.getTitle()
                + "?\nTasks will not be deleted, only removed from this quest.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                questManager.removeQuest(quest.getId());
                refreshQuestList();
                notifyTasksUpdated();
            }
        });
    }

    private void viewQuestDetails() {
        QuestItem selectedItem = questListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("No Selection", "Please select a quest to view details.");
            return;
        }

        Quest quest = selectedItem.getQuest();

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Quest Details: " + quest.getTitle());

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        // Quest info
        Label titleLabel = new Label("Title: " + quest.getTitle());
        titleLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 18));

        Label descLabel = new Label("Description: " + quest.getDescription());
        descLabel.setWrapText(true);

        Label progressLabel = new Label("Progress: " + quest.getProgressPercentage() + "%");
        ProgressBar progressBar = new ProgressBar(quest.getProgressPercentage() / 100.0);
        progressBar.setPrefWidth(300);

        Label xpLabel = new Label("Total XP Reward: " + quest.calculateXpReward());
        Label statusLabel = new Label("Status: " + (quest.isCompleted() ? "COMPLETED" : "IN PROGRESS"));

        // Task list
        Label tasksLabel = new Label("Tasks in this Quest:");
        tasksLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 14));

        ListView<String> tasksList = new ListView<>();
        List<String> taskNames = quest.getTasks().stream()
                .map(task -> (task.isComplete() ? "✓ " : "○ ") + task.getName())
                .collect(Collectors.toList());
        tasksList.setItems(FXCollections.observableArrayList(taskNames));
        tasksList.setPrefHeight(150);

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> dialog.close());

        layout.getChildren().addAll(titleLabel, descLabel, progressLabel, progressBar,
                xpLabel, statusLabel, tasksLabel, tasksList, closeButton);

        Scene scene = new Scene(layout, 400, 500);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void refreshTaskList() {
        taskItems.clear();
        for (Task task : tasks) {
            taskItems.add(new TaskItem(task));
        }
    }

    private void refreshQuestList() {
        questItems.clear();
        for (Quest quest : questManager.getAllQuests().values()) {
            questItems.add(new QuestItem(quest));
        }
    }

    private void applyTaskFilter(String filter) {
        taskItems.clear();
        List<Task> filteredTasks;

        switch (filter) {
            case "Incomplete Tasks":
                filteredTasks = tasks.stream().filter(task -> !task.isComplete()).collect(Collectors.toList());
                break;
            case "Complete Tasks":
                filteredTasks = tasks.stream().filter(Task::isComplete).collect(Collectors.toList());
                break;
            case "Overdue Tasks":
                filteredTasks = tasks.stream()
                        .filter(task -> task.hasDueDateTime() &&
                                task.getDueDateTime().isBefore(LocalDateTime.now()) &&
                                !task.isComplete())
                        .collect(Collectors.toList());
                break;
            default: // "All Tasks"
                filteredTasks = new ArrayList<>(tasks);
                break;
        }

        for (Task task : filteredTasks) {
            taskItems.add(new TaskItem(task));
        }
    }

    private void updateQuestComboBox(ComboBox<String> questCombo) {
        List<String> questTitles = new ArrayList<>();
        questTitles.add("No Quest");
        questManager.getAllQuests().values().forEach(quest -> questTitles.add(quest.getTitle()));
        questCombo.setItems(FXCollections.observableArrayList(questTitles));
        questCombo.setValue("No Quest");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void notifyTasksUpdated() {
        if (onTasksUpdated != null) {
            onTasksUpdated.run();
        }
    }

    public void show() {
        stage.show();
    }

    // Helper classes for list items
    private static class TaskItem {
        private final Task task;

        public TaskItem(Task task) {
            this.task = task;
        }

        public Task getTask() {
            return task;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(task.isComplete() ? "✓ " : "○ ");
            sb.append(task.getName());
            sb.append(" [").append(task.getPriority().name()).append("]");

            if (task.hasDueDateTime()) {
                sb.append(" - Due: ").append(task.getDueDateTime().toLocalDate());
                if (task.getDueDateTime().isBefore(LocalDateTime.now()) && !task.isComplete()) {
                    sb.append(" (OVERDUE)");
                }
            }

            return sb.toString();
        }
    }

    private static class QuestItem {
        private final Quest quest;

        public QuestItem(Quest quest) {
            this.quest = quest;
        }

        public Quest getQuest() {
            return quest;
        }

        @Override
        public String toString() {
            return String.format("%s [%d%% complete] - %d XP",
                    quest.getTitle(),
                    quest.getProgressPercentage(),
                    quest.calculateXpReward());
        }
    }

    public javafx.scene.Parent getRoot() {
        return root;
    }
}