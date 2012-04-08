package com.gracecode.gpsrecorder.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.gracecode.gpsrecorder.R;

import java.util.ArrayList;
import java.util.Date;

public class Records extends Base {
    public static final int HIDE_PROGRESS_DIALOG = 0x1;
    private final String TAG = Records.class.getName();
    private Context context;

    //
    private ListView listView;
    protected ArrayList<Location> gpsDatabaseList;
    private GPSDatabaseAdapter gpsdatabaseAdapter;

    //    private SimpleAdapter listViewAdapter;
    private ProgressDialog progressDialog;
//    private Thread saveKMLFileThread;

    public class GPSDatabaseAdapter extends ArrayAdapter<Location> {
//        public GPSDatabaseAdapter(ArrayList<GPSDatabase> values) {
//            super(Records.this, R.layout.records_row, values);
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            GPSDatabase database = gpsDatabaseList.get(position);
//            GPSDatabase.Meta meta = database.getMeta();
//
//            LayoutInflater inflater = (LayoutInflater) context
//                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            View rowView = inflater.inflate(R.layout.records_row, parent, false);
//
//
//            TextView countView = (TextView) rowView.findViewById(R.id.db_records_num);
//            TextView nameView = (TextView) rowView.findViewById(R.id.db_name);
//            TextView descriptionView = (TextView) rowView.findViewById(R.id.description);
//            TextView betweenView = (TextView) rowView.findViewById(R.id.between);
//
//            countView.setText(String.valueOf(database.getValvedCount()));
//
//            String title = meta.getTitle();
//            if (title.length() <= 0) {
//                File tmp = database.getFile();
//                title = tmp.getName().replace(Environment.SQLITE_DATABASE_FILENAME_EXT, "");
//            }
//            nameView.setText(title);
//
//            String description = meta.getDescription();
//            if (description.length() <= 0) {
//                description = getString(R.string.no_description);
//            }
//            descriptionView.setText(description);
//
//            SimpleDateFormat formatter = new SimpleDateFormat(getString(R.string.time_format));
//            Date startTime = meta.getStartTime();
//            Date stopTime = meta.getStopTime();
//
//            String between = String.format("%s%s",
//                startTime != null ? formatter.format(startTime) : "",
//                stopTime != null ? " - " + formatter.format(stopTime) : "");
//            betweenView.setText(between);
//
//            return rowView;
//        }

        public GPSDatabaseAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.records);
//
//        this.context = getApplicationContext();
//
//        listView = (ListView) findViewById(R.id.records_list);
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        progressDialog.setMessage(getString(R.string.saving));
//        progressDialog.setCancelable(false);
//
//        gpsDatabaseList = new ArrayList<GPSDatabase>();
//        getStorageDatabases();
//
//        updateListView();
    }


    protected void getStorageDatabases() {
        getStorageDatabasesMeta(new Date());
    }

    protected void getStorageDatabasesMeta(Date selectedDate) {
//        gpsDatabaseList.clear();
//
//        // get the parent directory handle
//        File currentStorageDir = Environment.getStorageDirectory(selectedDate);
//        boolean autoClean = sharedPreferences.getBoolean(Preference.AUTO_CLEAN, true);
//
//        File[] storageFileArray = currentStorageDir.listFiles(new FilenameFilter() {
//            @Override
//            public boolean accept(File file, String s) {
//                if (s.endsWith(Environment.SQLITE_DATABASE_FILENAME_EXT)) {
//                    return true;
//                }
//                return false;
//            }
//        });
//
//        // comparator with last modified
//        Arrays.sort(storageFileArray, new Comparator<File>() {
//            public int compare(File f1, File f2) {
//                return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
//            }
//        });
//
//        // reopen the database
//        closeDatabases();
//        for (File dbFile : storageFileArray) {
//            if (dbFile.exists() && dbFile.isFile()) {
//                try {
//                    GPSDatabase database = new GPSDatabase(dbFile);
//                    if (autoClean && database.getValvedCount() <= 0) {
//                        dbFile.delete();
//                    } else {
//                        gpsDatabaseList.add(database);
//                    }
//                } catch (SQLiteException e) {
//                    Log.e(TAG, e.getMessage());
//                }
//            }
//        }
//
//        gpsdatabaseAdapter = new GPSDatabaseAdapter(gpsDatabaseList);

    }

    private void updateListView() {
        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu,
                                            View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.records_context, contextMenu);
            }
        });

        listView.setAdapter(gpsdatabaseAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.records, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.calendar:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);

                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.date_picker, null);

                ViewGroup datePacker = (ViewGroup) layout.findViewById(R.id.select_date);
                datePacker.getChildAt(2).setVisibility(View.GONE);

                dialog.setView(layout);
                dialog.setTitle("Select Month");

                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

                dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                dialog.show();

                return true;
        }

        return false;
    }

    //长按菜单响应函数
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int position = info.position;

        switch (item.getItemId()) {
            case R.id.export:
//                progressDialog.show();
                return true;

            case R.id.description:
                updateDescriptionByModalDialog(position);
                return true;
            case R.id.delete:
                confirmDeleteDatabaseFile(position);
                return true;
        }
        return false;
    }

    private void updateDescriptionByModalDialog(final int position) {
//        final EditText editText = new EditText(this);
//        final GPSDatabase storageDatabase = gpsDatabaseList.get(position);
//        final GPSDatabase.Meta meta = storageDatabase.getMeta();
//        editText.setText(meta.getDescription());
//
//        uiHelper.showModalDialog(getString(R.string.update_description), null, editText,
//            new Runnable() {
//                @Override
//                public void run() {
//                    String description = editText.getText().toString();
//                    String result = String.format("%s is updated", storageDatabase.getFile().getName());
//
//                    if (!meta.addOrUpdateDescription(description)) {
//                        result = "update error!";
//                    }
//                    Toast.makeText(context, result, Toast.LENGTH_LONG).show();
//                    gpsdatabaseAdapter.notifyDataSetChanged();
//                }
//            },
//            new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            }
//        );
    }


    private void confirmDeleteDatabaseFile(final int position) {
//        final GPSDatabase storageDatabase = gpsDatabaseList.get(position);
//        final File storageFile = storageDatabase.getFile();
//
//        if (storageFile.isFile() && storageFile.canWrite()) {
//            Runnable onConfirmDelete = new Runnable() {
//                @Override
//                public void run() {
//                    storageDatabase.close();
//                    if (storageFile.delete()) {
//                        Toast.makeText(context, String.format(getString(R.string.has_deleted), storageFile.getAbsolutePath()),
//                            Toast.LENGTH_LONG).show();
//
//                        gpsDatabaseList.remove(position);
//                        gpsdatabaseAdapter.notifyDataSetChanged();
//                    }
//                }
//            };
//
//            Runnable onCancelDelete = new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            };
//
//            uiHelper.showConfirmDialog(getString(R.string.notice),
//                String.format(getString(R.string.sure_to_del), storageFile.getName()),
//                onConfirmDelete, onCancelDelete);
//        }
    }


    //    private Handler handle = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case HIDE_PROGRESS_DIALOG:
//                    Toast.makeText(context,
//                        getString(R.string.save_kml_finished), Toast.LENGTH_LONG).show();
//                    progressDialog.dismiss();
//                    break;
//            }
//        }
//    };

    private void closeDatabases() {
//        if (gpsDatabaseList.size() > 0) {
//            for (GPSDatabase gpsDatabase : gpsDatabaseList) {
//                gpsDatabase.close();
//            }
//        }
    }

    @Override
    public void onDestroy() {
        closeDatabases();
        super.onDestroy();
    }
}
