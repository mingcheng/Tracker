package com.gracecode.tracker.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import com.gracecode.tracker.R;
import com.gracecode.tracker.service.Recoder;
import com.gracecode.tracker.util.UIHelper;
import com.markupartist.android.widget.ActionBar;
import com.mobclick.android.MobclickAgent;

public class Base extends Activity {
    protected SharedPreferences sharedPreferences;
    protected UIHelper uiHelper;
    public Intent recordServerIntent;
    protected ActionBar actionBar;
    protected Base context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
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
    public void onStart() {
        super.onStart();
        recordServerIntent = new Intent(getApplicationContext(), Recoder.class);
        startService(recordServerIntent);
        bindService(recordServerIntent, serviceConnection, BIND_AUTO_CREATE);

        actionBar = (ActionBar) findViewById(R.id.action_bar);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (serviceBinder != null) {
            unbindService(serviceConnection);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
