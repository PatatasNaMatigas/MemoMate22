package org.memomate.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import org.memomate.memomate.R;

import java.util.Calendar;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NotificationReceiver--", "Notification received!");
        String taskName = intent.getStringExtra("taskName");
        long triggerTime = intent.getLongExtra("triggerTime", 0);


        long currentTime = Calendar.getInstance().getTimeInMillis();
        long remainingTime = triggerTime - currentTime;

        String timeMessage = getTimeMessage(remainingTime);

        NotificationBuilder notificationBuilder = new NotificationBuilder();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationBuilder.getID())
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Assignment Reminder")
                .setContentText("The task \"" + taskName + "\" is due " + timeMessage)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true);

        notificationBuilder.showNotification(context, builder);
        Log.d("NotificationReceiver", "Notification sent: " + taskName + " - " + timeMessage);
    }

    private String getTimeMessage(long remainingTimeMillis) {
        long minutes = remainingTimeMillis / (1000 * 60);
        if (minutes >= 60) {
            long hours = minutes / 60;
            return "in " + hours + " hours";
        } else {
            return "in " + minutes + " minutes";
        }
    }
}
