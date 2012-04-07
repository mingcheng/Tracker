package com.gracecode.gpsrecorder.util;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import com.gracecode.gpsrecorder.R;

public class Environment extends android.os.Environment {

    public static final String TAG = Environment.class.getName();
    private NotificationManager notificationManager;

    private Context context;

    public static final int LED_NOTIFICATION_ID = 0x001;


    public Environment(Context context) {
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


    public void showModalDialog(String title, String message, View view,
                                final Runnable runOnPositiveButtonSelected, final Runnable runOnNegativeButtonSelected) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title)
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert);

        if (view != null) {
            dialog.setView(view);
        }

        dialog.setPositiveButton(context.getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                runOnPositiveButtonSelected.run();
            }
        });

        dialog.setNegativeButton(context.getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                runOnNegativeButtonSelected.run();
            }
        });

        dialog.show();
    }


    public void showConfirmDialog(String title, String message,
                                  final Runnable runOnPositiveButtonSelected, final Runnable runOnNegativeButtonSelected) {
        showModalDialog(title, message, null, runOnPositiveButtonSelected, runOnNegativeButtonSelected);
    }
}
