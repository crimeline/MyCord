
package com.android.diagnosislibrary.utils.base.usecase;


/**
 * Runs {@link UseCase}s using a {@link UseCaseScheduler}.
 */
public class UseCaseHandler {

    private static UseCaseHandler INSTANCE;

    private final UseCaseScheduler mUseCaseScheduler;

    public UseCaseHandler(UseCaseScheduler useCaseScheduler) {
        mUseCaseScheduler = useCaseScheduler;
    }

    public <T extends UseCase.RequestValues, R extends UseCase.ResponseValue> void execute(
            final UseCase<T, R> useCase, T values, UseCase.UseCaseCallback<R> callback) {
        useCase.setRequestValues(values);
        useCase.setUseCaseCallback(new CallbackWrapper(callback, this));

        mUseCaseScheduler.execute(new Runnable() {
            @Override
            public void run() {

                useCase.run();
            }
        });
    }

    public <V extends UseCase.ResponseValue> void notifyResponse(final V response,
            final UseCase.UseCaseCallback<V> useCaseCallback) {
        mUseCaseScheduler.notifyResponse(response, useCaseCallback);
    }

    private <V extends UseCase.ResponseValue> void notifyError(
            final Exception e,
            final UseCase.UseCaseCallback<V> useCaseCallback) {
        mUseCaseScheduler.onError(e, useCaseCallback);
    }

    public <V extends UseCase.ResponseValue> void notifyStart(
            final UseCase.UseCaseCallback<V> useCaseCallback) {
        mUseCaseScheduler.onBefore(useCaseCallback);
    }

    private <V extends UseCase.ResponseValue> void notifyProgress(
            final float progress, final long total, final int id,
            final UseCase.UseCaseCallback<V> useCaseCallback) {
        mUseCaseScheduler.onProgress(progress, total, id, useCaseCallback);
    }

    private static final class CallbackWrapper<V extends UseCase.ResponseValue> implements
            UseCase.UseCaseCallback<V> {
        private final UseCase.UseCaseCallback<V> mCallback;
        private final UseCaseHandler mUseCaseHandler;

        public CallbackWrapper(UseCase.UseCaseCallback<V> callback,
                               UseCaseHandler useCaseHandler) {
            mCallback = callback;
            mUseCaseHandler = useCaseHandler;
        }

        @Override
        public void onSuccess(V response) {
            mUseCaseHandler.notifyResponse(response, mCallback);
        }

        @Override
        public void onError(Exception e) {
            mUseCaseHandler.notifyError(e, mCallback);
        }

        @Override
        public void onBefore() {
            mUseCaseHandler.notifyStart(mCallback);
        }

        @Override
        public void onProgress(float progress, long total, int id) {
            mUseCaseHandler.notifyProgress(progress, total, id, mCallback);
        }
    }

    public static UseCaseHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UseCaseHandler(new UseCaseThreadPoolUIScheduler());
        }
        return INSTANCE;
    }
}
