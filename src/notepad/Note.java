package notepad;

import java.time.LocalDateTime;

public class Note {
    private int id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor for new notes
    public Note(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // Constructor for existing notes from database
    public Note(int id, String title, String content, 
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getId() { 
        return id; 
    }
    
    public void setId(int id) { 
        this.id = id; 
    }

    public String getTitle() { 
        return title; 
    }
    
    public void setTitle(String title) { 
        this.title = title; 
    }

    public String getContent() { 
        return content; 
    }
    
    public void setContent(String content) { 
        this.content = content; 
    }

    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }

    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) { 
        this.updatedAt = updatedAt; 
    }

    @Override
    public String toString() {
        return title;
    }
}