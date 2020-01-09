package com.android.diagnosislibrary.module.handlerManager;

import com.android.diagnosislibrary.module.shellCmdManager.ShellCmdManager;
import com.android.diagnosislibrary.DiagnosisManagement;
import com.android.diagnosislibrary.utils.Logger.Logger;

public class LogcatStartCmdImpl implements DiagnosisManagement.ICmdHandler {
    private static LogcatStartCmdImpl mLogcatStartCmdImpl = null;
    private static final String TAG = "LogcatStartCmdImpl";

    private LogcatStartCmdImpl() {
    }

    public void init() {
    }

    public static synchronized LogcatStartCmdImpl getInstance() {
        if (mLogcatStartCmdImpl == null) {
            mLogcatStartCmdImpl = new LogcatStartCmdImpl();
            DiagnosisManagement.getInstance().addCmd(mLogcatStartCmdImpl);
        }

        return mLogcatStartCmdImpl;
    }

    @Override
    public String getCmdName() {
        return CmdConstant.CMD_LOGCAT_BEGIN;
    }

    @Override
    public void cmdHandler(String id, String command) {
        Logger.d(TAG, "cmdHandler ....");
        command = "logcat  | grep -e \"VOS\" -e \"AndroidRuntime\" -e \"System.err\"";
        ShellCmdManager.getInstance().runShellCmd(id, command);
    }
}
