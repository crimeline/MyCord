package com.android.diagnosislibrary.module.logCollectionManager;

import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import com.android.diagnosislibrary.config.RDConfig;
import com.android.diagnosislibrary.module.websocket.LogInfo;
import com.android.diagnosislibrary.utils.Logger.Logger;
import com.android.diagnosislibrary.utils.UtilCallback;
import com.android.diagnosislibrary.utils.base.framework.model.FileInput;
import com.android.diagnosislibrary.utils.base.framework.usecase.HttpFileRequest;
import com.android.diagnosislibrary.utils.base.usecase.UseCaseDefaultCallback;
import com.android.diagnosislibrary.utils.base.usecase.UseCaseHandler;

import java.io.File;
import java.util.Map;

import static com.android.diagnosislibrary.utils.base.framework.enums.HttpMethod.POST;

/**
 * Created by Administrator on 2017/3/31.
 */

public class YNMPostManager {
    private static final String TAG = "VOS_YNMPostManager";

    private static final String METHOD_POST_BOXINFO = "boxInfo"; //设备信息上报
    private static final String METHOD_POST_PERIODIC = "periodic"; //终端心跳上报
    private static final String METHOD_POST_REPORTLAGINFO = "reportLagInfo"; //日志上报
    private static final String METHOD_POST_REPORTSERVICEALARM = "reportServiceAlarm"; //告警上报
    private static final String METHOD_POST_REPORTLOG = "reportLog"; //日志上报

    private static final String PARAM_LOG = "log";

    private static String buildUrl(String method) {
        String url = Uri.parse(RDConfig.getInstance().getNMUrl()).buildUpon()
                .appendPath(method)
                .appendQueryParameter("time", Long.toString(System.currentTimeMillis()))
                .build().toString();
        Logger.d(TAG, "request url : " + url);
        return url;
    }

    private static String buildBoxInfoUrl() {
        return buildUrl(METHOD_POST_BOXINFO);
    }

    private static String buildPeriodicUrl() {
        return buildUrl(METHOD_POST_PERIODIC);
    }

    private static String buildReportlaginfoUrl() {
        return buildUrl(METHOD_POST_REPORTLAGINFO);
    }

    private static String buildDeviceLogUrl() {
        return buildUrl(METHOD_POST_REPORTLOG);
    }

    private static String buildReportservicealarmUrl() {
        return buildUrl(METHOD_POST_REPORTSERVICEALARM);
    }

    private static void simpleUpload(String url, Map<String, String> params, String key, File file, final UtilCallback callback) {
        UseCaseHandler.getInstance().execute(
                new HttpFileRequest(),
                new HttpFileRequest.RequestValues(POST, url, params, new FileInput(key, file)),
                new UseCaseDefaultCallback<HttpFileRequest.ResponseValue>() {
                    @Override
                    public void onSuccess(HttpFileRequest.ResponseValue response) {
                        try {
                            callback.onSuccess(response.getResponse());
                        } catch (Exception e) {
                            Logger.e(TAG, e.getMessage());
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        try {
                            callback.onError(e.getMessage(), e.getClass().getSimpleName());
                        } catch (Exception ex) {
                            Logger.d(TAG, ex.getMessage());
                        }
                    }
                }
        );

    }

    /**
     * 日志上报
     */
    public static void postLogInfo(@NonNull LogInfo info, File log, final LogcatStroreManager listener) {
        simpleUpload(buildDeviceLogUrl(), info.getParamsMap(), PARAM_LOG, log, new UtilCallback() {
            @Override
            public void onSuccess(String response) throws RemoteException {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.postlogfileAgain();
                        }
                    }
                }).start();

                Logger.d(TAG, "postLogInfo onResponse : " + response);
            }

            @Override
            public void onError(String error, String name) throws RemoteException {
                Logger.e(TAG, "postLogInfo onError : " + error);
            }
        });
    }

    /**
     * 日志上报
     */
    public static void postLogInfo(@NonNull LogInfo info, File log) {
        postLogInfo(info, log, null);
    }

}

