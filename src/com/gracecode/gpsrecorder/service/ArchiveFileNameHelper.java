package com.gracecode.gpsrecorder.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import com.gracecode.gpsrecorder.activity.Preference;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ArchiveFileNameHelper {

    private Context context;
    private SharedPreferences sharedPreferences;
    private String lastOpenedDatabasePath;
    private static final String LAST_OPENED_ARCHIVE_FILE_NAME = "lastOpenedArchiveFileName";
    private SharedPreferences.Editor editor;

    public static final String SQLITE_DATABASE_FILENAME_EXT = ".sqlite";
    public static final String SAVED_EXTERNAL_DIRECTORY = "gpsrecorder";
    public static final String GROUP_BY_EACH_MONTH = "yyyyMM";
    public static final String GROUP_BY_EACH_DAY = "yyyyMMdd";


    public ArchiveFileNameHelper(Context context) {
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        this.editor = this.sharedPreferences.edit();
    }

    public static boolean isExternalStoragePresent() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static File getExternalStoragePath() {
        if (isExternalStoragePresent()) {
            return Environment.getExternalStorageDirectory();
        }
        return null;
    }

    public static File getStorageDirectory(Date date) {
        String saveDirectory = getExternalStoragePath() + File.separator + SAVED_EXTERNAL_DIRECTORY + File.separator;

        // 如果保存目录不存在，则自动创建个
        File saveDirectoryFile = new File(saveDirectory);
        if (!saveDirectoryFile.isDirectory()) {
            saveDirectoryFile.mkdirs();
        }

        saveDirectory += File.separator + new SimpleDateFormat(GROUP_BY_EACH_MONTH).format(date);
        return new File(saveDirectory);
    }

    public static File getCurrentStorageDirectory() {
        return getStorageDirectory(new Date());
    }


    public String getNewArchiveFileName() {
        String RECORD_BY = sharedPreferences.getString(Preference.RECORD_BY, Preference.RECORD_BY_TIMES);
        String databaseFileName = System.currentTimeMillis() + SQLITE_DATABASE_FILENAME_EXT;
        if (RECORD_BY.equals(Preference.RECORD_BY_DAY)) {
            databaseFileName = (new SimpleDateFormat(GROUP_BY_EACH_DAY).format(new Date())) + SQLITE_DATABASE_FILENAME_EXT;
        }

        File databaseFile = new File(getCurrentStorageDirectory().getAbsoluteFile() + File.separator + databaseFileName);
        return databaseFile.getAbsolutePath();
    }


    /**
     * 获得已经存在过的未清理的文件
     *
     * @return
     */
    public String getResumeArchiveFileName() {
        if (sharedPreferences.contains(LAST_OPENED_ARCHIVE_FILE_NAME)) {
            return sharedPreferences.getString(LAST_OPENED_ARCHIVE_FILE_NAME, "");
        }

        return null;
    }

    public boolean clearLastOpenedDatabaseFilePath() {
        if (sharedPreferences.contains(LAST_OPENED_ARCHIVE_FILE_NAME)) {
            editor.remove(LAST_OPENED_ARCHIVE_FILE_NAME);
            return editor.commit();
        }

        return false;
    }

    public boolean setLastOpenedDatabaseFilePath(String name) {
        editor.putString(LAST_OPENED_ARCHIVE_FILE_NAME, name);
        return editor.commit();
    }

    public boolean hasResumeArchiveFile() {
        String resumeArchiveFileName = getResumeArchiveFileName();
        if (resumeArchiveFileName != null) {
            File resumeFile = new File(resumeArchiveFileName);
            return (resumeFile.exists() && resumeFile.isFile() && resumeFile.canWrite()) ? true : false;
        } else {
            return false;
        }
    }
}
