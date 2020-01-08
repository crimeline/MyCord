package com.android.diagnosislibrary.module.handlerManager;

import android.content.Context;

import com.android.diagnosislibrary.module.logCollectionManager.LogCollectionManager;
import com.android.diagnosislibrary.module.logCollectionManager.LogcatStroreManager;
import com.android.diagnosislibrary.module.websocket.WebMsgListener;
import com.android.diagnosislibrary.utils.Logger.Logger;

public class UploadLogCmdImpl implements WebMsgListener.ICmdHandler {
    private static final String TAG = "UploadLogCmdImpl";
    private static UploadLogCmdImpl mUploadLogCmdImpl = null;
    private static Context mContext = null;

    private UploadLogCmdImpl() {
    }

    public void init(Context ctx) {
        this.mContext = ctx;
    }

    public static synchronized UploadLogCmdImpl getInstance() {
        if (mUploadLogCmdImpl == null) {
            mUploadLogCmdImpl = new UploadLogCmdImpl();
            WebMsgListener.getInstance().addCmd(mUploadLogCmdImpl);
        }

        return mUploadLogCmdImpl;
    }

    @Override
    public String getCmdName() {
        return "custom_";
    }

    @Override
    public void cmdHandler(String id, String command) {
        Logger.d(TAG, "cmdHandler ....");
        if (mContext == null) {
            Logger.e(TAG, "cmdHandler error: don't init");
        }
        //没有跑日志收集模块不让上传日志
        if (LogCollectionManager.getInstance(mContext).switchLogfile()) {
            LogcatStroreManager.getInstance(mContext).postLogInfo();
        }
        return;
    }
}
