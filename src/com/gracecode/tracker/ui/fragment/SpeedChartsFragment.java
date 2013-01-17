package com.gracecode.tracker.ui.fragment;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.dao.Archiver;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * <p/>
 * User: mingcheng
 * Date: 12-9-20
 */
public class SpeedChartsFragment extends Fragment {
    private Context context;
    private Archiver archiver;
    private ArrayList<Location> locations;
    private LineGraphView graphView;
    private static int HORIZONTAL_LABELS_ITEM_SIZE = 9;
    private ArrayList<GraphView.GraphViewData> speedSeries;
    private SimpleDateFormat dateFormatter;


    public SpeedChartsFragment(Context context, Archiver archiver) {
        this.context = context;
        this.archiver = archiver;

        this.locations = archiver.fetchAll();
        speedSeries = new ArrayList<GraphView.GraphViewData>();
        dateFormatter = new SimpleDateFormat(context.getString(R.string.sort_time_format), Locale.getDefault());

        graphView = new LineGraphView(context, "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setGraphDataAndStyle();
        return graphView;
    }

    @Override
    public void onStart() {
        super.onStart();
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
}
