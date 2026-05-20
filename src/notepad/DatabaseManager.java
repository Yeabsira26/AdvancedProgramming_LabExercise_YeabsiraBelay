package notepad;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    // XAMPP MySQL connection settings
    private static final String URL = "jdbc:mysql://localhost:3306/notepad_db";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Default XAMPP has no password
    
    private Connection connection;

    public DatabaseManager() {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Establish connection
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✓ Database connected successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ MySQL JDBC Driver not found!");
            System.err.println("Make sure mysql-connector-j-8.2.0.jar is in the lib folder");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("✗ Database connection failed!");
            System.err.println("Check if XAMPP MySQL is running");
            e.printStackTrace();
        }
    }

    // Save a new note to database
    public int saveNote(Note note) {
        String sql = "INSERT INTO notes (title, content) VALUES (?, ?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql, 
                Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, note.getTitle());
            pstmt.setString(2, note.getContent());
            pstmt.executeUpdate();
            
            // Get the auto-generated ID
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error saving note: " + e.getMessage());
        }
        return -1;
    }

    // Update an existing note
    public boolean updateNote(Note note) {
        String sql = "UPDATE notes SET title = ?, content = ? WHERE id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, note.getTitle());
            pstmt.setString(2, note.getContent());
            pstmt.setInt(3, note.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating note: " + e.getMessage());
        }
        return false;
    }

    // Delete a note
    public boolean deleteNote(int id) {
        String sql = "DELETE FROM notes WHERE id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting note: " + e.getMessage());
        }
        return false;
    }

    // Get all notes from database
    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM notes ORDER BY updated_at DESC";
        
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Note note = new Note(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getTimestamp("updated_at").toLocalDateTime()
                );
                notes.add(note);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching notes: " + e.getMessage());
        }
        return notes;
    }

    // Get a single note by ID
    public Note getNoteById(int id) {
        String sql = "SELECT * FROM notes WHERE id = ?";
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Note(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getTimestamp("updated_at").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching note: " + e.getMessage());
        }
        return null;
    }

    // Search notes by title
    public List<Note> searchNotes(String keyword) {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM notes WHERE title LIKE ? OR content LIKE ?";
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Note note = new Note(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getTimestamp("updated_at").toLocalDateTime()
                );
                notes.add(note);
            }
        } catch (SQLException e) {
            System.err.println("Error searching notes: " + e.getMessage());
        }
        return notes;
    }

    // Close database connection
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}