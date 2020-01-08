package com.android.diagnosislibrary.utils.base.framework;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/6/5.
 */

public class BaseResponse implements Serializable{
    protected String ret;
    protected String retInfo;

    public String getRet() {
        return ret;
    }

    public void setRet(String ret) {
        this.ret = ret;
    }

    public String getRetInfo() {
        return retInfo;
    }

    public void setRetInfo(String retInfo) {
        this.retInfo = retInfo;
    }
}
