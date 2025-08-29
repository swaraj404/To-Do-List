package com.swaraj.todolist.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration service for saving user preferences like theme, window size, etc.
 */
public class ConfigurationService {
    private static final String CONFIG_FILE = "config.json";
    private static ConfigurationService instance;
    private final ObjectMapper objectMapper;
    private Map<String, Object> config;
    
    private ConfigurationService() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        loadConfiguration();
    }
    
    public static ConfigurationService getInstance() {
        if (instance == null) {
            instance = new ConfigurationService();
        }
        return instance;
    }
    
    /**
     * Load configuration from file
     */
    private void loadConfiguration() {
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                config = objectMapper.readValue(configFile, HashMap.class);
            } else {
                config = getDefaultConfiguration();
            }
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            config = getDefaultConfiguration();
        }
    }
    
    /**
     * Save configuration to file
     */
    public void saveConfiguration() {
        try {
            objectMapper.writeValue(new File(CONFIG_FILE), config);
        } catch (IOException e) {
            System.err.println("Error saving configuration: " + e.getMessage());
        }
    }
    
    /**
     * Get default configuration
     */
    private Map<String, Object> getDefaultConfiguration() {
        Map<String, Object> defaultConfig = new HashMap<>();
        defaultConfig.put("theme", "light");
        defaultConfig.put("windowWidth", 1000.0);
        defaultConfig.put("windowHeight", 700.0);
        defaultConfig.put("windowX", 100.0);
        defaultConfig.put("windowY", 100.0);
        defaultConfig.put("notificationsEnabled", true);
        defaultConfig.put("autoSave", true);
        defaultConfig.put("defaultCategory", "OTHER");
        defaultConfig.put("defaultPriority", "MEDIUM");
        defaultConfig.put("playerLevel", 1);
        defaultConfig.put("playerXP", 0);
        defaultConfig.put("totalPoints", 0);
        return defaultConfig;
    }
    
    /**
     * Get a configuration value
     */
    public Object get(String key) {
        return config.get(key);
    }
    
    /**
     * Get a configuration value with default
     */
    public Object get(String key, Object defaultValue) {
        return config.getOrDefault(key, defaultValue);
    }
    
    /**
     * Set a configuration value
     */
    public void set(String key, Object value) {
        config.put(key, value);
    }
    
    /**
     * Get theme (light/dark)
     */
    public String getTheme() {
        return (String) get("theme", "light");
    }
    
    /**
     * Set theme
     */
    public void setTheme(String theme) {
        set("theme", theme);
        saveConfiguration();
    }
    
    /**
     * Check if dark mode is enabled
     */
    public boolean isDarkMode() {
        return "dark".equals(getTheme());
    }
    
    /**
     * Get window width
     */
    public double getWindowWidth() {
        return ((Number) get("windowWidth", 1000.0)).doubleValue();
    }
    
    /**
     * Get window height
     */
    public double getWindowHeight() {
        return ((Number) get("windowHeight", 700.0)).doubleValue();
    }
    
    /**
     * Set window dimensions
     */
    public void setWindowDimensions(double width, double height) {
        set("windowWidth", width);
        set("windowHeight", height);
        saveConfiguration();
    }
    
    /**
     * Get window X position
     */
    public double getWindowX() {
        return ((Number) get("windowX", 100.0)).doubleValue();
    }
    
    /**
     * Get window Y position
     */
    public double getWindowY() {
        return ((Number) get("windowY", 100.0)).doubleValue();
    }
    
    /**
     * Set window position
     */
    public void setWindowPosition(double x, double y) {
        set("windowX", x);
        set("windowY", y);
        saveConfiguration();
    }
    
    /**
     * Check if notifications are enabled
     */
    public boolean areNotificationsEnabled() {
        return (Boolean) get("notificationsEnabled", true);
    }
    
    /**
     * Set notifications enabled
     */
    public void setNotificationsEnabled(boolean enabled) {
        set("notificationsEnabled", enabled);
        saveConfiguration();
    }
    
    /**
     * Get player level for gamification
     */
    public int getPlayerLevel() {
        return ((Number) get("playerLevel", 1)).intValue();
    }
    
    /**
     * Get player XP for gamification
     */
    public int getPlayerXP() {
        return ((Number) get("playerXP", 0)).intValue();
    }
    
    /**
     * Add XP and check for level up
     */
    public boolean addXP(int xp) {
        int currentXP = getPlayerXP();
        int currentLevel = getPlayerLevel();
        int newXP = currentXP + xp;
        
        set("playerXP", newXP);
        
        // Calculate level (100 XP per level, increasing by 50 each level)
        int requiredXP = calculateRequiredXP(currentLevel);
        if (newXP >= requiredXP) {
            set("playerLevel", currentLevel + 1);
            set("playerXP", newXP - requiredXP);
            saveConfiguration();
            return true; // Level up occurred
        }
        
        saveConfiguration();
        return false; // No level up
    }
    
    /**
     * Calculate XP required for next level
     */
    public int calculateRequiredXP(int level) {
        return 100 + (level - 1) * 50;
    }
    
    /**
     * Get total points earned
     */
    public int getTotalPoints() {
        return ((Number) get("totalPoints", 0)).intValue();
    }
    
    /**
     * Add points to total
     */
    public void addPoints(int points) {
        int current = getTotalPoints();
        set("totalPoints", current + points);
        addXP(points); // XP equals points earned
        saveConfiguration();
    }

    public boolean isDarkTheme() {
        return isDarkMode();
    }

    public void restoreWindowState(Stage stage) {
        stage.setX(getWindowX());
        stage.setY(getWindowY());
        stage.setWidth(getWindowWidth());
        stage.setHeight(getWindowHeight());
    }

    public void saveWindowState(Stage stage) {
        setWindowPosition(stage.getX(), stage.getY());
        setWindowDimensions(stage.getWidth(), stage.getHeight());
        saveConfiguration();
    }
}
