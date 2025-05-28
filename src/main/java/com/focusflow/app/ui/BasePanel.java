package com.focusflow.app.ui;

import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
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
 * Enhanced with responsive design that adapts to screen size.
 * 
 * Features:
 * - Responsive sizing based on screen dimensions
 * - Consistent header with title and close button
 * - Modern card-like appearance with shadows and rounded corners
 * - Built-in button styling and animation effects
 * - Accessibility support with proper focus management
 * - Theme-aware styling system
 * 
 * @author FocusFlow Team
 * @version 2.1 - Responsive Design Enhancement
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

    // Responsive sizing constants (as percentages of screen size)
    private static final double MIN_WIDTH_PERCENT = 0.25; // 25% of screen width minimum
    private static final double MAX_WIDTH_PERCENT = 0.85; // 85% of screen width maximum
    private static final double DEFAULT_WIDTH_PERCENT = 0.6; // 60% of screen width default
    private static final double MAX_HEIGHT_PERCENT = 0.85; // 85% of screen height maximum

    // Styling constants
    private static final String PANEL_BACKGROUND_COLOR = "white";
    private static final String PANEL_BORDER_COLOR = "#E9ECEF";
    private static final String HEADER_BACKGROUND_COLOR = "#F8F9FA";
    private static final String CLOSE_BUTTON_COLOR = "#6C757D";
    private static final String CLOSE_BUTTON_HOVER_COLOR = "#495057";
    private static final double PANEL_BORDER_RADIUS = 15.0;

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

        // Setup responsive sizing
        setupResponsiveDesign();
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

        // Always setup responsive design
        setupResponsiveDesign();
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
     * Sets up responsive design bindings for the panel.
     */
    private void setupResponsiveDesign() {
        // Bind panel width to screen size
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupSizeBindings(newScene);
            }
        });

        // If scene is already available, setup bindings immediately
        if (getScene() != null) {
            setupSizeBindings(getScene());
        }
    }

    /**
     * Sets up size bindings based on the scene.
     */
    private void setupSizeBindings(javafx.scene.Scene scene) {
        // Responsive width - between 25% and 85% of screen width
        minWidthProperty().bind(
                Bindings.max(300, // Absolute minimum
                        scene.widthProperty().multiply(MIN_WIDTH_PERCENT)));

        maxWidthProperty().bind(
                Bindings.min(1000, // Absolute maximum for readability
                        scene.widthProperty().multiply(MAX_WIDTH_PERCENT)));

        prefWidthProperty().bind(
                Bindings.min(
                        scene.widthProperty().multiply(DEFAULT_WIDTH_PERCENT),
                        maxWidthProperty()));

        // Responsive height - maximum 85% of screen height
        maxHeightProperty().bind(
                scene.heightProperty().multiply(MAX_HEIGHT_PERCENT));

        // Responsive padding and spacing based on screen size
        contentContainer.paddingProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double width = scene.getWidth();
                    double basePadding = width < 800 ? 15 : 20; // Smaller padding on small screens
                    return new Insets(basePadding);
                }, scene.widthProperty()));

        contentContainer.spacingProperty().bind(
                Bindings.createDoubleBinding(() -> {
                    double width = scene.getWidth();
                    return width < 800 ? 12.0 : 15.0; // Smaller spacing on small screens
                }, scene.widthProperty()));

        // Responsive header padding
        headerContainer.paddingProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double width = scene.getWidth();
                    double basePadding = width < 800 ? 12 : 15;
                    return new Insets(basePadding);
                }, scene.widthProperty()));
    }

    /**
     * Initializes the base panel structure and styling.
     */
    private void initializePanel() {
        setSpacing(0);
        setMaxHeight(Region.USE_PREF_SIZE);

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
        headerContainer.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-background-radius: %.1f %.1f 0 0;",
                HEADER_BACKGROUND_COLOR,
                PANEL_BORDER_RADIUS, PANEL_BORDER_RADIUS));

        // Title label with responsive font size
        titleLabel = new Label(title);
        titleLabel.fontProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double baseSize = getScene() != null && getScene().getWidth() < 800 ? 16.0 : 18.0;
                    return Font.font(pixelFont.getFamily(), FontWeight.BOLD, baseSize);
                }, sceneProperty()));
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
        VBox.setVgrow(contentContainer, Priority.ALWAYS);
        getChildren().add(contentContainer);
    }

    /**
     * Creates the footer area for action buttons and other footer content.
     */
    private void createFooter() {
        footerContainer = new HBox();
        footerContainer.setAlignment(Pos.CENTER_RIGHT);
        footerContainer.paddingProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double basePadding = getScene() != null && getScene().getWidth() < 800 ? 15 : 20;
                    return new Insets(0, basePadding, basePadding, basePadding);
                }, sceneProperty()));
        footerContainer.setSpacing(10);
        footerContainer.setVisible(false);
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
        closeBtn.fontProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double baseSize = getScene() != null && getScene().getWidth() < 800 ? 14.0 : 16.0;
                    return Font.font(pixelFont.getFamily(), FontWeight.NORMAL, baseSize);
                }, sceneProperty()));
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
        HBox buttonBox = new HBox();
        buttonBox.spacingProperty().bind(
                Bindings.createDoubleBinding(() -> {
                    return getScene() != null && getScene().getWidth() < 800 ? 8.0 : 10.0;
                }, sceneProperty()));
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

        // Responsive font size
        button.fontProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double baseSize = getScene() != null && getScene().getWidth() < 800 ? 12.0 : 14.0;
                    return Font.font(pixelFont.getFamily(), FontWeight.BOLD, baseSize);
                }, sceneProperty()));

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

        // Responsive font size
        button.fontProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double baseSize = getScene() != null && getScene().getWidth() < 800 ? 12.0 : 14.0;
                    return Font.font(pixelFont.getFamily(), FontWeight.BOLD, baseSize);
                }, sceneProperty()));

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

        // Responsive font size
        button.fontProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double baseSize = getScene() != null && getScene().getWidth() < 800 ? 12.0 : 14.0;
                    return Font.font(pixelFont.getFamily(), FontWeight.BOLD, baseSize);
                }, sceneProperty()));

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

        // Responsive font size
        button.fontProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double baseSize = getScene() != null && getScene().getWidth() < 800 ? 12.0 : 14.0;
                    return Font.font(pixelFont.getFamily(), FontWeight.BOLD, baseSize);
                }, sceneProperty()));

        if (action != null) {
            button.setOnAction(e -> action.run());
        }

        addButtonAnimations(button);
        return button;
    }

    /**
     * Applies consistent styling to a button with responsive font sizing.
     * 
     * @param button The button to style
     */
    private void styleButton(Button button) {
        // Apply responsive font size if not already set
        if (button.getFont() == null || button.getFont().getSize() <= 12) {
            button.fontProperty().bind(
                    Bindings.createObjectBinding(() -> {
                        double baseSize = getScene() != null && getScene().getWidth() < 800 ? 12.0 : 14.0;
                        return Font.font(pixelFont.getFamily(), FontWeight.NORMAL, baseSize);
                    }, sceneProperty()));
        }

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
        // Store original action if it exists
        final javafx.event.EventHandler<javafx.event.ActionEvent> originalHandler = button.getOnAction();

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

        // Click animation with original action preservation
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
            if (originalHandler != null) {
                originalHandler.handle(e);
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
        VBox section = new VBox();
        section.spacingProperty().bind(
                Bindings.createDoubleBinding(() -> {
                    return getScene() != null && getScene().getWidth() < 800 ? 8.0 : 10.0;
                }, sceneProperty()));

        if (labelText != null && !labelText.trim().isEmpty()) {
            Label sectionLabel = new Label(labelText);
            sectionLabel.fontProperty().bind(
                    Bindings.createObjectBinding(() -> {
                        double baseSize = getScene() != null && getScene().getWidth() < 800 ? 14.0 : 16.0;
                        return Font.font(pixelFont.getFamily(), FontWeight.BOLD, baseSize);
                    }, sceneProperty()));
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
        helpLabel.fontProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double baseSize = getScene() != null && getScene().getWidth() < 800 ? 10.0 : 12.0;
                    return Font.font(pixelFont.getFamily(), FontWeight.NORMAL, baseSize);
                }, sceneProperty()));
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

    /**
     * Gets responsive font size based on screen width.
     * 
     * @param baseSize The base font size
     * @return Responsive font size
     */
    protected double getResponsiveFontSize(double baseSize) {
        if (getScene() == null)
            return baseSize;
        return getScene().getWidth() < 800 ? baseSize * 0.85 : baseSize;
    }

    /**
     * Gets responsive spacing based on screen width.
     * 
     * @param baseSpacing The base spacing value
     * @return Responsive spacing value
     */
    protected double getResponsiveSpacing(double baseSpacing) {
        if (getScene() == null)
            return baseSpacing;
        return getScene().getWidth() < 800 ? baseSpacing * 0.8 : baseSpacing;
    }

    /**
     * Gets responsive padding based on screen width.
     * 
     * @param basePadding The base padding value
     * @return Responsive padding value
     */
    protected double getResponsivePadding(double basePadding) {
        if (getScene() == null)
            return basePadding;
        return getScene().getWidth() < 800 ? basePadding * 0.75 : basePadding;
    }
}