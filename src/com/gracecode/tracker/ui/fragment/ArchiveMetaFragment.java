package com.gracecode.tracker.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.ui.activity.Records;
import com.gracecode.tracker.ui.activity.SpeedCharts;
import com.gracecode.tracker.util.Helper;

public class ArchiveMetaFragment extends Fragment implements View.OnClickListener {
    public ArchiveMeta meta;
    private Context context;
    private View layoutView;
    private TextView mDistance;
    private TextView mAvgSpeed;
    private TextView mMaxSpeed;
    private TextView mRecords;
    private String formatter;

    public ArchiveMetaFragment(Context context, ArchiveMeta meta) {
        this.meta = meta;
        this.context = context;
        this.formatter = context.getString(R.string.records_formatter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layoutView = inflater.inflate(R.layout.archive_meta_items, container, false);
        mDistance = (TextView) layoutView.findViewById(R.id.item_distance);
        mAvgSpeed = (TextView) layoutView.findViewById(R.id.item_avg_speed);
        mMaxSpeed = (TextView) layoutView.findViewById(R.id.item_max_speed);
        mRecords = (TextView) layoutView.findViewById(R.id.item_records);

        mAvgSpeed.setOnClickListener(this);
        mMaxSpeed.setOnClickListener(this);

        return layoutView;
    }

    @Override
    public void onStart() {
        super.onStart();
        update();
    }

    public void update() {
        try {
            mDistance.setText(String.format(formatter, meta.getDistance() / ArchiveMeta.TO_KILOMETRE));
            mMaxSpeed.setText(String.format(formatter, meta.getMaxSpeed() * ArchiveMeta.KM_PER_HOUR_CNT));
            mAvgSpeed.setText(String.format(formatter, meta.getAverageSpeed() * ArchiveMeta.KM_PER_HOUR_CNT));
            mRecords.setText(String.valueOf(meta.getCount()));
        } catch (Exception e) {
            Helper.Logger.e(e.getMessage());
        }
    }

    public void update(ArchiveMeta meta) {
        this.meta = meta;
        this.update(meta);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.item_avg_speed:
            case R.id.item_max_speed:
                Intent intent = new Intent(context, SpeedCharts.class);
                intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, meta.getName());
                startActivity(intent);
                break;
        }
    }
}
