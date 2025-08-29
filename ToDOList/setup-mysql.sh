#!/bin/bash

echo "=================================="
echo "   MySQL Setup for To-Do List"
echo "=================================="
echo ""

echo "📋 SETUP INSTRUCTIONS:"
echo ""
echo "1. Make sure MySQL is running on your system"
echo "2. Open MySQL Workbench or use command line to verify connection"
echo "3. Update the database.properties file with your MySQL credentials"
echo ""

echo "📝 What you need to update in database.properties:"
echo ""
echo "   - db.username: Your MySQL username (usually 'root')"
echo "   - db.password: Your MySQL password"
echo "   - db.url: Change hostname if MySQL is not on localhost"
echo ""

echo "📍 Database properties file location:"
echo "   src/main/resources/database.properties"
echo ""

echo "🔧 Default configuration assumes:"
echo "   - MySQL running on localhost:3306"
echo "   - Username: root"
echo "   - Database will be created automatically: todolist_db"
echo ""

echo "🧪 To test your configuration:"
echo "   1. Update the password in database.properties"
echo "   2. Run: mvn exec:java -Dexec.mainClass=\"com.swaraj.todolist.Main\""
echo ""

echo "❓ Example MySQL commands to create user (if needed):"
echo "   CREATE USER 'todouser'@'localhost' IDENTIFIED BY 'todopass';"
echo "   GRANT ALL PRIVILEGES ON todolist_db.* TO 'todouser'@'localhost';"
echo "   FLUSH PRIVILEGES;"
echo ""

read -p "Press Enter to continue and open the properties file for editing..."

# Open the properties file for editing
if command -v code &> /dev/null; then
    code src/main/resources/database.properties
elif command -v nano &> /dev/null; then
    nano src/main/resources/database.properties
elif command -v vim &> /dev/null; then
    vim src/main/resources/database.properties
else
    echo "Please manually edit: src/main/resources/database.properties"
    echo "Set your MySQL password in the db.password field"
fi

echo ""
echo "✅ After updating the password, test the application with:"
echo "   mvn clean compile exec:java -Dexec.mainClass=\"com.swaraj.todolist.Main\""
