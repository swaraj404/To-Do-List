package com.swaraj.todolist.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

/**
 * Database configuration utility to help set up MySQL connection
 */
public class DatabaseSetup {
    
    private static final String PROPERTIES_FILE = "src/main/resources/database.properties";
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== MySQL Database Setup for To-Do List Application ===\n");
        
        // Get MySQL connection details
        System.out.print("Enter MySQL host (default: localhost): ");
        String host = scanner.nextLine().trim();
        if (host.isEmpty()) host = "localhost";
        
        System.out.print("Enter MySQL port (default: 3306): ");
        String port = scanner.nextLine().trim();
        if (port.isEmpty()) port = "3306";
        
        System.out.print("Enter MySQL username (default: root): ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) username = "root";
        
        System.out.print("Enter MySQL password: ");
        String password = scanner.nextLine().trim();
        
        System.out.print("Enter database name (default: todolist_db): ");
        String dbName = scanner.nextLine().trim();
        if (dbName.isEmpty()) dbName = "todolist_db";
        
        // Test connection
        String testUrl = "jdbc:mysql://" + host + ":" + port + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        
        System.out.println("\nTesting MySQL connection...");
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            try (Connection conn = DriverManager.getConnection(testUrl, username, password)) {
                System.out.println("✓ MySQL connection successful!");
                
                // Create database
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
                    System.out.println("✓ Database '" + dbName + "' created/verified successfully!");
                }
                
                // Update properties file
                updatePropertiesFile(host, port, username, password, dbName);
                System.out.println("✓ Configuration saved to " + PROPERTIES_FILE);
                
                System.out.println("\n🎉 Database setup completed successfully!");
                System.out.println("You can now run your To-Do List application.");
                
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC driver not found. Make sure MySQL Connector/J is in your classpath.");
            System.err.println("Run 'mvn compile' to download dependencies.");
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            System.err.println("\nPlease check:");
            System.err.println("1. MySQL server is running");
            System.err.println("2. Username and password are correct");
            System.err.println("3. Host and port are accessible");
            System.err.println("4. User has CREATE DATABASE privileges");
        }
        
        scanner.close();
    }
    
    private static void updatePropertiesFile(String host, String port, String username, String password, String dbName) {
        Properties props = new Properties();
        
        // Load existing properties if file exists
        try (FileInputStream input = new FileInputStream(PROPERTIES_FILE)) {
            props.load(input);
        } catch (IOException e) {
            // File doesn't exist, will create new one
        }
        
        // Update database properties
        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        props.setProperty("db.url", url);
        props.setProperty("db.username", username);
        props.setProperty("db.password", password);
        props.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        
        // Set default pool properties if not already set
        if (!props.containsKey("db.pool.maximumPoolSize")) {
            props.setProperty("db.pool.maximumPoolSize", "10");
        }
        if (!props.containsKey("db.pool.minimumIdle")) {
            props.setProperty("db.pool.minimumIdle", "2");
        }
        if (!props.containsKey("db.pool.connectionTimeout")) {
            props.setProperty("db.pool.connectionTimeout", "30000");
        }
        if (!props.containsKey("db.pool.idleTimeout")) {
            props.setProperty("db.pool.idleTimeout", "600000");
        }
        if (!props.containsKey("db.pool.maxLifetime")) {
            props.setProperty("db.pool.maxLifetime", "1800000");
        }
        
        // Set default application properties
        if (!props.containsKey("app.createTables")) {
            props.setProperty("app.createTables", "true");
        }
        if (!props.containsKey("app.initializeData")) {
            props.setProperty("app.initializeData", "true");
        }
        
        // Save properties
        try (FileOutputStream output = new FileOutputStream(PROPERTIES_FILE)) {
            props.store(output, "MySQL Database Configuration for To-Do List Application");
        } catch (IOException e) {
            System.err.println("Failed to save properties file: " + e.getMessage());
        }
    }
    
    /**
     * Create sample tasks for testing
     */
    public static void createSampleData(String url, String username, String password) {
        String insertSample = """
            INSERT INTO tasks (short_description, details, deadline, category, priority, completed, created_date, points) VALUES
            ('Complete project proposal', 'Finish the Q4 project proposal and submit to management', '2025-08-30 17:00:00', 'WORK', 'HIGH', false, NOW(), 15),
            ('Grocery shopping', 'Buy vegetables, fruits, and household items', '2025-08-29 18:00:00', 'PERSONAL', 'MEDIUM', false, NOW(), 5),
            ('Doctor appointment', 'Annual health checkup with Dr. Smith', '2025-09-05 10:30:00', 'HEALTH', 'HIGH', false, NOW(), 10),
            ('Learn JavaFX', 'Complete the JavaFX tutorial series', '2025-09-15 23:59:59', 'EDUCATION', 'MEDIUM', false, NOW(), 20),
            ('Plan vacation', 'Research and book summer vacation trip', '2025-09-01 12:00:00', 'TRAVEL', 'LOW', false, NOW(), 8)
            """;
        
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(insertSample);
            System.out.println("✓ Sample tasks created successfully!");
            
        } catch (SQLException e) {
            System.err.println("Failed to create sample data: " + e.getMessage());
        }
    }
}
