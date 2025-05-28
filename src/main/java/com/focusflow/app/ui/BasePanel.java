package com.focusflow.app.ui;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Base class for all overlay panels in the FocusFlow application.
 * Provides consistent styling, header management, and common functionality.
 * 
 * Features:
 * - Consistent header with title and close button
 * - Modern card-like appearance with shadows and rounded corners
 * - Responsive design that adapts to content
 * - Built-in button styling and animation effects
 * - Accessibility support with proper focus management
 * - Theme-aware styling system
 * 
 * @author FocusFlow Team
 * @version 2.0 - Enhanced Styling and Accessibility
 */
public abstract class BasePanel extends VBox {

    // Core dependencies
    protected final OverlayManager overlayManager;
    protected final Font pixelFont;

    // UI Components
    private HBox headerContainer;
    private Label titleLabel;
    private Button closeButton;
    private VBox contentContainer;
    private HBox footerContainer;

    // Styling constants
    private static final String PANEL_BACKGROUND_COLOR = "white";
    private static final String PANEL_BORDER_COLOR = "#E9ECEF";
    private static final String HEADER_BACKGROUND_COLOR = "#F8F9FA";
    private static final String CLOSE_BUTTON_COLOR = "#6C757D";
    private static final String CLOSE_BUTTON_HOVER_COLOR = "#495057";
    private static final double PANEL_BORDER_RADIUS = 15.0;
    private static final double CONTENT_PADDING = 20.0;
    private static final double HEADER_PADDING = 15.0;

    /**
     * Creates a new BasePanel with the specified title.
     * 
     * @param overlayManager The overlay manager for handling panel lifecycle
     * @param pixelFont      The font to use for consistent typography
     * @param title          The title to display in the panel header
     */
    public BasePanel(OverlayManager overlayManager, Font pixelFont, String title) {
        this.overlayManager = overlayManager;
        this.pixelFont = pixelFont;

        initializePanel();
        createHeader(title);
        createContentArea();
        createFooter();

        // Create the actual panel content
        createContent();

        // Apply accessibility features
        setupAccessibility();
    }

    /**
     * Constructor that delays content creation for subclasses that need
     * to initialize fields before creating content.
     */
    protected BasePanel(OverlayManager overlayManager, Font pixelFont, String title, boolean delayContentCreation) {
        this.overlayManager = overlayManager;
        this.pixelFont = pixelFont;

        initializePanel();
        createHeader(title);
        createContentArea();
        createFooter();

        // Only create content if not delayed
        if (!delayContentCreation) {
            createContent();
            setupAccessibility();
        }
    }

    /**
     * Call this method to finish initialization when using delayed creation.
     */
    protected void finishInitialization() {
        createContent();
        setupAccessibility();
    }

    /**
     * Abstract method that subclasses must implement to create their content.
     * This method is called after the panel structure is set up.
     */
    protected abstract void createContent();

    /**
     * Initializes the base panel structure and styling.
     */
    private void initializePanel() {
        setSpacing(0);
        setMaxHeight(Region.USE_PREF_SIZE);
        setMaxWidth(600); // Reasonable max width for readability
        setMinWidth(300); // Minimum width for usability

        // Create modern card-like appearance
        setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-background-radius: %.1f %.1f 0 0; " +
                        "-fx-border-color: %s; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: %.1f %.1f 0 0;",
                PANEL_BACKGROUND_COLOR,
                PANEL_BORDER_RADIUS, PANEL_BORDER_RADIUS,
                PANEL_BORDER_COLOR,
                PANEL_BORDER_RADIUS, PANEL_BORDER_RADIUS));

        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.15));
        dropShadow.setRadius(20);
        dropShadow.setOffsetY(-5);
        setEffect(dropShadow);
    }

    /**
     * Creates the panel header with title and close button.
     * 
     * @param title The title to display
     */
    private void createHeader(String title) {
        headerContainer = new HBox();
        headerContainer.setAlignment(Pos.CENTER_LEFT);
        headerContainer.setPadding(new Insets(HEADER_PADDING, HEADER_PADDING, HEADER_PADDING, HEADER_PADDING));
        headerContainer.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-background-radius: %.1f %.1f 0 0;",
                HEADER_BACKGROUND_COLOR,
                PANEL_BORDER_RADIUS, PANEL_BORDER_RADIUS));

        // Title label
        titleLabel = new Label(title);
        titleLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#212529"));

        // Spacer to push close button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Close button
        closeButton = createCloseButton();

        headerContainer.getChildren().addAll(titleLabel, spacer, closeButton);
        getChildren().add(headerContainer);

        // Add separator line
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: " + PANEL_BORDER_COLOR + ";");
        getChildren().add(separator);
    }

    /**
     * Creates the main content area where subclasses add their content.
     */
    private void createContentArea() {
        contentContainer = new VBox();
        contentContainer.setPadding(new Insets(CONTENT_PADDING));
        contentContainer.setSpacing(15);
        VBox.setVgrow(contentContainer, Priority.ALWAYS);
        getChildren().add(contentContainer);
    }

    /**
     * Creates the footer area for action buttons and other footer content.
     */
    private void createFooter() {
        footerContainer = new HBox();
        footerContainer.setAlignment(Pos.CENTER_RIGHT);
        footerContainer.setPadding(new Insets(0, CONTENT_PADDING, CONTENT_PADDING, CONTENT_PADDING));
        footerContainer.setSpacing(10);
        footerContainer.setVisible(false); // Hidden by default
        footerContainer.setManaged(false);
        getChildren().add(footerContainer);
    }

    /**
     * Creates a styled close button with hover effects.
     * 
     * @return The configured close button
     */
    private Button createCloseButton() {
        Button closeBtn = new Button("✕");
        closeBtn.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, 16));
        closeBtn.setStyle(String.format(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: %s; " +
                        "-fx-border-color: transparent; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 5px; " +
                        "-fx-background-radius: 50%%;",
                CLOSE_BUTTON_COLOR));

        // Hover effects
        closeBtn.setOnMouseEntered(e -> {
            closeBtn.setStyle(closeBtn.getStyle() + String.format(
                    "-fx-background-color: rgba(108, 117, 125, 0.1); " +
                            "-fx-text-fill: %s;",
                    CLOSE_BUTTON_HOVER_COLOR));
        });

        closeBtn.setOnMouseExited(e -> {
            closeBtn.setStyle(String.format(
                    "-fx-background-color: transparent; " +
                            "-fx-text-fill: %s; " +
                            "-fx-border-color: transparent; " +
                            "-fx-cursor: hand; " +
                            "-fx-padding: 5px; " +
                            "-fx-background-radius: 50%%;",
                    CLOSE_BUTTON_COLOR));
        });

        // Close action
        closeBtn.setOnAction(e -> overlayManager.hideCurrentOverlay());

        return closeBtn;
    }

    /**
     * Sets up accessibility features for screen readers and keyboard navigation.
     */
    private void setupAccessibility() {
        // Make panel focusable for keyboard navigation
        setFocusTraversable(true);

        // Set accessible role and name
        setAccessibleRole(javafx.scene.AccessibleRole.DIALOG);
        setAccessibleText("Panel: " + titleLabel.getText());

        // Ensure close button is accessible
        closeButton.setAccessibleText("Close " + titleLabel.getText());

        // Handle escape key to close panel
        setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                overlayManager.hideCurrentOverlay();
            }
        });
    }

    // Protected methods for subclasses to use

    /**
     * Adds content to the main content area.
     * 
     * @param nodes The nodes to add to the content area
     */
    protected void addContent(Node... nodes) {
        contentContainer.getChildren().addAll(nodes);
    }

    /**
     * Clears all content from the content area.
     */
    protected void clearContent() {
        contentContainer.getChildren().clear();
    }

    /**
     * Gets the content container for direct manipulation.
     * 
     * @return The VBox containing the panel content
     */
    protected VBox getContentContainer() {
        return contentContainer;
    }

    /**
     * Updates the panel title.
     * 
     * @param newTitle The new title to display
     */
    protected void setTitle(String newTitle) {
        titleLabel.setText(newTitle);
        setAccessibleText("Panel: " + newTitle);
    }

    /**
     * Creates a row of buttons with consistent styling for the footer area.
     * 
     * @param buttons The buttons to include in the row
     * @return An HBox containing the styled buttons
     */
    protected HBox createButtonRow(Button... buttons) {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        for (Button button : buttons) {
            styleButton(button);
            buttonBox.getChildren().add(button);
        }

        return buttonBox;
    }

    /**
     * Shows the footer with the specified content.
     * 
     * @param content The content to show in the footer
     */
    protected void showFooter(Node... content) {
        footerContainer.getChildren().clear();
        footerContainer.getChildren().addAll(content);
        footerContainer.setVisible(true);
        footerContainer.setManaged(true);
    }

    /**
     * Hides the footer area.
     */
    protected void hideFooter() {
        footerContainer.setVisible(false);
        footerContainer.setManaged(false);
    }

    /**
     * Creates a primary action button with consistent styling.
     * 
     * @param text   The button text
     * @param action The action to perform when clicked
     * @return A styled primary button
     */
    protected Button createPrimaryButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: #007BFF; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;");

        if (action != null) {
            button.setOnAction(e -> action.run());
        }

        addButtonAnimations(button);
        return button;
    }

    /**
     * Creates a secondary action button with consistent styling.
     * 
     * @param text   The button text
     * @param action The action to perform when clicked
     * @return A styled secondary button
     */
    protected Button createSecondaryButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: #6C757D; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;");

        if (action != null) {
            button.setOnAction(e -> action.run());
        }

        addButtonAnimations(button);
        return button;
    }

    /**
     * Creates a success/positive action button.
     * 
     * @param text   The button text
     * @param action The action to perform when clicked
     * @return A styled success button
     */
    protected Button createSuccessButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: #28A745; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;");

        if (action != null) {
            button.setOnAction(e -> action.run());
        }

        addButtonAnimations(button);
        return button;
    }

    /**
     * Creates a danger/destructive action button.
     * 
     * @param text   The button text
     * @param action The action to perform when clicked
     * @return A styled danger button
     */
    protected Button createDangerButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: #DC3545; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;");

        if (action != null) {
            button.setOnAction(e -> action.run());
        }

        addButtonAnimations(button);
        return button;
    }

    /**
     * Applies consistent styling to a button.
     * 
     * @param button The button to style
     */
    private void styleButton(Button button) {
        button.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, 14));

        // Add animations if not already present
        if (button.getOnMouseEntered() == null) {
            addButtonAnimations(button);
        }
    }

    /**
     * Adds hover and click animations to a button.
     * 
     * @param button The button to animate
     */
    private void addButtonAnimations(Button button) {
        // Hover effects
        button.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), button);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });

        button.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });

        // Click animation
        Runnable originalAction = null;
        if (button.getOnAction() != null) {
            // Store original action
            final javafx.event.EventHandler<javafx.event.ActionEvent> originalHandler = button.getOnAction();
            originalAction = () -> originalHandler.handle(new javafx.event.ActionEvent());
        }

        final Runnable finalOriginalAction = originalAction;
        button.setOnAction(e -> {
            // Quick scale down and back up
            ScaleTransition press = new ScaleTransition(Duration.millis(100), button);
            press.setToX(0.95);
            press.setToY(0.95);
            press.setOnFinished(event -> {
                ScaleTransition release = new ScaleTransition(Duration.millis(100), button);
                release.setToX(1.0);
                release.setToY(1.0);
                release.play();
            });
            press.play();

            // Execute original action
            if (finalOriginalAction != null) {
                finalOriginalAction.run();
            }
        });
    }

    /**
     * Creates a section separator with optional label.
     * 
     * @param labelText Optional label text (can be null)
     * @return A styled separator
     */
    protected VBox createSection(String labelText) {
        VBox section = new VBox(10);

        if (labelText != null && !labelText.trim().isEmpty()) {
            Label sectionLabel = new Label(labelText);
            sectionLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 16));
            sectionLabel.setTextFill(Color.web("#495057"));
            section.getChildren().add(sectionLabel);
        }

        return section;
    }

    /**
     * Creates a help text label with consistent styling.
     * 
     * @param text The help text to display
     * @return A styled help label
     */
    protected Label createHelpText(String text) {
        Label helpLabel = new Label(text);
        helpLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, 12));
        helpLabel.setTextFill(Color.web("#6C757D"));
        helpLabel.setWrapText(true);
        return helpLabel;
    }

    /**
     * Gets the overlay manager for this panel.
     * 
     * @return The overlay manager
     */
    protected OverlayManager getOverlayManager() {
        return overlayManager;
    }

    /**
     * Gets the pixel font used by this panel.
     * 
     * @return The pixel font
     */
    protected Font getPixelFont() {
        return pixelFont;
    }
}