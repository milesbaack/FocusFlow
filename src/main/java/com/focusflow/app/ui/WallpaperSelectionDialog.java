package com.focusflow.app.ui;

import java.util.function.Consumer;

import com.focusflow.app.ui.BackgroundManager.WallpaperOption;
import com.focusflow.core.preferences.UserPreferences;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog for selecting wallpapers in the FocusFlow application.
 * Provides a grid of wallpaper thumbnails for user selection.
 * 
 * @author Miles Baack
 * @version 1.0
 */
public class WallpaperSelectionDialog {

    private Stage dialog;
    private UserPreferences userPreferences;
    private Font pixelFont;
    private Consumer<String> onWallpaperSelected;
    private String currentSelection;

    public WallpaperSelectionDialog(Stage parentStage, UserPreferences userPreferences,
            Font pixelFont, Consumer<String> onWallpaperSelected) {
        this.userPreferences = userPreferences;
        this.pixelFont = pixelFont;
        this.onWallpaperSelected = onWallpaperSelected;
        this.currentSelection = userPreferences.getSelectedWallpaper();

        initializeDialog(parentStage);
    }

    private void initializeDialog(Stage parentStage) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle("Select Wallpaper - FocusFlow");
        dialog.setResizable(false);

        createDialogContent();
    }

    private void createDialogContent() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");

        // Title
        Label titleLabel = new Label("Choose Your Wallpaper");
        titleLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.DARKBLUE);

        // Subtitle
        Label subtitleLabel = new Label("Select a background that inspires your productivity");
        subtitleLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.GRAY);

        // Wallpaper grid
        GridPane wallpaperGrid = createWallpaperGrid();

        // Wrap grid in scroll pane
        ScrollPane scrollPane = new ScrollPane(wallpaperGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background-color: transparent;");

        // Buttons
        HBox buttonBox = createButtonBox();

        mainLayout.getChildren().addAll(titleLabel, subtitleLabel, scrollPane, buttonBox);

        Scene scene = new Scene(mainLayout, 600, 550);
        dialog.setScene(scene);
    }

    private GridPane createWallpaperGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));
        grid.setAlignment(Pos.CENTER);

        int columns = 3;
        int row = 0;
        int col = 0;

        for (WallpaperOption wallpaper : BackgroundManager.getAvailableWallpapers()) {
            StackPane wallpaperCard = createWallpaperCard(wallpaper);
            grid.add(wallpaperCard, col, row);

            col++;
            if (col >= columns) {
                col = 0;
                row++;
            }
        }

        return grid;
    }

    private StackPane createWallpaperCard(WallpaperOption wallpaper) {
        final double THUMBNAIL_SIZE = 140;
        final double CARD_SIZE = 160;

        StackPane card = new StackPane();
        card.setPrefSize(CARD_SIZE, CARD_SIZE + 30); // Extra height for label

        // Background for the card
        Rectangle cardBackground = new Rectangle(CARD_SIZE, CARD_SIZE + 30);
        cardBackground.setFill(Color.WHITE);
        cardBackground.setStroke(Color.LIGHTGRAY);
        cardBackground.setStrokeWidth(1);
        cardBackground.setArcWidth(10);
        cardBackground.setArcHeight(10);

        // Selection indicator
        Rectangle selectionBorder = new Rectangle(CARD_SIZE, CARD_SIZE + 30);
        selectionBorder.setFill(Color.TRANSPARENT);
        selectionBorder.setStroke(Color.BLUE);
        selectionBorder.setStrokeWidth(3);
        selectionBorder.setArcWidth(10);
        selectionBorder.setArcHeight(10);
        selectionBorder.setVisible(wallpaper.getFilename().equals(currentSelection));

        // Thumbnail image
        ImageView thumbnail = BackgroundManager.createThumbnailImageView(
                wallpaper.getFilename(), THUMBNAIL_SIZE);

        // Label
        Label nameLabel = new Label(wallpaper.getDisplayName());
        nameLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, 12));
        nameLabel.setTextFill(Color.DARKSLATEGRAY);

        // Layout container for image and label
        VBox content = new VBox(8);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(thumbnail, nameLabel);

        // Add click handler
        card.setOnMouseClicked(e -> {
            // Update selection
            currentSelection = wallpaper.getFilename();
            updateSelectionIndicators(card.getParent(), wallpaper.getFilename());
        });

        // Hover effect
        card.setOnMouseEntered(e -> {
            if (!wallpaper.getFilename().equals(currentSelection)) {
                cardBackground.setFill(Color.web("#f0f8ff"));
            }
        });

        card.setOnMouseExited(e -> {
            if (!wallpaper.getFilename().equals(currentSelection)) {
                cardBackground.setFill(Color.WHITE);
            }
        });

        card.getChildren().addAll(cardBackground, content, selectionBorder);

        // Store reference to selection border for updates
        card.setUserData(new CardData(wallpaper.getFilename(), selectionBorder, cardBackground));

        return card;
    }

    private void updateSelectionIndicators(javafx.scene.Parent gridParent, String selectedFilename) {
        if (gridParent instanceof GridPane) {
            GridPane grid = (GridPane) gridParent;

            for (javafx.scene.Node node : grid.getChildren()) {
                if (node instanceof StackPane) {
                    StackPane card = (StackPane) node;
                    CardData cardData = (CardData) card.getUserData();

                    if (cardData != null) {
                        boolean isSelected = cardData.filename.equals(selectedFilename);
                        cardData.selectionBorder.setVisible(isSelected);
                        cardData.background.setFill(isSelected ? Color.web("#e6f3ff") : Color.WHITE);
                    }
                }
            }
        }
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button applyButton = new Button("Apply");
        applyButton.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 14));
        applyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-padding: 10px 20px; -fx-background-radius: 5px;");
        applyButton.setPrefWidth(100);

        Button cancelButton = new Button("Cancel");
        cancelButton.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, 14));
        cancelButton.setStyle("-fx-background-color: #757575; -fx-text-fill: white; " +
                "-fx-padding: 10px 20px; -fx-background-radius: 5px;");
        cancelButton.setPrefWidth(100);

        // Button actions
        applyButton.setOnAction(e -> {
            userPreferences.setSelectedWallpaper(currentSelection);
            if (onWallpaperSelected != null) {
                onWallpaperSelected.accept(currentSelection);
            }
            dialog.close();
        });

        cancelButton.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(applyButton, cancelButton);

        return buttonBox;
    }

    public void show() {
        dialog.showAndWait();
    }

    /**
     * Helper class to store card data for selection updates.
     */
    private static class CardData {
        final String filename;
        final Rectangle selectionBorder;
        final Rectangle background;

        CardData(String filename, Rectangle selectionBorder, Rectangle background) {
            this.filename = filename;
            this.selectionBorder = selectionBorder;
            this.background = background;
        }
    }
}