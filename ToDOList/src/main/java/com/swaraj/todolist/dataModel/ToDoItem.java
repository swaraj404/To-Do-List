package com.swaraj.todolist.dataModel;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Enhanced ToDoItem with categories, priority levels, and completion tracking
 */
public class ToDoItem {
    
    // Enums for categories and priorities
    public enum Category {
        WORK("Work", "#3498db"),
        PERSONAL("Personal", "#e74c3c"),
        SHOPPING("Shopping", "#2ecc71"),
        HEALTH("Health", "#f39c12"),
        EDUCATION("Education", "#9b59b6"),
        OTHER("Other", "#95a5a6");
        
        private final String displayName;
        private final String color;
        
        Category(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }
    
    public enum Priority {
        LOW("Low", 1, "#27ae60"),
        MEDIUM("Medium", 2, "#f39c12"),
        HIGH("High", 3, "#e74c3c"),
        URGENT("Urgent", 4, "#8e44ad");
        
        private final String displayName;
        private final int value;
        private final String color;
        
        Priority(String displayName, int value, String color) {
            this.displayName = displayName;
            this.value = value;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public int getValue() { return value; }
        public String getColor() { return color; }
    }
    
    // Properties for JavaFX binding
    private StringProperty shortDescription;
    private StringProperty details;
    private ObjectProperty<LocalDateTime> deadline;
    private ObjectProperty<Category> category;
    private ObjectProperty<Priority> priority;
    private BooleanProperty completed;
    private ObjectProperty<LocalDateTime> createdDate;
    private ObjectProperty<LocalDateTime> completedDate;
    private IntegerProperty points;
    private LongProperty id;
    
    // Default constructor for JSON deserialization
    public ToDoItem() {
        this("", "", LocalDateTime.now().plusDays(1), Category.OTHER, Priority.MEDIUM);
    }
    
    public ToDoItem(String shortDescription, String details, LocalDateTime deadline) {
        this(shortDescription, details, deadline, Category.OTHER, Priority.MEDIUM);
    }
    
    public ToDoItem(String shortDescription, String details, LocalDateTime deadline, 
                   Category category, Priority priority) {
        this.shortDescription = new SimpleStringProperty(shortDescription);
        this.details = new SimpleStringProperty(details);
        this.deadline = new SimpleObjectProperty<>(deadline);
        this.category = new SimpleObjectProperty<>(category);
        this.priority = new SimpleObjectProperty<>(priority);
        this.completed = new SimpleBooleanProperty(false);
        this.createdDate = new SimpleObjectProperty<>(LocalDateTime.now());
        this.completedDate = new SimpleObjectProperty<>();
        this.points = new SimpleIntegerProperty(calculatePoints());
        this.id = new SimpleLongProperty(System.currentTimeMillis());
    }
    
    /**
     * Calculate points based on priority and completion within deadline
     */
    private int calculatePoints() {
        int basePoints = priority.get() != null ? priority.get().getValue() * 10 : 10;
        if (completed.get() && completedDate.get() != null && 
            deadline.get() != null && !completedDate.get().isAfter(deadline.get())) {
            basePoints *= 2; // Double points for completing on time
        }
        return basePoints;
    }
    
    // Property getters for JavaFX binding
    public StringProperty shortDescriptionProperty() { return shortDescription; }
    public StringProperty detailsProperty() { return details; }
    public ObjectProperty<LocalDateTime> deadlineProperty() { return deadline; }
    public ObjectProperty<Category> categoryProperty() { return category; }
    public ObjectProperty<Priority> priorityProperty() { return priority; }
    public BooleanProperty completedProperty() { return completed; }
    public ObjectProperty<LocalDateTime> createdDateProperty() { return createdDate; }
    public ObjectProperty<LocalDateTime> completedDateProperty() { return completedDate; }
    public IntegerProperty pointsProperty() { return points; }
    public LongProperty idProperty() { return id; }
    
    // Getters and setters
    public String getShortDescription() { return shortDescription.get(); }
    public void setShortDescription(String shortDescription) { this.shortDescription.set(shortDescription); }
    
    public String getDetails() { return details.get(); }
    public void setDetails(String details) { this.details.set(details); }
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime getDeadline() { return deadline.get(); }
    public void setDeadline(LocalDateTime deadline) { 
        this.deadline.set(deadline);
        updatePoints();
    }
    
    public Category getCategory() { return category.get(); }
    public void setCategory(Category category) { this.category.set(category); }
    
    public Priority getPriority() { return priority.get(); }
    public void setPriority(Priority priority) { 
        this.priority.set(priority);
        updatePoints();
    }
    
    public boolean isCompleted() { return completed.get(); }
    public void setCompleted(boolean completed) { 
        this.completed.set(completed);
        if (completed && completedDate.get() == null) {
            this.completedDate.set(LocalDateTime.now());
        } else if (!completed) {
            this.completedDate.set(null);
        }
        updatePoints();
    }
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime getCreatedDate() { return createdDate.get(); }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate.set(createdDate); }
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime getCompletedDate() { return completedDate.get(); }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate.set(completedDate); }
    
    public int getPoints() { return points.get(); }
    public void setPoints(int points) { this.points.set(points); }
    
    public long getId() { return id.get(); }
    public void setId(long id) { this.id.set(id); }
    
    private void updatePoints() {
        this.points.set(calculatePoints());
    }
    
    /**
     * Check if task is overdue
     */
    @JsonIgnore
    public boolean isOverdue() {
        return !completed.get() && deadline.get() != null && 
               LocalDateTime.now().isAfter(deadline.get());
    }
    
    /**
     * Check if task is due soon (within 24 hours)
     */
    @JsonIgnore
    public boolean isDueSoon() {
        return !completed.get() && deadline.get() != null && 
               LocalDateTime.now().plusHours(24).isAfter(deadline.get()) &&
               LocalDateTime.now().isBefore(deadline.get());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToDoItem toDoItem = (ToDoItem) o;
        return Objects.equals(getId(), toDoItem.getId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
    
    @Override
    public String toString() {
        return getShortDescription();
    }
}
