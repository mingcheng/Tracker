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
import com.gracecode.tracker.activity.Preference;
import com.gracecode.tracker.dao.Archive;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.util.Helper;
import com.gracecode.tracker.util.Notifier;
import com.mobclick.android.MobclickAgent;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 */
interface Binder {
    public static final int STATUS_RECORDING = 0x0000;
    public static final int STATUS_STOPPED = 0x1111;

    public void startRecord();

    public void stopRecord();

    public int getStatus();

    public ArchiveMeta getMeta();

    public Archive getArchive();

    public Location getLastRecord();
}

public class Recorder extends Service {
    protected static Recorder.ServiceBinder serviceBinder;
    private SharedPreferences sharedPreferences;
    private Archive archive;

    private Listener listener;
    private StatusListener statusListener;
    private LocationManager locationManager = null;

    private ArchiveNameHelper nameHelper;
    private String archivName;
    private Helper helper;
    private Context context;
    private Notifier notifier;

    private static final String RECORDER_SERVER_ID = "Tracker Service";
    private static TimerTask notifierTask;
    private static Timer timer = null;

    public class ServiceBinder extends android.os.Binder implements Binder {
        private int status = ServiceBinder.STATUS_STOPPED;

        ServiceBinder() {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            archive = new Archive(getApplicationContext());
            listener = new Listener(archive);
            statusListener = new StatusListener();
        }

        @Override
        public void startRecord() {
            if (status != ServiceBinder.STATUS_RECORDING) {

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
                    archive.open(archivName, Archive.MODE_READ_WRITE);

                    // 设置开始时间，如果是恢复文件，则就不设置
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
                                float distance = getMeta().getDistance() / ArchiveMeta.TO_KILOMETRE;
                                double avgSpeed = getMeta().getAverageSpeed() * ArchiveMeta.KM_PER_HOUR_CNT;
                                double maxSpeed = getMeta().getMaxSpeed() * ArchiveMeta.KM_PER_HOUR_CNT;

                                notifier.setStatusString(
                                    String.format(getString(R.string.status_format),
                                        distance, avgSpeed, maxSpeed)
                                );
                                notifier.setCostTimeString(getMeta().getCostTimeStringByNow());
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
                status = ServiceBinder.STATUS_RECORDING;
                MobclickAgent.onEventBegin(context, RECORDER_SERVER_ID);
            }
        }

        public GpsStatus getGpsStatus() {
            return locationManager.getGpsStatus(null);
        }

        @Override
        public void stopRecord() {
            if (status == ServiceBinder.STATUS_RECORDING) {
                locationManager.removeUpdates(listener);
                locationManager.removeGpsStatusListener(statusListener);

                long totalCount = getMeta().getCount();
                if (totalCount <= 0) {
                    (new File(archivName)).delete();
                    helper.showLongToast(getString(R.string.not_record_anything));
                } else {
                    // 设置结束记录时间
                    getMeta().setEndTime(new Date());

                    helper.showLongToast(String.format(
                        getString(R.string.result_report), String.valueOf(totalCount)
                    ));
                }

                // 清除操作
                archive.close();
                notifier.cancel();
                timer.cancel();
                nameHelper.clearLastOpenedName();

                status = ServiceBinder.STATUS_STOPPED;
                MobclickAgent.onEventEnd(context, RECORDER_SERVER_ID);
            }
        }

        @Override
        public int getStatus() {
            return status;
        }

        @Override
        public ArchiveMeta getMeta() {
            return archive.getMeta();
        }

        @Override
        public Archive getArchive() {
            return archive;
        }

        @Override
        public Location getLastRecord() {
            return archive.getLastRecord();
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        this.context = getApplicationContext();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.notifier = new Notifier(context);

        this.nameHelper = new ArchiveNameHelper(context);
        this.helper = new Helper(context);
        if (this.serviceBinder == null) {
            this.serviceBinder = new ServiceBinder();
        }

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
        serviceBinder.stopRecord();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }
}
