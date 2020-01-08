package com.android.diagnosislibrary.utils.base.net.usecase;

import com.android.diagnosislibrary.utils.Logger.Logger;
import com.android.diagnosislibrary.utils.base.net.enums.HttpMethod;
import com.android.diagnosislibrary.utils.base.net.model.FileInput;
import com.android.diagnosislibrary.utils.base.usecase.UseCase;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.RequestCall;

import java.io.File;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by Administrator on 2017/6/5.
 */

public class HttpFileRequest extends UseCase<HttpFileRequest.RequestValues, HttpFileRequest.ResponseValue> {
    private static final String TAG = "HttpFileRequest";
    public static final String PROCESS = " --KEYPATH-- "; // 程序核心路径

    @Override
    protected void executeUseCase(RequestValues requestValues) {
        HttpMethod method = requestValues.getMethod();
        String url = requestValues.getUrl();
        Map<String, String> params = requestValues.getParams();
        FileInput fileInput = requestValues.getFileInput();

        switch (method) {
            case GET:
                getRequest(url, params, fileInput);
                break;
            case POST:
                postRequest(url, params, fileInput);
                break;
            default:
                break;
        }

    }

    public static class RequestValues implements UseCase.RequestValues {
        private final HttpMethod method;
        private final String url;
        private final Map<String, String> params;
        private final FileInput fileInput;

        public RequestValues(HttpMethod method, String url, Map<String, String> params, FileInput fileInput) {
            this.method = method;
            this.url = url;
            this.params = params;
            this.fileInput = fileInput;
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

        public FileInput getFileInput() {
            return fileInput;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {
        private final String response;
        private final File file;

        public ResponseValue(String response, File file) {
            this.response = response;
            this.file = file;
        }

        public String getResponse() {
            return response;
        }

        public File getFile() {
            return file;
        }
    }

    private void getRequest(String url, Map<String, String> params, FileInput fileInput) {
        try {
            GetBuilder builder = OkHttpUtils
                    .get()
                    .url(url);
            if (params != null && !params.isEmpty()) {
                builder.params(params);
            }
            final RequestCall build = builder.build();

            build.execute(new FileCallBack(fileInput.filedir, fileInput.filename) {
                @Override
                public void onBefore(Request request, int id) {
                    getUseCaseCallback().onBefore();
                }

                @Override
                public void inProgress(float progress, long total, int id) {
                    getUseCaseCallback().onProgress(progress, total, id);
                }

                @Override
                public void onError(Call call, Exception e, int i) {
                    Logger.d(TAG, PROCESS + build.getRequest().url() + " error ");
                    getUseCaseCallback().onError(e);
                }

                @Override
                public void onResponse(File file, int i) {
                    Logger.d(TAG, PROCESS + build.getRequest().url() + " success ");
                    ResponseValue responseValue = new ResponseValue(null, file);
                    getUseCaseCallback().onSuccess(responseValue);
                }
            });
            Logger.d(TAG, PROCESS + build.getRequest());
        } catch (Exception e) {
            getUseCaseCallback().onError(e);
        }
    }

    private void postRequest(String url, Map<String, String> params, FileInput fileInput) {
        try {
            PostFormBuilder builder = OkHttpUtils.post()
                    .addFile(fileInput.key, fileInput.file.getName(), fileInput.file)
                    .url(url);
            if (params != null && !params.isEmpty()) {
                builder.params(params);
            }
            final RequestCall build = builder.build();

            build.execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int i) {
                    Logger.d(TAG, PROCESS + " error ");
                    getUseCaseCallback().onError(e);
                }

                @Override
                public void onResponse(String s, int i) {
                    Logger.d(TAG, PROCESS + " success ");
                    ResponseValue responseValue = new ResponseValue(s, null);
                    getUseCaseCallback().onSuccess(responseValue);
                }
            });
            Logger.d(TAG, PROCESS + build.getRequest());
        } catch (Exception e) {
            getUseCaseCallback().onError(e);
        }
    }
}
