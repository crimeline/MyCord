package com.android.diagnosislibrary.utils.base.framework.usecase;

import com.android.diagnosislibrary.utils.Logger.Logger;
import com.android.diagnosislibrary.utils.base.framework.enums.HttpMethod;
import com.android.diagnosislibrary.utils.base.usecase.UseCase;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.RequestCall;

import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by Administrator on 2017/6/3.
 */

public class HttpStringRequest extends UseCase<HttpStringRequest.RequestValues, HttpStringRequest.ResponseValue> {
    private static final String TAG = "HttpStringRequest";
    public static final String PROCESS = " --KEYPATH-- "; // 程序核心路径

    @Override
    protected void executeUseCase(RequestValues requestValues) {
        HttpMethod method = requestValues.getMethod();
        String url = requestValues.getUrl();
        String content = requestValues.getContent();

        switch (method) {
            case POST:
                postRequest(url, content);
                break;
            default:
                break;
        }

    }

    public static class RequestValues implements UseCase.RequestValues {
        private final HttpMethod method;
        private final String url;
        private final String content;

        public RequestValues(HttpMethod method, String url, String content) {
            this.method = method;
            this.url = url;
            this.content = content;
        }

        public HttpMethod getMethod() {
            return method;
        }

        public String getUrl() {
            return url;
        }

        public String getContent() {
            return content;
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

    private void postRequest(String url, String content){
        try {
            final RequestCall build = OkHttpUtils.postString()
                    .url(url)
                    .content(content)
                    .mediaType(MediaType.parse("application/json; charset=utf-8"))
                    .build();
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

}
