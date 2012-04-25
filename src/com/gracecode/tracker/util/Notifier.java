package com.gracecode.tracker.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.gracecode.tracker.R;
import com.gracecode.tracker.activity.Tracker;

public class Notifier extends Notification {
    private NotificationManager notificationManager;
    public static final int LED_NOTIFICATION_ID = 0x001;
    public static final int NOTIFICATION_ID = 0x0001;

    private Context context;
    private Intent intent;

    public Notifier(Context context) {
        super(R.drawable.icon, context.getString(R.string.running), System.currentTimeMillis());

        this.flags |= Notification.FLAG_ONGOING_EVENT;
        this.flags |= Notification.DEFAULT_LIGHTS;
        this.context = context;

        this.intent = new Intent(context, Tracker.class);
        this.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
        this.contentView = new RemoteViews(context.getPackageName(), R.layout.notifier);
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void setDistance(float distance) {
        contentView.setTextViewText(R.id.distance, String.valueOf(distance));
    }

    public void setRecords(long records) {
        contentView.setTextViewText(R.id.records, String.valueOf(records));
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void publish() {
        notificationManager.notify(NOTIFICATION_ID, this);
    }

    public void cancel() {
        notificationManager.cancelAll();
    }
}
