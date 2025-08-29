#!/bin/bash

echo "🚀 Starting Professional To-Do List Application..."
echo ""

# Check if database properties file exists
if [ ! -f "src/main/resources/database.properties" ]; then
    echo "❌ Database configuration file not found!"
    echo "Please make sure src/main/resources/database.properties exists"
    exit 1
fi

# Check if password is configured
if grep -q "YOUR_MYSQL_PASSWORD_HERE" src/main/resources/database.properties; then
    echo "⚠️  MySQL password not configured!"
    echo ""
    echo "Please update your MySQL password in:"
    echo "   src/main/resources/database.properties"
    echo ""
    echo "Change 'YOUR_MYSQL_PASSWORD_HERE' to your actual MySQL password"
    echo ""
    echo "Then run this script again."
    exit 1
fi

# Build the project
echo "🔨 Building the project..."
mvn clean compile

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    echo ""
    echo "🗄️  Connecting to MySQL database..."
    echo "📱 Starting application..."
    echo ""
    
    # Run the application using Maven exec plugin
    mvn exec:java -Dexec.mainClass="com.swaraj.todolist.Main"
else
    echo "❌ Compilation failed. Please fix the errors before running."
    echo ""
    echo "Common issues:"
    echo "1. Make sure Java 17+ is installed"
    echo "2. Make sure Maven is installed"
    echo "3. Check that MySQL is running"
    echo "4. Verify database.properties is correctly configured"
fi
