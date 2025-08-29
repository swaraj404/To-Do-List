package com.swaraj.todolist.utils;

import com.swaraj.todolist.services.ConfigurationService;
import javafx.scene.Scene;

/**
 * Utility class for managing application themes (light/dark mode)
 */
public class ThemeManager {
    
    private static ThemeManager instance;
    
    private static final String LIGHT_THEME_CSS = """
        .root {
            -fx-base: #ffffff;
            -fx-background: #f4f4f4;
            -fx-control-inner-background: #ffffff;
            -fx-control-inner-background-alt: #f8f8f8;
            -fx-accent: #0096c9;
            -fx-default-button: #0096c9;
            -fx-focus-color: #0096c9;
            -fx-faint-focus-color: #0096c922;
            -fx-text-fill: #333333;
        }
        
        .list-view {
            -fx-background-color: #ffffff;
            -fx-border-color: #dddddd;
            -fx-border-width: 1px;
        }
        
        .list-cell {
            -fx-background-color: #ffffff;
            -fx-text-fill: #333333;
        }
        
        .list-cell:selected {
            -fx-background-color: #0096c9;
            -fx-text-fill: #ffffff;
        }
        
        .text-area {
            -fx-background-color: #ffffff;
            -fx-text-fill: #333333;
            -fx-border-color: #dddddd;
        }
        
        .button {
            -fx-background-color: #0096c9;
            -fx-text-fill: #ffffff;
            -fx-background-radius: 4px;
        }
        
        .button:hover {
            -fx-background-color: #007ba7;
        }
        
        .menu-bar {
            -fx-background-color: #f8f8f8;
        }
        
        .tool-bar {
            -fx-background-color: #f0f0f0;
        }
        
        .priority-low { -fx-text-fill: #27ae60; }
        .priority-medium { -fx-text-fill: #f39c12; }
        .priority-high { -fx-text-fill: #e74c3c; }
        .priority-urgent { -fx-text-fill: #8e44ad; }
        
        .category-work { -fx-background-color: #3498db22; }
        .category-personal { -fx-background-color: #e74c3c22; }
        .category-shopping { -fx-background-color: #2ecc7122; }
        .category-health { -fx-background-color: #f39c1222; }
        .category-education { -fx-background-color: #9b59b622; }
        .category-other { -fx-background-color: #95a5a622; }
        """;
    
    private static final String DARK_THEME_CSS = """
        .root {
            -fx-base: #2b2b2b;
            -fx-background: #1e1e1e;
            -fx-control-inner-background: #3c3c3c;
            -fx-control-inner-background-alt: #454545;
            -fx-accent: #00bcd4;
            -fx-default-button: #00bcd4;
            -fx-focus-color: #00bcd4;
            -fx-faint-focus-color: #00bcd422;
            -fx-text-fill: #e0e0e0;
        }
        
        .list-view {
            -fx-background-color: #3c3c3c;
            -fx-border-color: #555555;
            -fx-border-width: 1px;
        }
        
        .list-cell {
            -fx-background-color: #3c3c3c;
            -fx-text-fill: #e0e0e0;
        }
        
        .list-cell:selected {
            -fx-background-color: #00bcd4;
            -fx-text-fill: #ffffff;
        }
        
        .text-area {
            -fx-background-color: #3c3c3c;
            -fx-text-fill: #e0e0e0;
            -fx-border-color: #555555;
        }
        
        .button {
            -fx-background-color: #00bcd4;
            -fx-text-fill: #ffffff;
            -fx-background-radius: 4px;
        }
        
        .button:hover {
            -fx-background-color: #00acc1;
        }
        
        .menu-bar {
            -fx-background-color: #2b2b2b;
        }
        
        .tool-bar {
            -fx-background-color: #333333;
        }
        
        .label {
            -fx-text-fill: #e0e0e0;
        }
        
        .priority-low { -fx-text-fill: #4caf50; }
        .priority-medium { -fx-text-fill: #ff9800; }
        .priority-high { -fx-text-fill: #f44336; }
        .priority-urgent { -fx-text-fill: #e91e63; }
        
        .category-work { -fx-background-color: #3498db33; }
        .category-personal { -fx-background-color: #e74c3c33; }
        .category-shopping { -fx-background-color: #2ecc7133; }
        .category-health { -fx-background-color: #f39c1233; }
        .category-education { -fx-background-color: #9b59b633; }
        .category-other { -fx-background-color: #95a5a633; }
        """;
    
    private ThemeManager() {}
    
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    /**
     * Apply theme to a scene
     */
    public static void applyTheme(Scene scene) {
        ConfigurationService config = ConfigurationService.getInstance();
        ThemeManager instance = getInstance();
        if (config.isDarkTheme()) {
            instance.applyDarkTheme(scene);
        } else {
            instance.applyLightTheme(scene);
        }
    }
    
    private void applyLightTheme(Scene scene) {
        scene.getRoot().setStyle(LIGHT_THEME_CSS);
    }
    
    private void applyDarkTheme(Scene scene) {
        scene.getRoot().setStyle(DARK_THEME_CSS);
    }
    
    /**
     * Toggle between light and dark theme
     */
    public static void toggleTheme(Scene scene) {
        ConfigurationService config = ConfigurationService.getInstance();
        String currentTheme = config.getTheme();
        String newTheme = "light".equals(currentTheme) ? "dark" : "light";
        
        config.setTheme(newTheme);
        applyTheme(scene);
    }
    
    /**
     * Get CSS class for priority
     */
    public static String getPriorityClass(String priority) {
        return "priority-" + priority.toLowerCase();
    }
    
    /**
     * Get CSS class for category
     */
    public static String getCategoryClass(String category) {
        return "category-" + category.toLowerCase();
    }
    
    /**
     * Get color for priority
     */
    public static String getPriorityColor(String priority) {
        return switch (priority.toUpperCase()) {
            case "LOW" -> "#27ae60";
            case "MEDIUM" -> "#f39c12";
            case "HIGH" -> "#e74c3c";
            case "URGENT" -> "#8e44ad";
            default -> "#95a5a6";
        };
    }
    
    /**
     * Get color for category
     */
    public static String getCategoryColor(String category) {
        return switch (category.toUpperCase()) {
            case "WORK" -> "#3498db";
            case "PERSONAL" -> "#e74c3c";
            case "SHOPPING" -> "#2ecc71";
            case "HEALTH" -> "#f39c12";
            case "EDUCATION" -> "#9b59b6";
            default -> "#95a5a6";
        };
    }
}
