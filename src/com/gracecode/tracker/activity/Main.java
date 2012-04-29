package com.gracecode.tracker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.gracecode.tracker.R;
import com.gracecode.tracker.activity.base.Activity;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.fragment.ArchiveMetaFragment;
import com.gracecode.tracker.service.Recorder;
import com.gracecode.tracker.util.Logger;
import com.markupartist.android.widget.ActionBar;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Activity implements View.OnClickListener, View.OnLongClickListener {
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
    private static final String COST_TIME_FORMAT = "%02d:%02d:%02d";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracker);

        mStartButton = (Button) findViewById(R.id.btn_start);
        mEndButton = (Button) findViewById(R.id.btn_end);
        mCoseTime = (TextView) findViewById(R.id.item_cost_time);
    }

    private void notifyUpdateView() {
        Message message = new Message();
        message.what = MESSAGE_UPDATE_VIEW;
        uiHandler.sendMessage(message);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateViewTimer = new Timer();
        updateViewTimer.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    notifyUpdateView();
                }
            }, 0, TIMER_PERIOD);
    }

    @Override
    public void onPause() {
        super.onPause();
        updateViewTimer.cancel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRecording) {
            uiHelper.showLongToast(getString(R.string.still_running));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mStartButton.setOnClickListener(this);
        mEndButton.setOnClickListener(this);
        mEndButton.setOnLongClickListener(this);

        // 设置 ActionBar 样式
        actionBar.setTitle(getString(R.string.app_name));
        actionBar.removeAllActions();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.clearHomeAction();
        actionBar.addAction(
            new ActionBar.IntentAction(this,
                new Intent(this, Records.class), R.drawable.ic_menu_friendslist));
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
                uiHelper.showShortToast(getString(R.string.long_press_to_stop));
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (isRecording && serviceBinder != null) {
            long count = archiveMeta.getCount();

            serviceBinder.stopRecord();
            notifyUpdateView();

            if (count > MINI_RECORDS) {
                Intent intent = new Intent(context, Detail.class);
                intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archiveMeta.getName());
                startActivity(intent);
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

                    String costTime =
                        getCostTimeString(archiveMeta.getStartTime(), new Date(System.currentTimeMillis()));
                    mCoseTime.setText(costTime);
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
                        Logger.i(getString(R.string.not_available));
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

    protected String getCostTimeString(Date start, Date end) {
        try {
            long startTimeStamp = start.getTime();
            long endTimeStamp = end.getTime();
            long between = endTimeStamp - startTimeStamp;

            long day = between / (24 * 60 * 60 * 1000);
            long hour = (between / (60 * 60 * 1000) - day * 24);
            long minute = ((between / (60 * 1000)) - day * 24 * 60 - hour * 60);
            long second = (between / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);

            return String.format(COST_TIME_FORMAT, hour, minute, second);
        } catch (NullPointerException e) {
            return "";
        }
    }
}
