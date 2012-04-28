package com.gracecode.tracker.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.gracecode.tracker.R;
import com.gracecode.tracker.activity.base.Activity;
import com.gracecode.tracker.dao.Archive;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.service.ArchiveNameHelper;
import com.gracecode.tracker.service.Recorder.ServiceBinder;
import com.gracecode.tracker.util.Logger;
import com.markupartist.android.widget.ActionBar;

import java.text.SimpleDateFormat;
import java.util.*;

public class Tracker extends Activity {
    private Timer timer = null;

    private ArrayList<TextView> textViewsGroup = new ArrayList<TextView>();
    private Location lastLocationRecord;
    private static final int MESSAGE_UPDATE_STATE_VIEW = 0x0001;
    protected ArchiveMeta archiveMeta = null;
    private Archive archive = null;
    private long needCountDistance = 0;
    private ToggleButton toggleButton;

    /**
     * Handle the records_context for show the last recorded status.
     */
    private Handler handle = new Handler() {
        long visible_flag = 0;

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_UPDATE_STATE_VIEW:
                    if (serviceBinder == null) {
                        return;
                    }

                    TextView records = (TextView) findViewById(R.id.status);
                    String statusLabel = getString(R.string.ready);

                    switch (serviceBinder.getStatus()) {
                        case ServiceBinder.STATUS_RUNNING:
                            statusLabel = getString(R.string.recording);
                            toggleButton.setChecked(true);
                            break;
                        case ServiceBinder.STATUS_STOPPED:
                            statusLabel = getString(R.string.ready);
                            toggleButton.setChecked(false);
                        default:
                    }
                    records.setText(statusLabel);
                    records.setVisibility(++visible_flag % 2 == 0 ? View.INVISIBLE : View.VISIBLE);

                    updateView();
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

    private void updateViewStatus() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = MESSAGE_UPDATE_STATE_VIEW;
                handle.sendMessage(message);
            }
        }, 0, 1000);

        // change the font for nice look
//        Typeface face = Typeface.createFromAsset(getAssets(),
//            getString(R.string.default_font));
//
//        for (int i = 0; i < textViewsGroup.size(); i++) {
//            TextView t = textViewsGroup.get(i);
//            t.setTypeface(face);
//        }
    }

    private void updateView() {
        if (serviceBinder == null) {
            return;
        }
        Boolean isRunning = (serviceBinder.getStatus() == ServiceBinder.STATUS_RUNNING);

        for (int i = 0; i < textViewsGroup.size(); i++) {
            double numberValue = 0;
            String stringValue = "";
            TextView textView = textViewsGroup.get(i);
            long count = 0;
            float speed = 0;
            float maxSpeed = 0;

            try {
                if (isRunning) {
                    lastLocationRecord = serviceBinder.getLastRecord();
                    archiveMeta = serviceBinder.getMeta();
                    archive = serviceBinder.getArchive();
                    count = archiveMeta.getCount();
                    speed = archiveMeta.getAverageSpeed();
                    maxSpeed = archiveMeta.getMaxSpeed();


                    GpsStatus gpsStatus = serviceBinder.getGpsStatus();
                    Iterator<GpsSatellite> satellites = (gpsStatus.getSatellites()).iterator();


                    int k = 0;
                    int j = 0;
                    while (satellites.hasNext()) {
                        GpsSatellite satellite = satellites.next();
                        if (satellite.hasEphemeris()) {
                            j++;
                        }
                        k++;
                    }
//
//                    if (i % 50 == 0) {
//                        uiHelper.showShortToast("Connected: " + j + " / " + k + " / " + gpsStatus.getMaxSatellites());
//                    }
                }

                switch (textView.getId()) {
                    case R.id.status:
                        int stamp = (isRunning) ? R.string.recording : R.string.idle;
                        stringValue = getString(stamp);
                        break;
                    case R.id.records:
                        if (count > 0) {
                            stringValue = String.format(getString(R.string.records), count);
                        } else {
                            stringValue = getString(R.string.no_records);
                        }
                        break;
                    case R.id.distance:
                        // @todo 考虑性能问题
                        if (++needCountDistance % 5 == 0 && isRunning) {
                            float distance = archiveMeta.getDistance();
                            if (distance > 0) {
                                numberValue = distance / 1000;
                                textView.setVisibility(View.VISIBLE);
                            } else {
                                textView.setVisibility(View.INVISIBLE);
                            }
                        }
                        break;
                    case R.id.time:
                        stringValue = new SimpleDateFormat(getString(R.string.time_format)).format(
                            new Date(isRunning ? lastLocationRecord.getTime() : System.currentTimeMillis()));
                        break;
                    case R.id.speed:
                        if (speed > 0) {
                            stringValue = String.format("%.2f(%.2f)",
                                speed * ArchiveMeta.KM_PER_HOUR_CNT, maxSpeed * ArchiveMeta.KM_PER_HOUR_CNT);
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
                stringValue = getString(R.string.no_records);
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
        setContentView(R.layout.main);

        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        findAllTextView((ViewGroup) findViewById(R.id.root));
    }

    @Override
    public void onStart() {
        super.onStart();

        // 判断外界条件
        if (!ArchiveNameHelper.isExternalStoragePresent()) {
            Logger.e("External storage not presented.");
            Toast.makeText(this, getString(R.string.storage_not_presented), Toast.LENGTH_SHORT).show();
            Intent myIntent = new Intent(Settings.ACTION_MEMORY_CARD_SETTINGS);
            startActivity(myIntent);
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Logger.e("GPS not Enabled");
            Toast.makeText(this, getString(R.string.gps_not_presented), Toast.LENGTH_SHORT).show();

            Intent myIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
            startActivity(myIntent);
        }

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (serviceBinder != null) {
                    if (serviceBinder.getStatus() == ServiceBinder.STATUS_RUNNING) {
                        long count = archiveMeta.getCount();
                        serviceBinder.stopRecord();
                        toggleButton.setChecked(false);

                        // 如果已经有记录，则显示保存信息
                        if (count > 0) {
                            Intent intent = new Intent(context, Detail.class);
                            intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archive.getName());
                            startActivity(intent);
                        }
                    } else {
                        serviceBinder.startRecord();
                        toggleButton.setChecked(true);
                    }
                }
            }
        });

        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.app_name));
            actionBar.removeAllActions();
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.clearHomeAction();
            actionBar.addAction(new ActionBar.IntentAction(this,
                new Intent(this, Records.class), R.drawable.ic_menu_friendslist));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateViewStatus();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (timer != null) {
            timer.cancel();
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
            case R.id.start:
                serviceBinder.startRecord();
                return true;

            case R.id.pause:
                serviceBinder.stopRecord();
                return true;

            case R.id.records:
                t = new Intent(Tracker.this, Records.class);
                startActivity(t);
                return true;

            case R.id.configure:
                t = new Intent(Tracker.this, Preference.class);
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
        if (serviceBinder != null && serviceBinder.getStatus() == ServiceBinder.STATUS_RUNNING) {
            uiHelper.showLongToast(getString(R.string.still_running));
        }

        super.onDestroy();
    }
}
