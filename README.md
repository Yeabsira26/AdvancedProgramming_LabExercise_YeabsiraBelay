# Simple Notepad Application

A feature-rich Notepad application built with JavaFX and MySQL database storage using XAMPP.

## Features

- ✅ **Create, Read, Update, Delete** notes
- ✅ **MySQL Database** storage (XAMPP)
- ✅ **Character Count** - Live character counter
- ✅ **Dark/Light Theme** toggle
- ✅ **Font Formatting** - Font size, Bold, Italic
- ✅ **Text Color** picker
- ✅ **Search** notes by title or content
- ✅ **Keyboard Shortcuts** (Ctrl+S, Ctrl+B, Ctrl+I)
- ✅ **Auto-save** to database
- ✅ **Responsive UI** with toolbar and status bar

## Technologies Used

- **Java** - Core programming language
- **JavaFX** - GUI framework
- **MySQL** - Database (via XAMPP)
- **JDBC** - Database connectivity

## 📋 Prerequisites

Before running the application, make sure you have:

1. **Java JDK 17 or higher** installed
2. **JavaFX SDK** (JARs included in `lib/javafx-lib/`)
3. **XAMPP** with MySQL running
4. **MySQL Connector/J** (included in `lib/`)

## Database Setup (One-Time)

### 1. Start XAMPP MySQL
- Open XAMPP Control Panel
- Start MySQL service

### 2. Create Database
Open http://localhost/phpmyadmin and run:

```sql
CREATE DATABASE IF NOT EXISTS notepad_db;
USE notepad_db;

CREATE TABLE IF NOT EXISTS notes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
##  How to Run

### Step 1: Compile
```bash
javac -cp "lib\mysql-connector-j-9.7.0.jar;lib\javafx-lib\*" -d bin src\notepad\*.java