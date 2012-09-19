package com.gracecode.tracker.ui.activity;

import android.location.Location;
import android.os.Bundle;
import android.widget.LinearLayout;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.dao.Archiver;
import com.gracecode.tracker.ui.activity.base.Activity;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;


public class SpeedCharts extends Activity {
    private static int HORIZONTAL_LABELS_ITEM_SIZE = 9;
    private String archiveFileName;
    private Archiver archiver;
    private String description;
    private ArrayList<Location> locations;
    private ArrayList<GraphView.GraphViewData> speedSeries;
    private LineGraphView graphView;
    private LinearLayout chartsView;
    private SimpleDateFormat dateFormatter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speed_charts);

        archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
        //archiveFileName = "/mnt/sdcard/tracker/201209/1347966606576.sqlite";

        archiver = new Archiver(context, archiveFileName, Archiver.MODE_READ_ONLY);
        description = archiver.getMeta().getDescription();
        locations = archiver.fetchAll();
        speedSeries = new ArrayList<GraphView.GraphViewData>();
        dateFormatter = new SimpleDateFormat(getString(R.string.sort_time_format), Locale.getDefault());

        graphView = new LineGraphView(context, "");

        chartsView = (LinearLayout) findViewById(R.id.charts);
        chartsView.addView(graphView);

        setGraphDataAndStyle();
    }


    private void setGraphDataAndStyle() {
        // label
        graphView.setHorizontalLabels(getHorizontalLabels());

        //style
        graphView.setDrawBackground(true);

        // data
        graphView.addSeries(new GraphViewSeries(getSeriesData()));
    }

    private String[] getHorizontalLabels() {
        int size = locations.size();
        ArrayList<String> labels = new ArrayList<String>();

        for (int i = 0; i < size; i += (size / HORIZONTAL_LABELS_ITEM_SIZE)) {
            Location location = locations.get(i);
            if (location != null) {
                labels.add(dateFormatter.format(location.getTime()));
            }
        }

        return labels.toArray(new String[labels.size()]);
    }

    private GraphView.GraphViewData[] getSeriesData() {
        speedSeries.clear();

        Iterator<Location> locationIterator = locations.iterator();
        while (locationIterator.hasNext()) {
            Location location = locationIterator.next();
            GraphView.GraphViewData graphViewData = new GraphView.GraphViewData(location.getTime(),
                location.getSpeed() * ArchiveMeta.KM_PER_HOUR_CNT);

            speedSeries.add(graphViewData);
        }

        return speedSeries.toArray(new GraphView.GraphViewData[speedSeries.size()]);
    }

    @Override
    public void onStart() {
        super.onStart();
        actionBar.setTitle(description);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        archiver.close();
    }
}
