package com.gracecode.tracker.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Environment;
import android.preference.PreferenceManager;
import com.gracecode.tracker.dao.Archiver;
import com.gracecode.tracker.ui.activity.Preference;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class ArchiveNameHelper {
    private Context context;
    private SharedPreferences sharedPreferences;
    private static final String LAST_OPENED_ARCHIVE_FILE_NAME = "lastOpenedArchiveFileName";
    private SharedPreferences.Editor editor;

    public static final String SQLITE_DATABASE_FILENAME_EXT = ".sqlite";
    public static final String SAVED_EXTERNAL_DIRECTORY = "tracker";
    public static final String GROUP_BY_EACH_MONTH = "yyyyMM";
    public static final String GROUP_BY_EACH_DAY = "yyyyMMdd";

    public ArchiveNameHelper(Context context) {
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.editor = sharedPreferences.edit();
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
        String saveDirectory = getExternalStoragePath() + File.separator + SAVED_EXTERNAL_DIRECTORY
            + File.separator + new SimpleDateFormat(GROUP_BY_EACH_MONTH).format(date);

        // 如果保存目录不存在，则自动创建个
        File saveDirectoryFile = new File(saveDirectory);
        if (!saveDirectoryFile.isDirectory()) {
            saveDirectoryFile.mkdirs();
        }

        return saveDirectoryFile;
    }

    public static File getCurrentStorageDirectory() {
        return getStorageDirectory(new Date());
    }


    public ArrayList<String> getArchiveFilesNameByMonth(Date date) {
        ArrayList<String> result = new ArrayList<String>();

        File storageDirectory = getStorageDirectory(date);
        File[] archiveFiles = storageDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(SQLITE_DATABASE_FILENAME_EXT);
            }
        });

        if (archiveFiles != null) {
            /**
             * Sort by first record time.
             */
            Arrays.sort(archiveFiles, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    Archiver archiver1 = new Archiver(context, f1.getAbsolutePath(), Archiver.MODE_READ_ONLY);
                    Archiver archiver2 = new Archiver(context, f2.getAbsolutePath(), Archiver.MODE_READ_ONLY);

                    Location location1 = archiver1.getFirstRecord();
                    Location location2 = archiver2.getFirstRecord();

                    Long time1 = location1.getTime();
                    Long time2 = location2.getTime();

                    archiver1.close();
                    archiver2.close();

                    return Long.valueOf(time2).compareTo(time1);
                }
            });

            for (int i = 0; i < archiveFiles.length; i++) {
                result.add((archiveFiles[i]).getAbsolutePath());
            }
        }

        return result;
    }

    public ArrayList<String> getArchiveFilesFormCurrentMonth() {
        return getArchiveFilesNameByMonth(new Date());
    }

    public String getNewName() {
        String RECORD_BY = sharedPreferences.getString(Preference.RECORD_BY, Preference.RECORD_BY_TIMES);
        String databaseFileName = System.currentTimeMillis() + SQLITE_DATABASE_FILENAME_EXT;
        if (RECORD_BY.equals(Preference.RECORD_BY_DAY)) {
            databaseFileName = (new SimpleDateFormat(GROUP_BY_EACH_DAY).format(new Date())) + SQLITE_DATABASE_FILENAME_EXT;
        }

        File databaseFile = new File(getCurrentStorageDirectory().getAbsolutePath() + File.separator + databaseFileName);
        return databaseFile.getAbsolutePath();
    }


    /**
     * 获得已经存在过的未清理的文件
     *
     * @return
     */
    public String getResumeName() {
        if (sharedPreferences.contains(LAST_OPENED_ARCHIVE_FILE_NAME)) {
            return sharedPreferences.getString(LAST_OPENED_ARCHIVE_FILE_NAME, "");
        }

        return null;
    }

    public boolean clearLastOpenedName() {
        if (sharedPreferences.contains(LAST_OPENED_ARCHIVE_FILE_NAME)) {
            editor.remove(LAST_OPENED_ARCHIVE_FILE_NAME);
            return editor.commit();
        }

        return false;
    }

    public boolean setLastOpenedName(String name) {
        editor.putString(LAST_OPENED_ARCHIVE_FILE_NAME, name);
        return editor.commit();
    }

    public boolean hasResumeName() {
        String resumeArchiveFileName = getResumeName();
        if (resumeArchiveFileName != null) {
            File resumeFile = new File(resumeArchiveFileName);
            return (resumeFile.exists() && resumeFile.isFile() && resumeFile.canWrite()) ? true : false;
        } else {
            return false;
        }
    }
}
