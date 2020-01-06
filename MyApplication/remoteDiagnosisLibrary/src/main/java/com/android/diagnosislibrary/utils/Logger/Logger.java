package com.android.diagnosislibrary.utils.Logger;

import android.util.Log;

public class Logger {
    public static final int LOG_LEVEL_DEBUG = 0;
    public static final int LOG_LEVAL_INFO = 1;
    public static final int LOG_LEVEL_WARN = 2;
    public static final int LOG_LEVEL_ERROR = 3;
    public static final int LOG_LEVEL_NONE = 4;

    private static int logLevel = LOG_LEVEL_ERROR;

    public synchronized static void setLogLevel(int level) {
        logLevel = level;
    }

    public static void d(String tag, String msg) {
        if (logLevel > LOG_LEVEL_DEBUG)
            return;
        Log.d(tag, msg);
    }

    public static void i(String tag, String msg) {
        if (logLevel > LOG_LEVAL_INFO)
            return;
        Log.i(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (logLevel > LOG_LEVEL_WARN)
            return;
        Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (logLevel > LOG_LEVEL_ERROR)
            return;
        Log.e(tag, msg);
    }
}
