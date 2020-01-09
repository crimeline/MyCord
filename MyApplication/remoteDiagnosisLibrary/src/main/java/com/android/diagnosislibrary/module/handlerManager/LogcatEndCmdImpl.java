package com.android.diagnosislibrary.module.handlerManager;

import com.android.diagnosislibrary.module.shellCmdManager.ShellCmdManager;
import com.android.diagnosislibrary.DiagnosisManagement;
import com.android.diagnosislibrary.utils.Logger.Logger;

public class LogcatEndCmdImpl implements DiagnosisManagement.ICmdHandler {
    private static LogcatEndCmdImpl mLogcatEndCmdImpl = null;
    private static final String TAG = "LogcatEndCmdImpl";

    private LogcatEndCmdImpl() {
    }

    public void init() {
    }

    public static synchronized LogcatEndCmdImpl getInstance() {
        if (mLogcatEndCmdImpl == null) {
            mLogcatEndCmdImpl = new LogcatEndCmdImpl();
            DiagnosisManagement.getInstance().addCmd(mLogcatEndCmdImpl);
        }

        return mLogcatEndCmdImpl;
    }

    @Override
    public String getCmdName() {
        return CmdConstant.CMD_LOGCAT_END;
    }

    @Override
    public void cmdHandler(String id, String command) {
        Logger.d(TAG, "cmdHandler ....");
        ShellCmdManager.getInstance().stopRunningShellCmd();
    }
}
