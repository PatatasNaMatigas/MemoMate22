package org.memomate.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NotificationScheduler {

    public static void scheduleNotifications(Context context) {
        File tasksDir = new File(context.getFilesDir(), "tasks");
        if (!tasksDir.exists() || !tasksDir.isDirectory()) {
            Log.d("Scheduler", "Tasks directory does not exist or is not a directory!");
            return;
        }

        File[] taskFiles = tasksDir.listFiles();
        if (taskFiles == null || taskFiles.length == 0) {
            Log.d("Scheduler", "No task files found!");
            return;
        }

        for (File taskFile : taskFiles) {
            if (!taskFile.isFile()) continue;

            try (BufferedReader reader = new BufferedReader(new FileReader(taskFile))) {
                String subjectName = reader.readLine();
                String taskName = reader.readLine();
                String dueDate = reader.readLine();
                String dueTime = reader.readLine();

                Log.d("Scheduler", "Reading task from file: " + taskFile.getName());
                Log.d("Scheduler", "Task details: " + subjectName + ", " + taskName + ", " + dueDate + ", " + dueTime);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Calendar dueCalendar = Calendar.getInstance();
                dueCalendar.setTime(dateFormat.parse(dueDate + " " + dueTime));

                int[] offsets = {3 * 24 * 60, 24 * 60, 6 * 60, 60, 30};

                for (int offset : offsets) {
                    Calendar notifyTime = (Calendar) dueCalendar.clone();
                    notifyTime.add(Calendar.MINUTE, -offset);
                    scheduleAlarm(context, notifyTime, taskName);
                }
            } catch (Exception e) {
                Log.e("Scheduler", "Error scheduling notifications for file: " + taskFile.getName(), e);
            }
        }
    }

    public static void saveTask(Context context, String dueDate, String dueTime, String taskName) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("dueDate", dueDate);
        editor.putString("dueTime", dueTime);
        editor.putString("taskName", taskName);
        editor.apply();
    }

    public static void rescheduleTasks(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String dueDate = prefs.getString("dueDate", null);
        String dueTime = prefs.getString("dueTime", null);
        String taskName = prefs.getString("taskName", null);

        if (dueDate != null && dueTime != null && taskName != null) {
            scheduleNotifications(context);
        }
    }

    private static void scheduleAlarm(Context context, Calendar notifyTime, String taskName) {
        Log.d("Alarm--", "Notification scheduled for: " +
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(notifyTime.getTime()));

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("taskName", taskName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, notifyTime.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notifyTime.getTimeInMillis(), pendingIntent);
        }
    }

}
