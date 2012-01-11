package com.gracecode.gpsrecorder.activity;

import android.app.Dialog;
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
import com.gracecode.gpsrecorder.RecordService.ServiceBinder;
import com.gracecode.gpsrecorder.dao.LocationItem;
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


    private ArrayList<TextView> textViewsGroup = new ArrayList<TextView>();
    private LocationItem lastLocationRecord;
    private static final int MESSAGE_UPDATE_STATE_VIEW = 0x0001;


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

        if (!Environment.isExternalStoragePresent()) {
            Log.e(TAG, "External storage not presented.");
            return;
        }

        findAllTextView((ViewGroup) findViewById(R.id.root));
        initialViewUpdater();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent t;

        switch (item.getItemId()) {
            case R.id.stop:
                serviceBinder.stopRecord();
                stopService();
                finish();
                return true;

            case R.id.records:
//                t = new Intent(Main.this, Records.class);
//                startActivity(t);
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

    private void updateView() {
        // get last record by server binder
        lastLocationRecord = serviceBinder.getLastRecord();

        for (int i = 0; i < textViewsGroup.size(); i++) {
            double numberValue = 0;
            String stringValue = "";
            TextView textView = textViewsGroup.get(i);
            int count = lastLocationRecord.getCount();

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
                            .format(new Date(lastLocationRecord.getTime()));
                        break;
                    case R.id.speed:
                        double speed = lastLocationRecord.getSpeed();
                        if (maxSpeed < speed) {
                            maxSpeed = speed;
                        }

                        if (maxSpeed != 0.0) {
                            stringValue = String.format("%.1f(%.1f)", speed, maxSpeed);
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
                        numberValue = lastLocationRecord.getAccuracy();
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
        if (serviceBinder.getStatus() == ServiceBinder.STATUS_RUNNING) {
            Toast.makeText(this, getString(R.string.still_running), Toast.LENGTH_SHORT).show();
        }

        timer.cancel();
        super.onDestroy();
    }
}
