# Professional To-Do List Manager

A modern, feature-rich JavaFX to-do list application with professional-grade capabilities including categories, priorities, natural language processing, notifications, gamification, and more.

## 🚀 Features

### Core Features
- ✅ **Task Management**: Create, edit, delete, and mark tasks as complete
- 📁 **Categories**: Organize tasks by type (Work, Personal, Health, Education, Finance, Social, Shopping, Travel, Other)
- 🔥 **Priority Levels**: Four priority levels (Low, Medium, High, Urgent) with color coding
- 📅 **Due Dates & Times**: Set deadlines with specific dates and times
- 💾 **SQLite Database**: Persistent storage replacing simple text files

### Advanced Features
- 🗣️ **Natural Language Processing**: Parse dates like "tomorrow at 6pm", "next Monday morning"
- 🔍 **Search & Filter**: Real-time search and multi-criteria filtering
- 🎨 **Dark/Light Theme**: Toggle between themes with persistent configuration
- 🔔 **Desktop Notifications**: Deadline reminders and completion alerts
- 📊 **Statistics Dashboard**: Visual charts showing task completion and category breakdown
- 🎮 **Gamification**: Points system, levels, and XP progression
- 📤 **Import/Export**: CSV and JSON format support for data portability
- 🎯 **Drag & Drop**: Intuitive task reordering (UI ready)

### User Experience
- 🖱️ **Context Menus**: Right-click actions for quick task operations
- ⌨️ **Keyboard Shortcuts**: Efficiency through hotkeys
- 📱 **Responsive UI**: Modern Material Design components
- 🎭 **Rich List Cells**: Color-coded priority indicators and category badges
- 💾 **Auto-save**: Automatic configuration and data persistence

## 🛠️ Technology Stack

- **JavaFX 17.0.6**: Modern UI framework with rich controls
- **SQLite 3.42.0.0**: Embedded database for data persistence
- **Jackson 2.15.2**: JSON processing for configuration and export
- **Natty 0.13**: Natural language date parsing
- **OpenCSV 5.7.1**: CSV import/export functionality
- **JFoenix 9.0.10**: Material Design components
- **Maven**: Dependency management and build automation

## 📁 Project Structure

```
ToDOList/
├── src/main/java/com/swaraj/todolist/
│   ├── Main.java                    # Application entry point
│   ├── Controller.java              # Main UI controller
│   ├── DialogController.java        # Task dialog controller
│   ├── dataModel/
│   │   ├── ToDoItem.java           # Enhanced task model
│   │   └── ToDoData.java           # Legacy data handler
│   ├── services/
│   │   ├── DatabaseService.java    # SQLite operations
│   │   ├── ConfigurationService.java # Settings & preferences
│   │   ├── NotificationService.java # Background notifications
│   │   └── ExportImportService.java # Data import/export
│   └── utils/
│       ├── ThemeManager.java       # UI theme management
│       └── NaturalLanguageDateParser.java # Date parsing
├── src/main/resources/com/swaraj/todolist/
│   ├── mainWindow.fxml             # Main UI layout
│   ├── ToDoItemDialog.fxml         # Task dialog layout
│   └── styles.css                  # Application stylesheet
├── pom.xml                         # Maven configuration
└── run.sh                          # Application launcher script
```

## 🏃‍♂️ Running the Application

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- **MySQL 8.0+** (MySQL Workbench recommended)
- JavaFX runtime (included in dependencies)

### MySQL Database Setup

#### Step 1: Configure MySQL Connection
1. Open `ToDOList/src/main/resources/database.properties`
2. Update the following values:
   ```properties
   db.username=your_mysql_username
   db.password=your_mysql_password
   ```

#### Step 2: Ensure MySQL is Running
- Start MySQL service on your system
- Verify connection using MySQL Workbench or command line
- The application will automatically create the `todolist_db` database

#### Step 3: Run Setup Helper (Optional)
```bash
cd ToDOList
./setup-mysql.sh
```

### Quick Start
```bash
# Clone and navigate to project
cd ToDOList

# Update MySQL password in database.properties first!
# Then build and run:
mvn clean compile exec:java -Dexec.mainClass="com.swaraj.todolist.Main"

# Or use the run script:
./run.sh
```

### Alternative Database Configuration
If you prefer different settings, update `database.properties`:
```properties
# For custom host/port
db.url=jdbc:mysql://your-host:3306/todolist_db?useSSL=false&serverTimezone=UTC

# For custom database name
db.url=jdbc:mysql://localhost:3306/your_db_name?useSSL=false&serverTimezone=UTC
```

---

**Built with ❤️ using JavaFX and modern Java practices**
