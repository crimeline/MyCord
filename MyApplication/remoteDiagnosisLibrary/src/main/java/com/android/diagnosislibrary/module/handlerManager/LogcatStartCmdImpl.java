package com.android.diagnosislibrary.module.handlerManager;

import com.android.diagnosislibrary.module.shellCmdManager.ShellCmdManager;
import com.android.diagnosislibrary.module.websocket.WebMsgListener;
import com.android.diagnosislibrary.utils.Logger.Logger;

public class LogcatStartCmdImpl implements WebMsgListener.ICmdHandler {
    private static LogcatStartCmdImpl mLogcatStartCmdImpl = null;
    private static final String TAG = "LogcatStartCmdImpl";

    private LogcatStartCmdImpl() {
    }

    public void init() {
    }

    public static synchronized LogcatStartCmdImpl getInstance() {
        if (mLogcatStartCmdImpl == null) {
            mLogcatStartCmdImpl = new LogcatStartCmdImpl();
            WebMsgListener.getInstance().addCmd(mLogcatStartCmdImpl);
        }

        return mLogcatStartCmdImpl;
    }

    @Override
    public String getCmdName() {
        return "logcat_begin";
    }

    @Override
    public void cmdHandler(String id, String command) {
        Logger.d(TAG, "cmdHandler ....");
        command = "logcat  | grep -e \"VOS\" -e \"AndroidRuntime\" -e \"System.err\"";
        ShellCmdManager.getInstance().runShellCmd(id, command);
    }
}
