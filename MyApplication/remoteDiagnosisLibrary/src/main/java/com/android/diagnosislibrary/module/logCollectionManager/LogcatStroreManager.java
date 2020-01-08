package com.android.diagnosislibrary.module.logCollectionManager;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.android.diagnosislibrary.config.RDConfig;
import com.android.diagnosislibrary.module.websocket.LogInfo;
import com.android.diagnosislibrary.utils.DevUtils;
import com.android.diagnosislibrary.utils.Logger.Logger;

import java.io.File;
import java.io.PrintWriter;

/**
 * Created by Administrator on 2019/11/26.
 */

public class LogcatStroreManager {
    private static String TAG = "VOS_LogcatStroreManager";
    private static LogcatStroreManager instance;
    private static Context mContext = null;

    private static HandlerThread cmdThread = null;
    private static Handler cmdHandler = null;
    private static final int POST_LOG_ONECE = 1;
    private static final int POST_LOG_SECOND = 2;
    private static final String LOG_FILE_NAME_N = "RD_debug_%d.log";

    private LogcatStroreManager(Context ctx){
        this.mContext = ctx;
    }

    public static LogcatStroreManager getInstance(Context ctx) {
        if (instance == null) {
            if(ctx == null){
                return null;
            }
            instance = new LogcatStroreManager(ctx);
        }
        return instance;
    }

    public void init() {
        Log.d(TAG, " startLogcat ....");
        cmdThread = new HandlerThread("cmdThread");
        cmdThread.start();

        cmdHandler = new Handler(cmdThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String filename = String.format(LOG_FILE_NAME_N, logCount);
                String filePath = mContext.getFilesDir()+"/"+filename;
                Logger.d(TAG , "upload log filename :" + filePath);
                logCount ++;
                switch (msg.what) {
                    case POST_LOG_ONECE:
                        postlog(instance,filePath);
                        break;
                    case POST_LOG_SECOND:
                        postlog(null,filePath);
                        break;
                }
            }
        };
    }

    private int logCount = 0;
    public void postLogInfo() {
        Logger.d(TAG, "开始上传文件");
        logCount = 0;
        cmdHandler.sendEmptyMessageDelayed(POST_LOG_ONECE, 5 * 1000);
    }

    public void postlogfileAgain() {
        if(logCount >= RDConfig.mMaxCount+1){
            cmdHandler.sendEmptyMessageDelayed(POST_LOG_SECOND, 5 * 1000);
        }else{
            cmdHandler.sendEmptyMessageDelayed(POST_LOG_ONECE, 5 * 1000);
        }
    }

    private void postlog(LogcatStroreManager listener,String filePath) {
        chmodFile(filePath);
        File postfile = new File(filePath);
        Logger.d(TAG, "postlog 上传log文件开始 ==== "+ filePath +" : " + postfile.length());
        if (postfile != null && postfile.exists() && postfile.isFile() && postfile.length() > 0) {
            LogInfo info = new LogInfo();
            info.setSerialNumber(DevUtils.getSn(mContext));
            info.setAppkey(DevUtils.getAppkey(mContext));
            YNMPostManager.postLogInfo(info, postfile, listener);
        }else{
            if(listener != null){
                postlogfileAgain();
            }
        }
    }

    private void chmodFile(String filePath) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            String cmd = "chmod -R 777 " + filePath;
            Log.d(TAG, "  cmd: " + cmd);
            PrintWriter PrintWriter = new PrintWriter(process.getOutputStream());
            PrintWriter.println(cmd);
            PrintWriter.flush();
            PrintWriter.close();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
