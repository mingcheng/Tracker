package com.gracecode.tracker.activity.base;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import com.gracecode.tracker.R;
import com.gracecode.tracker.service.Recorder;
import com.gracecode.tracker.util.Helper;
import com.markupartist.android.widget.ActionBar;
import com.mobclick.android.MobclickAgent;

public abstract class Activity extends FragmentActivity {
    protected SharedPreferences sharedPreferences;
    protected Helper helper;
    public Intent recordServerIntent;
    protected ActionBar actionBar;
    protected Activity context;
    protected Recorder.ServiceBinder serviceBinder = null;
    protected FragmentManager fragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        helper = new Helper(this);
        fragmentManager = getSupportFragmentManager();

        MobclickAgent.onError(this);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            serviceBinder = (Recorder.ServiceBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBinder = null;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        recordServerIntent = new Intent(getApplicationContext(), Recorder.class);
        startService(recordServerIntent);
        bindService(recordServerIntent, serviceConnection, BIND_AUTO_CREATE);

        actionBar = (ActionBar) findViewById(R.id.action_bar);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAction(new ActionBar.Action() {
            @Override
            public int getDrawable() {
                return R.drawable.ic_menu_home;
            }

            @Override
            public void performAction(View view) {
                finish();
            }
        });
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
