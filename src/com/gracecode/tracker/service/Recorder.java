package com.gracecode.tracker.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import com.gracecode.tracker.R;
import com.gracecode.tracker.activity.Preference;
import com.gracecode.tracker.dao.Archive;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.util.Logger;
import com.gracecode.tracker.util.Notifier;
import com.gracecode.tracker.util.UIHelper;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 */
interface Binder {
    public static final int STATUS_RUNNING = 0x0000;
    public static final int STATUS_STOPPED = 0x1111;

    public void startRecord();

    public void stopRecord();

    public int getStatus();

    public ArchiveMeta getMeta();

    public Archive getArchive();

    public Location getLastRecord();
}

public class Recorder extends Service {
    protected Recorder.ServiceBinder serviceBinder;
    private SharedPreferences sharedPreferences;
    private Archive archive;

    private Listener listener;
    private LocationManager locationManager = null;

    private ArchiveNameHelper nameHelper;
    private String archivName;
    private UIHelper uiHelper;
    private Context context;
    private Notifier notifier;

    public class ServiceBinder extends android.os.Binder implements Binder {
        private int status = ServiceBinder.STATUS_STOPPED;
        private ArchiveMeta meta = null;
        private TimerTask notifierTask;
        private Timer timer = null;

        ServiceBinder() {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            archive = new Archive(getApplicationContext());
            listener = new Listener(archive);
        }

        @Override
        public void startRecord() {
            if (status != ServiceBinder.STATUS_RUNNING) {
                // 从配置文件获取距离和精度选项
                long minTime = Long.parseLong(sharedPreferences.getString(Preference.GPS_MINTIME,
                    Preference.DEFAULT_GPS_MINTIME));
                float minDistance = Float.parseFloat(sharedPreferences.getString(Preference.GPS_MINDISTANCE,
                    Preference.DEFAULT_GPS_MINDISTANCE));

                // 判定是否上次为异常退出
                if (nameHelper.hasResumeName()) {
                    archivName = nameHelper.getResumeName();
                    uiHelper.showLongToast(
                        String.format(
                            getString(R.string.use_resume_archive_file, archivName)
                        ));
                } else {
                    archivName = nameHelper.getNewName();
                }

                try {
                    archive.open(archivName, Archive.MODE_READ_WRITE);
                    nameHelper.setLastOpenedName(archivName);

                    // 获取 Meta 信息
                    meta = getMeta();

                    // 设置开始时间
                    meta.setStartTime(new Date());

                    // 绑定 GPS 回调
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        minTime, minDistance, listener);

                } catch (SQLiteException e) {
                    Logger.e(e.getMessage());
                }

                // 另开个线程展示通知信息
                notifierTask = new TimerTask() {
                    @Override
                    public void run() {
                        float distance = meta.getDistance();
                        long count = meta.getRawCount();
                        if (count > 0) {
                            notifier.setRecords(count);
                        }
                        if (distance > 0) {
                            notifier.setDistance(distance);
                        }
                        notifier.publish();
                    }
                };

                timer = new Timer();
                timer.schedule(notifierTask, 0, 5000);
                status = ServiceBinder.STATUS_RUNNING;
            }
        }

        @Override
        public void stopRecord() {
            if (status == ServiceBinder.STATUS_RUNNING) {
                locationManager.removeUpdates(listener);

                long totalCount = getMeta().getRawCount();
                if (totalCount <= 0) {
                    (new File(archivName)).delete();
                    uiHelper.showLongToast(getString(R.string.not_record_anything));
                } else {
                    // 设置结束记录时间
                    meta.setEndTime(new Date());

                    uiHelper.showLongToast(String.format(
                        getString(R.string.result_report), String.valueOf(totalCount)
                    ));
                }

                // 清除操作
                archive.close();
                notifier.cancel();
                timer.cancel();
                nameHelper.clearLastOpenedName();

                status = ServiceBinder.STATUS_STOPPED;
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
        this.uiHelper = new UIHelper(context);
        this.serviceBinder = new ServiceBinder();

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
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }
}
