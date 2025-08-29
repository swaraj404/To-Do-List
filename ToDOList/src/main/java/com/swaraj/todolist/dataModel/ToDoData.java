package com.swaraj.todolist.dataModel;

import com.swaraj.todolist.services.DatabaseService;
import com.swaraj.todolist.services.ExportImportService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.function.Predicate;

/**
 * Enhanced ToDoData class with database persistence and advanced filtering
 */
public class ToDoData {
    private static ToDoData instance = new ToDoData();
    private ObservableList<ToDoItem> toDoItems;
    private FilteredList<ToDoItem> filteredItems;
    private SortedList<ToDoItem> sortedItems;
    private DatabaseService databaseService;
    
    // Filter predicates
    private Predicate<ToDoItem> showAllItems = item -> true;
    private Predicate<ToDoItem> showTodayItems = item -> 
        !item.isCompleted() && item.getDeadline() != null && 
        item.getDeadline().toLocalDate().equals(LocalDateTime.now().toLocalDate());
    private Predicate<ToDoItem> showOverdueItems = item -> 
        !item.isCompleted() && item.isOverdue();
    private Predicate<ToDoItem> showCompletedItems = item -> item.isCompleted();
    private Predicate<ToDoItem> showPendingItems = item -> !item.isCompleted();
    
    private ToDoData() {
        databaseService = DatabaseService.getInstance();
        toDoItems = FXCollections.observableArrayList(
            // Add listeners for automatic property changes
            item -> new javafx.beans.Observable[] {
                item.shortDescriptionProperty(),
                item.detailsProperty(),
                item.deadlineProperty(),
                item.categoryProperty(),
                item.priorityProperty(),
                item.completedProperty()
            }
        );
        
        // Set up filtered and sorted lists
        filteredItems = new FilteredList<>(toDoItems, showAllItems);
        sortedItems = new SortedList<>(filteredItems, getDefaultComparator());
    }
    
    public static ToDoData getInstance() {
        return instance;
    }
    
    /**
     * Get the observable list of todo items
     */
    public ObservableList<ToDoItem> getToDoItems() {
        return toDoItems;
    }
    
    /**
     * Get the filtered list
     */
    public FilteredList<ToDoItem> getFilteredItems() {
        return filteredItems;
    }
    
    /**
     * Get the sorted list
     */
    public SortedList<ToDoItem> getSortedItems() {
        return sortedItems;
    }
    
    /**
     * Add a new todo item
     */
    public void addToDoItem(ToDoItem item) {
        toDoItems.add(item);
        databaseService.saveTodoItem(item);
    }
    
    /**
     * Update an existing todo item
     */
    public void updateToDoItem(ToDoItem item) {
        databaseService.saveTodoItem(item);
    }
    
    /**
     * Delete a todo item
     */
    public void deleteToDoItem(ToDoItem item) {
        toDoItems.remove(item);
        databaseService.deleteTodoItem(item.getId());
    }
    
    /**
     * Load todo items from database
     */
    public void loadToDoItems() throws IOException {
        try {
            ObservableList<ToDoItem> loadedItems = databaseService.loadTodoItems();
            toDoItems.clear();
            toDoItems.addAll(loadedItems);
        } catch (Exception e) {
            throw new IOException("Failed to load todo items from database", e);
        }
    }
    
    /**
     * Store todo items to database
     */
    public void storeToDoItems() throws IOException {
        try {
            // Items are automatically saved when added/updated, but we can force save all
            for (ToDoItem item : toDoItems) {
                databaseService.saveTodoItem(item);
            }
        } catch (Exception e) {
            throw new IOException("Failed to store todo items to database", e);
        }
    }
    
    /**
     * Search items by text
     */
    public void searchItems(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredItems.setPredicate(showAllItems);
        } else {
            String lowerCaseSearch = searchText.toLowerCase();
            filteredItems.setPredicate(item -> 
                item.getShortDescription().toLowerCase().contains(lowerCaseSearch) ||
                item.getDetails().toLowerCase().contains(lowerCaseSearch)
            );
        }
    }
    
    /**
     * Filter by category
     */
    public void filterByCategory(ToDoItem.Category category) {
        if (category == null) {
            filteredItems.setPredicate(showAllItems);
        } else {
            filteredItems.setPredicate(item -> item.getCategory() == category);
        }
    }
    
    /**
     * Filter by priority
     */
    public void filterByPriority(ToDoItem.Priority priority) {
        if (priority == null) {
            filteredItems.setPredicate(showAllItems);
        } else {
            filteredItems.setPredicate(item -> item.getPriority() == priority);
        }
    }
    
    /**
     * Filter by completion status
     */
    public void filterByStatus(String status) {
        switch (status.toLowerCase()) {
            case "completed" -> filteredItems.setPredicate(showCompletedItems);
            case "pending" -> filteredItems.setPredicate(showPendingItems);
            case "overdue" -> filteredItems.setPredicate(showOverdueItems);
            case "today" -> filteredItems.setPredicate(showTodayItems);
            default -> filteredItems.setPredicate(showAllItems);
        }
    }
    
    /**
     * Apply custom filter
     */
    public void applyFilter(Predicate<ToDoItem> filter) {
        filteredItems.setPredicate(filter);
    }
    
    /**
     * Clear all filters
     */
    public void clearFilters() {
        filteredItems.setPredicate(showAllItems);
    }
    
    /**
     * Sort by different criteria
     */
    public void sortBy(String criteria, boolean ascending) {
        Comparator<ToDoItem> comparator = getComparator(criteria);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        sortedItems.setComparator(comparator);
    }
    
    /**
     * Get comparator for sorting criteria
     */
    private Comparator<ToDoItem> getComparator(String criteria) {
        return switch (criteria.toLowerCase()) {
            case "deadline" -> Comparator.comparing(ToDoItem::getDeadline, 
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "priority" -> Comparator.comparing(item -> item.getPriority().getValue(), 
                Comparator.reverseOrder());
            case "category" -> Comparator.comparing(item -> item.getCategory().getDisplayName());
            case "created" -> Comparator.comparing(ToDoItem::getCreatedDate);
            case "points" -> Comparator.comparing(ToDoItem::getPoints, Comparator.reverseOrder());
            case "title" -> Comparator.comparing(ToDoItem::getShortDescription, 
                String.CASE_INSENSITIVE_ORDER);
            default -> getDefaultComparator();
        };
    }
    
    /**
     * Get default comparator (uncompleted first, then by priority, then by deadline)
     */
    private Comparator<ToDoItem> getDefaultComparator() {
        return Comparator
            .comparing(ToDoItem::isCompleted) // Completed tasks last
            .thenComparing(item -> item.getPriority().getValue(), Comparator.reverseOrder()) // High priority first
            .thenComparing(ToDoItem::getDeadline, Comparator.nullsLast(Comparator.naturalOrder())); // Earliest deadline first
    }
    
    /**
     * Get statistics
     */
    public DatabaseService.TaskStatistics getStatistics() {
        return databaseService.getTaskStatistics();
    }
    
    /**
     * Create backup
     */
    public void createBackup() {
        ExportImportService.getInstance().createBackup(toDoItems);
    }
    
    /**
     * Mark task as completed and award points
     */
    public void completeTask(ToDoItem item) {
        if (!item.isCompleted()) {
            item.setCompleted(true);
            updateToDoItem(item);
            
            // Award points through configuration service
            com.swaraj.todolist.services.ConfigurationService config = 
                com.swaraj.todolist.services.ConfigurationService.getInstance();
            boolean leveledUp = config.addXP(item.getPoints());
            
            // Show notifications
            com.swaraj.todolist.services.NotificationService notificationService = 
                com.swaraj.todolist.services.NotificationService.getInstance();
            notificationService.showTaskCompletedNotification(item);
            
            if (leveledUp) {
                notificationService.showLevelUpNotification(config.getPlayerLevel());
            }
        }
    }
}
