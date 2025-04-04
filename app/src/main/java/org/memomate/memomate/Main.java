package org.memomate.memomate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.memomate.history.History;
import org.memomate.notification.NotificationBuilder;
import org.memomate.notification.NotificationScheduler;
import org.memomate.ui.StrokedTextClockView;
import org.memomate.ui.StrokedTextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Main extends AppCompatActivity {
    private RecyclerView recyclerView;
    private static TaskAdapter taskAdapter;
    private static List<Task> taskList = new ArrayList<>();
    public static int tasks = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        ImageButton addTaskButton = findViewById(R.id.add_task);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskAdapter = new TaskAdapter(this, taskList, true);
        recyclerView.setAdapter(taskAdapter);

        addTaskButton.setOnClickListener(k -> {
            startActivity(new Intent(this, AddTaskActivity.class));
            finish();
        });

        ConstraintLayout constraintLayout = findViewById(R.id.home_page);

        Button exitDrawer = findViewById(R.id.exit_drawer);

        ImageButton popDrawer = findViewById(R.id.menu_icon);
        popDrawer.setOnClickListener(view -> {
            Log.d("DEBUG","popDrawer");
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.clear(R.id.drawer, ConstraintSet.END);
            constraintSet.connect(R.id.drawer, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            popDrawer.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setStartDelay(0)
                    .start();
            popDrawer.setClickable(false);
            findViewById(R.id.filter).animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();

            constraintSet.clear(R.id.exit_drawer, ConstraintSet.START);
            constraintSet.connect(R.id.exit_drawer, ConstraintSet.START, R.id.drawer, ConstraintSet.END);
            constraintSet.connect(R.id.exit_drawer, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            TransitionManager.beginDelayedTransition(constraintLayout);
            constraintSet.applyTo(constraintLayout);
        });

        exitDrawer.setOnClickListener(view -> {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.clear(R.id.drawer, ConstraintSet.START);
            constraintSet.connect(R.id.drawer, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.START);
            popDrawer.animate()
                    .alpha(1f)
                    .setStartDelay(300)
                    .setDuration(300)
                    .start();

            popDrawer.setClickable(true);

            findViewById(R.id.filter).animate()
                    .alpha(0f)
                    .setDuration(300)
                    .start();

            constraintSet.clear(R.id.exit_drawer, ConstraintSet.START);
            constraintSet.clear(R.id.exit_drawer, ConstraintSet.END);
            constraintSet.connect(R.id.exit_drawer, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.END);
            TransitionManager.beginDelayedTransition(constraintLayout);
            constraintSet.applyTo(constraintLayout);
        });

        findViewById(R.id.home).setOnClickListener(view -> {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.clear(R.id.drawer, ConstraintSet.START);
            constraintSet.connect(R.id.drawer, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.START);
            popDrawer.animate()
                    .alpha(1f)
                    .setStartDelay(300)
                    .setDuration(300)
                    .start();

            popDrawer.setClickable(true);

            findViewById(R.id.filter).animate()
                    .alpha(0f)
                    .setDuration(300)
                    .start();

            constraintSet.clear(R.id.exit_drawer, ConstraintSet.START);
            constraintSet.clear(R.id.exit_drawer, ConstraintSet.END);
            constraintSet.connect(R.id.exit_drawer, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.END);
            TransitionManager.beginDelayedTransition(constraintLayout);
            constraintSet.applyTo(constraintLayout);
        });

        findViewById(R.id.history).setOnClickListener(view -> {
            startActivity(new Intent(this, History.class));
            finish();
        });

        loadTasks();
        try {
            loadStreak();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        NotificationBuilder.createNotificationChannel(this);
        NotificationScheduler.scheduleNotifications(this);
        NotificationScheduler.rescheduleTasks(this);

        ((StrokedTextClockView) findViewById(R.id.text_clock)).onTimeChanged(this::loadTasks);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("ADADADA", "onResume");
        loadTasks();
    }

    public void loadTasks() {
        File tasksDirectory = new File(getFilesDir(), "tasks");
        if (!tasksDirectory.exists() || !tasksDirectory.isDirectory()) {
            return;
        }

        File[] files = tasksDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (files == null) {
            return;
        };

        tasks = files.length;
        ((TextView) findViewById(R.id.tasks_count)).setText(String.valueOf(files.length));

        View noAvailableTaskText = findViewById(R.id.no_available_task_text);
        View taskCompletedIcon = findViewById(R.id.task_completed_icon);

        if (tasks == 0) {
            noAvailableTaskText.setVisibility(View.VISIBLE);
            taskCompletedIcon.setVisibility(View.VISIBLE);
        } else {
            noAvailableTaskText.setVisibility(View.INVISIBLE);
            taskCompletedIcon.setVisibility(View.INVISIBLE);
        }

        taskList.clear();
        for (File file : files) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                String subject = bufferedReader.readLine();
                String taskName = bufferedReader.readLine();
                String dueDate = bufferedReader.readLine();
                String dueTime = bufferedReader.readLine();

                float daysLeft = getDaysLeft(dueDate, dueTime);
                if (daysLeft <= 0) {
                    File streaks = new File(getFilesDir(), "streak.txt");
                    if (!streaks.exists()) {
                        streaks.createNewFile();
                        FileWriter writer = new FileWriter(streaks);
                        writer.write("0");
                        writer.close();
                        ((TextView) findViewById(R.id.streak_count)).setText("0");
                    }
                }

                taskList.add(new Task(subject, taskName, dueDate, dueTime));
                Log.d("MemoMate", "✅ Loaded Task: " + subject + " - " + taskName);
            } catch (Exception e) {
                Log.d("MemoMate", "❌ Error reading file: " + file.getName() + " - " + e.getMessage());
            }
        }

        taskAdapter.notifyDataSetChanged();
    }

    public static class Task {
        public String subject;
        public String taskName;
        public String dueDate;
        public String dueTime;

        public Task(String subject, String taskName, String dueDate, String dueTime) {
            this.subject = subject;
            this.taskName = taskName;
            this.dueDate = dueDate;
            this.dueTime = dueTime;
        }
    }

    public void loadStreak() throws IOException {
        File file = new File(getFilesDir(), "streak.txt");
        if (!file.exists()) {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write("0");
            writer.close();
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        reader.close();

        int streakValue = (line == null || line.isEmpty()) ? 0 : Integer.parseInt(line);

        ((TextView) findViewById(R.id.streak_count)).setText(String.valueOf(streakValue));
    }

    private float getDaysLeft(String dueDate, String dueTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date dueDateTime = sdf.parse(dueDate + " " + dueTime);
            if (dueDateTime == null) return -1;

            long currentTimeMillis = System.currentTimeMillis();
            long dueTimeMillis = dueDateTime.getTime();

            long diffMillis = dueTimeMillis - currentTimeMillis;
            if (diffMillis <= 0) return 0; // Task is due

            // Convert to days
            float daysLeft = (float) diffMillis / (1000 * 60 * 60 * 24);

            // If ≤ 1 day, return hours instead
            if (daysLeft <= 1) {
                return (float) diffMillis / (1000 * 60 * 60); // Convert to hours
            }

            return Float.parseFloat(String.format(Locale.getDefault(), "%.2f", daysLeft));
        } catch (ParseException e) {
            return -1; // Error case
        }
    }
}
