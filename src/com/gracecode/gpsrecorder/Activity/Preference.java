package com.gracecode.gpsrecorder.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.gracecode.gpsrecorder.R;

public class Preference extends PreferenceActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
    }

}
