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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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

                            File tasksDirectory = new File(context.getFilesDir(), "tasks");
                            if (!tasksDirectory.exists() || !tasksDirectory.isDirectory()) {
                                return;
                            }

                            // ✅ List only `.txt` files
                            File[] files = tasksDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
                            if (files == null) return;

                            ((TextView)context.findViewById(R.id.currentTask)).setText(String.valueOf(files.length));

                            File file = new File(context.getFilesDir(), "streak.txt");
                            if (!file.exists()) {
                                try {
                                    file.createNewFile();
                                    FileWriter initWriter = new FileWriter(file);
                                    initWriter.write("0"); // Initialize with "0"
                                    initWriter.close();
                                } catch (IOException e) {
                                    Log.e("MemoMate", "❌ Error creating streak file", e);
                                }
                            }

                            BufferedReader reader = null;
                            FileWriter writer = null;
                            try {
                                // Read the current streak
                                reader = new BufferedReader(new FileReader(file));
                                String line = reader.readLine();
                                reader.close(); // Close reader before writing

                                int streak = (line == null || line.isEmpty()) ? 0 : Integer.parseInt(line);
                                streak++; // Increment streak

                                // Write updated streak
                                writer = new FileWriter(file, false); // false = overwrite mode
                                writer.write(String.valueOf(streak));
                                writer.close(); // Close writer after writing

                                // Update UI
                                ((TextView) context.findViewById(R.id.streak)).setText(String.valueOf(streak));

                                Log.d("MemoMate", "✅ Streak updated: " + streak);
                            } catch (IOException | NumberFormatException e) {
                                Log.e("MemoMate", "❌ Error updating streak", e);
                            } finally {
                                try {
                                    if (reader != null) reader.close();
                                    if (writer != null) writer.close();
                                } catch (IOException e) {
                                    Log.e("MemoMate", "❌ Error closing file streams", e);
                                }
                            }

                        } else {
                            Log.e("MemoMate", "❌ Error: Failed to move file to completed_tasks folder.");
                        }
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
