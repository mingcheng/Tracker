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
import android.widget.Toast;
import com.gracecode.gpsrecorder.activity.Preference;
import com.gracecode.gpsrecorder.dao.GPSDatabase;
import com.gracecode.gpsrecorder.dao.Points;
import com.gracecode.gpsrecorder.util.Environment;
import com.gracecode.gpsrecorder.util.GPSWatcher;

import java.io.File;

/**
 *
 */
interface RecordServerBinder {
    public static final int STATUS_RUNNING = 0x0000;
    public static final int STATUS_STOPPED = 0x1111;

    public void startRecord();

    public void stopRecord();

    public int getStatus();

    public Points getLastRecord();
}

public class RecordService extends Service {
    private final String TAG = RecordService.class.getName();
    private SharedPreferences sharedPreferences;
    private GPSDatabase gpsDatabase;
    private RecordService.ServiceBinder serviceBinder;
    private File gpsDatabaseFile;


    private int currentAirPlaneMode;
    private boolean switchAirplaneMode;
    private Boolean lightningLed;
    private Environment environment;
    private static final String LAST_OPENED_DATABASE_PATH = "lastOpenedDatabasePath";
    private String lastOpenedDatabasePath;


    public class ServiceBinder extends Binder implements RecordServerBinder {
        private int status = ServiceBinder.STATUS_STOPPED;
        private GPSWatcher gpsWatcher;
        private LocationManager locationManager;

        ServiceBinder() {
            gpsWatcher = new GPSWatcher(getApplicationContext(), gpsDatabase);
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            lightningLed = sharedPreferences.getBoolean(Preference.LIGHTNING_LED, true);
        }

        @Override
        public void startRecord() {
            if (status != ServiceBinder.STATUS_RUNNING) {
                long minTime = Long.parseLong(sharedPreferences.getString(Preference.GPS_MINTIME,
                    Preference.DEFAULT_GPS_MINTIME));
                float minDistance = Float.parseFloat(sharedPreferences.getString(Preference.GPS_MINDISTANCE,
                    Preference.DEFAULT_GPS_MINDISTANCE));

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsWatcher);

                if (lightningLed) {
                    environment.turnOnLED();
                }
                if (switchAirplaneMode) {
                    currentAirPlaneMode = environment.getCurrentAirPlaneMode();
                    environment.setAirPlaneMode(Environment.AIRPLANE_MODE_ON);
                }

                status = ServiceBinder.STATUS_RUNNING;
            }
        }

        @Override
        public void stopRecord() {
            if (status == ServiceBinder.STATUS_RUNNING) {
                locationManager.removeUpdates(gpsWatcher);

                if (lightningLed) {
                    environment.turnOffLED();
                }
                if (switchAirplaneMode) {
                    environment.setAirPlaneMode(currentAirPlaneMode);
                }

                clearLastOpenedDatabaseFilePath();
                status = ServiceBinder.STATUS_STOPPED;
            }
        }

        @Override
        public int getStatus() {
            return status;
        }

        @Override
        public Points getLastRecord() {
            return gpsDatabase.getLastRecord();
        }
    }

    private File getLastOpenedDatabaseFile() {
        File dbfile = null;
        lastOpenedDatabasePath = sharedPreferences.getString(LAST_OPENED_DATABASE_PATH, "");
        if (lastOpenedDatabasePath.length() != 0) {
            dbfile = new File(lastOpenedDatabasePath);
        }

        return dbfile;
    }

    private boolean clearLastOpenedDatabaseFilePath() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LAST_OPENED_DATABASE_PATH, "");
        return editor.commit();
    }

    private boolean setLastOpenedDatabaseFilePath(File file) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LAST_OPENED_DATABASE_PATH, file.getAbsolutePath());
        return editor.commit();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String recordBy = sharedPreferences.getString(Preference.RECORD_BY, Preference.RECORD_BY_TIMES);

        // resume the database file which not close by application self.
        File lastOpenedDatabaseFile = getLastOpenedDatabaseFile();
        Boolean useRecoveryDatabaseFile = (lastOpenedDatabaseFile != null && lastOpenedDatabaseFile.isFile());
        gpsDatabaseFile = useRecoveryDatabaseFile ?
            lastOpenedDatabaseFile : Environment.getDatabaseFile(recordBy);

        environment = new Environment(getApplicationContext());
        gpsDatabase = new GPSDatabase(gpsDatabaseFile);
        serviceBinder = new ServiceBinder();

        if (useRecoveryDatabaseFile) {
            gpsDatabase.addMeta(GPSDatabase.Meta.RESUME_TIME, String.valueOf(System.currentTimeMillis()));
            Toast.makeText(this, getString(R.string.use_recovery_database_file), Toast.LENGTH_LONG).show();
        } else {
            gpsDatabase.addMeta(GPSDatabase.Meta.START_TIME, String.valueOf(System.currentTimeMillis()));
        }
        setLastOpenedDatabaseFilePath(gpsDatabaseFile);

        boolean autoStart = sharedPreferences.getBoolean(Preference.AUTO_START, false);
        if (autoStart) {
            serviceBinder.startRecord();
        }
    }


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        currentAirPlaneMode = environment.getCurrentAirPlaneMode();
        switchAirplaneMode = sharedPreferences.getBoolean(Preference.SWITCH_AIRPLANE_MODE, false);
    }

    @Override
    public void onDestroy() {
        long valvedCount = gpsDatabase.getValvedCount();
        String resultMessage = String.format(getString(R.string.result_report), valvedCount);
        boolean autoClean = sharedPreferences.getBoolean(Preference.AUTO_CLEAN, true);

        if (switchAirplaneMode) {
            environment.setAirPlaneMode(currentAirPlaneMode);
        }
        serviceBinder.stopRecord();
        gpsDatabase.addMeta(GPSDatabase.Meta.STOP_TIME, String.valueOf(System.currentTimeMillis()));
        gpsDatabase.close();

        if (autoClean && valvedCount <= 0) {
            resultMessage = getString(R.string.not_record_anything);
            gpsDatabaseFile.delete();
            Log.w(TAG, "Records is finished, but without any records, clean it");
        }

        Toast.makeText(this, resultMessage, Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }
}
