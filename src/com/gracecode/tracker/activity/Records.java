package com.gracecode.tracker.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.gracecode.tracker.R;
import com.gracecode.tracker.activity.base.Activity;
import com.gracecode.tracker.dao.Archive;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.service.ArchiveNameHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class Records extends Activity implements AdapterView.OnItemClickListener {
    private Context context;
    public static final String INTENT_ARCHIVE_FILE_NAME = "name";

    private ListView listView;
    private ArrayList<String> archiveFileNames;
    private ArrayList<Archive> archives;

    private ArchiveNameHelper archiveFileNameHelper;
    private ArchivesAdapter archivesAdapter;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Archive archive = archives.get(i);
        Intent intent = new Intent(this, Detail.class);
        intent.putExtra(INTENT_ARCHIVE_FILE_NAME, archive.getName());
        startActivity(intent);
    }


    /**
     * ListView 的 Adapter
     */
    public class ArchivesAdapter extends ArrayAdapter<Archive> {

        public ArchivesAdapter(ArrayList<Archive> archives) {
            super(context, R.layout.records_row, archives);
        }

        protected String countTime(Date start, Date end) {
            try {
                long startTimeStamp = start.getTime();
                long endTimeStamp = end.getTime();
                long between = endTimeStamp - startTimeStamp;

                long day = between / (24 * 60 * 60 * 1000);
                long hour = (between / (60 * 60 * 1000) - day * 24);
                long minute = ((between / (60 * 1000)) - day * 24 * 60 - hour * 60);
                long second = (between / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);

                String result = "";
                if (day > 0) {
                    result += day + "d";
                }

                if (hour > 0) {
                    result += ((result.length() > 0) ? ", " : "") + hour + "h";
                }
                if (minute > 0) {
                    result += ((result.length() > 0) ? ", " : "") + minute + "min";
                }
                if (day <= 0 && second > 0) {
                    result += ((result.length() > 0) ? ", " : "") + second + "sec";
                }

                return result;
            } catch (NullPointerException e) {
                return "";
            }
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Archive archive = archives.get(position);
            ArchiveMeta archiveMeta = archive.getMeta();

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.records_row, parent, false);

            TextView mDescription = (TextView) rowView.findViewById(R.id.description);
            TextView mCostTime = (TextView) rowView.findViewById(R.id.cost_time);
            TextView mDistance = (TextView) rowView.findViewById(R.id.distance);

            mDistance.setText(String.format("%02.2f", archiveMeta.getDistance() / 1000));

            Date startTime = archiveMeta.getStartTime();
            Date endTime = archiveMeta.getEndTime();

            String costTime = countTime(startTime, endTime);
            mCostTime.setText(costTime);

            String description = archiveMeta.getDescription();
            if (description.length() <= 0) {
                description = getString(R.string.no_description);
            }
            mDescription.setText(description);

            return rowView;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.records);

        this.context = getApplicationContext();
        this.listView = (ListView) findViewById(R.id.records_list);
        this.archiveFileNameHelper = new ArchiveNameHelper(context);

        this.archives = new ArrayList<Archive>();
        this.archivesAdapter = new ArchivesAdapter(archives);
        this.listView.setAdapter(archivesAdapter);


    }

    @Override
    public void onStart() {
        super.onStart();
        listView.setOnItemClickListener(this);

        getArchiveFilesByMonth(new Date());
    }


    @Override
    public void onStop() {
        super.onStop();
        closeArchives();
    }

    @Override
    public void onResume() {
        super.onResume();
        archivesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void getArchiveFilesByMonth(Date date) {
        archiveFileNames = archiveFileNameHelper.getArchiveFilesNameByMonth(date);
        openArchivesFromFileNames();
    }

    /**
     * 从指定目录读取所有已保存的列表
     */
    private void openArchivesFromFileNames() {
        Iterator<String> iterator = archiveFileNames.iterator();
        while (iterator.hasNext()) {
            String name = iterator.next();
            Archive archive = new Archive(context, name);

            if (archive.getMeta().getCount() > 0) {
                archives.add(archive);
            }
        }
    }

    /**
     * 清除列表
     */
    private void closeArchives() {
        Iterator<Archive> iterator = archives.iterator();
        while (iterator.hasNext()) {
            Archive archive = (Archive) iterator.next();
            if (archive != null) {
                archive.close();
            }
        }
        archives.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
