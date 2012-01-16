package com.gracecode.gpsrecorder.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.gracecode.gpsrecorder.R;
import com.gracecode.gpsrecorder.dao.GPSDatabase;
import com.gracecode.gpsrecorder.util.Environment;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Records extends BaseActivity {
    public static final int HIDE_PROGRESS_DIALOG = 0x1;
    private final String TAG = Records.class.getName();
    private Context context;

    //
    private ListView listView;
    protected File[] storageFileList;

    protected ArrayList<GPSDatabase> gpsDatabaseList;
    //    private SimpleAdapter listViewAdapter;
    private ProgressDialog progressDialog;
//    private Thread saveKMLFileThread;

    public class GPSDatabaseAdapter extends ArrayAdapter<GPSDatabase> {
        public GPSDatabaseAdapter(ArrayList<GPSDatabase> values) {
            super(Records.this, R.layout.records_row, values);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GPSDatabase database = gpsDatabaseList.get(position);
            File databaseFile = storageFileList[position];
            GPSDatabase.Meta meta = database.getMeta();

            LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.records_row, parent, false);


            TextView countView = (TextView) rowView.findViewById(R.id.db_records_num);
            TextView nameView = (TextView) rowView.findViewById(R.id.db_name);
            TextView descriptionView = (TextView) rowView.findViewById(R.id.description);
            TextView betweenView = (TextView) rowView.findViewById(R.id.between);

            countView.setText(String.valueOf(database.getValvedCount()));

            String title = meta.getTitle();
            if (title.length() <= 0) {
                title = databaseFile.getName().replace(Environment.SQLITE_DATABASE_FILENAME_EXT, "");
            }
            nameView.setText(title);

            String description = meta.getDescription();
            if (description.length() <= 0) {
                description = getString(R.string.no_description);
            }
            descriptionView.setText(description);

            SimpleDateFormat formatter = new SimpleDateFormat(getString(R.string.time_format));
            Date startTime = meta.getStartTime();
            Date stopTime = meta.getStopTime();

            String between = String.format("%s%s",
                startTime != null ? formatter.format(startTime) : "",
                stopTime != null ? " - " + formatter.format(stopTime) : "");
            betweenView.setText(between);

            return rowView;
        }
    }

    private GPSDatabaseAdapter gpsdatabaseAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.records);

        this.context = getApplicationContext();

        listView = (ListView) findViewById(R.id.records_list);
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.saving));
        progressDialog.setCancelable(false);

        gpsDatabaseList = new ArrayList<GPSDatabase>();
        getStorageDatabases();
    }


    protected void getStorageDatabases() {
        getStorageDatabasesMeta(new Date());
    }

    protected void getStorageDatabasesMeta(Date selectedDate) {
        // get the parent directory handle
        File currentStorageDir = Environment.getStorageDirectory(selectedDate);
        boolean autoClean = sharedPreferences.getBoolean(Preference.AUTO_CLEAN, true);

        storageFileList = currentStorageDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                if (s.endsWith(Environment.SQLITE_DATABASE_FILENAME_EXT)) {
                    return true;
                }
                return false;
            }
        });

        closeDatabases();
        gpsDatabaseList.clear();
        for (File dbFile : storageFileList) {
            if (dbFile.exists() && dbFile.isFile()) {
                try {
                    GPSDatabase database = new GPSDatabase(dbFile);
                    if (autoClean && database.getValvedCount() <= 0) {
                        dbFile.delete();
                    } else {
                        gpsDatabaseList.add(database);
                    }
                } catch (SQLiteException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }

        gpsdatabaseAdapter = new GPSDatabaseAdapter(gpsDatabaseList);
        listView.setAdapter(gpsdatabaseAdapter);
        return;
    }


    private void updateListView() {
//        listViewAdapter = new SimpleAdapter(this, storageFileHashList,
//            R.layout.records_row,
//            new String[]{"database", "count"},
//            new int[]{R.id.db_name, R.id.db_records_num});

//        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//            @Override
//            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
//                MenuInflater inflater = getMenuInflater();
//                inflater.inflate(R.menu.records, contextMenu);
//            }
//        });

        listView.setAdapter(gpsdatabaseAdapter);
    }

    //长按菜单响应函数
    //@Override
//    public boolean onContextItemSelected(MenuItem item) {
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//        final HashMap<String, String> map = storageFileHashList.get(info.position);
//        String absolutePath = map.get("absolute");
//
//        gpsDatabase = new GPSDatabase(new File(absolutePath));
//        switch (item.getItemId()) {
//            case R.id.export:
//                progressDialog.show();
//                saveKMLFileThread = new Thread() {
//                    @Override
//                    public void run() {
////                        String name = map.get("database");
////                        String description = "";
//
//                        // @todo convert cursor into list
//                        ArrayList<Points> data = gpsDatabase.getValvedData();
////                            final KMLHelper kml = new KMLHelper(name, description, data);
////
////                            String basePath = getString(R.string.app_database_store_path);
////                            File kmlFile = new File(basePath + "/" + name.replace(".sqlite", ".kml"));
////                            kml.saveKMLFile(kmlFile.getAbsoluteFile());
////
////                            Message message = new Message();
////                            message.what = HIDE_PROGRESS_DIALOG;
////                            handle.sendMessage(message);
//
//                    }
//
//                };
//                saveKMLFileThread.start();
//                return true;
//            /**
//             * Mark as deleted not really delete the data.
//             */
//            case R.id.delete:
//                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//                dialog.setTitle(getString(R.string.notice))
//                    .setMessage(getString(R.string.sure_to_del))
//                    .setIcon(android.R.drawable.ic_dialog_alert);
//
//                dialog.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        int effectedRows = 0;
//                        if (effectedRows > 0) {
//                            getStorageDatabases();
//                            listViewAdapter.notifyDataSetChanged();
//
//                            Toast.makeText(context,
//                                String.format(getString(R.string.has_deleted), Integer.toString(effectedRows)),
//                                Toast.LENGTH_LONG).show();
//                        }
//                    }
//                }).setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//
//                    }
//                }).show();
//
//                return true;
//        }
//        gpsDatabase.closeDatabases();
//        return false;
//    }

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
        if (gpsDatabaseList.size() > 0) {
            for (GPSDatabase gpsDatabase : gpsDatabaseList) {
                gpsDatabase.close();
            }
        }
    }

    @Override
    public void onDestroy() {
        closeDatabases();
        super.onDestroy();
    }
}
