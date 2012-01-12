package com.gracecode.gpsrecorder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import com.gracecode.gpsrecorder.dao.GPSDatabase;
import com.gracecode.gpsrecorder.dao.LocationItem;
import com.gracecode.gpsrecorder.util.Environment;
import com.gracecode.gpsrecorder.util.GPSWatcher;

/**
 *
 */
interface RecordServerBinder {
    public static final int STATUS_RUNNING = 0x0000;
    public static final int STATUS_STOPPED = 0x1111;

    public void startRecord();

    public void stopRecord();

    public int getStatus();

    public LocationItem getLastRecord();
}

public class RecordService extends Service {
    private SharedPreferences sharedPreferences;
    private GPSDatabase gpsDatabase;
    private RecordService.ServiceBinder serviceBinder;

    /**
     *
     */
    public class ServiceBinder extends Binder implements RecordServerBinder {
        private int status = ServiceBinder.STATUS_STOPPED;
        private GPSWatcher gpsWatcher;
        private LocationManager locationManager;

        ServiceBinder() {
            gpsWatcher = new GPSWatcher(getApplicationContext(), gpsDatabase);
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        @Override
        public void startRecord() {
            if (status != ServiceBinder.STATUS_RUNNING) {
                Log.v(TAG, "Start gps records server");

                long minTime = Long.parseLong(sharedPreferences.getString(Environment.PREF_GPS_MINTIME, Environment.DEFAULT_GPS_MINTIME));
                float minDistance = Float.parseFloat(sharedPreferences.getString(Environment.PREF_GPS_MINDISTANCE, Environment.DEFAULT_GPS_MINDISTANCE));
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsWatcher);

                gpsDatabase.addMeta("START_TIME", String.valueOf(System.currentTimeMillis()));
                status = ServiceBinder.STATUS_RUNNING;
            }
        }

        @Override
        public void stopRecord() {
            if (status == ServiceBinder.STATUS_RUNNING) {
                locationManager.removeUpdates(gpsWatcher);
                status = ServiceBinder.STATUS_STOPPED;
                gpsDatabase.addMeta("STOP_TIME", String.valueOf(System.currentTimeMillis()));
                Log.v(TAG, "Gps records server is stopped");
            }
        }

        @Override
        public int getStatus() {
            return status;
        }

        @Override
        public LocationItem getLastRecord() {
            return gpsDatabase.getLastRecord();
        }
    }


    private final String TAG = RecordService.class.getName();

//
//    private static final int LED_NOTIFICATION_ID = 0x001;
//    protected static final int AIRPLANE_MODE_ON = 0x010;
//    protected static final int AIRPLANE_MODE_OFF = 0x000;
//    private NotificationManager notificationManager;
//    private LocationManager locationManager;
//    private GPSWatcher gpsWatcher;
//    private ContentResolver contentResolver;
//    private GPSDatabase gpsDatabase;
//    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

//        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        String recordBy = sharedPreferences.getString(Environment.PREF_RECORD_BY, Environment.RECORD_BY_TIMES);
        gpsDatabase = new GPSDatabase(Environment.getDatabaseFile(recordBy));

        serviceBinder = new ServiceBinder();

        boolean autoStart = sharedPreferences.getBoolean(Environment.PREF_AUTO_START, false);
        if (autoStart) {
            serviceBinder.startRecord();
        }
    }


//    private void turnOnLED() {
//        Notification notif = new Notification();
//        notif.ledARGB = 0xFFff0000;
//        notif.flags = Notification.FLAG_SHOW_LIGHTS;
//        notif.ledOnMS = 1000;
//        notif.ledOffMS = 1500;
//        notificationManager.notify(LED_NOTIFICATION_ID, notif);
//    }
//
//    private void turnOffLED() {
//        notificationManager.cancel(LED_NOTIFICATION_ID);
//    }


    /**
     * 绑定 GPS，获得地理位置等信息
     */
//    public void bindLocationListener() {

//    }


//    public void setAirPlaneMode(int mode) {
//        contentResolver = getContentResolver();
//
//        try {
//            Log.e(TAG, "" + Settings.System.getInt(contentResolver, Settings.System.AIRPLANE_MODE_ON));
//        } catch (Settings.SettingNotFoundException e) {
//            Log.e(TAG, e.getMessage());
//        }
//
//        switch (mode) {
//            case AIRPLANE_MODE_OFF:
//                Settings.System.putInt(contentResolver, Settings.System.AIRPLANE_MODE_ON, AIRPLANE_MODE_OFF);
//                break;
//
//            case AIRPLANE_MODE_ON:
//                Settings.System.putInt(contentResolver, Settings.System.AIRPLANE_MODE_ON, AIRPLANE_MODE_ON);
//                break;
//
//            default:
//                return;
//        }
//
//        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
//        sendBroadcast(intent);
//    }
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.e("", "Start Service");
//        turnOnLED();
//        setAirPlaneMode(AIRPLANE_MODE_ON);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        turnOffLED();
//        setAirPlaneMode(AIRPLANE_MODE_OFF);

        serviceBinder.stopRecord();
        gpsDatabase.close(); // Close the database
    }


    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }
}
