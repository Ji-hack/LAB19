package com.example.taskmanager.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.data.local.Note;
import com.example.taskmanager.viewmodel.NoteViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

/**
 * Entry point of the application.
 * Displays the task list and handles all user interactions.
 */
public class MainActivity extends AppCompatActivity {

    private NoteViewModel viewModel;
    private NoteAdapter   adapter;
    private EditText      etSearch;
    private TextView      tvCount;
    private String        activeCategory = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etSearch = findViewById(R.id.etSearch);
        tvCount  = findViewById(R.id.tvCount);
        RecyclerView         recycler = findViewById(R.id.recyclerView);
        FloatingActionButton fabAdd   = findViewById(R.id.fabAdd);
        ChipGroup            chips    = findViewById(R.id.chipGroup);

        // Set up the list
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setHasFixedSize(true);
        adapter = new NoteAdapter();
        recycler.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        viewModel.getDisplayedNotes().observe(this, tasks -> {
            adapter.setNotes(tasks);
            findViewById(R.id.tvEmpty).setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getNoteCount().observe(this, n ->
                tvCount.setText(n + " note" + (n > 1 ? "s" : "")));

        // Live search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                viewModel.setSearchQuery(s.toString());
            }
        });

        fabAdd.setOnClickListener(v -> openAddDialog());

        buildCategoryChips(chips);

        adapter.setOnNoteClickListener(task -> openEditDialog(task));
        adapter.setOnNoteLongClickListener((task, anchor) -> openContextMenu(task, anchor));
    }

    private void buildCategoryChips(ChipGroup group) {
        String[] labels = {"TOUS", "TRAVAIL", "PERSO", "IDÉES", "URGENT"};
        for (String label : labels) {
            Chip chip = new Chip(this);
            chip.setText(label);
            chip.setCheckable(true);
            chip.setChecked(label.equals("TOUS"));
            chip.setOnClickListener(v -> {
                activeCategory = label.equals("TOUS") ? null : label;
                if (activeCategory == null) {
                    viewModel.setSearchQuery(etSearch.getText().toString());
                } else {
                    viewModel.getNotesByCategory(activeCategory)
                            .observe(this, tasks -> adapter.setNotes(tasks));
                }
            });
            group.addView(chip);
        }
    }

    private void openAddDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null);

        EditText etTitle   = view.findViewById(R.id.etDialogTitle);
        EditText etContent = view.findViewById(R.id.etDialogContent);
        Spinner  spinCat   = view.findViewById(R.id.spinnerCategory);
        Spinner  spinPrio  = view.findViewById(R.id.spinnerPriority);
        TextView tvWords   = view.findViewById(R.id.tvLiveWordCount);

        etContent.addTextChangedListener(buildWordCounter(tvWords));

        String[] cats  = {"TRAVAIL", "PERSO", "IDÉES", "URGENT"};
        String[] prios = {"BASSE", "MOYENNE", "HAUTE"};
        spinCat.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cats));
        spinPrio.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, prios));

        new AlertDialog.Builder(this)
                .setTitle("✦ Nouvelle note")
                .setView(view)
                .setPositiveButton("Enregistrer", (d, w) -> {
                    String title   = etTitle.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(this, "Le titre est obligatoire", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.insert(new Note(title, content,
                            cats[spinCat.getSelectedItemPosition()],
                            spinPrio.getSelectedItemPosition() + 1));
                    Toast.makeText(this, "Note ajoutée ✓", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void openEditDialog(Note task) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null);

        EditText etTitle   = view.findViewById(R.id.etDialogTitle);
        EditText etContent = view.findViewById(R.id.etDialogContent);
        Spinner  spinCat   = view.findViewById(R.id.spinnerCategory);
        Spinner  spinPrio  = view.findViewById(R.id.spinnerPriority);
        TextView tvWords   = view.findViewById(R.id.tvLiveWordCount);

        String[] cats  = {"TRAVAIL", "PERSO", "IDÉES", "URGENT"};
        String[] prios = {"BASSE", "MOYENNE", "HAUTE"};
        spinCat.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cats));
        spinPrio.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, prios));

        etTitle.setText(task.getTitle());
        etContent.setText(task.getContent());
        for (int i = 0; i < cats.length; i++)
            if (cats[i].equals(task.getCategory())) spinCat.setSelection(i);
        spinPrio.setSelection(task.getPriority() - 1);

        updateWordCount(tvWords, task.getContent());
        etContent.addTextChangedListener(buildWordCounter(tvWords));

        new AlertDialog.Builder(this)
                .setTitle("✎ Modifier la note")
                .setView(view)
                .setPositiveButton("Mettre à jour", (d, wi) -> {
                    String title = etTitle.getText().toString().trim();
                    if (title.isEmpty()) return;
                    task.setTitle(title);
                    task.setContent(etContent.getText().toString().trim());
                    task.setCategory(cats[spinCat.getSelectedItemPosition()]);
                    task.setPriority(spinPrio.getSelectedItemPosition() + 1);
                    viewModel.update(task);
                    Toast.makeText(this, "Note mise à jour ✓", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void openContextMenu(Note task, View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, 1, 0, task.isPinned() ? "📌 Désépingler" : "📌 Épingler");
        menu.getMenu().add(0, 2, 0, "🗑 Supprimer");
        menu.getMenu().add(0, 3, 0, "✎ Modifier");

        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    viewModel.togglePin(task);
                    Toast.makeText(this, task.isPinned() ? "Désépinglée" : "Épinglée 📌", Toast.LENGTH_SHORT).show();
                    return true;
                case 2:
                    confirmDelete(task);
                    return true;
                case 3:
                    openEditDialog(task);
                    return true;
            }
            return false;
        });
        menu.show();
    }

    private void confirmDelete(Note task) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer cette note ?")
                .setMessage("« " + task.getTitle() + " » sera supprimée définitivement.")
                .setPositiveButton("Supprimer", (d, w) -> {
                    viewModel.delete(task);
                    Toast.makeText(this, "Note supprimée", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    // Reusable word counter watcher
    private TextWatcher buildWordCounter(TextView target) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                updateWordCount(target, s.toString());
            }
        };
    }

    private void updateWordCount(TextView tv, String text) {
        int w = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
        tv.setText(w + " mot" + (w > 1 ? "s" : ""));
    }
}
