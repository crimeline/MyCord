package com.android.diagnosislibrary.utils.base.net;

import java.util.Map;

/**
 * Created by Administrator on 2017/6/5.
 */

public abstract class BaseRequest{
    protected String terminalType = "1"; // 1 TV
    public abstract Map<String, String> getParamsMap();
}
