package com.gracecode.gpsrecorder.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import com.gracecode.gpsrecorder.RecordService;
import com.gracecode.gpsrecorder.dao.GPSDatabase;
import com.mobclick.android.MobclickAgent;

public class BaseActivity extends Activity {
    protected GPSDatabase gpsDatabase;
    protected SharedPreferences sharedPreferences;

    protected RecordService.ServiceBinder serviceBinder;
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            serviceBinder = (RecordService.ServiceBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private Intent recordServerIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        recordServerIntent = new Intent(this, RecordService.class);

        startService(recordServerIntent);
        bindService(recordServerIntent, serviceConnection, BIND_AUTO_CREATE);

        MobclickAgent.onError(this);
    }

    public void stopService() {
        stopService(recordServerIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public void onDestroy() {
        if (serviceBinder != null) {
            unbindService(serviceConnection);
            serviceBinder = null;
        }

        super.onDestroy();
    }
}
