package com.gracecode.tracker.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.gracecode.tracker.R;
import com.gracecode.tracker.activity.base.Activity;
import com.gracecode.tracker.dao.Archive;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.service.ArchiveNameHelper;
import com.markupartist.android.widget.ActionBar;

import java.util.*;

public class Records extends Activity implements AdapterView.OnItemClickListener, DatePickerDialog.OnDateSetListener {
    private Context context;
    public static final String INTENT_ARCHIVE_FILE_NAME = "name";
    public static final String INTENT_SELECT_BY_MONTH = "month";

    private ListView listView;
    private ArrayList<String> archiveFileNames;
    private ArrayList<Archive> archives;

    private ArchiveNameHelper archiveFileNameHelper;
    private ArchivesAdapter archivesAdapter;
    private long selectedTime;

    private class MonthPickerDialog extends DatePickerDialog {
        public MonthPickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
            super(context, callBack, year, monthOfYear, dayOfMonth);
            setTitle(year, monthOfYear);
        }

        @Override
        public void onDateChanged(DatePicker view, int year, int month, int day) {
            super.onDateChanged(view, year, month, day);
            setTitle(year, month);
        }

        private void setTitle(int year, int month) {
            this.setTitle(String.format(getString(R.string.month_format), year, month + 1));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Archive archive = archives.get(i);
        Intent intent = new Intent(this, Detail.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(INTENT_ARCHIVE_FILE_NAME, archive.getName());
        startActivity(intent);
    }

    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.set(year, month, day);

        Date selectDate = new Date(selectedTime);
        if (selectDate.getMonth() != month) {
            Intent intent = new Intent(context, Records.class);
            intent.putExtra(INTENT_SELECT_BY_MONTH, calendar.getTimeInMillis());
            startActivity(intent);
        }
    }

    /**
     * ListView 的 Adapter
     */
    public class ArchivesAdapter extends ArrayAdapter<Archive> {

        public ArchivesAdapter(ArrayList<Archive> archives) {
            super(context, R.layout.records_row, archives);
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

            mDistance.setText(String.format(getString(R.string.records_formatter),
                archiveMeta.getDistance() / ArchiveMeta.TO_KILOMETRE));

            String costTime = archiveMeta.getRawCostTimeString();
            mCostTime.setText(costTime.length() > 0 ? costTime : getString(R.string.not_available));

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

        actionBar.removeAllActions();
        actionBar.addAction(
            new ActionBar.Action() {
                @Override
                public int getDrawable() {
                    return R.drawable.ic_menu_today;
                }

                @Override
                public void performAction(View view) {
                    showTimeSelectDialog();
                }
            }
        );

        selectedTime = getIntent().getLongExtra(INTENT_SELECT_BY_MONTH, System.currentTimeMillis());
        getArchiveFilesByMonth(new Date(selectedTime));
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

    private DatePicker findDatePicker(ViewGroup group) {
        if (group != null) {
            for (int i = 0, j = group.getChildCount(); i < j; i++) {
                View child = group.getChildAt(i);
                if (child instanceof DatePicker) {
                    return (DatePicker) child;
                } else if (child instanceof ViewGroup) {
                    DatePicker result = findDatePicker((ViewGroup) child);
                    if (result != null)
                        return result;
                }
            }
        }
        return null;
    }

    private void showTimeSelectDialog() {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.setTime(new Date(selectedTime));

        DatePickerDialog datePicker = new MonthPickerDialog(
            Records.this, Records.this,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.show();

        DatePicker dp = findDatePicker((ViewGroup) datePicker.getWindow().getDecorView());
        if (dp != null) {
            ((ViewGroup) dp.getChildAt(0)).getChildAt(2).setVisibility(View.GONE);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.records, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_calendar:
                showTimeSelectDialog();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
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
