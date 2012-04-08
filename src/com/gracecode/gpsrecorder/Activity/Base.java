package com.gracecode.gpsrecorder.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import com.gracecode.gpsrecorder.service.Recoder;
import com.gracecode.gpsrecorder.util.UIHelper;
import com.mobclick.android.MobclickAgent;

public class Base extends Activity {
    protected SharedPreferences sharedPreferences;
    protected UIHelper uiHelper;
    public Intent recordServerIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        uiHelper = new UIHelper(this);
        MobclickAgent.onError(this);
    }

    protected Recoder.ServiceBinder serviceBinder = null;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            serviceBinder = (Recoder.ServiceBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBinder = null;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        recordServerIntent = new Intent(getApplicationContext(), Recoder.class);
        startService(recordServerIntent);
        bindService(recordServerIntent, serviceConnection, BIND_AUTO_CREATE);
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (serviceBinder != null) {
            unbindService(serviceConnection);
        }
        MobclickAgent.onPause(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
