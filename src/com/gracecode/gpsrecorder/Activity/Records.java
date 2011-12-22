package com.gracecode.gpsrecorder.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
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
import com.gracecode.gpsrecorder.util.Database;
import com.gracecode.gpsrecorder.util.KMLHepler;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Records extends Activity {
    private final String TAG = Records.class.getName();
    private Database db;
    private Context context;

    private ListView listView;
    protected File[] storageFileList;
    protected ArrayList<HashMap<String, String>> storageFileHashList = new ArrayList<HashMap<String, String>>();
    private SimpleAdapter listViewAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.records);

        this.context = getApplicationContext();
        this.db = new Database(context);

        listView = (ListView) findViewById(R.id.records_list);

        updateStorageFileList();
        updateListView();
    }

    protected void updateStorageFileList() {
        updateStorageFileList(new Date());
    }


    protected void updateStorageFileList(Date selectedDate) {
//        String definedDate = getIntent().getStringExtra("date");

        // get the parent directory handle
        File currentStorageDir = new File(db.getDatabasePath(selectedDate)).getParentFile();

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
                Database d = new Database(context, dbFile);
                HashMap<String, String> map = new HashMap<String, String>();

                map.put("database", dbFile.getName());
                map.put("count", String.format("%d records", d.getValvedCount()));
                map.put("absolute", dbFile.getAbsolutePath());

                storageFileHashList.add(map);
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
        HashMap<String, String> map = storageFileHashList.get(info.position);
        String absolutePath = map.get("absolute");

        db = new Database(context, new File(absolutePath));
        switch (item.getItemId()) {
            case R.id.export:
                KMLHepler kml = new KMLHepler(db.getValvedData());
                String kmlXMLString = kml.getXMLString();
                Log.e(TAG, kmlXMLString);
                // Toast.makeText(context, absolutePath, Toast.LENGTH_LONG).show();
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
        return false;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (db != null) {
            db.close();
        }
    }
}
