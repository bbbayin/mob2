package com.mob.sms.skin3.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.mob.sms.skin3.R;
import com.mob.sms.skin3.base.BaseActivity;
import com.mob.sms.skin3.dialog.CheckTipDialog;
import com.mob.sms.skin3.network.RetrofitHelper;
import com.mob.sms.skin3.rx.ExitEvent;
import com.mob.sms.skin3.rx.RxBus;
import com.mob.sms.skin3.utils.SPConstant;
import com.mob.sms.skin3.utils.SPUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SettingActivity extends BaseActivity {
    @BindView(R.id.toggle_push)
    SwitchCompat toggle;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
        initView();
        toggle.setChecked(SPUtils.getBoolean(SPConstant.SP_PUSH, true));
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showProgress();
                toggle.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgress();
                    }
                }, 2000);
                SPUtils.getBoolean(SPConstant.SP_PUSH, isChecked);
            }
        });
    }

    private void showProgress() {
        if (progressDialog == null)
            progressDialog = new ProgressDialog(this);
        progressDialog.show();
    }

    private void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void initView() {

    }

    @OnClick({R.id.back, R.id.bind_rl, R.id.modify_rl, R.id.zhuxiao, R.id.exit})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.bind_rl:
                startActivity(new Intent(SettingActivity.this, BindMobileActivity.class));
                break;
            case R.id.modify_rl:
                Intent intent = new Intent(SettingActivity.this, ForgetPwdActivity.class);
                intent.putExtra("type", "modify");
                startActivity(intent);
                break;
            case R.id.zhuxiao:
                CheckTipDialog checkTipDialog = new CheckTipDialog(this);
                checkTipDialog.setTitle("提示");
                checkTipDialog.setContent("注销账号后将清除账号的所有信息，是否确认注销？");
                checkTipDialog.setCancelListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // do nothing
                    }
                });
                checkTipDialog.setPositiveListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        delUser();
                    }
                });
                checkTipDialog.show();
                break;
            case R.id.exit:
                logout();
                break;
        }
    }

    private void delUser() {
        RetrofitHelper.getApi().delUser().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseBean -> {
                    if (baseBean != null && baseBean.code == 200) {
                        logout();
                    } else {
                        Toast.makeText(SettingActivity.this, baseBean.msg, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    Toast.makeText(SettingActivity.this, "注销失败", Toast.LENGTH_LONG).show();
                    throwable.printStackTrace();
                });
    }


    private void logout() {
        SPUtils.clear();
        RxBus.getInstance().post(new ExitEvent());
        finish();
    }
}
