package com.example.memomate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

        Button addTaskButton = findViewById(R.id.add_task);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskAdapter = new TaskAdapter(this, taskList);
        recyclerView.setAdapter(taskAdapter);

        addTaskButton.setOnClickListener(k -> startActivity(new Intent(this, AddTaskActivity.class)));

        loadTasks();
    }

    private void loadTasks() {
        File tasksDirectory = new File(getFilesDir(), "tasks");
        if (!tasksDirectory.exists() || !tasksDirectory.isDirectory()) {
            return;
        }

        // ✅ List only `.txt` files
        File[] files = tasksDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (files == null) return;

        tasks = files.length;
        ((TextView) findViewById(R.id.currentTask)).setText(String.valueOf(files.length));

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

        taskAdapter.notifyDataSetChanged(); // Refresh RecyclerView
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
}
