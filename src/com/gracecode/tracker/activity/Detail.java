package com.gracecode.tracker.activity;

import android.os.Bundle;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.Archive;
import com.gracecode.tracker.dao.ArchiveMeta;

import java.io.IOException;

public class Detail extends Base {
    private String archiveFileName;
    private Archive archive;
    private ArchiveMeta archiveMeta;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
        try {
            archive = new Archive(context, archiveFileName);
            archiveMeta = archive.getArchiveMeta();
        } catch (IOException e) {
            uiHelper.showLongToast(getString(R.string.archive_not_exists));
            finish();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        archive.close();
    }

}
