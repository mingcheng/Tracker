package com.gracecode.tracker.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.baidu.location.LocationClient;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.service.Recorder;
import com.gracecode.tracker.ui.activity.base.Activity;
import com.gracecode.tracker.ui.fragment.ArchiveMetaFragment;
import com.gracecode.tracker.util.Helper;
import com.markupartist.android.widget.ActionBar;
import com.umeng.fb.NotificationType;
import com.umeng.fb.UMFeedbackService;
import com.umeng.update.UmengUpdateAgent;

import java.util.Timer;
import java.util.TimerTask;

public class Tracker extends Activity implements View.OnClickListener, View.OnLongClickListener {
    private Button mStartButton;
    private Button mEndButton;

    private ArchiveMetaFragment archiveMetaFragment;

    protected ArchiveMeta archiveMeta;

    private static final int FLAG_RECORDING = 0x001;
    private static final int FLAG_ENDED = 0x002;
    private static final long MINI_RECORDS = 2;

    private boolean isRecording = false;
    public static final int MESSAGE_UPDATE_VIEW = 0x011;
    private Timer updateViewTimer;
    private static final long TIMER_PERIOD = 1000;
    private TextView mCoseTime;
    private Button mDisabledButton;
    private LocationClient mLocationClient;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracker);

        mStartButton = (Button) findViewById(R.id.btn_start);
        mEndButton = (Button) findViewById(R.id.btn_end);
        mDisabledButton = (Button) findViewById(R.id.btn_disabled);

        mCoseTime = (TextView) findViewById(R.id.item_cost_time);

        mStartButton.setOnClickListener(this);
        mEndButton.setOnClickListener(this);
        mDisabledButton.setOnClickListener(this);
        mEndButton.setOnLongClickListener(this);

        UmengUpdateAgent.update(context);
        UMFeedbackService.enableNewReplyNotification(context, NotificationType.AlertDialog);
    }

    private void notifyUpdateView() {
        Message message = new Message();
        message.what = MESSAGE_UPDATE_VIEW;
        uiHandler.sendMessage(message);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (helper.isGPSProvided()) {
            updateViewTimer = new Timer();
            updateViewTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        notifyUpdateView();
                    }
                }, 0, TIMER_PERIOD);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!helper.isGPSProvided()) {
            mStartButton.setVisibility(View.GONE);
            mEndButton.setVisibility(View.GONE);
            mDisabledButton.setVisibility(View.VISIBLE);

            helper.showLongToast(getString(R.string.gps_not_presented));
        } else {
            mDisabledButton.setVisibility(View.GONE);
        }

        // 设置 ActionBar 样式
        actionBar.setTitle(getString(R.string.app_name));
        actionBar.removeAllActions();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.clearHomeAction();
        actionBar.addAction(
            new ActionBar.Action() {
                @Override
                public int getDrawable() {
                    return R.drawable.ic_menu_friendslist;
                }

                @Override
                public void performAction(View view) {
                    gotoActivity(Records.class);
                }
            }
        );
    }

    private void gotoActivity(java.lang.Class cls) {
        Intent intent = new Intent(context, cls);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                if (serviceBinder != null && !isRecording) {
                    serviceBinder.startRecord();
                    notifyUpdateView();
                }
                break;
            case R.id.btn_end:
                helper.showShortToast(getString(R.string.long_press_to_stop));
                break;

            case R.id.btn_disabled:
                Intent intent = new Intent(
                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (isRecording && serviceBinder != null) {

            serviceBinder.stopRecord();
            notifyUpdateView();

            if (archiveMeta != null) {
                long count = archiveMeta.getCount();
                if (count > MINI_RECORDS) {
                    Intent intent = new Intent(context, Detail.class);
                    intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archiveMeta.getName());
                    startActivity(intent);
                }
            }
        }

        setViewStatus(FLAG_ENDED);
        return true;
    }

    private void setViewStatus(int status) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch (status) {
            case FLAG_RECORDING:
                mStartButton.setVisibility(View.GONE);
                mEndButton.setVisibility(View.VISIBLE);
                if (archiveMeta != null) {
                    archiveMetaFragment = new ArchiveMetaFragment(context, archiveMeta);
                    fragmentTransaction.replace(R.id.status_layout, archiveMetaFragment);
//                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                    mCoseTime.setText(archiveMeta.getCostTimeStringByNow());
                }
                break;
            case FLAG_ENDED:
                mStartButton.setVisibility(View.VISIBLE);
                mEndButton.setVisibility(View.GONE);
                if (archiveMetaFragment != null) {
                    fragmentTransaction.remove(archiveMetaFragment);
                }
                mCoseTime.setText(R.string.none_cost_time);
                break;
        }

        fragmentTransaction.commit();
    }

    // 控制界面显示 UI
    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_UPDATE_VIEW:

                    if (serviceBinder == null) {
                        Helper.Logger.i(getString(R.string.not_available));
                        return;
                    }

                    archiveMeta = serviceBinder.getMeta();

                    switch (serviceBinder.getStatus()) {
                        case Recorder.ServiceBinder.STATUS_RECORDING:
                            setViewStatus(FLAG_RECORDING);
                            isRecording = true;
                            break;
                        case Recorder.ServiceBinder.STATUS_STOPPED:
                            setViewStatus(FLAG_ENDED);
                            isRecording = false;
                    }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tracker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_records:
                gotoActivity(Records.class);
                break;

            case R.id.menu_configure:
                gotoActivity(Preference.class);
                break;

            case R.id.menu_feedback:
                UMFeedbackService.openUmengFeedbackSDK(context);
                break;

            case R.id.menu_help:
                gotoActivity(Info.class);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (updateViewTimer != null) {
            updateViewTimer.cancel();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRecording) {
            helper.showLongToast(getString(R.string.still_running));
        }
    }
}
