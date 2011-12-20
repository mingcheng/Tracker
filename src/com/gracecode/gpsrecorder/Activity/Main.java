package com.gracecode.gpsrecorder.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.gracecode.gpsrecorder.RecordServer;

public class Main extends Activity {
    Intent i;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);

        i = new Intent(Main.this, RecordServer.class);
        startService(i);


    }

    @Override
    public void onPause() {
//          stopService(i);
        super.onPause();
    }
}
