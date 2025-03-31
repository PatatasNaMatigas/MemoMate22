package org.memomate.memomate;

import static org.memomate.notification.NotificationScheduler.saveTask;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {
    private String selectedDate = "", selectedTime = "";
    EditText dueDate = null;
    EditText subject = null;
    EditText taskName = null;
    EditText dueTime = null;
    ImageButton addButton = null;
    ImageButton cancelButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_task);
        initializeFiles();
        initializeUI();
    }
    public void initializeUI() {
        subject = findViewById(R.id.subject);
        taskName = findViewById(R.id.taskName);
        dueDate = findViewById(R.id.dueDate);
        dueTime = findViewById(R.id.dueTime);
        addButton = findViewById(R.id.addButton);
        cancelButton = findViewById(R.id.cancelButton);

        dueDate.setOnClickListener(v -> showDatePicker());
        dueTime.setOnClickListener(v -> showTimePicker());


        addButton.setOnClickListener(view -> {
            String subjectText = subject.getText().toString().trim();
            String taskNameText = taskName.getText().toString().trim();

            if (subjectText.isEmpty() || taskNameText.isEmpty()  || selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            File file = new File(getFilesDir() + "/tasks", subjectText + "-" + taskNameText + ".txt");
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(subjectText + "\n" + taskNameText + "\n" + selectedDate + "\n" + selectedTime);

                startActivity(new Intent(this, Main.class));
                finish();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            saveTask(
                    this,
                    dueDate.getText().toString(),
                    dueTime.getText().toString(),
                    taskName.getText().toString()
            );
        });

        cancelButton.setOnClickListener(view -> {
            startActivity(new Intent(this, Main.class));
            finish();
        });
    }

    public void initializeFiles() {
        File folder = new File(getFilesDir(), "tasks");
        if (!folder.exists()) {
            boolean success = folder.mkdirs();
            if (success) {
                Log.d("FolderCreation", "Folder created successfully!");
            } else {
                Log.e("FolderCreation", "Failed to create folder!");
            }
        }
    }
    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    dueDate.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    dueTime.setText(selectedTime);
                }, hour, minute, true);
        timePickerDialog.show();
    }
}
