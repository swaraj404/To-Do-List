package com.swaraj.todolist.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.swaraj.todolist.dataModel.ToDoItem;
import javafx.collections.ObservableList;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for exporting and importing tasks in various formats
 */
public class ExportImportService {
    private static ExportImportService instance;
    private final ObjectMapper objectMapper;
    private final DateTimeFormatter dateTimeFormatter;
    
    private ExportImportService() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }
    
    public static ExportImportService getInstance() {
        if (instance == null) {
            instance = new ExportImportService();
        }
        return instance;
    }
    
    /**
     * Export tasks to JSON file
     */
    public void exportToJSON(ObservableList<ToDoItem> tasks, File file) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, tasks);
    }
    
    /**
     * Import tasks from JSON file
     */
    public List<ToDoItem> importFromJSON(File file) throws IOException {
        ToDoItem[] tasks = objectMapper.readValue(file, ToDoItem[].class);
        List<ToDoItem> result = new ArrayList<>();
        for (ToDoItem task : tasks) {
            // Generate new ID to avoid conflicts
            task.setId(System.currentTimeMillis() + result.size());
            result.add(task);
        }
        return result;
    }
    
    /**
     * Export tasks to CSV file
     */
    public void exportToCSV(ObservableList<ToDoItem> tasks, File file) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file);
             CSVWriter csvWriter = new CSVWriter(fileWriter)) {
            
            // Write header
            String[] header = {
                "ID", "Short Description", "Details", "Deadline", "Category", 
                "Priority", "Completed", "Created Date", "Completed Date", "Points"
            };
            csvWriter.writeNext(header);
            
            // Write data
            for (ToDoItem task : tasks) {
                String[] row = {
                    String.valueOf(task.getId()),
                    task.getShortDescription(),
                    task.getDetails(),
                    task.getDeadline() != null ? task.getDeadline().format(dateTimeFormatter) : "",
                    task.getCategory().name(),
                    task.getPriority().name(),
                    String.valueOf(task.isCompleted()),
                    task.getCreatedDate().format(dateTimeFormatter),
                    task.getCompletedDate() != null ? task.getCompletedDate().format(dateTimeFormatter) : "",
                    String.valueOf(task.getPoints())
                };
                csvWriter.writeNext(row);
            }
        }
    }
    
    /**
     * Import tasks from CSV file
     */
    public List<ToDoItem> importFromCSV(File file) throws IOException, com.opencsv.exceptions.CsvValidationException {
        List<ToDoItem> tasks = new ArrayList<>();
        
        try (FileReader fileReader = new FileReader(file);
             CSVReader csvReader = new CSVReader(fileReader)) {
            
            String[] header = csvReader.readNext(); // Skip header
            if (header == null) {
                throw new IOException("CSV file is empty or invalid");
            }
            
            String[] row;
            int idCounter = 0;
            while ((row = csvReader.readNext()) != null) {
                try {
                    ToDoItem task = new ToDoItem();
                    
                    // Generate new ID to avoid conflicts
                    task.setId(System.currentTimeMillis() + idCounter++);
                    
                    if (row.length > 1) task.setShortDescription(row[1]);
                    if (row.length > 2) task.setDetails(row[2]);
                    
                    // Parse deadline
                    if (row.length > 3 && !row[3].isEmpty()) {
                        try {
                            task.setDeadline(LocalDateTime.parse(row[3], dateTimeFormatter));
                        } catch (Exception e) {
                            // Try alternative formats or skip
                            task.setDeadline(LocalDateTime.now().plusDays(1));
                        }
                    }
                    
                    // Parse category
                    if (row.length > 4) {
                        try {
                            task.setCategory(ToDoItem.Category.valueOf(row[4]));
                        } catch (Exception e) {
                            task.setCategory(ToDoItem.Category.OTHER);
                        }
                    }
                    
                    // Parse priority
                    if (row.length > 5) {
                        try {
                            task.setPriority(ToDoItem.Priority.valueOf(row[5]));
                        } catch (Exception e) {
                            task.setPriority(ToDoItem.Priority.MEDIUM);
                        }
                    }
                    
                    // Parse completed
                    if (row.length > 6) {
                        task.setCompleted(Boolean.parseBoolean(row[6]));
                    }
                    
                    // Parse created date
                    if (row.length > 7 && !row[7].isEmpty()) {
                        try {
                            task.setCreatedDate(LocalDateTime.parse(row[7], dateTimeFormatter));
                        } catch (Exception e) {
                            task.setCreatedDate(LocalDateTime.now());
                        }
                    }
                    
                    // Parse completed date
                    if (row.length > 8 && !row[8].isEmpty()) {
                        try {
                            task.setCompletedDate(LocalDateTime.parse(row[8], dateTimeFormatter));
                        } catch (Exception e) {
                            // Leave as null
                        }
                    }
                    
                    // Parse points
                    if (row.length > 9) {
                        try {
                            task.setPoints(Integer.parseInt(row[9]));
                        } catch (Exception e) {
                            // Points will be recalculated
                        }
                    }
                    
                    tasks.add(task);
                } catch (Exception e) {
                    System.err.println("Error parsing CSV row: " + e.getMessage());
                    // Continue with next row
                }
            }
        }
        
        return tasks;
    }
    
    /**
     * Create a simple backup of all tasks
     */
    public void createBackup(ObservableList<ToDoItem> tasks) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File backupFile = new File("backup_" + timestamp + ".json");
            exportToJSON(tasks, backupFile);
            System.out.println("Backup created: " + backupFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error creating backup: " + e.getMessage());
        }
    }
}
