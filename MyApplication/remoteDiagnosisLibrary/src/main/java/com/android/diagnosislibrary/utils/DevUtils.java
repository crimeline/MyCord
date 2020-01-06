package com.android.diagnosislibrary.utils;

import android.content.Context;
import android.provider.Settings;

import com.android.diagnosislibrary.config.RDConfig;

public class DevUtils {
    public static String getSn(Context ctx) {
        String sn = RDConfig.getInstance().getDevId();
        if(!StringUtils.isNullOrEmpty(sn)){
            return sn;
        }
        sn = android.os.Build.SERIAL;
        if (!StringUtils.isNullOrEmpty(sn)) {
            return sn;
        }
        if (ctx == null) {
            return null;
        }
        return getSmartCardId(ctx);
    }

    public static String getSmartCardId(Context ctx) {
        String androidId = "666";
        try {
            androidId = android.provider.Settings.Secure.getString(ctx.getContentResolver(), Settings.System.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return androidId;
    }

    public static String getWebSocketUserID(Context ctx) {
        return getSn(ctx) + "@" + getAppkey(ctx);
    }

    interface PACKAGE {
        String SKTV = "com.sktv.tvliveremote";
        String SEECLOUD = "com.seecloud.tvliveremote";
    }

    interface APPKEY {
        String COMM_SEECLOUD = "seecoolControlTV";
        String SKTV_SEECLOUD = "com.sktv.tvliveremote";
    }

    public static String getAppkey(Context ctx) {
        if (ctx == null) {
            return APPKEY.COMM_SEECLOUD;
        }
        String appkey = "";
        if (PackageUtils.getName(ctx).equals(PACKAGE.SKTV)) {
            appkey = APPKEY.SKTV_SEECLOUD;
        } else {
            appkey = APPKEY.COMM_SEECLOUD;
        }

        return appkey;
    }
}
