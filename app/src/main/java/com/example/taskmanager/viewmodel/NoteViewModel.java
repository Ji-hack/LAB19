package com.example.taskmanager.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.taskmanager.data.NoteRepository;
import com.example.taskmanager.data.local.Note;

import java.util.List;

/**
 * ViewModel for the main screen.
 * Bridges the UI and the repository, surviving configuration changes.
 */
public class NoteViewModel extends AndroidViewModel {

    private final NoteRepository repository;

    private final MutableLiveData<String> searchQuery    = new MutableLiveData<>("");
    private final MutableLiveData<String> categoryFilter = new MutableLiveData<>(null);

    // Reactively switches between full list and search results
    private final LiveData<List<Note>> displayedNotes;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);

        displayedNotes = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                return repository.getAllNotes();
            }
            return repository.searchNotes(query.trim());
        });
    }

    public void insert(Note note)  { repository.insert(note); }
    public void update(Note note)  { repository.update(note); }
    public void delete(Note note)  { repository.delete(note); }
    public void deleteAll()        { repository.deleteAll(); }

    public LiveData<List<Note>> getDisplayedNotes()              { return displayedNotes; }
    public LiveData<Integer>    getNoteCount()                   { return repository.getNoteCount(); }
    public void                 setSearchQuery(String q)         { searchQuery.setValue(q); }
    public LiveData<List<Note>> getNotesByCategory(String cat)   { return repository.getNotesByCategory(cat); }

    /** Toggle the pinned state of a task and persist the change. */
    public void togglePin(Note note) {
        note.setPinned(!note.isPinned());
        repository.update(note);
    }
}
