package com.mob.sms.skin3.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.mob.sms.skin3.R;
import com.mob.sms.skin3.base.BaseActivity;
import com.mob.sms.skin3.bean.PhoneNumberBean;
import com.mob.sms.skin3.databinding.ActivitySecretInfoSettingLayoutBinding;
import com.mob.sms.skin3.network.RetrofitHelper;
import com.mob.sms.skin3.network.bean.UserInfoBean;
import com.mob.sms.skin3.rx.BaseObserver;
import com.mob.sms.skin3.rx.MobError;
import com.mob.sms.skin3.utils.Constants;
import com.mob.sms.skin3.utils.SPConstant;
import com.mob.sms.skin3.utils.SPUtils;
import com.mob.sms.skin3.utils.ToastUtil;

import java.util.Random;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SetSecretInfoActivity extends BaseActivity {

    private ActivitySecretInfoSettingLayoutBinding binding;
    private ProgressDialog progressDialog;
    private String randomPhone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySecretInfoSettingLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setStatusBar(getColor(R.color.green));
        initView();
        initClick();
        initData();
    }

    private String CHANGE_NUMBER_TIME = "change_number_time";
    private String SP_USED_TIMES = "sp_used_times";// 使用的更换次数
    private String SP_MAX_TIMES = "sp_max_times";// 指定时间段内允许更换的次数
    private String[] phoneList;
    private int hours;
    private int defaultTimes;


    private void initData() {
        RetrofitHelper.getApi().getPhoneNumbers()
                .subscribe(new BaseObserver<PhoneNumberBean>() {
                    @Override
                    protected void onSuccess(PhoneNumberBean data) {
                        if (data != null) {
                            formatPhoneList(data.tels);
                            if (phoneList != null && phoneList.length > 0) {
                                showConfigLayout(true);
                                hours = Integer.parseInt(data.hours);
                                defaultTimes = Integer.parseInt(data.num);
                                // 已绑定的隐私号
                                String secreteNumber = SPUtils.getString(Constants.SECRET_NUMBER, "");
                                if (!TextUtils.isEmpty(secreteNumber)) {
                                    binding.callTypeTvSecretNumber.setText(secreteNumber);
                                } else {
                                    // 随机一个
                                    String phone = getRandomPhone();
                                    SPUtils.put(Constants.SECRET_NUMBER, phone);
                                    binding.callTypeTvSecretNumber.setText(phone);
                                }
                            } else {
                                ToastUtil.show("隐私号码列表获取失败");
                                showConfigLayout(false);
                            }
                        } else {
                            showConfigLayout(false);
                            ToastUtil.show("隐私号配置信息获取失败，请重试");
                        }
                    }

                    @Override
                    protected void onFailed(MobError error) {
                        showConfigLayout(false);
                        ToastUtil.show(error.getErrorMsg());
                    }
                });

        // vip
        RetrofitHelper.getApi().getUserInfo().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userInfoBean -> {
                    if (userInfoBean != null && userInfoBean.code == 200) {
                        UserInfoBean.DataBean mUserInfo = userInfoBean.data;

                        if (mUserInfo.allMinute > 0) {
                            // vip
                            binding.callTypeTvSecretNumber.setVisibility(View.VISIBLE);
                            binding.callTypeBtnChangeSecretNumber.setEnabled(true);
                        } else {
                            // 普通
                            binding.callTypeTvSecretNumber.setVisibility(View.GONE);
                            binding.callTypeBtnChangeSecretNumber.setEnabled(false);
                        }
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }

    // 随机号码
    private String getRandomPhone() {
        int index = new Random().nextInt(phoneList.length);
        return phoneList[index];
    }

    private void showConfigLayout(boolean success) {
        if (success) {
            binding.callTypeLlSecretNumber.setVisibility(View.VISIBLE);
            binding.btnRefreshConfig.setVisibility(View.GONE);
        } else {
            binding.callTypeLlSecretNumber.setVisibility(View.GONE);
            binding.btnRefreshConfig.setVisibility(View.VISIBLE);
            binding.btnRefreshConfig.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    initData();
                }
            });
        }
    }

    private void formatPhoneList(String tels) {
        if (!TextUtils.isEmpty(tels)) {
            tels = tels.replace(" ", "");
            tels = tels.replace("，", ",");
            phoneList = tels.split(",");
        }
    }

    private void initView() {
        String string = SPUtils.getString(SPConstant.SP_USER_PHONE, "");
        binding.secretSettingEtPhone.setText(string);
        int sim = SPUtils.getInt(SPConstant.SP_SECRET_SIM_NO, 0);
        if (sim == 0) {
            binding.secretSettingRg.check(R.id.secret_setting_rb_sim1);
        } else {
            binding.secretSettingRg.check(R.id.secret_setting_rb_sim2);
        }
    }

    private void initClick() {
        binding.secretSettingBtnSave.setOnClickListener(v -> {
            String phone = binding.secretSettingEtPhone.getText().toString();
            if (TextUtils.isEmpty(phone) || phone.length() != 11) {
                ToastUtil.show("请填写正确的手机号");
            } else {
                SPUtils.put(SPConstant.SP_USER_PHONE, phone);
                int id = binding.secretSettingRg.getCheckedRadioButtonId();
                if (id == R.id.secret_setting_rb_sim1) {
                    SPUtils.put(SPConstant.SP_SECRET_SIM_NO, 0);
                } else {
                    SPUtils.put(SPConstant.SP_SECRET_SIM_NO, 1);
                }
                setResult(RESULT_OK);
                finish();
            }
        });


        // 切换绑定号码
        binding.callTypeBtnChangeSecretNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check
                boolean b = checkChangeTime();
                if (!b) {
                    return;
                }
                showLoading();
                binding.callTypeBtnChangeSecretNumber.setText("获取中...");
                String nowPhone = binding.callTypeTvSecretNumber.getText().toString();
                randomPhone = getRandomPhone();
                while (randomPhone.equals(nowPhone)) {
                    randomPhone = getRandomPhone();
                }
                SPUtils.put(Constants.SECRET_NUMBER, randomPhone);
                saveChangeTime();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        binding.callTypeBtnChangeSecretNumber.setText("切换隐私号码");
                        binding.callTypeTvSecretNumber.setText(randomPhone);
                        dismissLoading();
                    }
                }, 2000);

            }
        });
    }

    private boolean checkChangeTime() {
        String s = SPUtils.getString(CHANGE_NUMBER_TIME, "");
        int usedTimes = SPUtils.getInt(SP_USED_TIMES, 0);
        long lastTime = 0;
        try {
            lastTime = Long.parseLong(s);
        } catch (Exception e) {

        }
        long now = System.currentTimeMillis() / 1000;
        // 秒
        long duration = now - lastTime;
        // 秒->小时
        long hour = duration / 3600;
        if (hour >= hours) {
            // 超过指定时间了，复原
            SPUtils.put(SP_USED_TIMES, 0);
            return true;
        } else {
            // 判断次数
            if (usedTimes > defaultTimes) {
                ToastUtil.show(String.format("为了防止恶意拨号，每%s小时只可更换%s次隐私号，请稍等再试！", hours, defaultTimes));
                return false;
            } else {
                return true;
            }
        }
    }

    private void saveChangeTime() {
        long l = System.currentTimeMillis() / 1000;
        SPUtils.put(CHANGE_NUMBER_TIME, String.valueOf(l));
        int anInt = SPUtils.getInt(SP_USED_TIMES, 0);
        SPUtils.put(SP_USED_TIMES, ++anInt);
    }

    private Handler handler = new Handler(Looper.getMainLooper());

    private synchronized void showLoading() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void dismissLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
