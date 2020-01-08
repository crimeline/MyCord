package com.android.diagnosislibrary.module.handlerManager;

import com.android.diagnosislibrary.module.shellCmdManager.ShellCmdManager;
import com.android.diagnosislibrary.module.websocket.WebMsgListener;
import com.android.diagnosislibrary.utils.Logger.Logger;

public class LogcatEndCmdImpl implements WebMsgListener.ICmdHandler {
    private static LogcatEndCmdImpl mLogcatEndCmdImpl = null;
    private static final String TAG = "LogcatEndCmdImpl";

    private LogcatEndCmdImpl() {
    }

    public void init() {
    }

    public static synchronized LogcatEndCmdImpl getInstance() {
        if (mLogcatEndCmdImpl == null) {
            mLogcatEndCmdImpl = new LogcatEndCmdImpl();
            WebMsgListener.getInstance().addCmd(mLogcatEndCmdImpl);
        }

        return mLogcatEndCmdImpl;
    }

    @Override
    public String getCmdName() {
        return "logcat_end";
    }

    @Override
    public void cmdHandler(String id, String command) {
        Logger.d(TAG, "cmdHandler ....");
        ShellCmdManager.getInstance().stopRunningShellCmd();
    }
}
