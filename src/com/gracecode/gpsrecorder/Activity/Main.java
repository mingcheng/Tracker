package com.gracecode.gpsrecorder.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.gracecode.gpsrecorder.RecordServer;

public class Main extends Activity implements View.OnClickListener {
    private static final String TAG = Main.class.getName();
    private Intent recordServerIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);

        recordServerIntent = new Intent(Main.this, RecordServer.class);

        //bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(recordServerIntent);
    }

    @Override
    public void onPause() {
//          stopService(i);
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public void onDestroy() {

        //stopService(recordServerIntent);
        super.onDestroy();
    }
}
