package com.focusflow.app.ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import com.focusflow.core.gameify.QuestManager;
import com.focusflow.core.task.Task;
import com.focusflow.core.task.TaskCategory;
import com.focusflow.core.task.TaskPriority;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Task Creation Panel with comprehensive task setup capabilities.
 * Provides a clean, efficient interface for creating new tasks with all
 * necessary properties and quest assignment.
 * 
 * Features:
 * - Quick task creation with keyboard shortcuts
 * - Smart form validation with visual feedback
 * - Quest assignment with live quest list
 * - Priority and category management
 * - Due date setting with date picker
 * - Template support for common task types
 * - Auto-focus for efficient workflow
 * 
 * @author FocusFlow Team
 * @version 2.0 - Enhanced UX and Validation
 */
public class TaskCreationPanel extends BasePanel {

    // Dependencies
    private final List<Task> tasks;
    private final QuestManager questManager;
    private final Consumer<Runnable> onTaskCreated;

    // Form components
    private TextField nameField;
    private TextArea descriptionArea;
    private ComboBox<TaskPriority> priorityCombo;
    private ComboBox<String> categoryCombo;
    private DatePicker dueDatePicker;
    private ComboBox<String> questCombo;
    private CheckBox startImmediatelyCheckBox;

    // Template system
    private ComboBox<TaskTemplate> templateCombo;
    private Button applyTemplateBtn;

    // Action buttons
    private Button saveButton;
    private Button saveAndStartButton;
    private Button cancelButton;

    // Validation state
    private boolean isFormValid = false;

    /**
     * Creates a new TaskCreationPanel.
     * 
     * @param overlayManager The overlay manager for panel lifecycle
     * @param pixelFont      The font for consistent typography
     * @param tasks          The main task list to add tasks to
     * @param questManager   The quest manager for quest assignment
     * @param onTaskCreated  Callback when a task is successfully created
     */
    public TaskCreationPanel(OverlayManager overlayManager, Font pixelFont,
            List<Task> tasks, QuestManager questManager,
            Consumer<Runnable> onTaskCreated) {
        super(overlayManager, pixelFont, "Create New Task", true);
        this.tasks = tasks;
        this.questManager = questManager;
        this.onTaskCreated = onTaskCreated;
        finishInitialization();
    }

    @Override
    protected void createContent() {
        // Template section (optional quick start)
        VBox templateSection = createTemplateSection();

        // Main form
        GridPane formGrid = createMainForm();

        // Options section
        VBox optionsSection = createOptionsSection();

        // Action buttons
        createActionButtons();

        addContent(templateSection, formGrid, optionsSection);
        showFooter(createButtonRow(saveButton, saveAndStartButton, cancelButton));

        // Setup validation and keyboard shortcuts
        setupFormValidation();
        setupKeyboardShortcuts();

        // Auto-focus on name field
        nameField.requestFocus();
    }

    /**
     * Creates the template section for quick task creation.
     * 
     * @return VBox containing template controls
     */
    private VBox createTemplateSection() {
        VBox section = createSection("Quick Start Templates");

        HBox templateRow = new HBox(10);
        templateRow.setAlignment(Pos.CENTER_LEFT);

        Label templateLabel = new Label("Template:");
        templateLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.NORMAL, 14));

        templateCombo = new ComboBox<>();
        templateCombo.setItems(FXCollections.observableArrayList(TaskTemplate.values()));
        templateCombo.setPromptText("Choose a template...");
        templateCombo.setPrefWidth(200);

        applyTemplateBtn = new Button("Apply");
        applyTemplateBtn.setStyle("-fx-background-color: #17A2B8; -fx-text-fill: white; " +
                "-fx-background-radius: 6px; -fx-padding: 5 12;");
        applyTemplateBtn.setOnAction(e -> applyTemplate());
        applyTemplateBtn.setDisable(true);

        templateCombo.setOnAction(e -> applyTemplateBtn.setDisable(templateCombo.getValue() == null));

        templateRow.getChildren().addAll(templateLabel, templateCombo, applyTemplateBtn);

        Label templateHelp = createHelpText("Templates provide pre-filled forms for common task types.");

        section.getChildren().addAll(templateRow, templateHelp);
        return section;
    }

    /**
     * Creates the main form with task details.
     * 
     * @return GridPane containing form fields
     */
    private GridPane createMainForm() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 10, 0));

        int row = 0;

        // Task name (required)
        Label nameLabel = new Label("Task Name *");
        nameLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));
        nameField = new TextField();
        nameField.setPromptText("What needs to be done?");
        nameField.setPrefWidth(300);
        nameField.setStyle("-fx-border-color: #DEE2E6; -fx-border-radius: 6px; -fx-padding: 8px;");

        grid.add(nameLabel, 0, row);
        grid.add(nameField, 1, row++, 2, 1);

        // Description
        Label descLabel = new Label("Description");
        descLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Add details, notes, or instructions...");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setPrefWidth(300);
        descriptionArea.setWrapText(true);
        descriptionArea.setStyle("-fx-border-color: #DEE2E6; -fx-border-radius: 6px;");

        grid.add(descLabel, 0, row);
        grid.add(descriptionArea, 1, row++, 2, 1);

        // Priority and Category row
        Label priorityLabel = new Label("Priority");
        priorityLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));
        priorityCombo = new ComboBox<>();
        priorityCombo.setItems(FXCollections.observableArrayList(TaskPriority.values()));
        priorityCombo.setValue(TaskPriority.MEDIUM);
        priorityCombo.setPrefWidth(120);
        styleComboBox(priorityCombo);

        Label categoryLabel = new Label("Category");
        categoryLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));
        categoryCombo = new ComboBox<>();
        categoryCombo.setEditable(true);
        categoryCombo.setPromptText("Enter or select...");
        categoryCombo.setPrefWidth(140);
        styleComboBox(categoryCombo);
        updateCategoryOptions();

        grid.add(priorityLabel, 0, row);
        grid.add(priorityCombo, 1, row);
        grid.add(categoryLabel, 2, row);
        grid.add(categoryCombo, 3, row++);

        // Due date and Quest row
        Label dueDateLabel = new Label("Due Date");
        dueDateLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));
        dueDatePicker = new DatePicker();
        dueDatePicker.setPromptText("Optional");
        dueDatePicker.setPrefWidth(140);
        dueDatePicker.setStyle("-fx-border-color: #DEE2E6; -fx-border-radius: 6px;");

        Label questLabel = new Label("Quest");
        questLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));
        questCombo = new ComboBox<>();
        questCombo.setPromptText("Select quest...");
        questCombo.setPrefWidth(180);
        styleComboBox(questCombo);
        updateQuestOptions();

        grid.add(dueDateLabel, 0, row);
        grid.add(dueDatePicker, 1, row);
        grid.add(questLabel, 2, row);
        grid.add(questCombo, 3, row++);

        return grid;
    }

    /**
     * Creates the options section with additional settings.
     * 
     * @return VBox containing option controls
     */
    private VBox createOptionsSection() {
        VBox section = createSection("Options");

        startImmediatelyCheckBox = new CheckBox("Start working on this task immediately after creation");
        startImmediatelyCheckBox.setFont(Font.font(getPixelFont().getFamily(), FontWeight.NORMAL, 13));
        startImmediatelyCheckBox.setSelected(false);

        Label optionHelp = createHelpText(
                "When enabled, this task will be selected and the timer will start automatically.");

        section.getChildren().addAll(startImmediatelyCheckBox, optionHelp);
        return section;
    }

    /**
     * Creates the action buttons for the panel.
     */
    private void createActionButtons() {
        saveButton = createSuccessButton("Create Task", this::saveTask);
        saveButton.setDisable(true); // Disabled until form is valid

        saveAndStartButton = createPrimaryButton("Create & Start", this::saveAndStartTask);
        saveAndStartButton.setDisable(true);

        cancelButton = createSecondaryButton("Cancel", () -> getOverlayManager().hideCurrentOverlay());
    }

    /**
     * Sets up real-time form validation.
     */
    private void setupFormValidation() {
        // Validate on every text change
        nameField.textProperty().addListener((obs, oldText, newText) -> validateForm());

        // Visual feedback for required fields
        nameField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused && nameField.getText().trim().isEmpty()) {
                nameField.setStyle("-fx-border-color: #DC3545; -fx-border-radius: 6px; -fx-padding: 8px;");
            } else if (!nameField.getText().trim().isEmpty()) {
                nameField.setStyle("-fx-border-color: #28A745; -fx-border-radius: 6px; -fx-padding: 8px;");
            }
        });

        // Initial validation
        validateForm();
    }

    /**
     * Sets up keyboard shortcuts for efficient task creation.
     */
    private void setupKeyboardShortcuts() {
        // Enter in name field moves to description
        nameField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                descriptionArea.requestFocus();
            }
        });

        // Ctrl+Enter saves the task from any field
        setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                if (isFormValid) {
                    saveTask();
                }
            } else if (e.getCode() == KeyCode.ESCAPE) {
                getOverlayManager().hideCurrentOverlay();
            }
        });
    }

    /**
     * Validates the form and updates button states.
     */
    private void validateForm() {
        String taskName = nameField.getText().trim();
        isFormValid = !taskName.isEmpty() && taskName.length() >= 2;

        saveButton.setDisable(!isFormValid);
        saveAndStartButton.setDisable(!isFormValid);

        // Update title to show validation state
        if (isFormValid) {
            setTitle("Create New Task");
        } else {
            setTitle("Create New Task (Name Required)");
        }
    }

    /**
     * Applies the selected template to the form.
     */
    private void applyTemplate() {
        TaskTemplate template = templateCombo.getValue();
        if (template != null) {
            nameField.setText(template.getDefaultName());
            descriptionArea.setText(template.getDefaultDescription());
            priorityCombo.setValue(template.getDefaultPriority());

            if (template.getDefaultCategory() != null) {
                categoryCombo.setValue(template.getDefaultCategory());
            }

            if (template.getDefaultDueDays() > 0) {
                dueDatePicker.setValue(LocalDate.now().plusDays(template.getDefaultDueDays()));
            }

            // Focus on name field for customization
            nameField.requestFocus();
            nameField.selectAll();

            validateForm();
        }
    }

    /**
     * Updates the category combo box with existing categories.
     */
    private void updateCategoryOptions() {
        categoryCombo.getItems().clear();
        categoryCombo.getItems().add("Work");
        categoryCombo.getItems().add("Personal");
        categoryCombo.getItems().add("Learning");
        categoryCombo.getItems().add("Health");
        categoryCombo.getItems().add("Creative");

        // Add categories from existing tasks
        tasks.stream()
                .map(Task::getCategory)
                .filter(cat -> cat != null && !cat.getTaskCategory().trim().isEmpty())
                .map(cat -> cat.getTaskCategory())
                .distinct()
                .forEach(catName -> {
                    if (!categoryCombo.getItems().contains(catName)) {
                        categoryCombo.getItems().add(catName);
                    }
                });
    }

    /**
     * Updates the quest combo box with available quests.
     */
    private void updateQuestOptions() {
        questCombo.getItems().clear();
        questCombo.getItems().add("No Quest");

        questManager.getAllQuests().values().stream()
                .filter(quest -> !quest.isCompleted())
                .forEach(quest -> questCombo.getItems().add(quest.getTitle()));

        questCombo.setValue("No Quest");
    }

    /**
     * Saves the task and closes the panel.
     */
    private void saveTask() {
        Task newTask = createTaskFromForm();
        if (newTask != null) {
            tasks.add(newTask);
            assignToQuest(newTask);
            onTaskCreated.accept(() -> {
            });
            getOverlayManager().hideCurrentOverlay();

            // Show success feedback
            showTaskCreatedFeedback(newTask);
        }
    }

    /**
     * Saves the task and optionally starts working on it immediately.
     */
    private void saveAndStartTask() {
        Task newTask = createTaskFromForm();
        if (newTask != null) {
            tasks.add(newTask);
            assignToQuest(newTask);

            // Set the flag to start immediately
            startImmediatelyCheckBox.setSelected(true);

            onTaskCreated.accept(() -> {
            });
            getOverlayManager().hideCurrentOverlay();

            showTaskCreatedFeedback(newTask);
        }
    }

    /**
     * Creates a Task object from the form data.
     * 
     * @return The created Task, or null if creation failed
     */
    private Task createTaskFromForm() {
        try {
            String name = nameField.getText().trim();
            String description = descriptionArea.getText().trim();

            if (name.isEmpty()) {
                nameField.requestFocus();
                return null;
            }

            Task task = new Task(name, description);
            task.setPriority(priorityCombo.getValue());

            // Set category
            String categoryText = categoryCombo.getValue();
            if (categoryText != null && !categoryText.trim().isEmpty()) {
                task.setCategory(new TaskCategory(categoryText.trim()));
            }

            // Set due date
            if (dueDatePicker.getValue() != null) {
                LocalDateTime dueDateTime = dueDatePicker.getValue().atTime(23, 59);
                task.setDueDateTime(dueDateTime);
            }

            return task;

        } catch (Exception e) {
            System.err.println("Error creating task: " + e.getMessage());
            return null;
        }
    }

    /**
     * Assigns the task to the selected quest.
     * 
     * @param task The task to assign
     */
    private void assignToQuest(Task task) {
        String selectedQuest = questCombo.getValue();
        if (selectedQuest != null && !selectedQuest.equals("No Quest")) {
            questManager.getAllQuests().values().stream()
                    .filter(quest -> quest.getTitle().equals(selectedQuest))
                    .findFirst()
                    .ifPresent(quest -> questManager.addTaskToQuest(quest.getId(), task));
        }
    }

    /**
     * Shows feedback when a task is successfully created.
     * 
     * @param task The created task
     */
    private void showTaskCreatedFeedback(Task task) {
        // This could be enhanced with a toast notification or other feedback
        System.out.println("Task created: " + task.getName());

        if (startImmediatelyCheckBox.isSelected()) {
            // Signal to parent that this task should be started immediately
            // This would be handled by the App class
            System.out.println("Task should start immediately: " + task.getName());
        }
    }

    /**
     * Applies consistent styling to combo boxes.
     * 
     * @param comboBox The combo box to style
     */
    private void styleComboBox(ComboBox<?> comboBox) {
        comboBox.setStyle("-fx-border-color: #DEE2E6; -fx-border-radius: 6px; -fx-background-radius: 6px;");
    }

    /**
     * Enum for task templates to speed up common task creation.
     */
    public enum TaskTemplate {
        MEETING("Meeting", "Attend meeting", TaskPriority.HIGH, "Work", 0),
        EMAIL("Email Task", "Send important email", TaskPriority.MEDIUM, "Work", 1),
        RESEARCH("Research", "Research topic or gather information", TaskPriority.MEDIUM, "Learning", 3),
        EXERCISE("Exercise", "Complete workout or physical activity", TaskPriority.MEDIUM, "Health", 0),
        CALL("Phone Call", "Make important phone call", TaskPriority.HIGH, "Work", 0),
        REVIEW("Review Task", "Review document or work", TaskPriority.MEDIUM, "Work", 2),
        CREATIVE("Creative Work", "Work on creative project", TaskPriority.LOW, "Creative", 7),
        LEARNING("Learning Task", "Study or learn new skill", TaskPriority.MEDIUM, "Learning", 7);

        private final String defaultName;
        private final String defaultDescription;
        private final TaskPriority defaultPriority;
        private final String defaultCategory;
        private final int defaultDueDays;

        TaskTemplate(String defaultName, String defaultDescription, TaskPriority defaultPriority,
                String defaultCategory, int defaultDueDays) {
            this.defaultName = defaultName;
            this.defaultDescription = defaultDescription;
            this.defaultPriority = defaultPriority;
            this.defaultCategory = defaultCategory;
            this.defaultDueDays = defaultDueDays;
        }

        public String getDefaultName() {
            return defaultName;
        }

        public String getDefaultDescription() {
            return defaultDescription;
        }

        public TaskPriority getDefaultPriority() {
            return defaultPriority;
        }

        public String getDefaultCategory() {
            return defaultCategory;
        }

        public int getDefaultDueDays() {
            return defaultDueDays;
        }

        @Override
        public String toString() {
            return defaultName;
        }
    }
}