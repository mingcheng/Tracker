package com.gracecode.tracker.activity;

import android.app.ProgressDialog;
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
import com.gracecode.tracker.dao.Archive;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.service.ArchiveNameHelper;

import java.util.ArrayList;
import java.util.Iterator;

public class Records extends Base implements AdapterView.OnItemClickListener {
    private Context context;

    //
    private ListView listView;
    private ArrayList<String> archiveFileNames;
    private ArrayList<Archive> archives;

    //    private SimpleAdapter listViewAdapter;
    private ProgressDialog progressDialog;
    private ArchiveNameHelper archiveFileNameHelper;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Archive archive = archives.get(i);
        Intent intent = new Intent(this, BaiduMap.class);
        intent.putExtra("archiveName", archive.getArchiveFileName());

        startActivity(intent);
    }

    public class ArchivesAdapter extends ArrayAdapter<Archive> {

        public ArchivesAdapter(ArrayList<Archive> archives) {
            super(context, R.layout.records_row, archives);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Archive archive = archives.get(position);
            ArchiveMeta archiveMeta = archive.getArchiveMeta();

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.records_row, parent, false);

            TextView countView = (TextView) rowView.findViewById(R.id.db_records_num);
            TextView nameView = (TextView) rowView.findViewById(R.id.db_name);
            TextView descriptionView = (TextView) rowView.findViewById(R.id.description);
            TextView betweenView = (TextView) rowView.findViewById(R.id.between);

            countView.setText(String.valueOf(archiveMeta.getCount()));

//            String description = archiveMeta.getDescription();
//            if (description.length() <= 0) {
//                description = getString(R.string.no_description);
//            }
//            descriptionView.setText(description);

            return rowView;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.records);

        this.context = getApplicationContext();
        listView = (ListView) findViewById(R.id.records_list);

        listView.setOnItemClickListener(this);

        this.archiveFileNameHelper = new ArchiveNameHelper(context);

        archives = new ArrayList<Archive>();
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        progressDialog.setMessage(getString(R.string.saving));
//        progressDialog.setCancelable(false);
//
        //  locations = new ArrayList<Location>();

//        getStorageDatabases();
//
//
    }


    @Override
    public void onResume() {
        super.onResume();

        archiveFileNames = archiveFileNameHelper.getArchiveFilesFormCurrentMonth();
        openArchivesFromFileNames();

        listView.setAdapter(new ArchivesAdapter(archives));


    }


    @Override
    public void onPause() {
        super.onPause();

        closeArchives();
    }

    private void openArchivesFromFileNames() {
        closeArchives();
        Iterator<String> iterator = archiveFileNames.iterator();
        while (iterator.hasNext()) {
            String name = (String) iterator.next();
            archives.add(new Archive(context, name));
        }
    }

    private void closeArchives() {
        if (archives != null && archives.size() > 1) {
            Iterator<Archive> iterator = archives.iterator();
            while (iterator.hasNext()) {
                Archive archive = (Archive) iterator.next();
                archive.close();
            }

            archives.clear();
        }
    }

//
//    private void updateListView() {
//        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//            @Override
//            public void onCreateContextMenu(ContextMenu contextMenu,
//                                            View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
//                MenuInflater inflater = getMenuInflater();
//                inflater.inflate(R.menu.records_context, contextMenu);
//            }
//        });
//
//        listView.setAdapter(gpsdatabaseAdapter);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.records, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        switch (item.getItemId()) {
//            case R.id.calendar:
////                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
////
////                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
////                View layout = inflater.inflate(R.layout.date_picker, null);
////
////                ViewGroup datePacker = (ViewGroup) layout.findViewById(R.id.select_date);
////                datePacker.getChildAt(2).setVisibility(View.GONE);
////
////                dialog.setView(layout);
////                dialog.setTitle("Select Month");
////
////                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
////                    public void onClick(DialogInterface dialog, int id) {
////
////                    }
////                });
////
////                dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
////                    public void onClick(DialogInterface dialog, int id) {
////                        dialog.cancel();
////                    }
////                });
////                dialog.show();
//
//                return true;
//        }
//
//        return false;
//    }

    //长按菜单响应函数
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//        final int position = info.position;
//
//        switch (item.getItemId()) {
//            case R.id.export:
////                progressDialog.show();
//                return true;
//
//            case R.id.description:
//                updateDescriptionByModalDialog(position);
//                return true;
//            case R.id.delete:
//                confirmDeleteDatabaseFile(position);
//                return true;
//        }
//        return false;
//    }
//
//    private void updateDescriptionByModalDialog(final int position) {
//        final EditText editText = new EditText(this);
//        final GPSDatabase storageDatabase = locations.get(position);
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
//    }
//
//    private void confirmDeleteDatabaseFile(final int position) {
//        final GPSDatabase storageDatabase = locations.get(position);
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
//                        locations.remove(position);
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

//    private void closeDatabases() {
////        if (locations.size() > 0) {
////            for (GPSDatabase gpsDatabase : locations) {
////                gpsDatabase.close();
////            }
////        }
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }
}
