package com.mob.sms2.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.mob.sms2.R;
import com.mob.sms2.adapter.SplashBannerAdapter;
import com.mob.sms2.application.MyApplication;
import com.mob.sms2.base.SimpleObserver;
import com.mob.sms2.bean.BannerBean;
import com.mob.sms2.dialog.UserAgreementDialog;
import com.mob.sms2.network.RetrofitHelper;
import com.mob.sms2.network.bean.BaseResponse;
import com.mob.sms2.sdk.SDKManager;
import com.mob.sms2.utils.SPConstant;
import com.mob.sms2.utils.SPUtils;
import com.mob.sms2.utils.ToastUtil;
import com.youth.banner.Banner;
import com.youth.banner.indicator.CircleIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends Activity {
    @BindView(R.id.banner)
    Banner mBanner;
    @BindView(R.id.skip)
    ImageView mSkip;
    @BindView(R.id.welcome_rl)
    RelativeLayout mWelcomeRl;
    @BindView(R.id.splash_image)
    ImageView launchImage;
    @BindView(R.id.logo_text)
    View logoText;

    private final int MSG_SKIP = 0;
    private SDKManager sdkManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        initView();
        initGlobalData();
        sdkManager = new SDKManager();
        sdkManager.addObserver(MyApplication.mApplication);
    }

    private void showPolicyDialog() {
        UserAgreementDialog userAgreementDialog = new UserAgreementDialog(this);
        userAgreementDialog.show();
        userAgreementDialog.setOnClickListener(new UserAgreementDialog.OnClickListener() {
            @Override
            public void agree() {
                userAgreementDialog.dismiss();
                sdkManager.init();
                SPUtils.put(SPConstant.SP_USER_PERMISSION_OK, true);
                mHandler.sendEmptyMessageDelayed(MSG_SKIP, 500);
            }

            @Override
            public void refuse() {
                userAgreementDialog.dismiss();
                finish();
                System.exit(0);
            }
        });
    }

    private void initGlobalData() {
        // 批量拨打，随机间隔
        String interval = SPUtils.getString(SPConstant.SP_CALL_JGSZ, "");
        if (TextUtils.isEmpty(interval)) {
            // 默认随机30
            SPUtils.put(SPConstant.SP_CALL_JGSZ, "15-30");
        }

    }

    private void initView() {
        // 隐私协议是否允许
        if (!SPUtils.getBoolean(SPConstant.SP_USER_PERMISSION_OK, false)) {
            mSkip.setOnClickListener(view -> {
                SPUtils.put(SPConstant.SP_SPLASH_WELCOME, true);
                mHandler.sendEmptyMessageDelayed(MSG_SKIP, 500);
            });
            logoText.setVisibility(View.VISIBLE);
            showPolicyDialog();
            return;
        }else {
            logoText.setVisibility(View.GONE);
        }
        if (SPUtils.getBoolean(SPConstant.SP_SPLASH_WELCOME, false)) {
            RetrofitHelper.getApi().getImage(1)
                    .subscribe(new SimpleObserver<BaseResponse<List<BannerBean>>>() {
                        @Override
                        public void onNext(BaseResponse<List<BannerBean>> listBaseResponse) {
                            if (listBaseResponse != null && listBaseResponse.data != null && !listBaseResponse.data.isEmpty()) {
                                BannerBean imageBean = listBaseResponse.data.get(0);
                                Glide.with(SplashActivity.this)
                                        .load(imageBean.img)
                                        .into(launchImage);
                            }
                        }
                    });
            mWelcomeRl.setVisibility(View.VISIBLE);
            mBanner.setVisibility(View.GONE);
            mSkip.setVisibility(View.GONE);
            mHandler.sendEmptyMessageDelayed(MSG_SKIP, 3000);
        } else {
            RetrofitHelper.getApi().getImage(2)
                    .subscribe(new SimpleObserver<BaseResponse<List<BannerBean>>>() {
                        @Override
                        public void onNext(BaseResponse<List<BannerBean>> response) {
                            if (response != null && response.data != null && !response.data.isEmpty()) {
                                mWelcomeRl.setVisibility(View.GONE);
                                initGuideBanner(response.data);
                            }
                        }
                    });
            mSkip.setOnClickListener(view -> {
                SPUtils.put(SPConstant.SP_SPLASH_WELCOME, true);
                mHandler.sendEmptyMessageDelayed(MSG_SKIP, 500);
            });
        }
    }

    private void initGuideBanner(List<BannerBean> list) {
        ArrayList<String> data = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            data.add(list.get(i).img);
        }
        // 第一次启动
        mBanner.setAdapter(new SplashBannerAdapter(this, data, list))
                .setIndicator(new CircleIndicator(this))
                .setIndicatorRadius(0).start();

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SKIP:
                    if (TextUtils.isEmpty(SPUtils.getString(SPConstant.SP_USER_TOKEN, ""))) {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    } else {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 3000);
                    break;
            }
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permissionDenied = false;//有权限没有通过
        if (1 == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionDenied = true;
                }
            }
            if (permissionDenied) {
                ToastUtil.show("权限受限，退出APP");
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
