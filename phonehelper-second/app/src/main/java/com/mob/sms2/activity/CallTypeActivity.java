package com.mob.sms2.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.mob.sms2.R;
import com.mob.sms2.base.BaseActivity;
import com.mob.sms2.base.SimpleObserver;
import com.mob.sms2.bean.CloudPermissionBean;
import com.mob.sms2.bean.HomeFuncBean;
import com.mob.sms2.dialog.CheckTipDialog;
import com.mob.sms2.network.RetrofitHelper;
import com.mob.sms2.network.bean.BaseResponse;
import com.mob.sms2.utils.Constants;
import com.mob.sms2.utils.SPConstant;
import com.mob.sms2.utils.SPUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 拨打方式
 */
public class CallTypeActivity extends BaseActivity {
    @BindView(R.id.gou_iv)
    ImageView mGouIv;
    @BindView(R.id.gou_iv2)
    ImageView mGouIv2;
    @BindView(R.id.gou_iv3)
    ImageView mGouIv3;
    @BindView(R.id.sim_rl2)
    RelativeLayout mSimRl2;
    @BindView(R.id.divider2)
    View mDivider2;
    @BindView(R.id.ysh_rl)
    View secretTypeLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_type);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));

        TelephonyManager manager = (TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int numSlots = manager.getPhoneCount();
            if (numSlots < 2) {
                mSimRl2.setVisibility(View.GONE);
                mDivider2.setVisibility(View.GONE);
            }
        }
        String type = SPUtils.getString(SPConstant.SP_SIM_CARD_TYPE, Constants.SIM_TYPE_SIM_1);
        if (Constants.SIM_TYPE_SIM_1.equals(type)) {
            mGouIv.setVisibility(View.VISIBLE);
            mGouIv2.setVisibility(View.GONE);
            mGouIv3.setVisibility(View.GONE);
        } else if (Constants.SIM_TYPE_SIM_2.equals(type)) {
            mGouIv.setVisibility(View.GONE);
            mGouIv2.setVisibility(View.VISIBLE);
            mGouIv3.setVisibility(View.GONE);
        } else if (Constants.SIM_TYPE_SECRET.equals(type)) {
            mGouIv.setVisibility(View.GONE);
            mGouIv2.setVisibility(View.GONE);
            mGouIv3.setVisibility(View.VISIBLE);
        }

        initData();
    }

    private void initData() {
        RetrofitHelper.getApi().getThreadInfo().subscribe(new SimpleObserver<BaseResponse<HomeFuncBean>>() {
            @Override
            public void onNext(BaseResponse<HomeFuncBean> response) {
                if (response != null && response.data != null && TextUtils.equals(response.data.status, "1")) {
                    secretTypeLayout.setVisibility(View.VISIBLE);
                } else {
                    secretTypeLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    @OnClick({R.id.back, R.id.sim_rl, R.id.sim_rl2, R.id.ysh_rl})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.sim_rl:
                mGouIv.setVisibility(View.VISIBLE);
                mGouIv2.setVisibility(View.GONE);
                mGouIv3.setVisibility(View.GONE);
                SPUtils.put(SPConstant.SP_SIM_CARD_TYPE, Constants.SIM_TYPE_SIM_1);
                setResult(RESULT_OK, getIntent());
                finish();
                break;
            case R.id.sim_rl2:
                mGouIv.setVisibility(View.GONE);
                mGouIv2.setVisibility(View.VISIBLE);
                mGouIv3.setVisibility(View.GONE);
                SPUtils.put(SPConstant.SP_SIM_CARD_TYPE, Constants.SIM_TYPE_SIM_2);
                setResult(RESULT_OK, getIntent());
                finish();
                break;
            case R.id.ysh_rl:
                Intent intent = new Intent(this, SetSecretInfoActivity.class);
                startActivityForResult(intent, 1234);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234 && resultCode == RESULT_OK) {
            enableCloudCall();
        }
    }

    private void enableCloudCall() {
        mGouIv.setVisibility(View.GONE);
        mGouIv2.setVisibility(View.GONE);
        mGouIv3.setVisibility(View.VISIBLE);
        SPUtils.put(SPConstant.SP_SIM_CARD_TYPE, Constants.SIM_TYPE_SECRET);
        setResult(RESULT_OK, getIntent());
        finish();
    }

    private void cloudDialCheck() {
        RetrofitHelper.getApi().cloudDial()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<CloudPermissionBean>() {
                    @Override
                    public void onNext(CloudPermissionBean permissionBean) {
                        if (permissionBean != null) {
                            if ("200".equals(permissionBean.code)) {
                                enableCloudCall();
                            } else {
                                CheckTipDialog dialog = new CheckTipDialog(CallTypeActivity.this);
                                dialog.setContent(permissionBean.msg);
                                dialog.show();
                            }
                        } else {
                            CheckTipDialog dialog = new CheckTipDialog(CallTypeActivity.this);
                            dialog.setContent("隐私拨打不能使用，您还未购买套餐");
                            dialog.show();
                        }
                    }
                });
    }
}
