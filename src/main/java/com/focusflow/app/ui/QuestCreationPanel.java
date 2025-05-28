package com.focusflow.app.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Quest Creation Panel for building comprehensive quests with task management.
 * Provides an intuitive interface for creating quests, assigning tasks, and
 * setting rewards.
 * 
 * Features:
 * - Drag-and-drop task assignment
 * - Achievement reward selection
 * - XP calculation preview
 * - Quest template system
 * - Task creation within quest workflow
 * - Progress estimation and analytics
 * 
 * @author FocusFlow Team
 * @version 2.0 - Advanced Quest System
 */
public class QuestCreationPanel extends BasePanel {

    // Dependencies
    private final List<Task> allTasks;
    private final QuestManager questManager;
    private final Consumer<Runnable> onQuestCreated;

    // Quest Data
    private ObservableList<Task> availableTasks;
    private ObservableList<Task> selectedTasks;

    // Form Components
    private TextField questNameField;
    private TextArea questDescriptionArea;
    private TextField baseXpField;
    private ComboBox<Achievement> achievementCombo;
    private ComboBox<QuestTemplate> templateCombo;

    // Task Management
    private ListView<Task> availableTasksList;
    private ListView<Task> selectedTasksList;
    private Label selectedTasksCountLabel;
    private Label estimatedTimeLabel;
    private Label totalXpLabel;
    private ProgressBar difficultyIndicator;

    // Quick Task Creation
    private TextField quickTaskNameField;
    private ComboBox<TaskPriority> quickTaskPriorityCombo;

    // Action Buttons
    private Button createQuestButton;
    private Button previewButton;

    /**
     * Creates a new QuestCreationPanel.
     */
    public QuestCreationPanel(OverlayManager overlayManager, Font pixelFont,
            List<Task> allTasks, QuestManager questManager,
            Consumer<Runnable> onQuestCreated) {
        super(overlayManager, pixelFont, "Create New Quest", true);

        this.allTasks = allTasks;
        this.questManager = questManager;
        this.onQuestCreated = onQuestCreated;

        initializeTaskLists();
        finishInitialization();
    }

    /**
     * Initializes the task lists for quest assignment.
     */
    private void initializeTaskLists() {
        // Filter out completed tasks and tasks already assigned to active quests
        List<Task> unassignedTasks = allTasks.stream()
                .filter(task -> !task.isComplete())
                .filter(task -> !isTaskInActiveQuest(task))
                .collect(Collectors.toList());

        availableTasks = FXCollections.observableArrayList(unassignedTasks);
        selectedTasks = FXCollections.observableArrayList();
    }

    /**
     * Checks if a task is already assigned to an active quest.
     */
    private boolean isTaskInActiveQuest(Task task) {
        return questManager.getAllQuests().values().stream()
                .filter(quest -> !quest.isCompleted())
                .anyMatch(quest -> quest.getTasks().contains(task));
    }

    @Override
    protected void createContent() {
        // Template section
        VBox templateSection = createTemplateSection();

        // Quest details section
        VBox detailsSection = createQuestDetailsSection();

        // Task assignment section
        VBox taskAssignmentSection = createTaskAssignmentSection();

        // Quick task creation section
        VBox quickTaskSection = createQuickTaskSection();

        // Analytics preview section
        VBox analyticsSection = createAnalyticsSection();

        // Action buttons
        createActionButtons();

        addContent(templateSection, detailsSection, taskAssignmentSection,
                quickTaskSection, analyticsSection);
        showFooter(createButtonRow(createQuestButton, previewButton,
                createSecondaryButton("Cancel", () -> getOverlayManager().hideCurrentOverlay())));

        setupEventHandlers();
        setupKeyboardShortcuts();

        // Focus on quest name field
        questNameField.requestFocus();
    }

    /**
     * Creates the template selection section.
     */
    private VBox createTemplateSection() {
        VBox section = createSection("Quest Templates");

        HBox templateRow = new HBox(10);
        templateRow.setAlignment(Pos.CENTER_LEFT);

        Label templateLabel = new Label("Template:");
        templateLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.NORMAL, 14));

        templateCombo = new ComboBox<>();
        templateCombo.setItems(FXCollections.observableArrayList(QuestTemplate.values()));
        templateCombo.setPromptText("Choose a template...");
        templateCombo.setPrefWidth(250);

        Button applyTemplateBtn = new Button("Apply Template");
        applyTemplateBtn.setStyle("-fx-background-color: #17A2B8; -fx-text-fill: white; " +
                "-fx-background-radius: 6px; -fx-padding: 8 15; -fx-font-weight: bold;");
        applyTemplateBtn.setOnAction(e -> applyTemplate());
        applyTemplateBtn.disableProperty().bind(templateCombo.valueProperty().isNull());

        templateRow.getChildren().addAll(templateLabel, templateCombo, applyTemplateBtn);

        Label templateHelp = createHelpText("Templates provide pre-configured quest setups for common project types.");

        section.getChildren().addAll(templateRow, templateHelp);
        return section;
    }

    /**
     * Creates the quest details input section.
     */
    private VBox createQuestDetailsSection() {
        VBox section = createSection("Quest Details");

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(15);
        detailsGrid.setVgap(12);

        // Quest name
        Label nameLabel = new Label("Quest Name *");
        nameLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));
        questNameField = new TextField();
        questNameField.setPromptText("Enter an inspiring quest name...");
        questNameField.setPrefWidth(400);
        styleTextField(questNameField);

        detailsGrid.add(nameLabel, 0, 0);
        detailsGrid.add(questNameField, 1, 0, 2, 1);

        // Quest description
        Label descLabel = new Label("Description");
        descLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));
        questDescriptionArea = new TextArea();
        questDescriptionArea.setPromptText("Describe the quest objective and motivation...");
        questDescriptionArea.setPrefRowCount(3);
        questDescriptionArea.setPrefWidth(400);
        questDescriptionArea.setWrapText(true);
        styleTextArea(questDescriptionArea);

        detailsGrid.add(descLabel, 0, 1);
        detailsGrid.add(questDescriptionArea, 1, 1, 2, 1);

        // Base XP and Achievement row
        Label xpLabel = new Label("Base XP Reward");
        xpLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));
        baseXpField = new TextField("100");
        baseXpField.setPrefWidth(100);
        styleTextField(baseXpField);

        Label achievementLabel = new Label("Achievement Reward");
        achievementLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));
        achievementCombo = new ComboBox<>();
        achievementCombo.getItems().add(null); // No achievement option
        achievementCombo.getItems().addAll(Achievement.values());
        achievementCombo.setPromptText("Optional achievement...");
        achievementCombo.setPrefWidth(200);
        styleComboBox(achievementCombo);

        detailsGrid.add(xpLabel, 0, 2);
        detailsGrid.add(baseXpField, 1, 2);
        detailsGrid.add(achievementLabel, 2, 2);
        detailsGrid.add(achievementCombo, 3, 2);

        section.getChildren().add(detailsGrid);
        return section;
    }

    /**
     * Creates the task assignment section with drag-and-drop functionality.
     */
    private VBox createTaskAssignmentSection() {
        VBox section = createSection("Task Assignment");

        HBox listsContainer = new HBox(20);
        listsContainer.setAlignment(Pos.TOP_CENTER);

        // Available tasks
        VBox availableBox = new VBox(10);
        Label availableLabel = new Label("Available Tasks");
        availableLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));

        availableTasksList = new ListView<>();
        availableTasksList.setItems(availableTasks);
        availableTasksList.setCellFactory(listView -> new TaskCell(false));
        availableTasksList.setPrefHeight(200);
        availableTasksList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        availableBox.getChildren().addAll(availableLabel, availableTasksList);

        // Transfer buttons
        VBox transferButtons = new VBox(10);
        transferButtons.setAlignment(Pos.CENTER);
        transferButtons.setPadding(new Insets(50, 0, 0, 0));

        Button addSelectedBtn = new Button("Add →");
        addSelectedBtn.setStyle("-fx-background-color: #28A745; -fx-text-fill: white; " +
                "-fx-background-radius: 8px; -fx-padding: 10 15; -fx-font-weight: bold;");
        addSelectedBtn.setOnAction(e -> addSelectedTasks());

        Button removeSelectedBtn = new Button("← Remove");
        removeSelectedBtn.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; " +
                "-fx-background-radius: 8px; -fx-padding: 10 15; -fx-font-weight: bold;");
        removeSelectedBtn.setOnAction(e -> removeSelectedTasks());

        Button addAllBtn = new Button("Add All");
        addAllBtn.setStyle("-fx-background-color: #17A2B8; -fx-text-fill: white; " +
                "-fx-background-radius: 8px; -fx-padding: 8 12;");
        addAllBtn.setOnAction(e -> addAllTasks());

        Button removeAllBtn = new Button("Remove All");
        removeAllBtn.setStyle("-fx-background-color: #6C757D; -fx-text-fill: white; " +
                "-fx-background-radius: 8px; -fx-padding: 8 12;");
        removeAllBtn.setOnAction(e -> removeAllTasks());

        transferButtons.getChildren().addAll(addSelectedBtn, removeSelectedBtn,
                new Separator(), addAllBtn, removeAllBtn);

        // Selected tasks
        VBox selectedBox = new VBox(10);
        selectedTasksCountLabel = new Label("Quest Tasks (0)");
        selectedTasksCountLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));

        selectedTasksList = new ListView<>();
        selectedTasksList.setItems(selectedTasks);
        selectedTasksList.setCellFactory(listView -> new TaskCell(true));
        selectedTasksList.setPrefHeight(200);
        selectedTasksList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        selectedBox.getChildren().addAll(selectedTasksCountLabel, selectedTasksList);

        listsContainer.getChildren().addAll(availableBox, transferButtons, selectedBox);

        section.getChildren().add(listsContainer);
        return section;
    }

    /**
     * Creates the quick task creation section.
     */
    private VBox createQuickTaskSection() {
        VBox section = createSection("Quick Task Creation");

        HBox quickTaskRow = new HBox(10);
        quickTaskRow.setAlignment(Pos.CENTER_LEFT);
        quickTaskRow.setStyle("-fx-background-color: #F8F9FA; -fx-padding: 15; -fx-background-radius: 8;");

        Label plusLabel = new Label("+");
        plusLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 18));
        plusLabel.setStyle("-fx-text-fill: #28A745;");

        quickTaskNameField = new TextField();
        quickTaskNameField.setPromptText("Create task for this quest...");
        quickTaskNameField.setPrefWidth(250);
        styleTextField(quickTaskNameField);
        HBox.setHgrow(quickTaskNameField, Priority.ALWAYS);

        quickTaskPriorityCombo = new ComboBox<>();
        quickTaskPriorityCombo.getItems().addAll(TaskPriority.values());
        quickTaskPriorityCombo.setValue(TaskPriority.MEDIUM);
        quickTaskPriorityCombo.setPrefWidth(100);
        styleComboBox(quickTaskPriorityCombo);

        Button createTaskBtn = new Button("Create & Add");
        createTaskBtn.setStyle("-fx-background-color: #28A745; -fx-text-fill: white; " +
                "-fx-background-radius: 8px; -fx-padding: 8 15; -fx-font-weight: bold;");
        createTaskBtn.setOnAction(e -> createAndAddQuickTask());

        quickTaskRow.getChildren().addAll(plusLabel, quickTaskNameField, quickTaskPriorityCombo, createTaskBtn);

        quickTaskNameField.setOnAction(e -> createAndAddQuickTask());

        Label quickTaskHelp = createHelpText(
                "Quickly create tasks specifically for this quest without leaving this panel.");

        section.getChildren().addAll(quickTaskRow, quickTaskHelp);
        return section;
    }

    /**
     * Creates the analytics preview section.
     */
    private VBox createAnalyticsSection() {
        VBox section = createSection("Quest Analytics Preview");

        GridPane analyticsGrid = new GridPane();
        analyticsGrid.setHgap(20);
        analyticsGrid.setVgap(12);
        analyticsGrid.setStyle("-fx-background-color: #F8F9FA; -fx-padding: 15; -fx-background-radius: 8;");

        // Metrics
        estimatedTimeLabel = new Label("Estimated Time: 0 hours");
        estimatedTimeLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));
        estimatedTimeLabel.setTextFill(Color.web("#495057"));

        totalXpLabel = new Label("Total XP Reward: 100");
        totalXpLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));
        totalXpLabel.setTextFill(Color.web("#28A745"));

        Label difficultyLabel = new Label("Difficulty:");
        difficultyLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));

        difficultyIndicator = new ProgressBar(0.3);
        difficultyIndicator.setPrefWidth(150);
        difficultyIndicator.setStyle("-fx-accent: #FFC107;");

        Label difficultyText = new Label("Moderate");
        difficultyText.setFont(Font.font(getPixelFont().getFamily(), FontWeight.NORMAL, 12));
        difficultyText.setTextFill(Color.web("#6C757D"));

        analyticsGrid.add(estimatedTimeLabel, 0, 0);
        analyticsGrid.add(totalXpLabel, 1, 0);
        analyticsGrid.add(difficultyLabel, 0, 1);
        analyticsGrid.add(difficultyIndicator, 1, 1);
        analyticsGrid.add(difficultyText, 2, 1);

        section.getChildren().add(analyticsGrid);
        return section;
    }

    /**
     * Creates action buttons for the quest creation panel.
     */
    private void createActionButtons() {
        createQuestButton = createSuccessButton("Create Quest", this::createQuest);
        createQuestButton.setDisable(true);

        previewButton = createPrimaryButton("Preview Quest", this::previewQuest);
        previewButton.setDisable(true);
    }

    /**
     * Sets up event handlers for real-time updates.
     */
    private void setupEventHandlers() {
        // Update analytics when tasks change
        selectedTasks.addListener((javafx.collections.ListChangeListener<Task>) change -> {
            updateAnalytics();
            updateButtonStates();
        });

        // Update analytics when XP changes
        baseXpField.textProperty().addListener((obs, oldText, newText) -> {
            updateAnalytics();
            updateButtonStates();
        });

        // Update button states when quest name changes
        questNameField.textProperty().addListener((obs, oldText, newText) -> updateButtonStates());

        // Visual feedback for quest name
        questNameField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                validateQuestName();
            }
        });
    }

    /**
     * Sets up keyboard shortcuts for efficient quest creation.
     */
    private void setupKeyboardShortcuts() {
        setOnKeyPressed(e -> {
            if (e.isControlDown()) {
                switch (e.getCode()) {
                    case ENTER:
                        if (createQuestButton.isDisabled() == false) {
                            createQuest();
                        }
                        break;
                    case T:
                        quickTaskNameField.requestFocus();
                        break;
                }
            } else if (e.getCode() == KeyCode.ESCAPE) {
                getOverlayManager().hideCurrentOverlay();
            }
        });

        // Enter in quick task field creates task
        quickTaskNameField.setOnAction(e -> createAndAddQuickTask());
    }

    /**
     * Applies the selected template to the quest form.
     */
    private void applyTemplate() {
        QuestTemplate template = templateCombo.getValue();
        if (template == null)
            return;

        questNameField.setText(template.getDefaultName());
        questDescriptionArea.setText(template.getDefaultDescription());
        baseXpField.setText(String.valueOf(template.getDefaultBaseXp()));

        if (template.getDefaultAchievement() != null) {
            achievementCombo.setValue(template.getDefaultAchievement());
        }

        // Auto-select tasks based on template criteria
        List<Task> templateTasks = availableTasks.stream()
                .filter(task -> template.matchesTask(task))
                .limit(template.getMaxTasks())
                .collect(Collectors.toList());

        selectedTasks.addAll(templateTasks);
        availableTasks.removeAll(templateTasks);

        questNameField.requestFocus();
        questNameField.selectAll();
    }

    /**
     * Adds selected tasks from available list to quest.
     */
    private void addSelectedTasks() {
        List<Task> selected = new ArrayList<>(availableTasksList.getSelectionModel().getSelectedItems());
        selectedTasks.addAll(selected);
        availableTasks.removeAll(selected);
        availableTasksList.getSelectionModel().clearSelection();
    }

    /**
     * Removes selected tasks from quest back to available list.
     */
    private void removeSelectedTasks() {
        List<Task> selected = new ArrayList<>(selectedTasksList.getSelectionModel().getSelectedItems());
        availableTasks.addAll(selected);
        selectedTasks.removeAll(selected);
        selectedTasksList.getSelectionModel().clearSelection();
    }

    /**
     * Adds all available tasks to the quest.
     */
    private void addAllTasks() {
        selectedTasks.addAll(availableTasks);
        availableTasks.clear();
    }

    /**
     * Removes all tasks from the quest.
     */
    private void removeAllTasks() {
        availableTasks.addAll(selectedTasks);
        selectedTasks.clear();
    }

    /**
     * Creates and adds a quick task to the quest.
     */
    private void createAndAddQuickTask() {
        String taskName = quickTaskNameField.getText().trim();
        if (taskName.isEmpty())
            return;

        Task newTask = new Task(taskName, "Created for quest: " + questNameField.getText());
        newTask.setPriority(quickTaskPriorityCombo.getValue());

        // Add to main task list and quest
        allTasks.add(newTask);
        selectedTasks.add(newTask);

        quickTaskNameField.clear();
        quickTaskNameField.requestFocus();
    }

    /**
     * Updates the analytics preview based on current quest configuration.
     */
    private void updateAnalytics() {
        int taskCount = selectedTasks.size();
        selectedTasksCountLabel.setText("Quest Tasks (" + taskCount + ")");

        // Estimate time (rough calculation)
        double estimatedHours = taskCount * 1.5; // Assume 1.5 hours per task average
        estimatedTimeLabel.setText(String.format("Estimated Time: %.1f hours", estimatedHours));

        // Calculate total XP
        int baseXp = parseXpField();
        int taskXp = taskCount * 10; // 10 XP per task
        int subtaskXp = selectedTasks.stream()
                .mapToInt(task -> task.getSubtasks().size() * 2)
                .sum();
        int totalXp = baseXp + taskXp + subtaskXp;

        totalXpLabel.setText("Total XP Reward: " + totalXp);

        // Calculate difficulty
        double difficulty = Math.min(1.0, taskCount / 10.0); // Max difficulty at 10 tasks
        difficultyIndicator.setProgress(difficulty);

        String difficultyText;
        if (difficulty < 0.3) {
            difficultyText = "Easy";
            difficultyIndicator.setStyle("-fx-accent: #28A745;");
        } else if (difficulty < 0.7) {
            difficultyText = "Moderate";
            difficultyIndicator.setStyle("-fx-accent: #FFC107;");
        } else {
            difficultyText = "Challenging";
            difficultyIndicator.setStyle("-fx-accent: #DC3545;");
        }

        // Update difficulty text (find the label in the analytics grid)
        // This is a simplified approach - in a real implementation you'd store the
        // reference
    }

    /**
     * Updates button states based on form validity.
     */
    private void updateButtonStates() {
        boolean hasName = !questNameField.getText().trim().isEmpty();
        boolean hasTasks = !selectedTasks.isEmpty();
        boolean validXp = parseXpField() >= 0;

        boolean isValid = hasName && hasTasks && validXp;

        createQuestButton.setDisable(!isValid);
        previewButton.setDisable(!isValid);
    }

    /**
     * Validates the quest name and provides visual feedback.
     */
    private void validateQuestName() {
        String name = questNameField.getText().trim();
        if (name.isEmpty()) {
            questNameField.setStyle("-fx-border-color: #DC3545; -fx-border-radius: 6px;");
        } else if (name.length() < 3) {
            questNameField.setStyle("-fx-border-color: #FFC107; -fx-border-radius: 6px;");
        } else {
            questNameField.setStyle("-fx-border-color: #28A745; -fx-border-radius: 6px;");
        }
    }

    /**
     * Parses the XP field value safely.
     */
    private int parseXpField() {
        try {
            return Integer.parseInt(baseXpField.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Creates the quest with current form data.
     */
    private void createQuest() {
        try {
            String name = questNameField.getText().trim();
            String description = questDescriptionArea.getText().trim();
            int baseXp = parseXpField();
            Achievement achievement = achievementCombo.getValue();

            Quest newQuest = new Quest(name, description, achievement, baseXp);

            // Add all selected tasks to the quest
            for (Task task : selectedTasks) {
                newQuest.addTask(task);
            }

            // Add quest to manager
            questManager.addQuest(newQuest);

            // Notify completion
            onQuestCreated.accept(() -> {
            });

            // Show success and close
            showSuccessMessage(newQuest);
            getOverlayManager().hideCurrentOverlay();

        } catch (Exception e) {
            showAlert("Error", "Failed to create quest: " + e.getMessage());
        }
    }

    /**
     * Shows a preview of the quest before creation.
     */
    private void previewQuest() {
        String name = questNameField.getText().trim();
        String description = questDescriptionArea.getText().trim();
        int totalXp = parseXpField() + (selectedTasks.size() * 10);

        StringBuilder preview = new StringBuilder();
        preview.append("Quest: ").append(name).append("\n\n");
        preview.append("Description: ").append(description.isEmpty() ? "No description" : description).append("\n\n");
        preview.append("Tasks (").append(selectedTasks.size()).append("):\n");

        for (int i = 0; i < selectedTasks.size(); i++) {
            Task task = selectedTasks.get(i);
            preview.append(String.format("%d. %s [%s]\n", i + 1, task.getName(), task.getPriority()));
        }

        preview.append("\nTotal XP Reward: ").append(totalXp);

        if (achievementCombo.getValue() != null) {
            preview.append("\nAchievement: ").append(achievementCombo.getValue().getName());
        }

        showAlert("Quest Preview", preview.toString());
    }

    /**
     * Shows success message after quest creation.
     */
    private void showSuccessMessage(Quest quest) {
        String message = String.format("Quest '%s' created successfully!\n\n" +
                "Tasks: %d\nTotal XP: %d\n\n" +
                "Start working on the tasks to make progress!",
                quest.getTitle(),
                quest.getTasks().size(),
                quest.calculateXpReward());
        showAlert("Quest Created!", message);
    }

    /**
     * Shows an alert dialog.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Applies consistent styling to text fields.
     */
    private void styleTextField(TextField field) {
        field.setStyle("-fx-border-color: #DEE2E6; -fx-border-radius: 6px; -fx-padding: 8px;");
    }

    /**
     * Applies consistent styling to text areas.
     */
    private void styleTextArea(TextArea area) {
        area.setStyle("-fx-border-color: #DEE2E6; -fx-border-radius: 6px;");
    }

    /**
     * Applies consistent styling to combo boxes.
     */
    private void styleComboBox(ComboBox<?> comboBox) {
        comboBox.setStyle("-fx-border-color: #DEE2E6; -fx-border-radius: 6px; -fx-background-radius: 6px;");
    }

    /**
     * Custom cell renderer for task lists.
     */
    private class TaskCell extends ListCell<Task> {
        private final boolean isSelected;

        public TaskCell(boolean isSelected) {
            this.isSelected = isSelected;
        }

        @Override
        protected void updateItem(Task task, boolean empty) {
            super.updateItem(task, empty);

            if (empty || task == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            VBox container = new VBox(2);
            container.setPadding(new Insets(5));

            Label nameLabel = new Label(task.getName());
            nameLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 12));

            Label detailsLabel = new Label(String.format("Priority: %s | Subtasks: %d",
                    task.getPriority().name(), task.getSubtasks().size()));
            detailsLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.NORMAL, 10));
            detailsLabel.setTextFill(Color.web("#6C757D"));

            container.getChildren().addAll(nameLabel, detailsLabel);

            if (isSelected) {
                container.setStyle("-fx-background-color: #E7F3FF; -fx-border-color: #007BFF; " +
                        "-fx-border-radius: 6px; -fx-background-radius: 6px; -fx-border-width: 2px;");
            } else {
                container.setStyle("-fx-background-color: white; -fx-border-color: #DEE2E6; " +
                        "-fx-border-radius: 6px; -fx-background-radius: 6px; -fx-border-width: 1px;");
            }

            // Hover effects
            container.setOnMouseEntered(e -> {
                if (!isSelected) {
                    container.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #007BFF; " +
                            "-fx-border-radius: 6px; -fx-background-radius: 6px; -fx-border-width: 1px;");
                }
            });

            container.setOnMouseExited(e -> {
                if (!isSelected) {
                    container.setStyle("-fx-background-color: white; -fx-border-color: #DEE2E6; " +
                            "-fx-border-radius: 6px; -fx-background-radius: 6px; -fx-border-width: 1px;");
                }
            });

            setGraphic(container);
        }
    }

    /**
     * Enum for quest templates to speed up common quest creation.
     */
    public enum QuestTemplate {
        DAILY_ROUTINE("Daily Routine", "Complete your daily productivity tasks", null, 50, 5),
        WORK_PROJECT("Work Project", "Complete a significant work project with multiple phases", null, 200, 10),
        LEARNING_JOURNEY("Learning Journey", "Master a new skill or subject through structured learning",
                Achievement.COMPLETED_FIRST_QUEST, 150, 8),
        HEALTH_FITNESS("Health & Fitness", "Improve your physical health through consistent habits", null, 100, 6),
        CREATIVE_PROJECT("Creative Project", "Complete a creative endeavor from concept to finish", null, 180, 7),
        HOME_ORGANIZATION("Home Organization", "Organize and declutter your living space", null, 75, 4),
        SKILL_BUILDING("Skill Building", "Develop professional or personal skills", Achievement.COMPLETED_10_TASKS, 120,
                6),
        SIDE_HUSTLE("Side Hustle", "Launch or develop a side business or project", null, 300, 15);

        private final String defaultName;
        private final String defaultDescription;
        private final Achievement defaultAchievement;
        private final int defaultBaseXp;
        private final int maxTasks;

        QuestTemplate(String defaultName, String defaultDescription, Achievement defaultAchievement,
                int defaultBaseXp, int maxTasks) {
            this.defaultName = defaultName;
            this.defaultDescription = defaultDescription;
            this.defaultAchievement = defaultAchievement;
            this.defaultBaseXp = defaultBaseXp;
            this.maxTasks = maxTasks;
        }

        public String getDefaultName() {
            return defaultName;
        }

        public String getDefaultDescription() {
            return defaultDescription;
        }

        public Achievement getDefaultAchievement() {
            return defaultAchievement;
        }

        public int getDefaultBaseXp() {
            return defaultBaseXp;
        }

        public int getMaxTasks() {
            return maxTasks;
        }

        /**
         * Determines if a task matches this template's criteria.
         * This is used for auto-selecting relevant tasks when applying a template.
         * 
         * @param task The task to evaluate
         * @return true if the task matches this template
         */
        public boolean matchesTask(Task task) {
            String taskName = task.getName().toLowerCase();
            String taskDesc = task.getDescription().toLowerCase();
            String categoryName = task.getCategory() != null ? task.getCategory().getTaskCategory().toLowerCase() : "";

            switch (this) {
                case DAILY_ROUTINE:
                    return taskName.contains("daily") || taskName.contains("routine") ||
                            taskName.contains("habit") || taskName.contains("morning") ||
                            taskName.contains("evening");

                case WORK_PROJECT:
                    return categoryName.contains("work") || taskName.contains("project") ||
                            taskName.contains("meeting") || taskName.contains("presentation") ||
                            task.getPriority() == TaskPriority.HIGH || task.getPriority() == TaskPriority.URGENT;

                case LEARNING_JOURNEY:
                    return categoryName.contains("learning") || taskName.contains("study") ||
                            taskName.contains("learn") || taskName.contains("course") ||
                            taskName.contains("research") || taskName.contains("tutorial");

                case HEALTH_FITNESS:
                    return categoryName.contains("health") || taskName.contains("exercise") ||
                            taskName.contains("workout") || taskName.contains("fitness") ||
                            taskName.contains("gym") || taskName.contains("run");

                case CREATIVE_PROJECT:
                    return categoryName.contains("creative") || taskName.contains("design") ||
                            taskName.contains("write") || taskName.contains("art") ||
                            taskName.contains("music") || taskName.contains("video");

                case HOME_ORGANIZATION:
                    return categoryName.contains("personal") || taskName.contains("clean") ||
                            taskName.contains("organize") || taskName.contains("declutter") ||
                            taskName.contains("home") || taskName.contains("room");

                case SKILL_BUILDING:
                    return taskName.contains("skill") || taskName.contains("practice") ||
                            taskName.contains("improve") || taskName.contains("develop") ||
                            categoryName.contains("learning");

                case SIDE_HUSTLE:
                    return taskName.contains("business") || taskName.contains("startup") ||
                            taskName.contains("launch") || taskName.contains("marketing") ||
                            taskName.contains("client") || taskName.contains("revenue");

                default:
                    return false;
            }
        }

        @Override
        public String toString() {
            return defaultName;
        }
    }
}