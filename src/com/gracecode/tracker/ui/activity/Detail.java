package com.gracecode.tracker.ui.activity;

import android.app.LocalActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.dao.Archiver;
import com.gracecode.tracker.ui.activity.base.Activity;
import com.gracecode.tracker.ui.activity.maps.BaiduMap;
import com.gracecode.tracker.ui.fragment.ArchiveMetaFragment;
import com.gracecode.tracker.ui.fragment.ArchiveMetaTimeFragment;
import com.markupartist.android.widget.ActionBar;
import com.umeng.api.sns.UMSnsService;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Detail extends Activity implements View.OnTouchListener, View.OnClickListener {
    private String archiveFileName;

    private Archiver archiver;
    private ArchiveMeta archiveMeta;

    private ArchiveMetaFragment archiveMetaFragment;
    private ArchiveMetaTimeFragment archiveMetaTimeFragment;

    private TextView mDescription;
    private LocalActivityManager localActivityManager;
    private TabHost mTabHost;
    private View mMapMask;
    public static final String INSIDE_TABHOST = "inside_tabhost";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        localActivityManager = new LocalActivityManager(this, false);
        localActivityManager.dispatchCreate(savedInstanceState);

        archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
        archiver = new Archiver(context, archiveFileName, Archiver.MODE_READ_WRITE);
        archiveMeta = archiver.getMeta();

        mMapMask = findViewById(R.id.map_mask);
        mDescription = (TextView) findViewById(R.id.item_description);
        mTabHost = (TabHost) findViewById(R.id.tabhost);

        archiveMetaFragment = new ArchiveMetaFragment(context, archiveMeta);
        archiveMetaTimeFragment = new ArchiveMetaTimeFragment(context, archiveMeta);
    }

    @Override
    public void onStart() {
        super.onStart();

        String description = archiveMeta.getDescription().trim();
        if (description.length() > 0) {
            mDescription.setTextColor(getResources().getColor(R.color.snowhite));
            mDescription.setText(description);
        } else {
            mDescription.setTextColor(getResources().getColor(R.color.gray));
            mDescription.setText(getString(R.string.no_description));
        }
        mDescription.setOnClickListener(this);

        addArchiveMetaTimeFragment();
        addArchiveMetaFragment();

        actionBar.setTitle(getString(R.string.title_detail));
        actionBar.removeAllActions();
        actionBar.addAction(new ActionBar.Action() {
            @Override
            public int getDrawable() {
                return R.drawable.ic_menu_share;
            }

            @Override
            public void performAction(View view) {
                shareToSina();
            }
        });
    }


    private void shareToSina() {
        byte[] bitmap = convertBitmapToByteArray(getRouteBitmap());
        String recordsFormatter = getString(R.string.records_formatter);
        SimpleDateFormat dateFormatter = new SimpleDateFormat(getString(R.string.time_format), Locale.getDefault());

        // Build string for share by microblog etc.
        String message = String.format(getString(R.string.share_report_formatter),
            archiveMeta.getDescription().length() > 0 ? "(" + archiveMeta.getDescription() + ")" : "",
            String.format(recordsFormatter, archiveMeta.getDistance() / ArchiveMeta.TO_KILOMETRE),
            dateFormatter.format(archiveMeta.getStartTime()),
            dateFormatter.format(archiveMeta.getEndTime()),
            archiveMeta.getRawCostTimeString(),
            String.format(recordsFormatter, archiveMeta.getMaxSpeed() * ArchiveMeta.KM_PER_HOUR_CNT),
            String.format(recordsFormatter, archiveMeta.getAverageSpeed() * ArchiveMeta.KM_PER_HOUR_CNT)
        );
        UMSnsService.shareToSina(context, bitmap, message, null);
    }

    private void confirmDelete() {
        helper.showConfirmDialog(
            getString(R.string.delete),
            String.format(getString(R.string.sure_to_del), archiveMeta.getName()),
            new Runnable() {
                @Override
                public void run() {
                    if (archiver.delete()) {
                        finish();
                    }
                }
            },
            new Runnable() {
                @Override
                public void run() {
                    // ...
                }
            }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_share:
                shareToSina();
                break;

            case R.id.menu_delete:
                confirmDelete();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    /**
     * Take screenshot from tabhost for sharing
     *
     * @return
     */
    private Bitmap getRouteBitmap() {
        View view = findViewById(R.id.detail_layout);
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        view.destroyDrawingCache();
        return Bitmap.createBitmap(view.getDrawingCache());
    }

    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    @Override
    public void onResume() {
        super.onResume();
        localActivityManager.dispatchResume();

        Intent mapIntent = new Intent(this, BaiduMap.class);
        String name = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
        mapIntent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, name);
        mapIntent.putExtra(INSIDE_TABHOST, true);

        TabHost.TabSpec tabSpec =
            mTabHost.newTabSpec("").setIndicator("").setContent(mapIntent);
        mTabHost.setup(localActivityManager);
        mTabHost.addTab(tabSpec);
        mMapMask.setOnTouchListener(this);

        if (!archiver.exists()) {
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mTabHost.clearAllTabs();
        localActivityManager.removeAllActivities();
        localActivityManager.dispatchPause(isFinishing());
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

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Intent intent = new Intent(this, BaiduMap.class);
            intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archiveFileName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, Modify.class);
        intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archiveFileName);
        startActivity(intent);
    }
}
