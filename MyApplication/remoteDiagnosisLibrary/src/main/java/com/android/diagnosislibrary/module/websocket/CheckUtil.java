package com.android.diagnosislibrary.module.websocket;

import android.content.Context;
import android.net.Uri;

import com.android.diagnosislibrary.utils.CommonUtil;
import com.android.diagnosislibrary.utils.Logger.Logger;


public class CheckUtil {
    private static final String TAG = "CheckUtil";

    public static final String ENDPOINT_10079 = "http://127.0.0.1:10079";

    public static final String URL_10090 = "http://127.0.0.1:10090/";
    public static final String URL_10079 = "http://127.0.0.1:10079/requestVersion.action";
    public final static String ACTION_PUSHUPDATEURL = "pushUpdateUrl.action";

    public static String buildUpgradeUrl(String param) {
        String url = Uri.parse(ENDPOINT_10079).buildUpon()
                .appendPath(ACTION_PUSHUPDATEURL)
                .build().toString()
                + "?url=" + param;
        Logger.d(TAG, "request url : " + url);

        return url;
    }

    public static void check(Context ctx, final String url) {
//        UtilCallback callback = new UtilCallback(){
//            @Override
//            public void onSuccess(String response) throws RemoteException {
//                Logger.d(TAG, url + " check onResponse : " + response);
//            }
//
//            @Override
//            public void onError(String error, String name) throws RemoteException {
//                Logger.e(TAG, url + " check onError : " + error);
//            }
//        };

        if (CommonUtil.isMainProcess(ctx)) {
            try {
//                TVLiveService.iUtilInterface.simpleGet(url, callback);
            } catch (Exception e) {
                Logger.e(TAG, e.getMessage());
            }
        } else {
//            PubPostManager.simpleGet(url, null, callback);
        }
    }
}
