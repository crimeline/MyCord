package com.android.diagnosislibrary.module.handlerManager;

import android.support.annotation.NonNull;

import com.android.diagnosislibrary.config.RDConfig;
import com.android.diagnosislibrary.module.websocket.WebMsgListener;

public class ShellCmdManager {
    private static ShellCmdManager mShellCmdManager = null;
    private RunCommand mRunCommand = null;

    private ShellCmdManager (){
    }

    public static ShellCmdManager getInstance(){
        if(mShellCmdManager == null){
            mShellCmdManager = new ShellCmdManager();
        }
        return mShellCmdManager;
    }

    public void runShellCmd(String id, String command){
        mRunCommand = handleShellCmd(id,command);
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
     * linux 标准命令
     */
    private RunCommand handleShellCmd(@NonNull final String id, String command) {
        stopRunningShellCmd();
        RunCommand runCommand = new RunCommand(command, RDConfig.getInstance().getTimeout());
        runCommand.setCallBack(WebMsgListener.getInstance().getCommandCallBack(id));
        runCommand.start();
        return runCommand;
    }
}
