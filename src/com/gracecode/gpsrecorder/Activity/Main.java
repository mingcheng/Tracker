package com.gracecode.gpsrecorder.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.gracecode.gpsrecorder.R;
import com.gracecode.gpsrecorder.RecordServer;
import com.gracecode.gpsrecorder.util.Database;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends BaseActivity {
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

        initialViewUpdater();

        recordServerIntent = new Intent(Main.this, RecordServer.class);
        startService(recordServerIntent);
    }

    private void initialViewUpdater() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handle.sendMessage(new Message());
            }
        }, 0, 1000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop:
                stopService(recordServerIntent);
                finish();
                return true;

            case R.id.records:
                Intent t = new Intent(Main.this, Records.class);
                startActivity(t);
                return true;

            case R.id.about:
                Dialog dialog = new Dialog(this);
                dialog.setTitle(R.string.app_name);
                dialog.setContentView(R.layout.about);
                dialog.show();
                return true;

            default:
                return false;
        }
    }


    private static double maxSpeed = 0.0;

    private void updateView() {
        String resultString = "";

        try {
            Cursor result = null;
            SQLiteDatabase tmpDb = db.getReadableDatabase();
            result = tmpDb.rawQuery(
                "SELECT * FROM location WHERE del = 0 ORDER BY time DESC LIMIT 1", null);

            if (result.getCount() <= 0) {
                resultString = getResources().getString(R.string.is_empty);
            } else {

                double latitude, longitude, speed, bearing, altitude, accuracy;
                String timeStamp;
                result.moveToFirst();

                latitude = result.getDouble(result.getColumnIndex("latitude"));
                longitude = result.getDouble(result.getColumnIndex("longitude"));

                speed = result.getDouble(result.getColumnIndex("speed"));
                if (maxSpeed < speed) {
                    maxSpeed = speed;
                }

                bearing = result.getDouble(result.getColumnIndex("bearing"));
                altitude = result.getDouble(result.getColumnIndex("altitude"));
                accuracy = result.getDouble(result.getColumnIndex("accuracy"));

                timeStamp = result.getString(result.getColumnIndex("time"));
                timeStamp = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                    .format(new java.util.Date(Long.parseLong(timeStamp)));

                resultString = String.format(
                    "count %d, \n"
                        + "latitude %.3f, \n"
                        + "longitude %.3f, \n"
                        + "speed %.2f / %.2f, \n"
                        + "bearing %.2f,\n"
                        + "altitude %.2f,\n"
                        + "accuracy %.2f,\n"
                        + "time %s\n",
                    db.getValvedCount(), latitude, longitude, speed, maxSpeed, bearing, altitude, accuracy, timeStamp);

                resultString += String.format("update %s",
                    new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));

            }

            result.close();
            tmpDb.close();
        } catch (SQLiteException e) {
            resultString = e.getMessage();
        }

        TextView t = (TextView) findViewById(R.id.status);
        t.setText(resultString);
    }

//    @Override
//    public void onClick(View view) {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    public void onResume() {
//        super.onResume();
//        if (timer != null) {
//            timer.cancel();
//        }
//        initialViewUpdater();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//    }

    @Override
    public void onDestroy() {
        Toast.makeText(context, getString(R.string.still_running), Toast.LENGTH_LONG).show();
        if (db != null) {
            db.close();
        }
        timer.cancel();
        super.onDestroy();
    }
}
