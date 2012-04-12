package com.gracecode.tracker.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import com.gracecode.tracker.R;

public class Preference extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String USER_ORIENTATION = "orientation";
    public static final String AUTO_START = "autoStart";
    public static final String RECORD_BY = "recordBy";
    public static final String LIGHTNING_LED = "lightLed";
    public static final String GPS_MINTIME = "gpsMinTime";
    public static final String GPS_MINDISTANCE = "gpsMinDistance";
    public static final String AUTO_CLEAN = "autoClean";
    public static final String SWITCH_AIRPLANE_MODE = "switchAirplaneMode";

    public static final String RECORD_BY_DAY = "RECORD_BY_DAY";
    public static final String RECORD_BY_TIMES = "RECORD_BY_TIMES";

    public static final String DEFAULT_GPS_MINTIME = "2000";
    public static final String DEFAULT_GPS_MINDISTANCE = "10";
    private SharedPreferences preferenceManager;
    public static final String DEFAULT_USER_ORIENTATION = "portrait";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        preferenceManager = PreferenceManager.getDefaultSharedPreferences(this);
        preferenceManager.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Boolean autoClean = sharedPreferences.getBoolean(AUTO_CLEAN, true);
        if (key.equals(AUTO_CLEAN) && !autoClean) {

//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putBoolean(AUTO_CLEAN, true);
//            editor.commit();

        }
    }
}
