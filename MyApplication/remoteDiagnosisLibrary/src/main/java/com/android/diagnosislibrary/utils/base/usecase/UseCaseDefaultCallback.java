package com.android.diagnosislibrary.utils.base.usecase;

/**
 * Created by Administrator on 2017/6/12.
 */

public class UseCaseDefaultCallback<R> implements UseCase.UseCaseCallback<R> {
    @Override
    public void onSuccess(R response) {

    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onBefore() {

    }

    @Override
    public void onProgress(float progress, long total, int id) {

    }
}
