package com.gracecode.tracker.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.baidu.mapapi.MapView;
import com.gracecode.tracker.R;
import com.gracecode.tracker.activity.base.MapActivity;
import com.gracecode.tracker.dao.Archive;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.util.UIHelper;
import com.markupartist.android.widget.ActionBar;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Detail extends MapActivity implements View.OnClickListener {
    private String archiveFileName;
    private ActionBar actionBar;
    private Archive archive;
    private ArchiveMeta archiveMeta;
    private TextView mStartTime;
    private TextView mEndTime;
    private TextView mDistance;
    private TextView mSpeed;
    private TextView mRecords;
    private EditText mDescription;
    private Button mButton;
    private SimpleDateFormat formatter;
    private TextView mArchiveName;
    private TextView mMaxSpeed;
    private Context context;
    private UIHelper uiHelper;

    private TextView mapMask;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        context = this;

        mapView = (MapView) findViewById(R.id.bmapsView);

        archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
        archive = new Archive(context, archiveFileName, Archive.MODE_READ_WRITE);
        archiveMeta = archive.getMeta();

        actionBar = (ActionBar) findViewById(R.id.action_bar);
        mArchiveName = (TextView) findViewById(R.id.archive_name);
        mStartTime = (TextView) findViewById(R.id.start_time);
        mEndTime = (TextView) findViewById(R.id.end_time);
        mDistance = (TextView) findViewById(R.id.distance);
        mRecords = (TextView) findViewById(R.id.records);
        mSpeed = (TextView) findViewById(R.id.speed);
        mMaxSpeed = (TextView) findViewById(R.id.max_speed);
        mDescription = (EditText) findViewById(R.id.description);
        mButton = (Button) findViewById(R.id.update);

//        mapView.setSatellite(false);

        mapMask = (TextView) findViewById(R.id.map_mask);

        formatter = new SimpleDateFormat(getString(R.string.time_format));
        uiHelper = new UIHelper(context);


    }

    @Override
    public void onStart() {
        super.onStart();

        setCenterPoint(archive.getLastRecord(), false);
        mapViewController.setZoom(14);

        mapMask.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Intent intent = new Intent(context, BaiduMap.class);
                    intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archive.getName());
                    startActivity(intent);
                }

                return true;
            }
        });

        mArchiveName.setText(archive.getName());
        mStartTime.setText(formatter.format(archiveMeta.getStartTime()));

        Date endTime = archiveMeta.getEndTime();
        mEndTime.setText((endTime != null) ? formatter.format(endTime) : getString(R.string.norecords));

        mDistance.setText(String.valueOf(archiveMeta.getDistance()));
        mRecords.setText(String.valueOf(archiveMeta.getCount()));
        mSpeed.setText(String.valueOf(archiveMeta.getAverageSpeed() * ArchiveMeta.KM_PER_HOUR_CNT));
        mMaxSpeed.setText(String.valueOf(archiveMeta.getMaxSpeed() * ArchiveMeta.KM_PER_HOUR_CNT));
        mDescription.setText(archiveMeta.getDescription());

        mButton.setOnClickListener(this);

        actionBar.removeAllActions();
        actionBar.addAction(new ActionBar.Action() {
            @Override
            public int getDrawable() {
                return R.drawable.ic_menu_delete;
            }

            @Override
            public void performAction(View view) {
                uiHelper.showConfirmDialog(getString(R.string.delete),
                    String.format(getString(R.string.sure_to_del), archiveFileName),
                    new Runnable() {
                        @Override
                        public void run() {
                            if (archive.delete()) {
                                uiHelper.showShortToast(String.format(getString(R.string.has_deleted), archiveFileName));
                            } else {
                                uiHelper.showLongToast(getString(R.string.delete_error));
                            }
                            finish();
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {

                        }
                    }
                );
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!archive.exists()) {
            finish();
        }
    }

    @Override
    public void onDestroy() {
        if (archive != null) {
            archive.close();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        String description = mDescription.getText().toString().trim();
        if (description.length() > 0 && archiveMeta.setDescription(description)) {
            uiHelper.showShortToast(getString(R.string.updated));
        }
    }
}
