package com.android.diagnosislibrary.module.handlerManager;

import android.content.Context;

import com.android.diagnosislibrary.DiagnosisManagement;
import com.android.diagnosislibrary.module.logCollectionManager.LogCollectionManager;
import com.android.diagnosislibrary.utils.Logger.Logger;
import com.android.diagnosislibrary.utils.StringUtils;

public class SetLogFilterCmdImpl implements DiagnosisManagement.ICmdHandler {
    private static SetLogFilterCmdImpl mSetLogFilterCmdImpl = null;
    private static final String TAG = "SetLogFilterCmdImpl";
    private static Context mContext = null;

    private SetLogFilterCmdImpl() {
    }

    public void init(Context ctx) {
        this.mContext = ctx;
    }

    public static synchronized SetLogFilterCmdImpl getInstance() {
        if (mSetLogFilterCmdImpl == null) {
            mSetLogFilterCmdImpl = new SetLogFilterCmdImpl();
            DiagnosisManagement.getInstance().addCmd(mSetLogFilterCmdImpl);
        }

        return mSetLogFilterCmdImpl;
    }

    @Override
    public String getCmdName() {
        return "setLogFilter";
    }

    @Override
    public void cmdHandler(String id, String command) {
        if (mContext == null) {
            return;
        }
        String filter = null;
        if (command.contains("|grep") || command.contains("--pid") || command.contains("-s")) {
            filter = command.replace(getCmdName(), "");
            if (StringUtils.isNullOrEmpty(filter)) {
                DiagnosisManagement.getInstance().sendDiagnoseResponse("set error: parameter error!!!", id);
                return;
            }
        } else {
            DiagnosisManagement.getInstance().sendDiagnoseResponse("set error: parameter error!!!", id);
            return;
        }
        Logger.d(TAG, "filter: " + filter);
        String result = LogCollectionManager.getInstance(mContext).setLogFilter(filter);
        DiagnosisManagement.getInstance().sendDiagnoseResponse(result, id);
    }
}
