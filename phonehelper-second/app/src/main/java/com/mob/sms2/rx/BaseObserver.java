package com.mob.sms2.rx;

import com.mob.sms2.network.bean.BaseResponse;

import rx.Observer;

public abstract class BaseObserver<T> implements Observer<BaseResponse<T>> {
    @Override
    public void onCompleted() {

    }

    @Override
    public void onNext(BaseResponse<T> response) {
        if (response != null) {
            if (200 == response.code) {
                onSuccess(response.data);
            } else {
                onFailed(new MobError(response.msg, response.code, response.msg));
            }
        } else {
            onFailed(new MobError("response null", 500, "response null"));
        }
    }

    @Override
    public void onError(Throwable e) {
        onFailed(new MobError(e.getMessage(), 501, e.getMessage()));
    }

    protected abstract void onSuccess(T data);

    protected abstract void onFailed(MobError error);
}
