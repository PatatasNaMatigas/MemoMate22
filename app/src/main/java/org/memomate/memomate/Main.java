package org.memomate.memomate;

import android.content.Intent;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();
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

        addTaskButton.setOnClickListener(k -> startActivity(new Intent(this, AddTaskActivity.class)));

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
    }

    private void loadTasks() {
        File tasksDirectory = new File(getFilesDir(), "tasks");
        if (!tasksDirectory.exists() || !tasksDirectory.isDirectory()) {
            return;
        }

        File[] files = tasksDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (files == null) return;

        tasks = files.length;
        ((TextView) findViewById(R.id.tasks_count)).setText(String.valueOf(files.length));

        taskList.clear();
        for (File file : files) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                String subject = bufferedReader.readLine();
                String taskName = bufferedReader.readLine();
                String dueDate = bufferedReader.readLine();
                String dueTime = bufferedReader.readLine();

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
}
