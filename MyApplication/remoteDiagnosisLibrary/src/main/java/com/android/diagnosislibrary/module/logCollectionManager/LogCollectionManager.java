package com.android.diagnosislibrary.module.logCollectionManager;

import android.content.Context;

import com.android.diagnosislibrary.config.RDConfig;
import com.android.diagnosislibrary.module.shellCmdManager.RunCommand;
import com.android.diagnosislibrary.DiagnosisManagement;
import com.android.diagnosislibrary.utils.Logger.Logger;
import com.android.diagnosislibrary.utils.StringUtils;

public class LogCollectionManager {
    private static final String TAG = "LogCollectionManager";
    private static Context mContext = null;
    private static LogCollectionManager mLogCollectionManager = null;
    private RunCommand mRunCommand = null;
    private String oldFilter = null;

    private LogCollectionManager(Context ctx) {
        this.mContext = ctx;
    }

    public static synchronized LogCollectionManager getInstance(Context ctx) {
        if (mLogCollectionManager == null) {
            if (ctx == null) {
                Logger.d(TAG, "context is null");
                return null;
            }
            mLogCollectionManager = new LogCollectionManager(ctx);
        }
        return mLogCollectionManager;
    }

    private void stopRunningLogCmd() {
        if (mRunCommand != null) {
            try {
                Thread.sleep(500);
                mRunCommand.terminal();
                mRunCommand = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 下发过滤条件
     *
     * @param filter
     */
    public void setLogFilter(String filter) {
        stopRunningLogCmd();
        writLogToFile(filter);
    }

    private String getFilter() {
        String filter = RDConfig.getInstance().getFilter();
        if (StringUtils.isNullOrEmpty(filter)) {
            filter = " | grep -e \"VOS\" -e \"AndroidRuntime\" -e \"System.err\" -e \"WebSocketUtil\"";
        }
        return filter;
    }

    private void writLogToFile(String filter) {
        if (filter.equals(oldFilter) || mRunCommand != null) {
            return;
        }
        if (filter.contains("grep") && !filter.contains("line-buffered")) {
            filter = filter.replace("grep", "grep --line-buffered");
        }
        String command = "logcat " + filter;
        Logger.d(TAG, "writLogToFile : " + command);
        RunCommand runCommand = new RunCommand(command, 0);
        runCommand.setCallBack(new DiagnosisManagement.CommandCallBack() {
            @Override
            public void sendResult(String line) {
                WriteLogToFile.getInstance(mContext).writeLog(line + '\n');
            }
        });
        runCommand.start();
        oldFilter = filter;
        mRunCommand = runCommand;
    }

    public void startLog() {
        String filter = getFilter();
        WriteLogToFile.getInstance(mContext).openWriteLog();
        writLogToFile(filter);
    }

    public void pauseLog() {
        if (mRunCommand == null) {
            return;
        }
        WriteLogToFile.getInstance(mContext).pauseWriteLog();
    }

    public void stopLog() {
        if (mRunCommand == null) {
            return;
        }
        pauseLog();
        stopRunningLogCmd();
    }

    /**
     * 上传日志
     */
    public boolean switchLogfile() {
        Logger.d(TAG, "switchLogfile ...");
        if (mRunCommand == null) {
            return false;
        }
        WriteLogToFile.getInstance(mContext).switchLogfile();
        return true;
    }

}
