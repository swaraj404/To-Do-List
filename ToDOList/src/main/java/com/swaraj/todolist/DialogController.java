package com.swaraj.todolist;

import com.swaraj.todolist.services.DatabaseService;
import com.swaraj.todolist.dataModel.ToDoItem;
import com.swaraj.todolist.utils.NaturalLanguageDateParser;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDateTime;

/**
 * Enhanced dialog controller with natural language parsing and advanced features
 */
public class DialogController {
    @FXML
    private TextField shortDescriptionField;
    @FXML
    private TextArea detailsArea;
    @FXML
    private TextField naturalDateField;
    @FXML
    private DatePicker deadlinePicker;
    @FXML
    private Spinner<Integer> hourSpinner;
    @FXML
    private Spinner<Integer> minuteSpinner;
    @FXML
    private ComboBox<ToDoItem.Category> categoryComboBox;
    @FXML
    private ComboBox<ToDoItem.Priority> priorityComboBox;
    @FXML
    private CheckBox completedCheckBox;
    @FXML
    private Label pointsLabel;
    
    private ToDoItem editingItem = null;
    
    /**
     * Initialize the dialog
     */
    public void initialize() {
        // Set up category combo box
        if (categoryComboBox != null) {
            categoryComboBox.getItems().addAll(ToDoItem.Category.values());
            categoryComboBox.setValue(ToDoItem.Category.OTHER);
        }
        
        // Set up priority combo box
        if (priorityComboBox != null) {
            priorityComboBox.getItems().addAll(ToDoItem.Priority.values());
            priorityComboBox.setValue(ToDoItem.Priority.MEDIUM);
        }
        
        // Set up time spinners
        if (hourSpinner != null) {
            hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
        }
        if (minuteSpinner != null) {
            minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        }
        
        // Set up natural language field listener
        if (naturalDateField != null) {
            naturalDateField.textProperty().addListener((obs, oldText, newText) -> {
                if (newText != null && !newText.trim().isEmpty()) {
                    try {
                        LocalDateTime parsed = NaturalLanguageDateParser.parseDateTime(newText);
                        if (deadlinePicker != null) {
                            deadlinePicker.setValue(parsed.toLocalDate());
                        }
                        if (hourSpinner != null) {
                            hourSpinner.getValueFactory().setValue(parsed.getHour());
                        }
                        if (minuteSpinner != null) {
                            minuteSpinner.getValueFactory().setValue(parsed.getMinute());
                        }
                        
                        // Auto-extract task description if natural language contains task info
                        String extractedDescription = NaturalLanguageDateParser.extractTaskDescription(newText);
                        if (!extractedDescription.isEmpty() && shortDescriptionField != null && shortDescriptionField.getText().trim().isEmpty()) {
                            shortDescriptionField.setText(extractedDescription);
                        }
                    } catch (Exception e) {
                        // Ignore parsing errors - user might still be typing
                    }
                }
            });
        }
        
        // Update points when priority changes
        if (priorityComboBox != null) {
            priorityComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updatePointsDisplay());
        }
        if (completedCheckBox != null) {
            completedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> updatePointsDisplay());
        }
        
        updatePointsDisplay();
    }
    
    /**
     * Set item for editing
     */
    public void setEditingItem(ToDoItem item) {
        this.editingItem = item;
        
        if (item != null) {
            shortDescriptionField.setText(item.getShortDescription());
            detailsArea.setText(item.getDetails());
            
            if (item.getDeadline() != null) {
                deadlinePicker.setValue(item.getDeadline().toLocalDate());
                hourSpinner.getValueFactory().setValue(item.getDeadline().getHour());
                minuteSpinner.getValueFactory().setValue(item.getDeadline().getMinute());
            }
            
            categoryComboBox.setValue(item.getCategory());
            priorityComboBox.setValue(item.getPriority());
            completedCheckBox.setSelected(item.isCompleted());
            
            updatePointsDisplay();
        }
    }
    
    /**
     * Process the dialog results
     */
    public ToDoItem processResults() {
        String shortDescription = shortDescriptionField.getText().trim();
        String details = detailsArea.getText().trim();
        
        // Validate required fields
        if (shortDescription.isEmpty()) {
            showAlert("Validation Error", "Short description is required.");
            return null;
        }
        
        LocalDateTime deadline = null;
        if (deadlinePicker.getValue() != null) {
            int hour = hourSpinner.getValue();
            int minute = minuteSpinner.getValue();
            deadline = LocalDateTime.of(deadlinePicker.getValue(), 
                java.time.LocalTime.of(hour, minute));
        }
        
        ToDoItem.Category category = categoryComboBox.getValue();
        ToDoItem.Priority priority = priorityComboBox.getValue();
        boolean completed = completedCheckBox.isSelected();
        
        ToDoItem item;
        DatabaseService databaseService = DatabaseService.getInstance();
        
        if (editingItem != null) {
            // Update existing item
            item = editingItem;
            item.setShortDescription(shortDescription);
            item.setDetails(details);
            item.setDeadline(deadline);
            item.setCategory(category);
            item.setPriority(priority);
            
            boolean wasCompleted = item.isCompleted();
            item.setCompleted(completed);
            
            // Save the updated item
            databaseService.saveTodoItem(item);
        } else {
            // Create new item
            item = new ToDoItem(shortDescription, details, deadline, category, priority);
            item.setCompleted(completed);
            
            // Save the new item
            databaseService.saveTodoItem(item);
        }
        
        return item;
    }
    
    /**
     * Update points display based on current settings
     */
    private void updatePointsDisplay() {
        if (priorityComboBox != null && completedCheckBox != null && pointsLabel != null) {
            ToDoItem.Priority priority = priorityComboBox.getValue();
            boolean completed = completedCheckBox.isSelected();
            
            if (priority != null) {
                int basePoints = priority.getValue() * 10;
                if (completed) {
                    basePoints *= 2; // Double points for completion
                }
                pointsLabel.setText("Points: " + basePoints);
            } else {
                pointsLabel.setText("Points: 0");
            }
        }
    }
    
    /**
     * Show alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Parse natural language input
     */
    @FXML
    private void parseNaturalLanguage() {
        String input = naturalDateField.getText();
        if (input != null && !input.trim().isEmpty()) {
            try {
                LocalDateTime parsed = NaturalLanguageDateParser.parseDateTime(input);
                deadlinePicker.setValue(parsed.toLocalDate());
                hourSpinner.getValueFactory().setValue(parsed.getHour());
                minuteSpinner.getValueFactory().setValue(parsed.getMinute());
                
                // Show success message
                showInfo("Parsed Successfully", 
                    "Deadline set to: " + parsed.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")));
            } catch (Exception e) {
                showAlert("Parse Error", "Could not understand the date/time. Please try a different format.");
            }
        }
    }
    
    /**
     * Show info dialog
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Clear all fields
     */
    @FXML
    private void clearFields() {
        shortDescriptionField.clear();
        detailsArea.clear();
        naturalDateField.clear();
        deadlinePicker.setValue(null);
        hourSpinner.getValueFactory().setValue(9);
        minuteSpinner.getValueFactory().setValue(0);
        categoryComboBox.setValue(ToDoItem.Category.OTHER);
        priorityComboBox.setValue(ToDoItem.Priority.MEDIUM);
        completedCheckBox.setSelected(false);
        updatePointsDisplay();
    }
}

