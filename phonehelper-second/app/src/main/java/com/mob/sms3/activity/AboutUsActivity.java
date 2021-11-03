package com.mob.sms3.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.mob.sms3.R;
import com.mob.sms3.base.BaseActivity;
import com.mob.sms3.dialog.CheckTipDialog;
import com.mob.sms3.network.RetrofitHelper;
import com.mob.sms3.utils.ToastUtil;

import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AboutUsActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
    }

    @OnClick({R.id.back, R.id.about_rl, R.id.agreement_rl, R.id.privacy_rl, R.id.check_rl})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.about_rl:
                Intent intent = new Intent(AboutUsActivity.this, AgreementActivity.class);
                intent.putExtra("type", 3);
                intent.putExtra("title", "关于我们");
                startActivity(intent);
                break;
            case R.id.agreement_rl:
                intent = new Intent(AboutUsActivity.this, AgreementActivity.class);
                intent.putExtra("type", 1);
                intent.putExtra("title", "用户协议");
                startActivity(intent);
                break;
            case R.id.privacy_rl:
                intent = new Intent(AboutUsActivity.this, AgreementActivity.class);
                intent.putExtra("type", 2);
                intent.putExtra("title", "隐私协议");
                startActivity(intent);
                break;
            case R.id.check_rl:
                getRule();
                break;
        }
    }

    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "无法获取到版本号";
        }
    }

    private void getRule(){
        RetrofitHelper.getApi().getVersion().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(versionBean -> {
                    if (versionBean != null && versionBean.code == 200) {
                        if (versionBean.data.get(0).name.equals(getVersion())) {
                            CheckTipDialog checkTipDialog = new CheckTipDialog(AboutUsActivity.this);
                            checkTipDialog.show();
                        } else {
                            ToastUtil.show("请去应用市场更新最新版本");
                        }
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }
}
