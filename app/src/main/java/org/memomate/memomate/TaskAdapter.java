package org.memomate.memomate;

import android.app.AlertDialog;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.memomate.notification.NotificationBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Main.Task> taskList;
    private final AppCompatActivity context;
    private final boolean completable;

    public TaskAdapter(AppCompatActivity context, List<Main.Task> taskList, boolean completable) {
        this.context = context;
        this.taskList = taskList;
        this.completable = completable;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_entry, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Main.Task task = taskList.get(position);
        holder.subjectText.setText(task.subject);
        holder.taskNameText.setText(task.taskName);
        holder.dueDateText.setText("Due: " + task.dueDate + " " + task.dueTime);

        float timeLeft = getTimeLeft(task.dueDate, task.dueTime);

        if (timeLeft > 0) {
            String timeUnit;
            String formattedTime;

            if (timeLeft <= 24) {
                // Show hours when ≤ 1 day
                formattedTime = String.format(Locale.getDefault(), "%.2f", timeLeft);
                timeUnit = "hour(s)";
            } else {
                // Show days otherwise
                formattedTime = String.format(Locale.getDefault(), "%.2f", timeLeft / 24);
                timeUnit = "day(s)";
            }

            holder.dayLeft.setText(formattedTime);
            holder.daysOrHours.setText(timeUnit);

            // Update Notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationBuilder.getID())
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle("Assignment Reminder")
                    .setContentText("The task \"" + task.taskName + "\" is due in " + formattedTime + " " + timeUnit)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true);
            NotificationBuilder.showNotification(context, builder);

        } else {
            // Overdue case
            holder.dayLeft.setText("Overdue");
            holder.daysOrHours.setText("");

            if (completable) {
                ((TextView) context.findViewById(R.id.streak_count)).setText("0");
                try {
                    File file = new File(context.getFilesDir(), "streak.txt");
                    new FileWriter(file, false).write("0");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (completable)
                showCompletionDialog(position);
        });
    }

    private float getTimeLeft(String dueDate, String dueTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date dueDateTime = sdf.parse(dueDate + " " + dueTime);
            if (dueDateTime == null) return -1;

            long currentTimeMillis = System.currentTimeMillis();
            long dueTimeMillis = dueDateTime.getTime();

            long diffMillis = dueTimeMillis - currentTimeMillis;
            if (diffMillis <= 0) return 0; // Task is due

            float hoursLeft = (float) diffMillis / (1000 * 60 * 60);

            return hoursLeft; // Return hours directly
        } catch (ParseException e) {
            return -1; // Error case
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView subjectText, taskNameText, dueDateText, dayLeft, daysOrHours;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectText = itemView.findViewById(R.id.subject);
            taskNameText = itemView.findViewById(R.id.taskName);
            dueDateText = itemView.findViewById(R.id.dueDate);
            dayLeft = itemView.findViewById(R.id.dayleft);
            daysOrHours = itemView.findViewById(R.id.tv2);
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

                            ((TextView)context.findViewById(R.id.tasks_count)).setText(String.valueOf(files.length));

                            File file = new File(context.getFilesDir(), "streak.txt");
                            if (!file.exists()) {
                                try {
                                    file.createNewFile();
                                    FileWriter initWriter = new FileWriter(file);
                                    initWriter.write("0");
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
                                ((TextView) context.findViewById(R.id.streak_count)).setText(String.valueOf(streak));

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
