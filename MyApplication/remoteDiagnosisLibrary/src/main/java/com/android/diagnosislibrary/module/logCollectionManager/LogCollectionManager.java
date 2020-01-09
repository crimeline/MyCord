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
    private boolean isRunning = false;

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
    public String setLogFilter(String filter) {
        if (filter.equals(oldFilter)) {
            return "set error: The filter is the same as the old one!!!\n" + "old filter:" + RDConfig.getInstance().getFilter();
        }
        if (!isRunning) {
            return "set error: don't running log collection !!!";
        }
        RDConfig.getInstance().setFilter(filter);
        stopRunningLogCmd();
        return "set log filter successful !!!";
    }

    private String getFilter() {
        String filter = RDConfig.getInstance().getFilter();
        if (StringUtils.isNullOrEmpty(filter)) {
            filter = " | grep -e \"VOS\" -e \"AndroidRuntime\" -e \"System.err\" -e \"WebSocketUtil\"";
        }
        return filter;
    }

    private void reStart(){
        if (isRunning) {
            oldFilter = null;
            writLogToFile(RDConfig.getInstance().getFilter());
        }
        return;
    }

    private boolean writLogToFile(String filter) {
        if (mRunCommand != null ) {
            if(filter.equals(oldFilter)){
                return false;
            }
            stopRunningLogCmd();
        }

        oldFilter = filter;
        //解决grep命令导致收集日志不及时问题
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
                if(line.contains(RunCommand.LOGCAT_END_INFO)) {
                    Logger.d(TAG,"old log collection end");
                    reStart();
                }
            }
        });
        runCommand.start();
        mRunCommand = runCommand;
        return true;
    }

    public void startLog() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        String filter = getFilter();
        WriteLogToFile.getInstance(mContext).openWriteLog();
        writLogToFile(filter);
    }

    public void pauseLog() {
        if (!isRunning) {
            return;
        }
        WriteLogToFile.getInstance(mContext).pauseWriteLog();
    }

    public void stopLog() {
        if (!isRunning) {
            return;
        }
        isRunning = false;
        pauseLog();
        stopRunningLogCmd();
    }

    /**
     * 上传日志
     */
    public boolean switchLogfile() {
        Logger.d(TAG, "switchLogfile ...");
        if (!isRunning) {
            return false;
        }
        WriteLogToFile.getInstance(mContext).switchLogfile();
        return true;
    }

}
