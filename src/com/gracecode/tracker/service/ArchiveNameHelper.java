package com.gracecode.tracker.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import com.gracecode.tracker.activity.Preference;

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
            // 根据最后修改时间排序
            Arrays.sort(archiveFiles, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
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

    public String getNewArchiveFileName() {
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
    public String getResumeArchiveFileName() {
        if (sharedPreferences.contains(LAST_OPENED_ARCHIVE_FILE_NAME)) {
            return sharedPreferences.getString(LAST_OPENED_ARCHIVE_FILE_NAME, "");
        }

        return null;
    }

    public boolean clearLastOpenedArchiveFileName() {
        if (sharedPreferences.contains(LAST_OPENED_ARCHIVE_FILE_NAME)) {
            editor.remove(LAST_OPENED_ARCHIVE_FILE_NAME);
            return editor.commit();
        }

        return false;
    }

    public boolean setLastOpenedArchiveFileName(String name) {
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
