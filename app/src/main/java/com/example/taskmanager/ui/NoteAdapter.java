package com.example.taskmanager.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.data.local.Note;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for the task list.
 * Uses DiffUtil for efficient incremental updates.
 */
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.TaskViewHolder> {

    private List<Note> taskList = new ArrayList<>();
    private OnNoteClickListener     clickListener;
    private OnNoteLongClickListener longClickListener;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    public interface OnNoteLongClickListener {
        void onNoteLongClick(Note note, View anchor);
    }

    public void setNotes(List<Note> incoming) {
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return taskList.size(); }
            @Override public int getNewListSize() { return incoming.size(); }
            @Override public boolean areItemsTheSame(int o, int n) {
                return taskList.get(o).getId() == incoming.get(n).getId();
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                Note a = taskList.get(o), b = incoming.get(n);
                return a.getTitle().equals(b.getTitle())
                        && a.getContent().equals(b.getContent())
                        && a.isPinned() == b.isPinned()
                        && a.getPriority() == b.getPriority();
            }
        });
        taskList = incoming;
        diff.dispatchUpdatesTo(this);
    }

    public void setOnNoteClickListener(OnNoteClickListener l)     { this.clickListener = l; }
    public void setOnNoteLongClickListener(OnNoteLongClickListener l) { this.longClickListener = l; }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Note task = taskList.get(position);
        holder.tvTitle.setText(task.getTitle());
        holder.tvContent.setText(task.getContent());
        holder.tvCategory.setText(task.getCategory());
        holder.tvDate.setText(formatTimestamp(task.getCreatedAt()));

        holder.ivPin.setVisibility(task.isPinned() ? View.VISIBLE : View.GONE);

        int tagColor = resolveTagColor(task.getCategory());
        holder.viewStripe.setBackgroundColor(tagColor);
        holder.tvCategory.setTextColor(tagColor);

        holder.tvPriority.setText(resolvePriorityLabel(task.getPriority()));
        holder.tvPriority.setBackgroundTintList(
                ColorStateList.valueOf(resolvePriorityColor(task.getPriority())));

        int words = task.getContent().trim().isEmpty() ? 0
                : task.getContent().trim().split("\\s+").length;
        holder.tvWordCount.setText(words + " mots");
    }

    @Override
    public int getItemCount() { return taskList.size(); }

    private String formatTimestamp(long ts) {
        return new SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.FRENCH).format(new Date(ts));
    }

    private int resolveTagColor(String tag) {
        switch (tag) {
            case "TRAVAIL": return Color.parseColor("#4F8EF7");
            case "PERSO":   return Color.parseColor("#34C97B");
            case "IDÉES":   return Color.parseColor("#F7A84F");
            case "URGENT":  return Color.parseColor("#F75F5F");
            default:        return Color.parseColor("#9B8BF4");
        }
    }

    private int resolvePriorityColor(int level) {
        if (level == 3) return Color.parseColor("#F75F5F");
        if (level == 2) return Color.parseColor("#F7A84F");
        return Color.parseColor("#34C97B");
    }

    private String resolvePriorityLabel(int level) {
        if (level == 3) return "● HAUTE";
        if (level == 2) return "● MOYENNE";
        return "● BASSE";
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        View viewStripe;
        TextView tvTitle, tvContent, tvCategory, tvDate, tvPriority, tvWordCount;
        ImageView ivPin;

        TaskViewHolder(@NonNull View v) {
            super(v);
            card        = v.findViewById(R.id.cardNote);
            viewStripe  = v.findViewById(R.id.viewStripe);
            tvTitle     = v.findViewById(R.id.tvTitle);
            tvContent   = v.findViewById(R.id.tvContent);
            tvCategory  = v.findViewById(R.id.tvCategory);
            tvDate      = v.findViewById(R.id.tvDate);
            tvPriority  = v.findViewById(R.id.tvPriority);
            tvWordCount = v.findViewById(R.id.tvWordCount);
            ivPin       = v.findViewById(R.id.ivPin);

            v.setOnClickListener(view -> {
                int pos = getAdapterPosition();
                if (clickListener != null && pos != RecyclerView.NO_POSITION)
                    clickListener.onNoteClick(taskList.get(pos));
            });

            v.setOnLongClickListener(view -> {
                int pos = getAdapterPosition();
                if (longClickListener != null && pos != RecyclerView.NO_POSITION) {
                    longClickListener.onNoteLongClick(taskList.get(pos), view);
                    return true;
                }
                return false;
            });
        }
    }
}
