package com.mob.sms2.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mob.sms2.pns.BaiduPnsServiceImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * 绑定隐私号工具类
 */
public class BindXUtils {
    private static ProgressDialog progressDialog;
    private final static int SHOW = 1;
    private final static int HIDE = 2;
    private static Handler mainHandler = new android.os.Handler(Looper.getMainLooper());

    public static void bindX(Activity activity, String callerPhone, String dialNumber, BindCallBack bindCallBack) {
        if (activity != null) {
            WeakReference<Activity> ref = new WeakReference<>(activity);
            showProgress("绑定隐私号码...", ref);
            new Thread() {
                @Override
                public void run() {
                    BaiduPnsServiceImpl impl = new BaiduPnsServiceImpl();
                    String s = impl.bindingAxb(callerPhone, dialNumber);
                    Log.d("绑定隐私号结果", s);
                    hideProgress();
                    //{"code":"0","msg":"成功","data":{"bindId":"2411790078574043902","telX":"18468575717"}}
                    //{"code":"10003","msg":"号码已有相关绑定关系","data":null}
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        JSONObject data = jsonObject.optJSONObject("data");
                        if (data != null) {
                            String telX = data.optString("telX");
                            String bindId = data.optString("bindId");
                            SPUtils.put(Constants.BINDX_BIND_ID, bindId);
                            if (bindCallBack != null) {
                                if (!TextUtils.isEmpty(telX)) {
                                    bindCallBack.bindSuccess(telX);
                                } else {
                                    bindCallBack.bindFailed(jsonObject.optString("msg"));
                                }
                            }
                        } else {
                            String code = jsonObject.optString("code");
                            if ("10003".equals(code)) {
                                // 已有绑定关系，需手动解绑
                                String unbinding = impl.unbinding(SPUtils.getString(Constants.BINDX_BIND_ID, ""));
                                if (unbinding == null) {
                                    if (bindCallBack != null) {
                                        bindCallBack.bindFailed("服务繁忙，请稍后再试");
                                    }
                                } else {
                                    // 重新绑定
                                    JSONObject unbindJson = new JSONObject(unbinding);
                                    String unbindCode = unbindJson.optString("code");
                                    if (TextUtils.equals(unbindCode, "0")) {
                                        bindX(activity, callerPhone, dialNumber, bindCallBack);
                                    } else {
                                        if (bindCallBack != null) {
                                            bindCallBack.bindFailed(unbindJson.optString("msg"));
                                        }
                                    }
                                }
                            } else {
                                if (bindCallBack != null) {
                                    bindCallBack.bindFailed(jsonObject.optString("msg"));
                                }
                            }
                        }
                    } catch (JSONException e) {
                        if (bindCallBack != null) {
                            bindCallBack.bindFailed(e.getMessage());
                        }
                    }
                }
            }.start();
        }
    }

    private static void showProgress(String msg, WeakReference<Activity> ref) {
        try {
            if (progressDialog == null && ref != null && ref.get() != null) {
                progressDialog = new ProgressDialog(ref.get());
            }
            if (progressDialog != null && !progressDialog.isShowing()) {
                progressDialog.setTitle(msg);
                if (ref.get().isDestroyed() || ref.get().isFinishing()) {
                    return;
                }
                progressDialog.show();
            }
        } catch (Exception e) {

        }
    }

    private static void hideProgress() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                } catch (Exception e) {

                }
            }
        });
    }

    public interface BindCallBack {
        void bindSuccess(String telX);

        void bindFailed(String msg);
    }
}

