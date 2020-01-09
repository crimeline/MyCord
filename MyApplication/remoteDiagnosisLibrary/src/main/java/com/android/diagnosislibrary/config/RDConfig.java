package com.android.diagnosislibrary.config;

import com.android.diagnosislibrary.utils.StringUtils;

public class RDConfig {
    private static final String TAG = "RDConfig";
    private static RDConfig mRDConfig = null;
    private String filter;
    private String websocketUrl;
    private int maxsize;
    private int timeout;
    private String devId;
    private String uploadLogUrl;

    public static final String LOG_FILE_NAME_N = "RD_debug_%d.log";
    public static final String LOG_FILE_NAME = ".run.log";
    public static final int mMaxCount = 3;

    private RDConfig() {
    }

    public static synchronized RDConfig getInstance() {
        if (mRDConfig == null) {
            mRDConfig = new RDConfig();
        }
        return mRDConfig;
    }

    public void setConfig(String filter, String websocketUrl, String uploadLogUrl, int maxsize, int timeout, String devId) {
        setFilter(filter);
        setUrl(websocketUrl);
        setLogUploadUrl(uploadLogUrl);
        setMaxsize(maxsize);
        setTimeout(timeout);
        setDevId(devId);
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getFilter() {
        if(StringUtils.isNullOrEmpty(filter)){
            return " | grep -e \"VOS\" -e \"AndroidRuntime\" -e \"System.err\" -e \"WebSocketUtil\"";
        }
        return this.filter;
    }

    public void setUrl(String url) {
        this.websocketUrl = url;
    }

    public String getUrl() {
        return this.websocketUrl;
    }

    public String setLogUploadUrl(String url) {
        return this.uploadLogUrl = url;
    }

    public String getLogUploadUrl() {
        return this.uploadLogUrl;
    }

    public void setMaxsize(int maxsize) {
        this.maxsize = maxsize;
    }

    public int getMaxsize() {
        return this.maxsize;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public String getDevId() {
        return this.devId;
    }
}
