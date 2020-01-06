package com.android.diagnosislibrary.config;

public class RDConfig {
    private static final String TAG = "RDConfig";
    private static RDConfig mRDConfig = null;
    private String filter;
    private String url;
    private int maxsize;
    private int timeout;
    private String devId;

    private RDConfig() {
    }

    public static synchronized RDConfig getInstance() {
        if (mRDConfig == null) {
            mRDConfig = new RDConfig();
        }
        return mRDConfig;
    }

    public void init(String filter, String url, int maxsize, int timeout, String devId) {
        setFilter(filter);
        setUrl(url);
        setMaxsize(maxsize);
        setTimeout(timeout);
        setDevId(devId);
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getFilter() {
        return this.filter;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
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
