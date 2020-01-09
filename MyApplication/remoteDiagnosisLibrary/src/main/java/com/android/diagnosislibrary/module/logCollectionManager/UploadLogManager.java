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
import com.android.diagnosislibrary.utils.StringUtils;

import java.io.File;
import java.io.PrintWriter;

/**
 * Created by Administrator on 2019/11/26.
 */

public class UploadLogManager {
    private static String TAG = "VOS_LogcatStroreManager";
    private static UploadLogManager instance;
    private static Context mContext = null;

    private static HandlerThread cmdThread = null;
    private static Handler cmdHandler = null;
    private static final int POST_LOG_ONECE = 1;
    private static final int POST_LOG_NEXT = 2;
    private static final int POST_LOG_END = 3;
    private static final int UPLOAD_LOG_DEFAULT = 1;

    private int logCount = 0;
    private String uploadTactics = null;

    private UploadLogManager(Context ctx) {
        this.mContext = ctx;
    }

    public static UploadLogManager getInstance(Context ctx) {
        if (instance == null) {
            if (ctx == null) {
                return null;
            }
            instance = new UploadLogManager(ctx);
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
                String filename = null;
                String filePath = null;
                switch (msg.what) {
                    case POST_LOG_ONECE:
                        //最新的日志
                        filename = String.format(RDConfig.LOG_FILE_NAME_N, RDConfig.mMaxCount + 1);
                        filePath = mContext.getFilesDir() + "/" + filename;
                        Logger.d(TAG, "upload log filename :" + filePath);
                        postlog(instance, filePath);
                        break;
                    case POST_LOG_NEXT:
                        filename = String.format(RDConfig.LOG_FILE_NAME_N, logCount);
                        filePath = mContext.getFilesDir() + "/" + filename;
                        Logger.d(TAG, "upload log filename :" + filePath);
                        postlog(instance, filePath);
                        break;
                    case POST_LOG_END:
                        filename = String.format(RDConfig.LOG_FILE_NAME_N, logCount);
                        filePath = mContext.getFilesDir() + "/" + filename;
                        Logger.d(TAG, "upload log filename :" + filePath);
                        postlog(null, filePath);
                        break;
                }
            }
        };
    }

    /**
     * 开始上传日志
     */
    public void postLogInfo() {
        Logger.d(TAG, "开始上传文件");
        logCount = 0;
        cmdHandler.sendEmptyMessageDelayed(POST_LOG_ONECE, 5 * 1000);
    }

    /**
     * 上传回调
     */
    public void postlogfileAgain() {
        if (getUploadTactics() == UPLOAD_LOG_DEFAULT) {
            getUploadFile();
            cmdHandler.sendEmptyMessageDelayed(POST_LOG_END, 5 * 1000);
        }
//        if(logCount >= RDConfig.mMaxCount){
//            logCount ++;
//            cmdHandler.sendEmptyMessageDelayed(POST_LOG_NEXT, 5 * 1000);
//        }else{
//            cmdHandler.sendEmptyMessageDelayed(POST_LOG_END, 5 * 1000);
//        }
    }

    /**
     * 根据传参设置上传文件策略
     *
     * @param para
     */
    private void setUploadTactics(String para) {
        uploadTactics = para;
    }

    private int getUploadTactics() {
        if (StringUtils.isNullOrEmpty(uploadTactics)) {
            return UPLOAD_LOG_DEFAULT;
        }
        int tactics = UPLOAD_LOG_DEFAULT;
        switch (uploadTactics) {
            case "tiem":
                break;
            default:
                break;
        }
        return tactics;
    }

    /**
     * 获取保存文件
     *
     * @return
     */
    private int getUploadFile() {
        File file = null;
        File uploadFile = null;
        try {
            for (int i = 0; i <= RDConfig.mMaxCount; i++) {
                String filename = String.format(RDConfig.LOG_FILE_NAME_N, i);
                file = new File(mContext.getFilesDir(), filename);
                long time = file.lastModified();
                if (time == 0) {
                    continue;
                }

                if (uploadFile == null) {
                    uploadFile = file;
                    logCount = i;
                    continue;
                }

                if (time > uploadFile.lastModified()) {
                    uploadFile = file;
                    logCount = i;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getSaveFileName: error " + e.toString());
            logCount = 0;
            return logCount;
        }

        return logCount;
    }

    private void postlog(UploadLogManager listener, String filePath) {
        chmodFile(filePath);
        File postfile = new File(filePath);
        Logger.d(TAG, "postlog 上传log文件开始 ==== " + filePath + " : " + postfile.length());
        if (postfile != null && postfile.exists() && postfile.isFile() && postfile.length() > 0) {
            LogInfo info = new LogInfo();
            info.setSerialNumber(DevUtils.getSn(mContext));
            info.setAppkey(DevUtils.getAppkey(mContext));
            YNMPostManager.postLogInfo(info, postfile, listener);
        } else {
            if (listener != null) {
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
