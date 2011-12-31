package com.gracecode.gpsrecorder.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.gracecode.gpsrecorder.R;
import com.gracecode.gpsrecorder.dao.GPSDatabase;
import com.gracecode.gpsrecorder.util.KMLHelper;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Records extends BaseActivity {
    public static final int HIDE_PROGRESS_DIALOG = 0x1;
    private final String TAG = Records.class.getName();
    private GPSDatabase db;
    private Context context;

    private ListView listView;
    protected File[] storageFileList;
    protected ArrayList<HashMap<String, String>> storageFileHashList = new ArrayList<HashMap<String, String>>();
    private SimpleAdapter listViewAdapter;
    private ProgressDialog progressDialog;
    private Thread saveKMLFileThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.records);

        this.context = getApplicationContext();
        this.db = new GPSDatabase(context);

        listView = (ListView) findViewById(R.id.records_list);
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.saving));
        progressDialog.setCancelable(false);

        updateStorageFileList();
        updateListView();
    }

    protected void updateStorageFileList() {
        updateStorageFileList(new Date());
    }


    protected void updateStorageFileList(Date selectedDate) {
//        String definedDate = getIntent().getStringExtra("date");

        // get the parent directory handle
        File currentStorageDir = db.getStorageDirectory(new Date());

        storageFileList = currentStorageDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                if (s.endsWith(".sqlite")) {
                    return true;
                }
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        storageFileHashList.clear();
        for (File dbFile : storageFileList) {
            try {
                GPSDatabase d = new GPSDatabase(context, dbFile);
                if (d.getValvedCount() > 0) {
                    HashMap<String, String> map = new HashMap<String, String>();

                    map.put("database", dbFile.getName());
                    map.put("count", String.format("%d records", d.getValvedCount()));
                    map.put("absolute", dbFile.getAbsolutePath());

                    storageFileHashList.add(map);
                }
                d.close();
            } catch (SQLiteException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }


    private void updateListView() {
        listViewAdapter = new SimpleAdapter(this, storageFileHashList,
            R.layout.records_item,
            new String[]{"database", "count"},
            new int[]{R.id.db_name, R.id.db_records_num});

        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.records, contextMenu);
            }
        });

        listView.setAdapter(listViewAdapter);
    }

    //长按菜单响应函数
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final HashMap<String, String> map = storageFileHashList.get(info.position);
        String absolutePath = map.get("absolute");

        db = new GPSDatabase(context, new File(absolutePath));
        switch (item.getItemId()) {
            case R.id.export:
                progressDialog.show();
                saveKMLFileThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            String name = map.get("database");
                            String description = "";

                            // @todo convert cursor into list
                            Cursor data = db.getValvedData();
                            final KMLHelper kml = new KMLHelper(name, description, data);

                            String basePath = getString(R.string.app_database_store_path);
                            File kmlFile = new File(basePath + "/" + name.replace(".sqlite", ".kml"));
                            kml.saveKMLFile(kmlFile.getAbsoluteFile());

                            Message message = new Message();
                            message.what = HIDE_PROGRESS_DIALOG;
                            handle.sendMessage(message);

                            data.close();
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }

                };
                saveKMLFileThread.start();
                return true;
            /**
             * Mark as deleted not really delete the data.
             */
            case R.id.delete:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle(getString(R.string.notice))
                    .setMessage(getString(R.string.sure_to_del))
                    .setIcon(android.R.drawable.ic_dialog_alert);

                dialog.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        int effectedRows = db.markAllAsDelete();
                        if (effectedRows > 0) {
                            updateStorageFileList();
                            listViewAdapter.notifyDataSetChanged();

                            Toast.makeText(context,
                                String.format(getString(R.string.has_deleted), Integer.toString(effectedRows)),
                                Toast.LENGTH_LONG).show();
                        }
                    }
                }).setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).show();

                return true;
        }
        db.close();
        return false;
    }

    private Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE_PROGRESS_DIALOG:
                    Toast.makeText(context,
                        getString(R.string.save_kml_finished), Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    break;
            }
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        if (db != null) {
            db.close();
        }
    }
}
