package com.gracecode.tracker.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.dao.Archiver;
import com.gracecode.tracker.ui.activity.base.Activity;

public class Modify extends Activity implements View.OnClickListener {

    private Button mBtnConfirm;
    private EditText mDescription;

    private String archiveFileName;
    private Archiver archive;
    private ArchiveMeta archiveMeta;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify);

        mBtnConfirm = (Button) findViewById(R.id.confirm);
        mDescription = (EditText) findViewById(R.id.description);

        archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
    }

    @Override
    public void onStart() {
        super.onStart();

        archive = new Archiver(context, archiveFileName, Archiver.MODE_READ_WRITE);
        if (archive == null || !archive.exists()) {
            helper.showShortToast(getString(R.string.archive_not_exists));
            finish();
            return;
        }
        archiveMeta = archive.getMeta();

        actionBar.setTitle(getString(R.string.title_modify));

        mDescription.setText(archiveMeta.getDescription());
        mBtnConfirm.setOnClickListener(this);
    }

    @Override
    public void onPause() {
        if (archive != null) {
            archive.close();
        }
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        String description = mDescription.getText().toString().trim();
        try {
            if (description.length() > 0 && archiveMeta.setDescription(description)) {
                helper.showLongToast(getString(R.string.has_benn_saved));
                finish();
            }
        } catch (Exception e) {
            helper.showLongToast(getString(R.string.shit_happens));
        }
    }
}
