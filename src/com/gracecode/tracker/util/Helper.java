package com.gracecode.tracker.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.gracecode.tracker.R;
import com.umeng.api.sns.UMSnsService;

import java.io.ByteArrayOutputStream;

public class Helper {
    private Context context;

    public Helper(Context context) {
        this.context = context;
    }

    public byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public void shareToSina(Context context, String message, byte[] bitmap) {
        UMSnsService.shareToSina(context, bitmap, message, null);
    }

    public void shareToSina(Context context, String message) {
        UMSnsService.shareToSina(context, message, null);
    }

    public boolean isGPSProvided() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ? true : false;
    }

    public void showLongToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public void showShortToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void showModalDialog(String title, String message, View view,
                                final Runnable runOnPositiveButtonSelected,
                                final Runnable runOnNegativeButtonSelected) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title)
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert);

        if (view != null) {
            dialog.setView(view);
        }

        dialog.setPositiveButton(context.getString(R.string.btn_ok),
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    runOnPositiveButtonSelected.run();
                }
            });

        dialog.setNegativeButton(context.getString(R.string.btn_cancel),
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    runOnNegativeButtonSelected.run();
                }
            });

        dialog.show();
    }


    public void showConfirmDialog(String title, String message,
                                  final Runnable runOnPositiveButtonSelected,
                                  final Runnable runOnNegativeButtonSelected) {
        showModalDialog(title, message, null, runOnPositiveButtonSelected, runOnNegativeButtonSelected);
    }

    public static class Logger {
        protected static final String TAG = "Tracker";

        public static void i(String message) {
            Log.i(TAG, message);
        }

        public static void e(String message) {
            Log.e(TAG, message);
        }

        public static void w(String message) {
            Log.w(TAG, message);
        }
    }
}
