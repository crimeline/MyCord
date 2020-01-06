package com.android.diagnosislibrary;

import android.content.Context;

import com.android.diagnosislibrary.config.RDConfig;
import com.android.diagnosislibrary.module.websocket.WebSocketUtil;
import com.android.diagnosislibrary.utils.Logger.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class DiagnosisManagement {
    private static final String TAG = "DiagnosisManagement";
    private static DiagnosisManagement mInstance = null;
    private static Context mContext = null;

    private DiagnosisManagement(Context ctx) {
        mContext = ctx;
    }

    public static synchronized DiagnosisManagement getInstance(Context ctx) {
        if (ctx == null) {
            return null;
        }
        if (mInstance == null) {
            mInstance = new DiagnosisManagement(ctx);
        }

        return mInstance;
    }

    public String getTAG() {
        return TAG;
    }

    public void start() {
        Logger.setLogLevel(Logger.LOG_LEVEL_DEBUG);
        RDConfig.getInstance().init(null, "wss://iepg-sy.vosnewland.com/ums", 5000, 30, null);
        WebSocketUtil.getInstance(mContext).startReconnect();
    }

    public boolean cmdrunning() {
        boolean result = false;
        DataOutputStream dataOutputStream = null;
//        BufferedReader mReader = null;
        try {
            final Process process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());

            String cmd = "cd " + mContext.getFilesDir() + "\n";
            dataOutputStream.write(cmd.getBytes(Charset.forName("utf-8")));
            dataOutputStream.flush();

            cmd = "ls /cache " + "\n";
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();

            Logger.d(TAG, " read sleep: start");
            Thread.sleep(30 * 1000);
            Logger.d(TAG, " read sleep: end");

            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();

            //获取结果
            new Thread() {
                @Override
                public void run() {
                    BufferedReader mReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    String msg = "";
                    try {
                        Logger.d(TAG, " read readLine: start");
                        while ((line = mReader.readLine()) != null) {
                            msg += line;
                        }
                        Logger.d(TAG, " read msg: " + msg);
                    } catch (Exception e) {
                        Logger.e(TAG, "cmdrunning error : " + e.toString());
                    } finally {
                        try {
                            if (mReader != null) {
                                mReader.close();
                            }
                        } catch (IOException e) {
                            Logger.e(TAG, "runShellFile error :" + e.toString());
                        }
                    }
                }
            }.start();
            Logger.d(TAG, " read waitFor: start");
            process.waitFor();
            Logger.d(TAG, " read waitFor: end");
            result = true;
        } catch (Exception e) {
            Logger.e(TAG, "runShellFile error :" + e.toString());
            result = false;
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
            } catch (IOException e) {
                Logger.e(TAG, "runShellFile error :" + e.toString());
            }
        }

        return result;
    }

}
