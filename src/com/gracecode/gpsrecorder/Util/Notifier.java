package com.gracecode.gpsrecorder.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

public class Notifier {
    protected Context context;
    private NotificationManager notificationManager;
    public static final int LED_NOTIFICATION_ID = 0x001;


    public Notifier(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }


    public void turnOnLED() {
        Notification notif = new Notification();
        notif.ledARGB = 0xFFff0000;
        notif.flags = Notification.FLAG_SHOW_LIGHTS;
        notif.ledOnMS = 1000;
        notif.ledOffMS = 1500;
        notificationManager.notify(LED_NOTIFICATION_ID, notif);
    }

    public void turnOffLED() {
        notificationManager.cancel(LED_NOTIFICATION_ID);
    }

}
