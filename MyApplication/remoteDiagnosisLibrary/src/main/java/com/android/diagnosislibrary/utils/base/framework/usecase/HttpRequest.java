package com.android.diagnosislibrary.utils.base.framework.usecase;

import com.android.diagnosislibrary.utils.Logger.Logger;
import com.android.diagnosislibrary.utils.base.framework.enums.HttpMethod;
import com.android.diagnosislibrary.utils.base.usecase.UseCase;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.RequestCall;

import java.util.Map;

import okhttp3.Call;

/**
 * Created by Administrator on 2017/6/3.
 */

public class HttpRequest extends UseCase<HttpRequest.RequestValues, HttpRequest.ResponseValue> {
    private static final String TAG = "HttpRequest";
    public static final String PROCESS = " --KEYPATH-- "; // 程序核心路径

    @Override
    protected void executeUseCase(RequestValues requestValues) {
        HttpMethod method = requestValues.getMethod();
        String url = requestValues.getUrl();
        Map<String, String> params = requestValues.getParams();

        switch (method) {
            case GET:
                getRequest(url, params);
                break;
            case POST:
                postRequest(url, params);
                break;
            default:
                break;
        }

    }

    public static class RequestValues implements UseCase.RequestValues {
        private final HttpMethod method;
        private final String url;
        private final Map<String, String> params;

        public RequestValues(HttpMethod method, String url, Map<String, String> params) {
            this.method = method;
            this.url = url;
            this.params = params;
        }

        public HttpMethod getMethod() {
            return method;
        }

        public String getUrl() {
            return url;
        }

        public Map<String, String> getParams() {
            return params;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {
        private final String response;

        public ResponseValue(String response) {
            this.response = response;
        }

        @Override
        public String toString() {
            return response;
        }
    }

    private void getRequest(String url, Map<String, String> params){
        try {
            GetBuilder builder = OkHttpUtils.get()
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0")
                    .url(url);
            if(params != null && !params.isEmpty()) {
                builder.params(params);
            }
            final RequestCall build = builder.build();

            build.execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int i) {
                            Logger.d(TAG, PROCESS + build.getRequest().url() + " error ");
                            getUseCaseCallback().onError(e);
                        }

                        @Override
                        public void onResponse(String s, int i) {
                            Logger.d(TAG, PROCESS + build.getRequest().url() + " success ");
                            ResponseValue requestValues = new ResponseValue(s);
                            getUseCaseCallback().onSuccess(requestValues);
                        }
                    });
            Logger.d(TAG, PROCESS +  build.getRequest());
        } catch (Exception e) {
            getUseCaseCallback().onError(e);
        }
    }

    private void postRequest(String url, Map<String, String> params){
        try {
            PostFormBuilder builder = OkHttpUtils.post()
                    .url(url);
            if(params != null && !params.isEmpty()) {
                builder.params(params);
            }
            final RequestCall build = builder.build();

            build.execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int i) {
                            Logger.d(TAG, PROCESS + build.getRequest().url() + " error ");
                            getUseCaseCallback().onError(e);
                        }

                        @Override
                        public void onResponse(String s, int i) {
                            Logger.d(TAG, PROCESS + build.getRequest().url() + " success ");
                            ResponseValue requestValues = new ResponseValue(s);
                            getUseCaseCallback().onSuccess(requestValues);
                        }
                    });
            Logger.d(TAG, PROCESS + build.getRequest());
        } catch (Exception e) {
            getUseCaseCallback().onError(e);
        }
    }

}
