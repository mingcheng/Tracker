package com.gracecode.gpsrecorder.activity;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;
import android.widget.TextView;
import com.gracecode.gpsrecorder.R;
import com.gracecode.gpsrecorder.util.Database;

import java.io.File;

public class RecordList extends Activity {
    private final String TAG = RecordList.class.getName();
    private Database db;
    private Context context;

    private ListView listView;
    private TextView listStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.records);

        this.context = getApplication();
        this.db = new Database(context);

        listView = (ListView) findViewById(R.id.records_list);
        listStatus = (TextView) findViewById(R.id.records_status);
        updateView();
    }


    private void updateView() {

        String dbPath = db.getDatabasePath();

        new File(dbPath);

        Cursor result = db.getValvedData();
        if (result != null) {
            Log.e(TAG, "" + result.getCount());
            listStatus.setText(result.getCount());

            /*
            String[] items = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, items);

            listView.setAdapter(adapter);
            */
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.records, menu);
        return true;
    }
}
