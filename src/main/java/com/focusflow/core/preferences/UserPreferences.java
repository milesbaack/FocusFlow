package com.focusflow.core.preferences;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Manages user preferences for the FocusFlow application.
 * Handles loading and saving user settings like selected wallpaper.
 * 
 * @author Miles Baack
 * @version 1.0
 */
public class UserPreferences {
    private static final String PREFERENCES_DIR = "focusflow";
    private static final String PREFERENCES_FILE = "user-preferences.properties";
    private static final String DEFAULT_WALLPAPER = "focusflowBG.png";
    
    private Properties properties;
    private Path preferencesPath;
    
    public UserPreferences() {
        this.properties = new Properties();
        initializePreferencesPath();
        loadPreferences();
    }
    
    /**
     * Initializes the preferences directory and file path.
     */
    private void initializePreferencesPath() {
        try {
            // Create preferences directory in user home
            Path userHome = Paths.get(System.getProperty("user.home"));
            Path preferencesDir = userHome.resolve(PREFERENCES_DIR);
            
            // Create directory if it doesn't exist
            if (!Files.exists(preferencesDir)) {
                Files.createDirectories(preferencesDir);
            }
            
            this.preferencesPath = preferencesDir.resolve(PREFERENCES_FILE);
        } catch (IOException e) {
            System.err.println("Could not create preferences directory: " + e.getMessage());
            // Fallback to current directory
            this.preferencesPath = Paths.get(PREFERENCES_FILE);
        }
    }
    
    /**
     * Loads preferences from the properties file.
     */
    private void loadPreferences() {
        if (Files.exists(preferencesPath)) {
            try (FileInputStream fis = new FileInputStream(preferencesPath.toFile())) {
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("Could not load preferences: " + e.getMessage());
                setDefaultValues();
            }
        } else {
            setDefaultValues();
        }
    }
    
    /**
     * Sets default preference values.
     */
    private void setDefaultValues() {
        properties.setProperty("wallpaper", DEFAULT_WALLPAPER);
        properties.setProperty("fullscreen", "true");
        properties.setProperty("music_enabled", "true");
    }
    
    /**
     * Saves preferences to the properties file.
     */
    public void savePreferences() {
        try (FileOutputStream fos = new FileOutputStream(preferencesPath.toFile())) {
            properties.store(fos, "FocusFlow User Preferences");
        } catch (IOException e) {
            System.err.println("Could not save preferences: " + e.getMessage());
        }
    }
    
    /**
     * Gets the selected wallpaper filename.
     * 
     * @return The wallpaper filename
     */
    public String getSelectedWallpaper() {
        return properties.getProperty("wallpaper", DEFAULT_WALLPAPER);
    }
    
    /**
     * Sets the selected wallpaper filename.
     * 
     * @param wallpaperName The wallpaper filename
     */
    public void setSelectedWallpaper(String wallpaperName) {
        properties.setProperty("wallpaper", wallpaperName);
        savePreferences();
    }
    
    /**
     * Gets whether fullscreen mode is enabled.
     * 
     * @return true if fullscreen is enabled
     */
    public boolean isFullscreenEnabled() {
        return Boolean.parseBoolean(properties.getProperty("fullscreen", "true"));
    }
    
    /**
     * Sets whether fullscreen mode is enabled.
     * 
     * @param enabled true to enable fullscreen
     */
    public void setFullscreenEnabled(boolean enabled) {
        properties.setProperty("fullscreen", String.valueOf(enabled));
        savePreferences();
    }
    
    /**
     * Gets whether background music is enabled.
     * 
     * @return true if music is enabled
     */
    public boolean isMusicEnabled() {
        return Boolean.parseBoolean(properties.getProperty("music_enabled", "true"));
    }
    
    /**
     * Sets whether background music is enabled.
     * 
     * @param enabled true to enable music
     */
    public void setMusicEnabled(boolean enabled) {
        properties.setProperty("music_enabled", String.valueOf(enabled));
        savePreferences();
    }
}