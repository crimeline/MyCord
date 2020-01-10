package com.android.diagnosislibrary.module.shellCmdManager;

import com.android.diagnosislibrary.utils.Logger.Logger;
import com.android.diagnosislibrary.utils.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class RunCommand extends Thread {
    private static final String TAG = "Command";
    private static final int RESULT_SIZE = 10 * 1024;//100KB
    private static final int DELAY_TIME = 50;
    private static final int MS = 1000;

    public static final String LOGCAT_END_INFO = "logcat cmd running end";
    public static final String LOGCAT_EMPTY_INFO = "running result: is empty";

    private String mCommand = null;
    private String mResult = null;
    private long mTimeOut = 30; //默认30秒超时
    private CommandCallBack mCallBack = null;
    private Process process;
    private boolean run = true;

    public RunCommand(String command, long timeout) {
        mCommand = command;
        if (timeout == 0) {
            mTimeOut = 12 * 30 * 24 * 60 * 60;//一年
        } else if (timeout > 0) {
            mTimeOut = timeout;
        } else {
            mTimeOut = 30;
        }

    }

    /**
     * 退出
     */
    public void terminal() {
        if (process != null) {
            run = false;
            process.destroy();
        }
    }

    /**
     * 启动
     */
    public void run() {
        if (mCommand != null) {
            mResult = StringexecShellStr(mCommand);
        }
    }

    public interface CommandCallBack {
        void sendResult(String line);
    }

    /**
     * 设置结果回调
     * @param callBack
     */
    public void setCallBack(CommandCallBack callBack) {
        mCallBack = callBack;
    }

    /**
     * 获取命令执行结果
     * @return
     */
    public String getResult() {
        return mResult;
    }

    private String StringexecShellStr(String cmd) {
        String[] cmdStrings = new String[]{"sh", "-c", cmd};
        String retString = "";
        BufferedReader stdout = null;
        BufferedReader stderr = null;

        try {
            Logger.i(TAG, "cmd= " + cmd);
            long startTime = System.currentTimeMillis();
//			process = Runtime.getRuntime().exec(cmdStrings);


            process = Runtime.getRuntime().exec("su");
            stdout = new BufferedReader(new InputStreamReader(
                    process.getInputStream()), RESULT_SIZE);
            stderr = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()), RESULT_SIZE);

            PrintWriter PrintWriter = new PrintWriter(process.getOutputStream());
            PrintWriter.println(cmd);
            PrintWriter.flush();
            PrintWriter.close();


            String line = null;

            Logger.i(TAG, "start read result ");
            while (run && (line = stdout.readLine()) != null) {
//				Thread.sleep(DELAY_TIME);
                if (!line.trim().isEmpty()) {
                    if (mCallBack != null) {
                        mCallBack.sendResult(line);
                    }
                    retString += line + "\n";
                }

                long endTime = System.currentTimeMillis();
                if ((endTime - startTime) > mTimeOut * MS) {
                    process.destroy();
                    break;
                }
            }

            if (StringUtils.isNullOrEmpty(retString)) {
                while (run && (line = stderr.readLine()) != null) {
//					Thread.sleep(DELAY_TIME);
                    if (false == line.trim().isEmpty()) {
                        if (mCallBack != null) {
                            mCallBack.sendResult(line);
                        }
                        retString += line + "\n";
                    }

                    long endTime = System.currentTimeMillis();
                    if ((endTime - startTime) > mTimeOut * MS) {
                        process.destroy();
                        break;
                    }
                }
            }
            Logger.i(TAG, "end read result ");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stdout != null) {
                    stdout.close();
                }
                if (stderr != null) {
                    stderr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(cmd.contains("logcat")){
                mCallBack.sendResult(LOGCAT_END_INFO);
            }
            if(StringUtils.isNullOrEmpty(retString)){
                mCallBack.sendResult(LOGCAT_EMPTY_INFO);
            }
        }

//        Logger.i(TAG, "retString=" + retString);
        return retString;
    }
}
