package com.gracecode.gpsrecorder.activity;

import android.app.Activity;
import android.os.Bundle;
import com.gracecode.gpsrecorder.dao.GPSDatabase;
import com.gracecode.gpsrecorder.util.Configure;
import com.mobclick.android.MobclickAgent;

import java.util.Date;


public class BaseActivity extends Activity {
    protected GPSDatabase gpsDatabase;
    protected Configure configure;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configure = Configure.getInstance(getApplication());

        if (gpsDatabase == null) {
            gpsDatabase = GPSDatabase.getInstance(configure.getDatabaseFile(new Date()));
        }
        MobclickAgent.onError(this);
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
}
