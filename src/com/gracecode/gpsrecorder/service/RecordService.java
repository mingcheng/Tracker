package com.gracecode.gpsrecorder.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import com.gracecode.gpsrecorder.activity.Preference;
import com.gracecode.gpsrecorder.recorder.Archive;
import com.gracecode.gpsrecorder.recorder.ArchiveMeta;
import com.gracecode.gpsrecorder.recorder.Listener;
import com.gracecode.gpsrecorder.util.Logger;

/**
 *
 */
interface RecordServerBinder {
    public static final int STATUS_RUNNING = 0x0000;
    public static final int STATUS_STOPPED = 0x1111;

    public void startRecord();

    public void stopRecord();

    public int getStatus();

    public ArchiveMeta getArchiveMeta();

    public Archive getArchive();

    public Location getLastRecord();
}

public class RecordService extends Service {
    protected RecordService.ServiceBinder serviceBinder;
    private SharedPreferences sharedPreferences;
    private Archive geoArchive;

    private Listener listener;
    private LocationManager locationManager;

    private ArchiveFileNameHelper archiveFileNameHelper;
    private String archiveFileName;


    public class ServiceBinder extends Binder implements RecordServerBinder {
        private int status = ServiceBinder.STATUS_STOPPED;

        ServiceBinder() {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            geoArchive = new Archive(getApplicationContext());
            listener = new Listener(geoArchive);
        }

        @Override
        public void startRecord() {
            if (status != ServiceBinder.STATUS_RUNNING) {
                // 从配置文件获取距离和精度选项
                long minTime = Long.parseLong(sharedPreferences.getString(Preference.GPS_MINTIME,
                    Preference.DEFAULT_GPS_MINTIME));
                float minDistance = Float.parseFloat(sharedPreferences.getString(Preference.GPS_MINDISTANCE,
                    Preference.DEFAULT_GPS_MINDISTANCE));

                if (archiveFileNameHelper.hasResumeArchiveFile()) {
                    archiveFileName = archiveFileNameHelper.getResumeArchiveFileName();
                } else {
                    archiveFileName = archiveFileNameHelper.getNewArchiveFileName();
                }

                try {
                    geoArchive.open(archiveFileName);


//                lightningLed = sharedPreferences.getBoolean(Preference.LIGHTNING_LED, true);
//                switchAirplaneMode = sharedPreferences.getBoolean(Preference.SWITCH_AIRPLANE_MODE, false);

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, listener);

                } catch (SQLiteException e) {
                    Logger.e(e.getMessage());
                }

//                if (lightningLed) {
//                    environment.turnOnLED();
//                }
//                if (switchAirplaneMode) {
//                    currentAirPlaneMode = environment.getCurrentAirPlaneMode();
//                    environment.setAirPlaneMode(Environment.AIRPLANE_MODE_ON);
//                }

                status = ServiceBinder.STATUS_RUNNING;
            }
        }

        @Override
        public void stopRecord() {
            if (status == ServiceBinder.STATUS_RUNNING) {
                locationManager.removeUpdates(listener);
                geoArchive.close();

//                if (lightningLed) {
//                    environment.turnOffLED();
//                }
//                if (switchAirplaneMode) {
//                    environment.setAirPlaneMode(currentAirPlaneMode);
//                }

                status = ServiceBinder.STATUS_STOPPED;
            }
        }

        @Override
        public int getStatus() {
            return status;
        }

        @Override
        public ArchiveMeta getArchiveMeta() {
            return geoArchive.getArchiveMeta();
        }

        @Override
        public Archive getArchive() {
            return geoArchive;
        }

        @Override
        public Location getLastRecord() {
            return geoArchive.getLastRecord();
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        this.archiveFileNameHelper = new ArchiveFileNameHelper(getApplicationContext());

        serviceBinder = new ServiceBinder();

        boolean autoStart = sharedPreferences.getBoolean(Preference.AUTO_START, false);
        if (autoStart) {
            serviceBinder.startRecord();
        }
    }


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }
}
