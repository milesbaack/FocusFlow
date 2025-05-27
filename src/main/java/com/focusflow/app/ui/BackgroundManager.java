package com.focusflow.app.ui;

import java.util.Arrays;
import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Manages background wallpapers for the FocusFlow application.
 * Handles loading, scaling, and switching between different wallpapers.
 * 
 * @author Miles Baack
 * @version 1.0
 */
public class BackgroundManager {

    /**
     * Represents a wallpaper option with display name and filename.
     */
    public static class WallpaperOption {
        private final String displayName;
        private final String filename;

        public WallpaperOption(String displayName, String filename) {
            this.displayName = displayName;
            this.filename = filename;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getFilename() {
            return filename;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    // Available wallpapers with user-friendly names
    private static final List<WallpaperOption> AVAILABLE_WALLPAPERS = Arrays.asList(
            new WallpaperOption("Original", "MainBG.png"),
            new WallpaperOption("Ancient Waterfall", "AncientRuinWaterfall.png"),
            new WallpaperOption("Peaceful Anime", "AnimeGirlPaperPlane.png"),
            new WallpaperOption("Blue Sunset", "blueSunset.png"),
            new WallpaperOption("Explorer's Castle", "ExplorerCastle.png"),
            new WallpaperOption("Mystic Forest", "MysticForest.png"),
            new WallpaperOption("Tranquil Waterfall", "WaterfallBackground.png"));

    private static final String RESOURCE_PATH = "/UI/";
    private static final String FALLBACK_WALLPAPER = "MainBG.png";

    /**
     * Gets the list of available wallpaper options.
     * 
     * @return List of wallpaper options
     */
    public static List<WallpaperOption> getAvailableWallpapers() {
        return AVAILABLE_WALLPAPERS;
    }

    /**
     * Loads and creates an ImageView for the specified wallpaper.
     * 
     * @param wallpaperFilename The filename of the wallpaper
     * @param width             The desired width
     * @param height            The desired height
     * @return ImageView with the loaded wallpaper
     */
    public static ImageView createBackgroundImageView(String wallpaperFilename, double width, double height) {
        Image backgroundImage = loadWallpaperImage(wallpaperFilename);

        ImageView imageView = new ImageView(backgroundImage);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false); // Stretch to fill screen
        imageView.setSmooth(true); // Enable smooth scaling

        return imageView;
    }

    /**
     * Loads a wallpaper image with fallback support.
     * 
     * @param wallpaperFilename The filename of the wallpaper to load
     * @return The loaded Image
     */
    public static Image loadWallpaperImage(String wallpaperFilename) {
        try {
            String resourcePath = RESOURCE_PATH + wallpaperFilename;

            // Try to load the requested wallpaper
            Image image = new Image(BackgroundManager.class.getResourceAsStream(resourcePath));

            // Check if image loaded successfully
            if (image.isError()) {
                System.err.println("Failed to load wallpaper: " + wallpaperFilename + ", using fallback");
                return loadFallbackImage();
            }

            return image;

        } catch (Exception e) {
            System.err.println("Error loading wallpaper " + wallpaperFilename + ": " + e.getMessage());
            return loadFallbackImage();
        }
    }

    /**
     * Loads the fallback wallpaper image.
     * 
     * @return The fallback Image
     */
    private static Image loadFallbackImage() {
        try {
            String fallbackPath = RESOURCE_PATH + FALLBACK_WALLPAPER;
            return new Image(BackgroundManager.class.getResourceAsStream(fallbackPath));
        } catch (Exception e) {
            System.err.println("Critical error: Could not load fallback wallpaper");
            // Return a simple colored background as last resort
            return createErrorImage();
        }
    }

    /**
     * Creates a simple error image as last resort.
     * 
     * @return A basic Image
     */
    private static Image createErrorImage() {
        // This creates a 1x1 pixel transparent image as emergency fallback
        // In practice, this should never be needed if resources are properly included
        return new Image(
                "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");
    }

    /**
     * Creates a thumbnail ImageView for wallpaper selection UI.
     * 
     * @param wallpaperFilename The filename of the wallpaper
     * @param thumbnailSize     The size of the thumbnail (width and height)
     * @return ImageView configured as thumbnail
     */
    public static ImageView createThumbnailImageView(String wallpaperFilename, double thumbnailSize) {
        Image backgroundImage = loadWallpaperImage(wallpaperFilename);

        ImageView thumbnail = new ImageView(backgroundImage);
        thumbnail.setFitWidth(thumbnailSize);
        thumbnail.setFitHeight(thumbnailSize);
        thumbnail.setPreserveRatio(true);
        thumbnail.setSmooth(true);

        // Add border styling
        thumbnail.setStyle("-fx-border-color: #cccccc; -fx-border-width: 2px;");

        return thumbnail;
    }

    /**
     * Finds a wallpaper option by filename.
     * 
     * @param filename The filename to search for
     * @return The WallpaperOption if found, null otherwise
     */
    public static WallpaperOption findWallpaperByFilename(String filename) {
        return AVAILABLE_WALLPAPERS.stream()
                .filter(option -> option.getFilename().equals(filename))
                .findFirst()
                .orElse(AVAILABLE_WALLPAPERS.get(0)); // Return original as default
    }
}