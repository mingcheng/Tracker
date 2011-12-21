package com.gracecode.gpsrecorder.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.gracecode.gpsrecorder.R;
import com.gracecode.gpsrecorder.RecordServer;
import com.gracecode.gpsrecorder.util.Database;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Activity implements View.OnClickListener {
    private static final String TAG = Main.class.getName();
    private Intent recordServerIntent;
    private Database db;
    private Context context;
    private Timer timer;

    private Handler handle = new Handler() {
        public void handleMessage(Message msg) {
            updateView();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        this.context = this.getApplicationContext();
        db = new Database(context);

        bindElements();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handle.sendMessage(new Message());
            }
        }, 0, 1000);

        recordServerIntent = new Intent(Main.this, RecordServer.class);
        startService(recordServerIntent);
    }


    private void bindElements() {


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop:
                stopService(recordServerIntent);
                finish();
            default:
                return false;
        }
    }


    private void updateView() {

        Cursor result = null;
        try {
            result = db.getReadableDatabase().rawQuery(
                "SELECT * FROM location WHERE del = 0 ORDER BY time DESC LIMIT 1", null);
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        }

        if (result.getCount() <= 0) {
            return;
        }

        double latitude, longitude, speed, bearing, altitude, accuracy;
        String timeStamp;
        result.moveToFirst();
        latitude = result.getDouble(result.getColumnIndex("latitude"));
        longitude = result.getDouble(result.getColumnIndex("longitude"));
        speed = result.getDouble(result.getColumnIndex("speed"));
        bearing = result.getDouble(result.getColumnIndex("bearing"));

        altitude = result.getDouble(result.getColumnIndex("altitude"));
        accuracy = result.getDouble(result.getColumnIndex("accuracy"));

        timeStamp = result.getString(result.getColumnIndex("time"));
        timeStamp = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            .format(new java.util.Date(Long.parseLong(timeStamp)));

        String resultString = String.format(
            "count %d, \n"
                + "latitude %.3f, \n"
                + "longitude %.3f, \n"
                + "speed %.2f, \n"
                + "bearing %.2f,\n"
                + "altitude %.2f,\n"
                + "accuracy %.2f,\n"
                + "time %s\n",
            db.getValvedCount(), latitude, longitude, speed, bearing, altitude, accuracy, timeStamp);

        resultString += String.format("update %s",
            new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));

        TextView t = (TextView) findViewById(R.id.status);
        t.setText(resultString);
    }

    @Override
    public void onClick(View view) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onStop() {
        super.onStop();
        if (db != null) {
            db.close();
        }
        timer.cancel();
    }
}
