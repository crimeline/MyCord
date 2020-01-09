package com.android.diagnosislibrary.module.logCollectionManager;

import android.content.Context;
import android.util.Log;

import com.android.diagnosislibrary.config.RDConfig;
import com.android.diagnosislibrary.utils.Logger.Logger;
import com.android.diagnosislibrary.utils.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

public class WriteLogToFile {
    private static final String TAG = "WriteLogToFile";
    private static WriteLogToFile mWriteLogToFile = null;
    private Context mContext = null;

    private static final int KB = 1024;
    private static final int MB = 1024 * KB;
    private static final int LOG_FILE_MAX_LENGTH = 10 * MB;
    private OutputStream outputStream = null;
    private boolean isWriteLog = true;
    private boolean isRunning = false;

    private WriteLogToFile(Context context) {
        if (mContext == null) {
            mContext = context;
        }
    }

    public static synchronized WriteLogToFile getInstance(Context context) {
        if (mWriteLogToFile == null) {
            if (context == null) {
                return null;
            }
            mWriteLogToFile = new WriteLogToFile(context);
        }

        return mWriteLogToFile;
    }

    /**
     * 暂停写日志
     */
    public void pauseWriteLog() {
        isWriteLog = false;
    }

    /**
     * 开始写日志
     */
    public void openWriteLog() {
        isWriteLog = true;
    }

    /**
     * 拷贝最新的日志文件
     */
    public void capyLogfile() {
        try {
            Logger.d(TAG, "switchLogfile ...");
            pauseWriteLog();
            File logfile = new File(mContext.getFilesDir(), RDConfig.LOG_FILE_NAME);
            String filename = String.format(RDConfig.LOG_FILE_NAME_N, RDConfig.mMaxCount + 1);
            File savefile = new File(mContext.getFilesDir(), filename);
            while (isRunning) {
                Thread.sleep(200);
            }
            closeLogFile();
            ///TODO:修改成cp
            if (!copyFile(logfile, savefile)) {
                Log.e(TAG, "saveLogfile: renameTo error.");
            }
            openWriteLog();
        } catch (Exception e) {
            Log.d(TAG, "getFime: error " + e.toString());
        }
        return;
    }

    /**
     * 写log文件
     *
     * @param log log 内容
     */
    public void writeLog(String log) {
        if (!isWriteLog) {
            return;
        }
        if (StringUtils.isNullOrEmpty(log)) {
            return;
        }
        try {
            isRunning = true;
            if (outputStream == null) {
                openLogfile();
            }
            outputStream.write(log.getBytes());
            saveLogfile();
            isRunning = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取配置文件大小
     *
     * @return
     */
    private long getLength() {
        int size = RDConfig.getInstance().getMaxsize();
        if (size <= 0) {
            return LOG_FILE_MAX_LENGTH;
        }
        if (size < 512 * KB) {
            size = 512 * KB;
        }
        return size;
    }

    /**
     * 获取保存文件
     *
     * @return
     */
    private File getSaveFile() {
        File file = null;
        File saveFile = null;
        try {
            for (int i = 0; i <= RDConfig.mMaxCount; i++) {
                String filename = String.format(RDConfig.LOG_FILE_NAME_N, i);
                file = new File(mContext.getFilesDir(), filename);
                long time = file.lastModified();
                if (time == 0) {
                    return file;
                }

                if (saveFile == null) {
                    saveFile = file;
                    continue;
                }

                if (time < saveFile.lastModified()) {
                    saveFile = file;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getSaveFileName: error " + e.toString());
            saveFile = new File(mContext.getFilesDir(), String.format(RDConfig.LOG_FILE_NAME_N, 0));
            return saveFile;
        }

        return saveFile;
    }

    /**
     * 保存log文件
     *
     * @return 返回
     */
    private void saveLogfile() {
        try {
            File logfile = new File(mContext.getFilesDir(), RDConfig.LOG_FILE_NAME);
            if (logfile.length() < getLength()) {
                return;
            }

            long localTime = new Date().getTime();
            File savefile = getSaveFile();
            if (localTime >= savefile.lastModified()) {
                closeLogFile();
                savefile.delete();
                if (logfile.renameTo(savefile)) {
                    Log.e(TAG, "saveLogfile: renameTo error.");
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "getFime: error " + e.toString());
        }
        return;
    }

    /**
     * 打开文件
     */
    private void openLogfile() {
        try {
            if (outputStream == null) {
                File logFile = new File(mContext.getFilesDir(), RDConfig.LOG_FILE_NAME);
                outputStream = new FileOutputStream(logFile, true);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭文件
     */
    private void closeLogFile() {
        try {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
                outputStream = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 复制文件
     */
    private static boolean copyFile(File src, File des) {
        if (!src.exists()) {
            Log.e("cppyFile", "file not exist:" + src.getAbsolutePath());
            return false;
        }
        if (!des.getParentFile().isDirectory() && !des.getParentFile().mkdirs()) {
            Log.e("cppyFile", "mkdir failed:" + des.getParent());
            return false;
        }
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(src));
            bos = new BufferedOutputStream(new FileOutputStream(des));
            byte[] buffer = new byte[4 * 1024];
            int count;
            while ((count = bis.read(buffer, 0, buffer.length)) != -1) {
                if (count > 0) {
                    bos.write(buffer, 0, count);
                }
            }
            bos.flush();
            return true;
        } catch (Exception e) {
            Log.e("copyFile", "exception:", e);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
