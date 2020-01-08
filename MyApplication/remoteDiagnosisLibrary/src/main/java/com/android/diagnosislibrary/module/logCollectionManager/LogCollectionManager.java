package com.android.diagnosislibrary.module.logCollectionManager;

import android.content.Context;

import com.android.diagnosislibrary.config.RDConfig;
import com.android.diagnosislibrary.module.handlerManager.RunCommand;
import com.android.diagnosislibrary.module.websocket.WebMsgListener;
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

    public void stopRunningShellCmd() {
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
        stopRunningShellCmd();
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
        String command = "logcat " + filter;
        Logger.d(TAG, "writLogToFile : " + command);
        RunCommand runCommand = new RunCommand(command, 0);
        runCommand.setCallBack(new WebMsgListener.CommandCallBack() {
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

    public void stopLog() {
        WriteLogToFile.getInstance(mContext).pauseWriteLog();
    }

    /**
     * 上传日志
     */
    public void switchLogfile() {
        Logger.d(TAG, "switchLogfile ...");
        WriteLogToFile.getInstance(mContext).switchLogfile();
    }

}
