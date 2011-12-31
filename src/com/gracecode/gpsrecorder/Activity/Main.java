package com.gracecode.gpsrecorder.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import com.gracecode.gpsrecorder.R;
import com.gracecode.gpsrecorder.RecordServer;
import com.gracecode.gpsrecorder.dao.LocationItem;
import com.gracecode.gpsrecorder.util.Environment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends BaseActivity {
    private static final String TAG = Main.class.getName();
    private Intent recordServerIntent;

    private Context context;
    private Timer timer;

    private static double maxSpeed = 0.0;

    private Handler handle = new Handler() {
        long visible_flag = 0;

        public void handleMessage(Message msg) {
            updateView();

            TextView records = (TextView) findViewById(R.id.status);
            records.setVisibility(++visible_flag % 2 == 0 ? View.INVISIBLE : View.VISIBLE);
        }
    };
    private boolean isServerStoped = false;

    private ArrayList<TextView> textViewsGroup = new ArrayList<TextView>();
    private LocationItem lastLocationItem;


    /**
     * 找到所有的 TextView 元素
     *
     * @param v
     */
    public void findAllTextView(ViewGroup v) {
        for (int i = 0; i < v.getChildCount(); i++) {
            View item = v.getChildAt(i);
            if (item instanceof TextView) {
                textViewsGroup.add((TextView) item);
            } else if (item instanceof ViewGroup) {
                findAllTextView((ViewGroup) item);
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        this.context = this.getApplicationContext();


        if (!Environment.isExternalStoragePresent()) {
            Log.e(TAG, "no SD Card");
            return;
        }

        recordServerIntent = new Intent(Main.this, RecordServer.class);
        startService(recordServerIntent);

        findAllTextView((ViewGroup) findViewById(R.id.root));
        initialViewUpdater();
    }

    private void initialViewUpdater() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handle.sendMessage(new Message());
            }
        }, 0, 1000);

        Typeface face = Typeface.createFromAsset(getAssets(),
            getString(R.string.default_font));
        for (int i = 0; i < textViewsGroup.size(); i++) {
            TextView t = textViewsGroup.get(i);
            t.setTypeface(face);
        }
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
                isServerStoped = true;
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


    private void updateView() {

        lastLocationItem = gpsDatabase.getLastRecords();

        for (int i = 0; i < textViewsGroup.size(); i++) {
            double numberValue = 0;
            String stringValue = "";
            TextView textView = textViewsGroup.get(i);
            int count = lastLocationItem.getCount();

            try {
                switch (textView.getId()) {
                    case R.id.status:
                        if (count > 0) {
                            stringValue = getString(R.string.recording);
                        } else {
                            stringValue = getString(R.string.initialing);
                        }
                        break;
                    case R.id.records:
                        if (count > 0) {
                            stringValue = String.format(getString(R.string.records), count);
                        } else {
                            stringValue = getString(R.string.norecords);
                        }
                        break;
                    case R.id.time:
                        stringValue = new SimpleDateFormat(getString(R.string.time_format))
                            .format(new Date(lastLocationItem.getTime()));
                        break;
                    case R.id.speed:
                        double speed = lastLocationItem.getSpeed();
                        if (maxSpeed < speed) {
                            maxSpeed = speed;
                        }

                        if (maxSpeed != 0.0) {
                            stringValue = String.format("%.1f(%.1f)", speed, maxSpeed);
                        }
                        break;
                    case R.id.longitude:
                        numberValue = lastLocationItem.getLongitude();
                        break;
                    case R.id.latitude:
                        numberValue = lastLocationItem.getLatitude();
                        break;
                    case R.id.bearing:
                        numberValue = lastLocationItem.getBearing();
                        break;
                    case R.id.altitude:
                        numberValue = lastLocationItem.getAltitude();
                        break;
                    case R.id.accuracy:
                        numberValue = lastLocationItem.getAccuracy();
                        break;
                }
            } catch (NullPointerException e) {
                stringValue = getString(R.string.norecords);
            }

            if (stringValue.length() > 0) {
                textView.setText(stringValue);
            } else if (numberValue != 0) {
                textView.setText(String.format("%.2f", numberValue));
            }
        }
    }


    @Override
    public void onDestroy() {
        if (isServerStoped == false) {
            Toast.makeText(context, getString(R.string.still_running), Toast.LENGTH_LONG).show();
        }

        timer.cancel();
        super.onDestroy();
    }
}
