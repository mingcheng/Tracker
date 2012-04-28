package com.gracecode.tracker.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gracecode.tracker.R;

public class ArchiveMetaFragment extends Fragment {
    private ArchiveMetaFragment meta;

    public ArchiveMetaFragment(ArchiveMetaFragment meta) {
        this.meta = meta;
    }

    public ArchiveMetaFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.archive_meta_items, container, false);
    }
}
