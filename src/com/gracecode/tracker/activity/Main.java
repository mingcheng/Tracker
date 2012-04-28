package com.gracecode.tracker.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.gracecode.tracker.R;
import com.gracecode.tracker.fragment.ArchiveMetaFragment;

public class Main extends FragmentActivity implements View.OnClickListener, View.OnLongClickListener {
    private FragmentManager fragmentManager;
    private Button mStartButton;
    private Button mEndButton;
    private ArchiveMetaFragment archiveMetaFragment;

    private static final int FLAG_START = 0x001;
    private static final int FLAG_END = 0x002;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracker);

        mStartButton = (Button) findViewById(R.id.btn_start);
        mEndButton = (Button) findViewById(R.id.btn_end);

        fragmentManager = getSupportFragmentManager();
    }

    @Override
    public void onStart() {
        super.onStart();
        mStartButton.setOnClickListener(this);
        mEndButton.setOnClickListener(this);
        mEndButton.setOnLongClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                setViewStatus(FLAG_START);
                break;
            case R.id.btn_end:
                Toast.makeText(this, getString(R.string.long_press_to_stop), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        setViewStatus(FLAG_END);
        return true;
    }

    private void setViewStatus(int status) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch (status) {
            case FLAG_START:
                mStartButton.setVisibility(View.GONE);
                mEndButton.setVisibility(View.VISIBLE);
                archiveMetaFragment = new ArchiveMetaFragment();

                fragmentTransaction.add(R.id.status_layout, archiveMetaFragment);
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                break;
            case FLAG_END:
                mStartButton.setVisibility(View.VISIBLE);
                mEndButton.setVisibility(View.GONE);

                fragmentTransaction.remove(archiveMetaFragment);
                break;
        }

        fragmentTransaction.commit();
    }
}
