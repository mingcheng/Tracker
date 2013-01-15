package com.gracecode.tracker.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.dao.Archiver;
import com.gracecode.tracker.ui.activity.Preference;
import com.gracecode.tracker.util.Helper;
import com.gracecode.tracker.util.Notifier;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @todo - need to fix recording flag errors
 */
interface Binder {
    public static final int STATUS_RECORDING = 0x0000;
    public static final int STATUS_STOPPED = 0x1111;

    public void startRecord();

    public void stopRecord();

    public int getStatus();

    public ArchiveMeta getMeta();

    public Archiver getArchive();

    public Location getLastRecord();
}

public class Recorder extends Service {
    protected Recorder.ServiceBinder serviceBinder = null;
    private SharedPreferences sharedPreferences;
    private Archiver archiver;

    private Listener listener;
    private StatusListener statusListener;
    private LocationManager locationManager = null;

    private ArchiveNameHelper nameHelper;
    private String archivName;
    private Helper helper;
    private Context context;
    private Notifier notifier;

    private static final String RECORDER_SERVER_ID = "Tracker Service";
    private static final String PREF_STATUS_FLAG = "Tracker Service Status";
    private TimerTask notifierTask;
    private Timer timer = null;

    public class ServiceBinder extends android.os.Binder implements Binder {
        ServiceBinder() {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            archiver = new Archiver(getApplicationContext());
            listener = new Listener(archiver, this);
            statusListener = new StatusListener();
        }

        @Override
        public void startRecord() {
            if (getStatus() != ServiceBinder.STATUS_RECORDING) {
                // 设置启动时更新配置
                notifier = new Notifier(context);

                // 如果没有外置存储卡
                if (!nameHelper.isExternalStoragePresent()) {
                    helper.showLongToast(getString(R.string.external_storage_not_present));
                    return;
                }

                // 从配置文件获取距离和精度选项
                long minTime = Long.parseLong(sharedPreferences.getString(Preference.GPS_MINTIME,
                    Preference.DEFAULT_GPS_MINTIME));
                float minDistance = Float.parseFloat(sharedPreferences.getString(Preference.GPS_MINDISTANCE,
                    Preference.DEFAULT_GPS_MINDISTANCE));

                // 判定是否上次为异常退出
                boolean hasResumeName = nameHelper.hasResumeName();
                if (hasResumeName) {
                    archivName = nameHelper.getResumeName();
                    helper.showLongToast(
                        String.format(
                            getString(R.string.use_resume_archive_file, archivName)
                        ));
                } else {
                    archivName = nameHelper.getNewName();
                }

                try {
                    archiver.open(archivName, Archiver.MODE_READ_WRITE);

                    // Set start time, if not resume from recovery
                    if (!hasResumeName) {
                        getMeta().setStartTime(new Date());
                    }

                    // 绑定 GPS 回调
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        minTime, minDistance, listener);

                    locationManager.addGpsStatusListener(statusListener);

                    // 标记打开的文件，方便奔溃时恢复
                    nameHelper.setLastOpenedName(archivName);
                } catch (SQLiteException e) {
                    Helper.Logger.e(e.getMessage());
                }

                // 另开个线程展示通知信息
                notifierTask = new TimerTask() {
                    @Override
                    public void run() {
                        switch (serviceBinder.getStatus()) {
                            case ServiceBinder.STATUS_RECORDING:
                                ArchiveMeta meta = getMeta();
                                float distance = meta.getDistance() / ArchiveMeta.TO_KILOMETRE;
                                double avgSpeed = meta.getAverageSpeed() * ArchiveMeta.KM_PER_HOUR_CNT;
                                double maxSpeed = meta.getMaxSpeed() * ArchiveMeta.KM_PER_HOUR_CNT;

                                notifier.setStatusString(
                                    String.format(getString(R.string.status_format),
                                        distance, avgSpeed, maxSpeed)
                                );
                                notifier.setCostTimeString(meta.getCostTimeStringByNow());
                                notifier.publish();
                                break;

                            case ServiceBinder.STATUS_STOPPED:
                                notifier.cancel();
                                break;
                        }
                    }
                };
                timer = new Timer();
                timer.schedule(notifierTask, 0, 5000);

                // Set status from shared preferences, default is stopped.
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(PREF_STATUS_FLAG, ServiceBinder.STATUS_RECORDING);
                editor.commit();

                // for umeng
                MobclickAgent.onEventBegin(context, RECORDER_SERVER_ID);
            }
        }

        public GpsStatus getGpsStatus() {
            return locationManager.getGpsStatus(null);
        }

        public void resetStatus() {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(PREF_STATUS_FLAG, ServiceBinder.STATUS_STOPPED);
            editor.commit();
        }

        @Override
        public void stopRecord() {
            if (getStatus() == ServiceBinder.STATUS_RECORDING) {

                // Flush listener cache
                listener.flushCache();

                // Remove listener
                locationManager.removeUpdates(listener);
                locationManager.removeGpsStatusListener(statusListener);

                ArchiveMeta meta = getMeta();
                long totalCount = meta.getCount();
                if (totalCount <= 0) {
                    (new File(archivName)).delete();
                    helper.showLongToast(getString(R.string.not_record_anything));
                } else {
                    meta.setEndTime(new Date());

                    // Show record result by toast
                    helper.showLongToast(String.format(
                        getString(R.string.result_report), String.valueOf(totalCount)
                    ));
                }

                // 清除操作
                archiver.close();
                notifier.cancel();

                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }

                nameHelper.clearLastOpenedName();

                // Set status from preference as stopped.
                resetStatus();

                MobclickAgent.onEventEnd(context, RECORDER_SERVER_ID);
            }
        }

        @Override
        public int getStatus() {
            return sharedPreferences.getInt(PREF_STATUS_FLAG, ServiceBinder.STATUS_STOPPED);
        }

        @Override
        public ArchiveMeta getMeta() {
            return archiver.getMeta();
        }

        @Override
        public Archiver getArchive() {
            return archiver;
        }

        @Override
        public Location getLastRecord() {
            return archiver.getLastRecord();
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        this.context = getApplicationContext();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        this.nameHelper = new ArchiveNameHelper(context);
        this.helper = new Helper(context);
        if (serviceBinder == null) {
            serviceBinder = new ServiceBinder();
        }

        boolean autoStart = sharedPreferences.getBoolean(Preference.AUTO_START, false);
        boolean alreadyStarted = (serviceBinder.getStatus() == ServiceBinder.STATUS_RECORDING);

        if (autoStart || alreadyStarted) {
            if (alreadyStarted) {
                serviceBinder.resetStatus();
            }
            serviceBinder.startRecord();
        }
    }


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        serviceBinder.stopRecord();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }
}
