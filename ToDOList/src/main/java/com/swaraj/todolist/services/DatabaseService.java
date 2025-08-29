package com.swaraj.todolist.services;

import com.swaraj.todolist.dataModel.ToDoItem;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Enhanced DatabaseService using MySQL with connection pooling
 */
public class DatabaseService {
    private static DatabaseService instance;
    private HikariDataSource dataSource;
    private static final String PROPERTIES_FILE = "/database.properties";
    
    // SQL Queries
    private static final String CREATE_DATABASE = "CREATE DATABASE IF NOT EXISTS todolist_db";
    private static final String USE_DATABASE = "USE todolist_db";
    
    private static final String CREATE_TASKS_TABLE = """
        CREATE TABLE IF NOT EXISTS tasks (
            id INT AUTO_INCREMENT PRIMARY KEY,
            short_description VARCHAR(500) NOT NULL,
            details TEXT,
            deadline DATETIME,
            category VARCHAR(50) NOT NULL DEFAULT 'OTHER',
            priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
            completed BOOLEAN NOT NULL DEFAULT FALSE,
            created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
            completed_date DATETIME NULL,
            points INT NOT NULL DEFAULT 0,
            INDEX idx_deadline (deadline),
            INDEX idx_category (category),
            INDEX idx_priority (priority),
            INDEX idx_completed (completed)
        )
        """;
    
    private static final String INSERT_TASK = """
        INSERT INTO tasks (short_description, details, deadline, category, priority, completed, created_date, completed_date, points)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
        short_description = VALUES(short_description),
        details = VALUES(details),
        deadline = VALUES(deadline),
        category = VALUES(category),
        priority = VALUES(priority),
        completed = VALUES(completed),
        completed_date = VALUES(completed_date),
        points = VALUES(points)
        """;
    
    private static final String UPDATE_TASK = """
        UPDATE tasks SET 
        short_description = ?, details = ?, deadline = ?, category = ?, 
        priority = ?, completed = ?, completed_date = ?, points = ?
        WHERE id = ?
        """;
    
    private static final String SELECT_ALL_TASKS = """
        SELECT id, short_description, details, deadline, category, priority, 
               completed, created_date, completed_date, points
        FROM tasks ORDER BY completed ASC, priority DESC, deadline ASC
        """;
    
    private static final String DELETE_TASK = "DELETE FROM tasks WHERE id = ?";
    
    private static final String SELECT_TASK_STATISTICS = """
        SELECT 
            COUNT(*) as total_tasks,
            SUM(CASE WHEN completed = 1 THEN 1 ELSE 0 END) as completed_tasks,
            SUM(CASE WHEN completed = 0 THEN 1 ELSE 0 END) as pending_tasks,
            SUM(CASE WHEN completed = 0 AND deadline < NOW() THEN 1 ELSE 0 END) as overdue_tasks,
            SUM(CASE WHEN completed = 0 AND DATE(deadline) = CURDATE() THEN 1 ELSE 0 END) as due_today_tasks,
            SUM(points) as total_points,
            category,
            COUNT(*) as category_count
        FROM tasks
        GROUP BY category WITH ROLLUP
        """;

    private DatabaseService() {
        try {
            initializeDataSource();
            initializeDatabase();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database service", e);
        }
    }

    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    private void initializeDataSource() throws IOException {
        Properties props = loadDatabaseProperties();
        
        HikariConfig config = new HikariConfig();
        
        // First connect without database to create it
        String baseUrl = props.getProperty("db.url").replace("/todolist_db", "");
        config.setJdbcUrl(baseUrl);
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
        config.setDriverClassName(props.getProperty("db.driver"));
        
        // Connection pool settings
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maximumPoolSize", "10")));
        config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minimumIdle", "2")));
        config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "30000")));
        config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "600000")));
        config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.maxLifetime", "1800000")));
        
        // Additional settings
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("TodoListConnectionPool");
        
        this.dataSource = new HikariDataSource(config);
        
        // Create database if it doesn't exist
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_DATABASE);
            stmt.execute(USE_DATABASE);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create database", e);
        }
        
        // Now reconnect to the specific database
        dataSource.close();
        config.setJdbcUrl(props.getProperty("db.url"));
        this.dataSource = new HikariDataSource(config);
    }

    private Properties loadDatabaseProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream input = getClass().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new IOException("Unable to find " + PROPERTIES_FILE);
            }
            props.load(input);
        }
        return props;
    }

    private void initializeDatabase() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(CREATE_TASKS_TABLE);
            System.out.println("Database tables initialized successfully");
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database tables", e);
        }
    }

    public void saveTodoItem(ToDoItem item) {
        if (item.getId() == 0) {
            insertTodoItem(item);
        } else {
            updateTodoItem(item);
        }
    }

    private void insertTodoItem(ToDoItem item) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_TASK, Statement.RETURN_GENERATED_KEYS)) {
            
            setTodoItemParameters(pstmt, item);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating todo item failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating todo item failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert todo item", e);
        }
    }

    private void updateTodoItem(ToDoItem item) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_TASK)) {
            
            setTodoItemParameters(pstmt, item);
            pstmt.setInt(9, item.getPoints());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update todo item", e);
        }
    }

    private void setTodoItemParameters(PreparedStatement pstmt, ToDoItem item) throws SQLException {
        pstmt.setString(1, item.getShortDescription());
        pstmt.setString(2, item.getDetails());
        
        if (item.getDeadline() != null) {
            pstmt.setTimestamp(3, Timestamp.valueOf(item.getDeadline()));
        } else {
            pstmt.setNull(3, Types.TIMESTAMP);
        }
        
        pstmt.setString(4, item.getCategory().name());
        pstmt.setString(5, item.getPriority().name());
        pstmt.setBoolean(6, item.isCompleted());
        pstmt.setTimestamp(7, Timestamp.valueOf(item.getCreatedDate()));
        
        if (item.getCompletedDate() != null) {
            pstmt.setTimestamp(8, Timestamp.valueOf(item.getCompletedDate()));
        } else {
            pstmt.setNull(8, Types.TIMESTAMP);
        }
        
        pstmt.setInt(9, item.getPoints());
    }

    public ObservableList<ToDoItem> loadTodoItems() {
        ObservableList<ToDoItem> items = FXCollections.observableArrayList();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL_TASKS);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                ToDoItem item = createTodoItemFromResultSet(rs);
                items.add(item);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load todo items", e);
        }
        
        return items;
    }

    private ToDoItem createTodoItemFromResultSet(ResultSet rs) throws SQLException {
        ToDoItem item = new ToDoItem();
        
        item.setId(rs.getInt("id"));
        item.setShortDescription(rs.getString("short_description"));
        item.setDetails(rs.getString("details"));
        
        Timestamp deadline = rs.getTimestamp("deadline");
        if (deadline != null) {
            item.setDeadline(deadline.toLocalDateTime());
        }
        
        item.setCategory(ToDoItem.Category.valueOf(rs.getString("category")));
        item.setPriority(ToDoItem.Priority.valueOf(rs.getString("priority")));
        item.setCompleted(rs.getBoolean("completed"));
        item.setCreatedDate(rs.getTimestamp("created_date").toLocalDateTime());
        
        Timestamp completedDate = rs.getTimestamp("completed_date");
        if (completedDate != null) {
            item.setCompletedDate(completedDate.toLocalDateTime());
        }
        
        item.setPoints(rs.getInt("points"));
        
        return item;
    }

    public void deleteTodoItem(long itemId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_TASK)) {
            
            pstmt.setLong(1, itemId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete todo item", e);
        }
    }

    public TaskStatistics getTaskStatistics() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_TASK_STATISTICS);
             ResultSet rs = pstmt.executeQuery()) {
            
            TaskStatistics stats = new TaskStatistics();
            Map<String, Integer> categoryStats = new HashMap<>();
            
            while (rs.next()) {
                String category = rs.getString("category");
                if (category == null) {
                    // This is the rollup row with totals
                    stats.totalTasks = rs.getInt("total_tasks");
                    stats.completedTasks = rs.getInt("completed_tasks");
                    stats.pendingTasks = rs.getInt("pending_tasks");
                    stats.overdueTasks = rs.getInt("overdue_tasks");
                    stats.dueTodayTasks = rs.getInt("due_today_tasks");
                    stats.totalPoints = rs.getInt("total_points");
                } else {
                    categoryStats.put(category, rs.getInt("category_count"));
                }
            }
            
            stats.categoryBreakdown = categoryStats;
            return stats;
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get task statistics", e);
        }
    }

    public ObservableList<ToDoItem> getTasksDueSoon(int hours) {
        String query = """
            SELECT id, short_description, details, deadline, category, priority, 
                   completed, created_date, completed_date, points
            FROM tasks 
            WHERE completed = 0 AND deadline IS NOT NULL 
            AND deadline BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL ? HOUR)
            ORDER BY deadline ASC
            """;
        
        ObservableList<ToDoItem> items = FXCollections.observableArrayList();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, hours);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(createTodoItemFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get tasks due soon", e);
        }
        
        return items;
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    // Statistics class
    public static class TaskStatistics {
        public int totalTasks;
        public int completedTasks;
        public int pendingTasks;
        public int overdueTasks;
        public int dueTodayTasks;
        public int totalPoints;
        public Map<String, Integer> categoryBreakdown;
        
        public TaskStatistics() {
            categoryBreakdown = new HashMap<>();
        }
        
        public double getCompletionRate() {
            return totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
        }
    }
}
