package com.focusflow.app.ui;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.focusflow.core.task.Task;
import com.focusflow.core.task.TaskPriority;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Task Selection Panel for choosing which task to work on.
 * Provides intelligent task recommendations, filtering, and quick selection.
 * 
 * Features:
 * - Smart task recommendations based on priority and due dates
 * - Real-time search and filtering
 * - Multiple sorting options (priority, due date, alphabetical)
 * - Quick selection with keyboard shortcuts
 * - Task preview with detailed information
 * - Empty state handling with helpful suggestions
 * - Recent tasks section for quick re-selection
 * 
 * @author FocusFlow Team
 * @version 2.0 - Enhanced Selection Intelligence
 */
public class TaskSelectionPanel extends BasePanel {

    // Dependencies
    private final List<Task> tasks;
    private final Consumer<Task> onTaskSelected;

    // Data management
    private FilteredList<Task> filteredTasks;
    private SortedList<Task> sortedTasks;
    private List<Task> incompleteTasks;
    private List<Task> recommendedTasks;

    // UI Components - Search and Filter
    private TextField searchField;
    private ComboBox<String> sortCombo;
    private ToggleGroup priorityFilterGroup;
    private CheckBox overdueOnlyCheck;
    private CheckBox dueTodayCheck;

    // UI Components - Task Display
    private ListView<Task> taskListView;
    private ListView<Task> recommendedListView;
    private VBox emptyStateBox;
    private Label statusLabel;

    // UI Components - Selection
    private Button selectButton;
    private Button createTaskButton;
    private Label selectedTaskInfo;

    // State
    private Task selectedTask;
    private String currentSearchText = "";

    /**
     * Creates a new TaskSelectionPanel.
     * 
     * @param overlayManager The overlay manager for panel lifecycle
     * @param pixelFont      The font for consistent typography
     * @param tasks          The list of all tasks to choose from
     * @param onTaskSelected Callback when a task is selected
     */
    public TaskSelectionPanel(OverlayManager overlayManager, Font pixelFont,
            List<Task> tasks, Consumer<Task> onTaskSelected) {
        super(overlayManager, pixelFont, "Select Task to Work On", true);
        this.tasks = tasks;
        this.onTaskSelected = onTaskSelected;
        prepareTaskData();
        finishInitialization();
    }

    @Override
    protected void createContent() {
        if (incompleteTasks.isEmpty()) {
            createEmptyState();
        } else {
            createTaskSelectionInterface();
        }

        setupKeyboardShortcuts();
    }

    /**
     * Prepares and analyzes task data for intelligent recommendations.
     */
    private void prepareTaskData() {
        // Filter incomplete tasks
        incompleteTasks = tasks.stream()
                .filter(task -> !task.isComplete())
                .collect(Collectors.toList());

        // Create filtered and sorted lists
        filteredTasks = new FilteredList<>(FXCollections.observableArrayList(incompleteTasks));
        sortedTasks = new SortedList<>(filteredTasks);

        // Generate smart recommendations
        generateRecommendations();
    }

    /**
     * Generates intelligent task recommendations based on various factors.
     */
    private void generateRecommendations() {
        recommendedTasks = incompleteTasks.stream()
                .sorted(Comparator
                        // 1. Overdue tasks first
                        .<Task>comparingInt(task -> task.hasDueDateTime() &&
                                task.getDueDateTime().isBefore(LocalDateTime.now()) ? 0 : 1)
                        // 2. Then by priority (urgent first)
                        .thenComparing(task -> task.getPriority().getValue(), Comparator.reverseOrder())
                        // 3. Then by due date (sooner first)
                        .thenComparing(task -> task.hasDueDateTime() ? task.getDueDateTime() : LocalDateTime.MAX)
                        // 4. Finally alphabetical
                        .thenComparing(Task::getName))
                .limit(5) // Top 5 recommendations
                .collect(Collectors.toList());
    }

    /**
     * Creates the empty state when no incomplete tasks are available.
     */
    private void createEmptyState() {
        emptyStateBox = new VBox(20);
        emptyStateBox.setAlignment(Pos.CENTER);
        emptyStateBox.setPadding(new Insets(40));

        // Empty state icon (using text for simplicity)
        Label emptyIcon = new Label("📋");
        emptyIcon.setFont(Font.font(48));

        Label emptyTitle = new Label("No Tasks Available");
        emptyTitle.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 20));
        emptyTitle.setTextFill(Color.web("#6C757D"));

        Label emptyMessage = new Label(
                "You don't have any incomplete tasks.\nCreate a task to get started with your focus session!");
        emptyMessage.setFont(Font.font(getPixelFont().getFamily(), FontWeight.NORMAL, 14));
        emptyMessage.setTextFill(Color.web("#ADB5BD"));
        emptyMessage.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        emptyMessage.setWrapText(true);

        createTaskButton = createPrimaryButton("Create Your First Task", this::createNewTask);

        Button cancelButton = createSecondaryButton("Cancel", () -> getOverlayManager().hideCurrentOverlay());

        emptyStateBox.getChildren().addAll(emptyIcon, emptyTitle, emptyMessage, createTaskButton, cancelButton);
        addContent(emptyStateBox);
    }

    /**
     * Creates the main task selection interface.
     */
    private void createTaskSelectionInterface() {
        // Search and filter section
        VBox searchSection = createSearchAndFilterSection();

        // Recommendations section
        VBox recommendationsSection = createRecommendationsSection();

        // All tasks section
        VBox allTasksSection = createAllTasksSection();

        // Selection info and buttons
        VBox selectionSection = createSelectionSection();

        addContent(searchSection, recommendationsSection, allTasksSection);
        showFooter(selectionSection);

        // Setup handlers
        setupListViewHandlers();
    }

    /**
     * Creates the search and filter controls.
     * 
     * @return VBox containing search and filter UI
     */
    private VBox createSearchAndFilterSection() {
        VBox section = createSection("Search & Filter");

        // Search box
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        Label searchLabel = new Label("🔍");
        searchLabel.setFont(Font.font(16));

        searchField = new TextField();
        searchField.setPromptText("Search tasks...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-border-color: #DEE2E6; -fx-border-radius: 6px; -fx-padding: 8px;");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // Sort dropdown
        sortCombo = new ComboBox<>();
        sortCombo.setItems(FXCollections.observableArrayList(
                "Smart (Recommended)", "Priority (High to Low)", "Due Date (Soonest First)",
                "Alphabetical", "Recently Created"));
        sortCombo.setValue("Smart (Recommended)");
        sortCombo.setPrefWidth(180);

        searchBox.getChildren().addAll(searchLabel, searchField, new Label("Sort:"), sortCombo);

        // Quick filters
        HBox filterBox = new HBox(15);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(10, 0, 0, 0));

        overdueOnlyCheck = new CheckBox("Overdue only");
        dueTodayCheck = new CheckBox("Due today");

        // Priority filter buttons
        Label priorityLabel = new Label("Priority:");
        priorityFilterGroup = new ToggleGroup();

        ToggleButton allPriorities = new ToggleButton("All");
        ToggleButton urgentOnly = new ToggleButton("Urgent");
        ToggleButton highOnly = new ToggleButton("High");

        allPriorities.setToggleGroup(priorityFilterGroup);
        urgentOnly.setToggleGroup(priorityFilterGroup);
        highOnly.setToggleGroup(priorityFilterGroup);
        allPriorities.setSelected(true);

        String toggleStyle = "-fx-background-radius: 15px; -fx-padding: 4 12;";
        allPriorities.setStyle(toggleStyle);
        urgentOnly.setStyle(toggleStyle);
        highOnly.setStyle(toggleStyle);

        filterBox.getChildren().addAll(overdueOnlyCheck, dueTodayCheck, new Separator(),
                priorityLabel, allPriorities, urgentOnly, highOnly);

        section.getChildren().addAll(searchBox, filterBox);

        // Setup event handlers
        setupFilterHandlers();

        return section;
    }

    /**
     * Creates the smart recommendations section.
     * 
     * @return VBox containing recommendations UI
     */
    private VBox createRecommendationsSection() {
        VBox section = createSection("Recommended Tasks");

        if (recommendedTasks.isEmpty()) {
            Label noRecommendations = createHelpText(
                    "No specific recommendations. All your tasks are equally important!");
            section.getChildren().add(noRecommendations);
            return section;
        }

        recommendedListView = new ListView<>();
        recommendedListView.setItems(FXCollections.observableArrayList(recommendedTasks));
        recommendedListView.setCellFactory(listView -> new TaskCell(true));
        recommendedListView.setPrefHeight(Math.min(120, recommendedTasks.size() * 40 + 10));
        recommendedListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldTask, newTask) -> handleTaskSelection(newTask, recommendedListView));

        // Double-click to select
        recommendedListView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                selectCurrentTask();
            }
        });

        Label recommendationHelp = createHelpText(
                "These tasks are recommended based on priority, due dates, and urgency.");

        section.getChildren().addAll(recommendedListView, recommendationHelp);
        return section;
    }

    /**
     * Creates the all tasks section with filtering.
     * 
     * @return VBox containing all tasks UI
     */
    private VBox createAllTasksSection() {
        VBox section = createSection("All Tasks");

        taskListView = new ListView<>();
        taskListView.setItems(sortedTasks);
        taskListView.setCellFactory(listView -> new TaskCell(false));
        taskListView.setPrefHeight(200);
        taskListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldTask, newTask) -> handleTaskSelection(newTask, taskListView));

        // Double-click to select
        taskListView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                selectCurrentTask();
            }
        });

        // Status label
        statusLabel = new Label(incompleteTasks.size() + " tasks available");
        statusLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.NORMAL, 12));
        statusLabel.setTextFill(Color.web("#6C757D"));

        section.getChildren().addAll(taskListView, statusLabel);
        return section;
    }

    /**
     * Creates the task selection info and action buttons.
     * 
     * @return VBox containing selection UI
     */
    private VBox createSelectionSection() {
        VBox section = new VBox(10);
        section.setAlignment(Pos.CENTER_RIGHT);

        selectedTaskInfo = new Label("Select a task to see details");
        selectedTaskInfo.setFont(Font.font(getPixelFont().getFamily(), FontWeight.NORMAL, 12));
        selectedTaskInfo.setTextFill(Color.web("#6C757D"));
        selectedTaskInfo.setWrapText(true);
        selectedTaskInfo.setMaxWidth(400);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Enhanced button creation with explicit styling
        selectButton = new Button("Start Working");
        selectButton.setOnAction(e -> selectCurrentTask());
        selectButton.setDisable(true);
        selectButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16; " +
                "-fx-background-radius: 4;");

        Button createButton = createSecondaryButton("Create New Task", this::createNewTask);
        Button cancelButton = createSecondaryButton("Cancel", () -> getOverlayManager().hideCurrentOverlay());

        buttonBox.getChildren().addAll(createButton, cancelButton, selectButton);
        section.getChildren().addAll(selectedTaskInfo, buttonBox);

        return section;
    }

    /**
     * Sets up event handlers for search and filter components.
     */
    private void setupFilterHandlers() {
        // Search field
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            currentSearchText = newText.toLowerCase().trim();
            applyFilters();
        });

        // Sort combo
        sortCombo.setOnAction(e -> applySorting());

        // Filter checkboxes
        overdueOnlyCheck.setOnAction(e -> applyFilters());
        dueTodayCheck.setOnAction(e -> applyFilters());

        // Priority toggles
        priorityFilterGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> applyFilters());

        // Initial setup
        applySorting();
        applyFilters();
    }

    /**
     * Sets up event handlers for both list views.
     */
    private void setupListViewHandlers() {
        // Double-click handler for task list
        taskListView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                Task clickedTask = taskListView.getSelectionModel().getSelectedItem();
                if (clickedTask != null) {
                    selectedTask = clickedTask;
                    selectCurrentTask();
                }
            }
        });

        // Double-click handler for recommended list
        if (recommendedListView != null) {
            recommendedListView.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                    Task clickedTask = recommendedListView.getSelectionModel().getSelectedItem();
                    if (clickedTask != null) {
                        selectedTask = clickedTask;
                        selectCurrentTask();
                    }
                }
            });
        }
    }

    /**
     * Applies current filter settings to the task list.
     */
    private void applyFilters() {
        Predicate<Task> filter = task -> {
            // Search filter
            if (!currentSearchText.isEmpty()) {
                String taskText = (task.getName() + " " + task.getDescription()).toLowerCase();
                if (!taskText.contains(currentSearchText)) {
                    return false;
                }
            }

            // Overdue filter
            if (overdueOnlyCheck.isSelected()) {
                if (!task.hasDueDateTime() || !task.getDueDateTime().isBefore(LocalDateTime.now())) {
                    return false;
                }
            }

            // Due today filter
            if (dueTodayCheck.isSelected()) {
                if (!task.hasDueDateTime() ||
                        !task.getDueDateTime().toLocalDate().equals(LocalDateTime.now().toLocalDate())) {
                    return false;
                }
            }

            // Priority filter
            Toggle selectedPriorityToggle = priorityFilterGroup.getSelectedToggle();
            if (selectedPriorityToggle != null) {
                String priorityFilter = ((ToggleButton) selectedPriorityToggle).getText();
                switch (priorityFilter) {
                    case "Urgent":
                        if (task.getPriority() != TaskPriority.URGENT)
                            return false;
                        break;
                    case "High":
                        if (task.getPriority() != TaskPriority.HIGH)
                            return false;
                        break;
                    // "All" allows everything through
                }
            }

            return true;
        };

        filteredTasks.setPredicate(filter);
        updateStatusLabel();
    }

    /**
     * Applies the selected sorting to the task list.
     */
    private void applySorting() {
        String sortType = sortCombo.getValue();
        Comparator<Task> comparator;

        switch (sortType) {
            case "Priority (High to Low)":
                comparator = Comparator.comparing((Task t) -> t.getPriority().getValue()).reversed()
                        .thenComparing(Task::getName);
                break;

            case "Due Date (Soonest First)":
                comparator = Comparator
                        .comparing((Task t) -> t.hasDueDateTime() ? t.getDueDateTime() : LocalDateTime.MAX)
                        .thenComparing(Task::getName);
                break;

            case "Alphabetical":
                comparator = Comparator.comparing(Task::getName);
                break;

            case "Recently Created":
                comparator = Comparator.comparing(Task::getCreationDateTime).reversed();
                break;

            default: // "Smart (Recommended)"
                comparator = Comparator
                        .<Task>comparingInt(task -> task.hasDueDateTime() &&
                                task.getDueDateTime().isBefore(LocalDateTime.now()) ? 0 : 1)
                        .thenComparing(task -> task.getPriority().getValue(), Comparator.reverseOrder())
                        .thenComparing(task -> task.hasDueDateTime() ? task.getDueDateTime() : LocalDateTime.MAX)
                        .thenComparing(Task::getName);
                break;
        }

        sortedTasks.setComparator(comparator);
    }

    /**
     * Updates the status label with current filter results.
     */
    private void updateStatusLabel() {
        int visibleCount = filteredTasks.size();
        int totalCount = incompleteTasks.size();

        if (visibleCount == totalCount) {
            statusLabel.setText(totalCount + " tasks available");
        } else {
            statusLabel.setText(visibleCount + " of " + totalCount + " tasks shown");
        }
    }

    /**
     * Handles task selection from either list view.
     * 
     * @param task       The selected task
     * @param sourceList The list view that triggered the selection
     */
    private void handleTaskSelection(Task task, ListView<Task> sourceList) {
        System.out.println("[DEBUG] Task selected: " + (task != null ? task.getName() : "null"));

        this.selectedTask = task;

        if (sourceList == taskListView && recommendedListView != null) {
            recommendedListView.getSelectionModel().clearSelection();
        } else if (sourceList == recommendedListView && taskListView != null) {
            taskListView.getSelectionModel().clearSelection();
        }

        updateSelectionInfo();
        selectButton.setDisable(selectedTask == null);
    }

    /**
     * Updates the selection info display and button states.
     */
    private void updateSelectionInfo() {
        if (selectedTask == null) {
            selectedTaskInfo.setText("Select a task to see details");
            selectButton.setDisable(true);
            return;
        }

        // Build info text
        StringBuilder info = new StringBuilder();
        info.append("Selected: ").append(selectedTask.getName());
        info.append("\nPriority: ").append(selectedTask.getPriority().name());

        // Add these missing details
        if (selectedTask.hasDueDateTime()) {
            info.append("\nDue: ").append(selectedTask.getDueDateTime().toLocalDate());
        }
        if (!selectedTask.getDescription().isEmpty()) {
            info.append("\nDetails: ").append(selectedTask.getDescription());
        }

        // Enable the select button and update info
        selectButton.setDisable(false);
        selectedTaskInfo.setText(info.toString());
    }

    /**
     * Sets up keyboard shortcuts for efficient task selection.
     */
    private void setupKeyboardShortcuts() {
        // Add event filter to consume events meant for this panel
        addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            // Only handle events if they're meant for components in this panel
            if (e.getTarget() instanceof Node &&
                    ((Node) e.getTarget()).getScene().getFocusOwner() != null &&
                    isDescendant(getPanelRoot(), ((Node) e.getTarget()).getScene().getFocusOwner())) {

                if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                    if (selectedTask != null) {
                        selectCurrentTask();
                        e.consume(); // Prevent event from bubbling up
                    }
                } else if (e.getCode() == KeyCode.ESCAPE) {
                    getOverlayManager().hideCurrentOverlay();
                    e.consume();
                } else if (e.isControlDown() && e.getCode() == KeyCode.N) {
                    createNewTask();
                    e.consume();
                }
            }
        });

        // Focus search field initially
        searchField.requestFocus();
    }

    private boolean isDescendant(Object panelRoot, Node focusOwner) {
        if (focusOwner == null || !(panelRoot instanceof Parent)) {
            return false;
        }

        Node current = focusOwner;
        while (current != null) {
            if (current == panelRoot) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private Parent getPanelRoot() {
        // Return this panel as the root since TaskSelectionPanel extends BasePanel
        // which is a Parent
        return this;
    }

    /**
     * Selects the current task and closes the panel.
     */
    private void selectCurrentTask() {
        System.out.println("[TaskSelectionPanel] selectCurrentTask called");

        if (selectedTask != null) {
            System.out.println("[TaskSelectionPanel] Selected task: " + selectedTask.getName());
            onTaskSelected.accept(selectedTask);
            getOverlayManager().hideCurrentOverlay();
            System.out.println("[TaskSelectionPanel] Task selected and overlay closed");
        } else {
            System.out.println("[TaskSelectionPanel] No task selected");
        }
    }

    /**
     * Triggers task creation by closing this panel and opening task creation.
     */
    private void createNewTask() {
        getOverlayManager().hideCurrentOverlay();
        // The parent App class should handle opening the task creation panel
        // This could be enhanced with a callback system
    }

    /**
     * Custom cell renderer for task list items.
     */
    private class TaskCell extends ListCell<Task> {
        private final boolean isRecommended;
        private HBox container;
        private VBox infoContainer;
        private Label nameLabel;
        private Label detailsLabel;
        private Label priorityBadge;
        private Label statusBadge;

        public TaskCell(boolean isRecommended) {
            this.isRecommended = isRecommended;
        }

        @Override
        protected void updateItem(Task task, boolean empty) {
            super.updateItem(task, empty);

            if (empty || task == null) {
                setGraphic(null);
                return;
            }

            if (container == null) {
                createCell();
            }

            updateCellContent(task);
            setGraphic(container);
        }

        private void createCell() {
            container = new HBox(12);
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPadding(new Insets(8));
            container.setStyle("-fx-background-color: white; -fx-border-color: #E9ECEF; " +
                    "-fx-border-radius: 6px; -fx-background-radius: 6px;");

            infoContainer = new VBox(2);

            nameLabel = new Label();
            nameLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));

            detailsLabel = new Label();
            detailsLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.NORMAL, 11));
            detailsLabel.setTextFill(Color.web("#6C757D"));

            infoContainer.getChildren().addAll(nameLabel, detailsLabel);
            HBox.setHgrow(infoContainer, Priority.ALWAYS);

            // Badges container
            VBox badgeContainer = new VBox(4);
            badgeContainer.setAlignment(Pos.CENTER_RIGHT);

            priorityBadge = new Label();
            priorityBadge.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 10));
            priorityBadge.setStyle("-fx-background-radius: 10px; -fx-padding: 2 8; -fx-text-fill: white;");

            statusBadge = new Label();
            statusBadge.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 10));
            statusBadge.setStyle("-fx-background-radius: 10px; -fx-padding: 2 8;");

            badgeContainer.getChildren().addAll(priorityBadge, statusBadge);

            container.getChildren().addAll(infoContainer, badgeContainer);
        }

        private void updateCellContent(Task task) {
            nameLabel.setText(task.getName());

            // Details line
            StringBuilder details = new StringBuilder();
            if (!task.getDescription().trim().isEmpty()) {
                String desc = task.getDescription().trim();
                if (desc.length() > 50) {
                    desc = desc.substring(0, 47) + "...";
                }
                details.append(desc);
            }

            if (task.hasDueDateTime()) {
                if (details.length() > 0)
                    details.append(" • ");
                details.append("Due: ").append(task.getDueDateTime().toLocalDate());
            }

            detailsLabel.setText(details.toString());

            // Priority badge
            TaskPriority priority = task.getPriority();
            priorityBadge.setText(priority.name());
            priorityBadge
                    .setStyle(priorityBadge.getStyle() + "-fx-background-color: " + getPriorityColor(priority) + ";");

            // Status badge
            statusBadge.setVisible(false);
            statusBadge.setManaged(false);

            if (task.hasDueDateTime()) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime dueDate = task.getDueDateTime();

                if (dueDate.isBefore(now)) {
                    statusBadge.setText("OVERDUE");
                    statusBadge.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; " +
                            "-fx-background-radius: 10px; -fx-padding: 2 8; -fx-font-weight: bold;");
                    statusBadge.setVisible(true);
                    statusBadge.setManaged(true);
                } else if (dueDate.toLocalDate().equals(now.toLocalDate())) {
                    statusBadge.setText("DUE TODAY");
                    statusBadge.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black; " +
                            "-fx-background-radius: 10px; -fx-padding: 2 8; -fx-font-weight: bold;");
                    statusBadge.setVisible(true);
                    statusBadge.setManaged(true);
                }
            }

            // Special styling for recommended tasks
            if (isRecommended) {
                container.setStyle(container.getStyle() + "-fx-border-color: #007BFF; -fx-border-width: 2px;");
            }

            // Hover effect
            container.setOnMouseEntered(e -> {
                container.setStyle(
                        container.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
            });

            container.setOnMouseExited(e -> {
                container.setStyle(container.getStyle()
                        .replace("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);", ""));
            });
        }

        private String getPriorityColor(TaskPriority priority) {
            switch (priority) {
                case URGENT:
                    return "#DC3545";
                case HIGH:
                    return "#FD7E14";
                case MEDIUM:
                    return "#007BFF";
                case LOW:
                    return "#28A745";
                default:
                    return "#6C757D";
            }
        }
    }
}