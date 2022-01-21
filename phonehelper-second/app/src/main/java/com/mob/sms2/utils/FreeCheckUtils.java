package com.mob.sms2.utils;

import android.app.Activity;
import android.text.TextUtils;

import com.mob.sms2.application.MyApplication;
import com.mob.sms2.bean.ChannelChargeBean;
import com.mob.sms2.bean.CloudPermissionBean;
import com.mob.sms2.config.GlobalConfig;
import com.mob.sms2.network.RetrofitHelper;
import com.mob.sms2.network.bean.BaseResponse;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class FreeCheckUtils {
    private static long lastCheckTime = 0;
    private static boolean isFree = false;

    public static boolean isSecretCall() {
        String sim = (String) SPUtils.get(SPConstant.SP_SIM_CARD_TYPE, "");
        return TextUtils.equals(sim, Constants.SIM_TYPE_SECRET);
    }

    public static void check(Activity activity, boolean isSecretCall, OnCheckCallback callback) {
        if (GlobalConfig.isVip) {
            callback.onResult(true);
        } else {
            long duration = System.currentTimeMillis() - lastCheckTime;
            long seconds = duration / 1000;
            lastCheckTime = System.currentTimeMillis();
            // 小于1分钟 且 是vip不用重复校验
            if (seconds < 60 && isFree && callback != null) {
                callback.onResult(true);
            } else {
                checkPermission(activity, isSecretCall, callback);
            }
        }
//        long duration = System.currentTimeMillis() - lastCheckTime;
//        long seconds = duration / 1000;
//        lastCheckTime = System.currentTimeMillis();
//        // 小于1分钟 且 是vip不用重复校验
//        if (seconds < 60 && isFree && callback != null) {
//            callback.onResult(true);
//        } else {
//            checkPermission(activity, isSecretCall, callback);
//        }
    }

    private static void checkPermission(Activity activity, boolean isSecretCall, OnCheckCallback callback) {
        // 先判断渠道
        RetrofitHelper.getApi().getMarketCharge(MyApplication.Channel)
                .subscribe(new Action1<BaseResponse<ChannelChargeBean>>() {
                    @Override
                    public void call(BaseResponse<ChannelChargeBean> response) {
                        if (response != null && response.data != null) {
                            switch (response.data.status) {
                                case "0":
                                    if (callback != null) {
                                        callback.onResult(true);
                                    }
                                    break;
                                default:
                                    checkUserVip(activity, isSecretCall, callback);
                                    break;
                            }
                        }

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        checkUserVip(activity, isSecretCall, callback);
                    }
                });
    }

    private static void checkUserVip(Activity activity, boolean isSecretCall, OnCheckCallback callback) {
        RetrofitHelper.getApi().cloudDial()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CloudPermissionBean>() {
                    @Override
                    public void call(CloudPermissionBean permissionBean) {
                        if (permissionBean != null && "200".equals(permissionBean.code)) {
                            // 有权限
                            isFree = true;
                            if (callback != null) {
                                callback.onResult(true);
                            }
                        } else {
                            isFree = false;
                            if (isSecretCall && GlobalConfig.isVip) {
                                ToastUtil.showLong(permissionBean.msg);
                            } else {
                                if (callback != null) {
                                    callback.onResult(false);
                                }
                            }
                        }
                    }
                });
    }

    public interface OnCheckCallback {
        void onResult(boolean free);
    }
}
