package com.gracecode.gpsrecorder.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import com.gracecode.gpsrecorder.R;
import com.gracecode.gpsrecorder.RecordService;
import com.gracecode.gpsrecorder.RecordService.ServiceBinder;
import com.gracecode.gpsrecorder.dao.Points;
import com.gracecode.gpsrecorder.util.Environment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends BaseActivity {
    private static final String TAG = Main.class.getName();
    private Timer timer;
    private static double maxSpeed = 0.0;

    private ArrayList<TextView> textViewsGroup = new ArrayList<TextView>();
    private Points lastLocationRecord;
    private static final int MESSAGE_UPDATE_STATE_VIEW = 0x0001;

    public Intent recordServerIntent;

    /**
     * Handle the records for show the last recorded status.
     */
    private Handler handle = new Handler() {
        long visible_flag = 0;

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_UPDATE_STATE_VIEW:
                    TextView records = (TextView) findViewById(R.id.status);
                    String statusLabel = getString(R.string.ready);

                    try {
                        switch (serviceBinder.getStatus()) {
                            case ServiceBinder.STATUS_RUNNING:
                                statusLabel = getString(R.string.recording);
                                break;
                            case ServiceBinder.STATUS_STOPPED:
                                statusLabel = getString(R.string.ready);
                            default:
                        }
                        records.setText(statusLabel);
                        records.setVisibility(++visible_flag % 2 == 0 ? View.INVISIBLE : View.VISIBLE);

                        // update the ui status
                        updateView();
                    } catch (NullPointerException e) {
                        Log.e(TAG, "ServerBinder is null, maybe service is not ready.");
                    }
                    break;
            }
        }
    };

    /**
     * 找到所有的 TextView 元素
     *
     * @param v
     */
    private void findAllTextView(ViewGroup v) {
        for (int i = 0; i < v.getChildCount(); i++) {
            View item = v.getChildAt(i);
            if (item instanceof TextView) {
                textViewsGroup.add((TextView) item);
            } else if (item instanceof ViewGroup) {
                findAllTextView((ViewGroup) item);
            }
        }
    }

    private void initialViewUpdater() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = MESSAGE_UPDATE_STATE_VIEW;
                handle.sendMessage(message);
            }
        }, 1000, 1000);

        // change the font for nice look
        Typeface face = Typeface.createFromAsset(getAssets(),
            getString(R.string.default_font));
        for (int i = 0; i < textViewsGroup.size(); i++) {
            TextView t = textViewsGroup.get(i);
            t.setTypeface(face);
        }
    }

    private void updateView() {
        // get last record by server binder
        lastLocationRecord = serviceBinder.getLastRecord();

        for (int i = 0; i < textViewsGroup.size(); i++) {
            double numberValue = 0;
            String stringValue = "";
            TextView textView = textViewsGroup.get(i);
            int count = lastLocationRecord.getCount();
            Boolean isrunning = (serviceBinder.getStatus() == ServiceBinder.STATUS_RUNNING);

            try {
                switch (textView.getId()) {
                    case R.id.status:
                        int stamp = (isrunning) ? R.string.recording : R.string.idle;
                        stringValue = getString(stamp);
                        break;
                    case R.id.records:
                        if (count > 0) {
                            stringValue = String.format(getString(R.string.records), count);
                        } else {
                            stringValue = getString(R.string.norecords);
                        }
                        break;
                    case R.id.time:
                        stringValue = new SimpleDateFormat(getString(R.string.time_format)).format(
                            new Date(isrunning ? lastLocationRecord.getTime() : System.currentTimeMillis()));
                        break;
                    case R.id.speed:
                        double speed = lastLocationRecord.getSpeed();
                        if (maxSpeed < speed) {
                            maxSpeed = speed;
                        }

                        if (maxSpeed != 0.0) {
                            stringValue = String.format("%.1f(%.1f)", speed, maxSpeed);
                        } else {
                            throw new NullPointerException();
                        }
                        break;
                    case R.id.longitude:
                        numberValue = lastLocationRecord.getLongitude();
                        break;
                    case R.id.latitude:
                        numberValue = lastLocationRecord.getLatitude();
                        break;
                    case R.id.bearing:
                        numberValue = lastLocationRecord.getBearing();
                        break;
                    case R.id.altitude:
                        numberValue = lastLocationRecord.getAltitude();
                        break;
                    case R.id.accuracy:
//                        numberValue = lastLocationRecord.getAccuracy();
                        stringValue = String.format("%sm/%ds",
                            sharedPreferences.getString(Preference.GPS_MINDISTANCE,
                                Preference.DEFAULT_GPS_MINDISTANCE),
                            Integer.parseInt(sharedPreferences.getString(Preference.GPS_MINTIME,
                                Preference.DEFAULT_GPS_MINTIME)) / 1000);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem pauseMenuItem = menu.findItem(R.id.pause);
        MenuItem startMenuItem = menu.findItem(R.id.start);
        Boolean isRunning = (ServiceBinder.STATUS_RUNNING == serviceBinder.getStatus());

        pauseMenuItem.setEnabled(isRunning ? true : false);
        startMenuItem.setEnabled(isRunning ? false : true);

        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Environment.isExternalStoragePresent()) {
            Log.e(TAG, "External storage not presented.");
            return;
        }

        recordServerIntent = new Intent(this, RecordService.class);
        startService(recordServerIntent);
        bindService(recordServerIntent, serviceConnection, BIND_AUTO_CREATE);

        setContentView(R.layout.main);

        findAllTextView((ViewGroup) findViewById(R.id.root));
        initialViewUpdater();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateOrientation();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateOrientation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    public void updateOrientation() {
        String userConfOrient = sharedPreferences.getString(Preference.USER_ORIENTATION, Preference.DEFAULT_USER_ORIENTATION);
        int orgOrient = getRequestedOrientation();

        if (userConfOrient.equals(Preference.DEFAULT_USER_ORIENTATION)) {
            if (orgOrient != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            if (orgOrient != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent t;

        switch (item.getItemId()) {
            case R.id.start:
                serviceBinder.startRecord();
                return true;

            case R.id.pause:
                serviceBinder.stopRecord();
                return true;

            case R.id.stop:
                serviceBinder.stopRecord();
                stopService(recordServerIntent);
                finish();
                return true;

            case R.id.records:
                t = new Intent(Main.this, Records.class);
                startActivity(t);
                return true;

            case R.id.configure:
                t = new Intent(Main.this, Preference.class);
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


    @Override
    public void onDestroy() {
//        try {
//            if (serviceBinder.getStatus() == ServiceBinder.STATUS_RUNNING) {
//                Toast.makeText(this, getString(R.string.still_running), Toast.LENGTH_SHORT).show();
//            }
//        } catch (NullPointerException e) {
//            Log.e(TAG, "Make toast text error while destroy activity.");
//        }

        timer.cancel();
        super.onDestroy();
    }
}
