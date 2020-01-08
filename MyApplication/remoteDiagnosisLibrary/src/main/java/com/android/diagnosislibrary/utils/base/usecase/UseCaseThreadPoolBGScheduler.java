package com.android.diagnosislibrary.utils.base.usecase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Executes asynchronous tasks using a {@link ThreadPoolExecutor}.
 * <p>
 * See also {@link Executors} for a list of factory methods to create common
 * {@link ExecutorService}s for different scenarios.
 */
public class UseCaseThreadPoolBGScheduler implements UseCaseScheduler {

    private static UseCaseThreadPoolBGScheduler sInstance;
    private ExecutorService mThreadPoolExecutor;

    private UseCaseThreadPoolBGScheduler() {
        mThreadPoolExecutor = Executors.newCachedThreadPool();
    }

    public static UseCaseThreadPoolBGScheduler getInstance() {
        if(sInstance == null) {
            synchronized (UseCaseThreadPoolBGScheduler.class) {
                if(sInstance == null) {
                    sInstance = new UseCaseThreadPoolBGScheduler();
                }
            }
        }
        return sInstance;
    }
    
    @Override
    public void execute(Runnable runnable) {
        mThreadPoolExecutor.execute(runnable);
    }

    @Override
    public <V extends UseCase.ResponseValue> void notifyResponse(final V response,
                                                                 final UseCase.UseCaseCallback<V> useCaseCallback) {
        useCaseCallback.onSuccess(response);
    }

    @Override
    public <V extends UseCase.ResponseValue> void onError(final Exception e,
            final UseCase.UseCaseCallback<V> useCaseCallback) {
        useCaseCallback.onError(e);
    }

    @Override
    public <V extends UseCase.ResponseValue> void onBefore(final UseCase.UseCaseCallback<V> useCaseCallback) {
        useCaseCallback.onBefore();
    }

    @Override
    public <V extends UseCase.ResponseValue> void onProgress(final float progress, final long total, final int id, final UseCase.UseCaseCallback<V> useCaseCallback) {
        useCaseCallback.onProgress(progress, total, id);
    }

}
