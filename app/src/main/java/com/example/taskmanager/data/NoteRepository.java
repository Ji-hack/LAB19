package com.example.taskmanager.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.taskmanager.data.local.Note;
import com.example.taskmanager.data.local.NoteDao;
import com.example.taskmanager.data.local.NoteDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository that abstracts access to the Room data source.
 * All write operations are executed on a background thread.
 */
public class NoteRepository {

    private final NoteDao dao;
    private final ExecutorService bgExecutor;

    public NoteRepository(Application app) {
        NoteDatabase db = NoteDatabase.getInstance(app);
        dao = db.noteDao();
        bgExecutor = Executors.newSingleThreadExecutor();
    }

    public void insert(Note note)  { bgExecutor.execute(() -> dao.insert(note)); }
    public void update(Note note)  { bgExecutor.execute(() -> dao.update(note)); }
    public void delete(Note note)  { bgExecutor.execute(() -> dao.delete(note)); }
    public void deleteAll()        { bgExecutor.execute(dao::deleteAll); }

    public LiveData<List<Note>>  getAllNotes()               { return dao.getAllNotes(); }
    public LiveData<List<Note>>  searchNotes(String query)  { return dao.searchNotes(query); }
    public LiveData<List<Note>>  getNotesByCategory(String c){ return dao.getNotesByCategory(c); }
    public LiveData<Integer>     getNoteCount()             { return dao.getNoteCount(); }
}
