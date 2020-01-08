package com.android.diagnosislibrary.module.websocket;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Administrator on 2017/6/6.
 */

public class LogInfo {
    private String serialNumber = "";

    private String code = "";

    private String type = "";

    private String appkey = "";

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, String> getParamsMap() {
        Map<String, String> params = new TreeMap<>();
        params.put("serialNumber", serialNumber);
        params.put("code", code);
        params.put("type", type);
        params.put("appkey", appkey);

        return params;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }
}

