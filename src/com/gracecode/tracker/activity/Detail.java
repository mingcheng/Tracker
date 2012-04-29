package com.gracecode.tracker.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;
import com.gracecode.tracker.R;
import com.gracecode.tracker.activity.base.Activity;
import com.gracecode.tracker.dao.Archive;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.fragment.ArchiveMetaFragment;
import com.gracecode.tracker.fragment.ArchiveMetaTimeFragment;

public class Detail extends Activity {
    private String archiveFileName;

    private Archive archive;
    private ArchiveMeta archiveMeta;

    private ArchiveMetaFragment archiveMetaFragment;
    private ArchiveMetaTimeFragment archiveMetaTimeFragment;

    private TextView mDescription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
        archive = new Archive(context, archiveFileName, Archive.MODE_READ_WRITE);
        archiveMeta = archive.getMeta();

        mDescription = (TextView) findViewById(R.id.item_description);

        archiveMetaFragment = new ArchiveMetaFragment(context, archiveMeta);
        archiveMetaTimeFragment = new ArchiveMetaTimeFragment(context, archiveMeta);
    }

    @Override
    public void onStart() {
        super.onStart();
        mDescription.setText(archiveMeta.getDescription());
        addArchiveMetaTimeFragment();
        addArchiveMetaFragment();
    }

    private void addFragment(int layout, Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(layout, fragment);
        fragmentTransaction.commit();
    }

    private void addArchiveMetaTimeFragment() {
        addFragment(R.id.archive_meta_time_layout, archiveMetaTimeFragment);
    }

    private void addArchiveMetaFragment() {
        addFragment(R.id.archive_meta_layout, archiveMetaFragment);
    }
}
