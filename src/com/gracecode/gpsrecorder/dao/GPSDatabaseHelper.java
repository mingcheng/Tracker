package com.gracecode.gpsrecorder.dao;

public class GPSDatabaseHelper {
//    private static final String FORMAT_RECORD_BY_MONTH = "yyyyMM";
//    private static final String FORMAT_RECORD_BY_DAY = "yyyyMMdd";
//    private static final String FORMAT_RECORD_BY_TIMES = "yyyyMMddHHmmss";
//
//
//
//    public File getDatabaseDirectory() {
//        String storageDirectory = Environment.getStorageDirectory().getAbsolutePath();
//        storageDirectory += File.separator + new SimpleDateFormat(FORMAT_RECORD_BY_MONTH).format(new Date());
//
//        File storageDirectoryFile = new File(storageDirectory);
//        if (!storageDirectoryFile.exists()) {
//            Log.w(TAG, String.format("%s is not exists, create directory first",
//                storageDirectoryFile.mkdirs()));
//            storageDirectoryFile.mkdirs();
//        }
//
//        return storageDirectoryFile;
//    }
//
//    public File getDatabaseFile() {
//        String prefsRecordBy = sharedPreferences.getString(Environment.PREF_RECORD_BY, Environment.RECORD_BY_DAY);
//
//        SimpleDateFormat databaseFileFormater = new SimpleDateFormat(FORMAT_RECORD_BY_DAY);
//        if (prefsRecordBy.equals(Environment.RECORD_BY_TIMES)) {
//            databaseFileFormater = new SimpleDateFormat(FORMAT_RECORD_BY_TIMES);
//        }
//
//        String databaseFileName = databaseFileFormater.format(new Date()) + ".sqlite";
//
//        Log.e(TAG, getDatabaseDirectory().getAbsolutePath());
//
//        Log.e(TAG, databaseFileName);
//
//        return new File(getDatabaseDirectory().getAbsolutePath() + File.separator + databaseFileName);
//    }
}
