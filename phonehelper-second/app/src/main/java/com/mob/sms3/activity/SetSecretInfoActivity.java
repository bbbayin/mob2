package com.mob.sms3.activity;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.mob.sms3.R;
import com.mob.sms3.base.BaseActivity;
import com.mob.sms3.databinding.ActivitySecretInfoSettingLayoutBinding;
import com.mob.sms3.utils.SPConstant;
import com.mob.sms3.utils.SPUtils;
import com.mob.sms3.utils.ToastUtil;

public class SetSecretInfoActivity extends BaseActivity {

    private ActivitySecretInfoSettingLayoutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySecretInfoSettingLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setStatusBar(getColor(R.color.green));
        initView();
        initClick();
    }

    private void initView() {
        String string = SPUtils.getString(SPConstant.SP_USER_PHONE, "");
        binding.secretSettingEtPhone.setText(string);
        int sim = SPUtils.getInt(SPConstant.SP_SECRET_SIM_NO, 0);
        if (sim == 0) {
            binding.secretSettingRg.check(R.id.secret_setting_rb_sim1);
        }else {
            binding.secretSettingRg.check(R.id.secret_setting_rb_sim2);
        }
    }

    private void initClick() {
        binding.secretSettingBtnSave.setOnClickListener(v -> {
            String phone = binding.secretSettingEtPhone.getText().toString();
            if (TextUtils.isEmpty(phone) || phone.length() != 11) {
                ToastUtil.show("请填写正确的手机号");
            }else {
                SPUtils.put(SPConstant.SP_USER_PHONE, phone);
                int id = binding.secretSettingRg.getCheckedRadioButtonId();
                if (id == R.id.secret_setting_rb_sim1) {
                    SPUtils.put(SPConstant.SP_SECRET_SIM_NO, 0);
                }else {
                    SPUtils.put(SPConstant.SP_SECRET_SIM_NO, 1);
                }
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
