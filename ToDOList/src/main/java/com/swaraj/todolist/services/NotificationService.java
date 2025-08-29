package com.swaraj.todolist.services;

import com.swaraj.todolist.dataModel.ToDoItem;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing notifications and alerts for upcoming deadlines
 */
public class NotificationService {
    private static NotificationService instance;
    private final ScheduledExecutorService scheduler;
    private final ConfigurationService config;
    
    private NotificationService() {
        scheduler = Executors.newScheduledThreadPool(1);
        config = ConfigurationService.getInstance();
        startNotificationChecker();
    }
    
    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    
    /**
     * Start the background notification checker
     */
    public void startNotificationChecker() {
        scheduler.scheduleAtFixedRate(this::checkForDueTasks, 0, 30, TimeUnit.SECONDS);
    }
    
    /**
     * Check for tasks that are due soon or overdue
     */
    private void checkForDueTasks() {
        if (!config.areNotificationsEnabled()) {
            return;
        }
        
        try {
            List<ToDoItem> allTasks = DatabaseService.getInstance().loadTodoItems();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourFromNow = now.plusHours(1);
            
            for (ToDoItem task : allTasks) {
                if (task.isCompleted() || task.getDeadline() == null) {
                    continue;
                }
                
                LocalDateTime deadline = task.getDeadline();
                
                // Check if task is due within the next hour
                if (deadline.isAfter(now) && deadline.isBefore(oneHourFromNow)) {
                    showDueSoonNotification(task);
                }
                // Check if task is overdue
                else if (deadline.isBefore(now)) {
                    showOverdueNotification(task);
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking for due tasks: " + e.getMessage());
        }
    }
    
    /**
     * Show notification for task due soon
     */
    private void showDueSoonNotification(ToDoItem task) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Task Due Soon");
            alert.setHeaderText("⏰ Upcoming Deadline");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
            String message = String.format(
                "Task: %s\nDue: %s\nPriority: %s",
                task.getShortDescription(),
                task.getDeadline().format(formatter),
                task.getPriority().getDisplayName()
            );
            
            alert.setContentText(message);
            alert.show();
        });
    }
    
    /**
     * Show notification for overdue task
     */
    private void showOverdueNotification(ToDoItem task) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Overdue Task");
            alert.setHeaderText("🚨 Task Overdue!");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
            String message = String.format(
                "Task: %s\nWas Due: %s\nPriority: %s",
                task.getShortDescription(),
                task.getDeadline().format(formatter),
                task.getPriority().getDisplayName()
            );
            
            alert.setContentText(message);
            alert.show();
        });
    }
    
    /**
     * Show completion notification with points earned
     */
    public void showTaskCompletedNotification(ToDoItem task) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Task Completed!");
            alert.setHeaderText("🎉 Great Job!");
            
            String message = String.format(
                "Task: %s\nPoints Earned: %d\n\nKeep up the excellent work!",
                task.getShortDescription(),
                task.getPoints()
            );
            
            alert.setContentText(message);
            alert.show();
        });
    }
    
    /**
     * Show level up notification
     */
    public void showLevelUpNotification(int newLevel) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Level Up!");
            alert.setHeaderText("🌟 Congratulations!");
            
            String message = String.format(
                "You've reached Level %d!\n\nYour productivity is amazing!",
                newLevel
            );
            
            alert.setContentText(message);
            alert.show();
        });
    }
    
    /**
     * Show confirmation dialog for task deletion
     */
    public boolean showDeleteConfirmation(ToDoItem task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Task");
        alert.setHeaderText("Delete this task?");
        alert.setContentText("Task: " + task.getShortDescription() + "\n\nThis action cannot be undone.");
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Show information alert
     */
    public void showInfo(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.show();
        });
    }
    
    /**
     * Show error alert
     */
    public void showError(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.show();
        });
    }
    
    /**
     * Shutdown the notification service
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}
