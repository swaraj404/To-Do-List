package com.swaraj.todolist;

import com.swaraj.todolist.services.DatabaseService;
import com.swaraj.todolist.services.ConfigurationService;
import com.swaraj.todolist.services.NotificationService;
import com.swaraj.todolist.utils.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    
    private DatabaseService databaseService;
    private ConfigurationService configService;
    private NotificationService notificationService;
    private ThemeManager themeManager;
    
    @Override
    public void start(Stage stage) throws IOException {
        // Initialize services
        initializeServices();
        
        // Load FXML
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("mainWindow.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        
        // Apply stylesheet
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        
        // Apply saved theme
        ThemeManager.applyTheme(scene);
        
        // Configure stage
        stage.setTitle("Professional To-Do List Manager");
        stage.setScene(scene);
        
        // Restore window position and size
        configService.restoreWindowState(stage);
        
        // Set up window state saving on close
        stage.setOnCloseRequest(event -> {
            configService.saveWindowState(stage);
            shutdown();
        });
        
        stage.show();
    }
    
    private void initializeServices() {
        try {
            // Initialize database
            databaseService = DatabaseService.getInstance();
            
            // Initialize configuration
            configService = ConfigurationService.getInstance();
            
            // Initialize theme manager
            themeManager = ThemeManager.getInstance();
            
            // Initialize notification service
            notificationService = NotificationService.getInstance();
            notificationService.startNotificationChecker();
            
        } catch (Exception e) {
            System.err.println("Error initializing services: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void shutdown() {
        try {
            // Stop notification service
            if (notificationService != null) {
                notificationService.shutdown();
            }
            
            // Save configuration
            if (configService != null) {
                configService.saveConfiguration();
            }
            
            // Close database connection
            if (databaseService != null) {
                databaseService.close();
            }
            
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop() throws Exception {
        shutdown();
        super.stop();
    }
}