package com.example.memomate;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memomate.Main;
import com.example.memomate.R;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private final List<Main.Task> taskList;
    private final AppCompatActivity context;
    private String subjectText = "";
    private String taskNameText = "";
    public TaskAdapter(AppCompatActivity context, List<Main.Task> taskList) {
        this.context = context;
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_entry, parent, false);
        return new TaskViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Main.Task task = taskList.get(position);
        holder.subjectText.setText(task.subject);
        holder.taskNameText.setText(task.taskName);
        holder.dueDateText.setText("Due: " + task.dueDate + " " + task.dueTime);
        subjectText = task.subject;
        taskNameText = task.taskName;

        holder.itemView.setOnClickListener(v -> {
            showCompletionDialog(position);

        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView subjectText, taskNameText, dueDateText;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectText = itemView.findViewById(R.id.subject);
            taskNameText = itemView.findViewById(R.id.taskName);
            dueDateText = itemView.findViewById(R.id.dueDate);
        }
    }

    private void showCompletionDialog(int position) {
        new AlertDialog.Builder(context)
                .setTitle("Task Completed?")
                .setMessage("Have you finished this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (position >= 0 && position < taskList.size()) {
                        Main.Task task = taskList.get(position);

                        // ✅ Ensure correct file path
                        File oldFile = new File(context.getFilesDir() + "/tasks", task.subject + "-" + task.taskName + ".txt");
                        File newDir = new File(context.getFilesDir(), "completed_tasks");
                        if (!newDir.exists()) {
                            newDir.mkdirs(); // Create directory if not exists
                        }
                        File newFile = new File(newDir, oldFile.getName());

                        // ✅ Debug: Check if file exists before moving
                        if (!oldFile.exists()) {
                            Log.e("MemoMate", "❌ Error: Task file does not exist - " + oldFile.getAbsolutePath());
                            return;
                        }

                        // ✅ Attempt to move file
                        boolean success = oldFile.renameTo(newFile);
                        if (success) {
                            Log.d("MemoMate", "✅ File moved successfully: " + oldFile.getAbsolutePath() + " → " + newFile.getAbsolutePath());

                            // ✅ Remove from list & update RecyclerView
                            taskList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, taskList.size());
                        } else {
                            Log.e("MemoMate", "❌ Error: Failed to move file to completed_tasks folder.");
                        }
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

}
