package com.android.diagnosislibrary.module.shellCmdManager;

import android.support.annotation.NonNull;

import com.android.diagnosislibrary.DiagnosisManagement;
import com.android.diagnosislibrary.config.RDConfig;

public class ShellCmdManager {
    private static final String TAG = "ShellCmdManager";
    private static ShellCmdManager mShellCmdManager = null;
    private RunCommand mRunCommand = null;

    private ShellCmdManager() {
    }

    public static ShellCmdManager getInstance() {
        if (mShellCmdManager == null) {
            mShellCmdManager = new ShellCmdManager();
        }
        return mShellCmdManager;
    }

    /**
     * 执行shell命令
     * @param id
     * @param command
     */
    public void runShellCmd(String id, String command) {
        mRunCommand = handleShellCmd(id, command);
    }

    /**
     * 停止执行shell命令
     */
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
     * 发送回调
     * @param id
     * @return
     */
    private RunCommand.CommandCallBack getCommandCallBack(@NonNull final String id) {
        return new RunCommand.CommandCallBack() {
            @Override
            public void sendResult(String line) {
                DiagnosisManagement.getInstance().sendDiagnoseResponse(line, id);
            }
        };
    }

    /**
     * linux 标准命令
     */
    private RunCommand handleShellCmd(@NonNull final String id, String command) {
        stopRunningShellCmd();
        RunCommand runCommand = new RunCommand(command, RDConfig.getInstance().getTimeout());
        runCommand.setCallBack(getCommandCallBack(id));
        runCommand.start();
        return runCommand;
    }
}
