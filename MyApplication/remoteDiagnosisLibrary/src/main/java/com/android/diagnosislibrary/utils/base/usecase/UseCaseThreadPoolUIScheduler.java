package com.android.diagnosislibrary.utils.base.usecase;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Executes asynchronous tasks using a {@link ThreadPoolExecutor}.
 * <p>
 * See also {@link Executors} for a list of factory methods to create common
 * {@link java.util.concurrent.ExecutorService}s for different scenarios.
 */
public class UseCaseThreadPoolUIScheduler implements UseCaseScheduler {

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    ExecutorService mThreadPoolExecutor;

    public UseCaseThreadPoolUIScheduler() {
        mThreadPoolExecutor = Executors.newCachedThreadPool();
    }

    @Override
    public void execute(Runnable runnable) {
        mThreadPoolExecutor.execute(runnable);
    }

    @Override
    public <V extends UseCase.ResponseValue> void notifyResponse(final V response,
            final UseCase.UseCaseCallback<V> useCaseCallback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                useCaseCallback.onSuccess(response);
            }
        });
    }

    @Override
    public <V extends UseCase.ResponseValue> void onError(final Exception e,
            final UseCase.UseCaseCallback<V> useCaseCallback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                useCaseCallback.onError(e);
            }
        });
    }

    @Override
    public <V extends UseCase.ResponseValue> void onBefore(final UseCase.UseCaseCallback<V> useCaseCallback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                useCaseCallback.onBefore();
            }
        });
    }

    @Override
    public <V extends UseCase.ResponseValue> void onProgress(final float progress, final long total, final int id, final UseCase.UseCaseCallback<V> useCaseCallback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                useCaseCallback.onProgress(progress, total, id);
            }
        });
    }

}
