package com.android.diagnosislibrary.utils.base.usecase;

/**
 * Interface for schedulers, see {@link UseCaseThreadPoolUIScheduler}.
 */
public interface UseCaseScheduler {

    void execute(Runnable runnable);

    <V extends UseCase.ResponseValue> void notifyResponse(final V response,
                                                          final UseCase.UseCaseCallback<V> useCaseCallback);

    <V extends UseCase.ResponseValue> void onError(final Exception e,
            final UseCase.UseCaseCallback<V> useCaseCallback);

    <V extends UseCase.ResponseValue> void onBefore(
            final UseCase.UseCaseCallback<V> useCaseCallback);

    <V extends UseCase.ResponseValue> void onProgress(final float progress, final long total, final int id,
            final UseCase.UseCaseCallback<V> useCaseCallback);
}
