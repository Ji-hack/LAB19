package com.example.taskmanager.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents a single task entry stored in the local database.
 * Each task has a title, body text, tag, urgency level, and pin state.
 */
@Entity(tableName = "tasks_table")
public class Note {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String content;
    private String category;   // e.g. "WORK", "PERSONAL", "IDEAS", "URGENT"
    private int priority;      // 1 = low, 2 = medium, 3 = high
    private long createdAt;
    private boolean isPinned;

    public Note(String title, String content, String category, int priority) {
        this.title    = title;
        this.content  = content;
        this.category = category;
        this.priority = priority;
        this.createdAt = System.currentTimeMillis();
        this.isPinned  = false;
    }

    public int     getId()        { return id; }
    public String  getTitle()     { return title; }
    public String  getContent()   { return content; }
    public String  getCategory()  { return category; }
    public int     getPriority()  { return priority; }
    public long    getCreatedAt() { return createdAt; }
    public boolean isPinned()     { return isPinned; }

    public void setId(int id)               { this.id = id; }
    public void setTitle(String title)      { this.title = title; }
    public void setContent(String content)  { this.content = content; }
    public void setCategory(String cat)     { this.category = cat; }
    public void setPriority(int priority)   { this.priority = priority; }
    public void setCreatedAt(long ts)       { this.createdAt = ts; }
    public void setPinned(boolean pinned)   { this.isPinned = pinned; }
}
