package com.example.taskmanager.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for task operations.
 * Provides reactive LiveData queries for the UI layer.
 */
@Dao
public interface NoteDao {

    @Insert
    void insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("DELETE FROM tasks_table")
    void deleteAll();

    // Pinned tasks first, then sorted by priority and creation date
    @Query("SELECT * FROM tasks_table ORDER BY isPinned DESC, priority DESC, createdAt DESC")
    LiveData<List<Note>> getAllNotes();

    // Full-text search across title and content fields
    @Query("SELECT * FROM tasks_table WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY isPinned DESC, priority DESC")
    LiveData<List<Note>> searchNotes(String query);

    // Filter by a specific category tag
    @Query("SELECT * FROM tasks_table WHERE category = :category ORDER BY isPinned DESC, priority DESC, createdAt DESC")
    LiveData<List<Note>> getNotesByCategory(String category);

    @Query("SELECT COUNT(*) FROM tasks_table")
    LiveData<Integer> getNoteCount();
}
