package com.mob.sms3.base;

import android.util.Log;

import rx.Observer;

public abstract class SimpleObserver<T> implements Observer<T> {
    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        Log.d("错粗了！！！", e.toString());
    }
//
//    @Override
//    public void onNext(T t) {
//        if (t == null) return;
//    }
}
