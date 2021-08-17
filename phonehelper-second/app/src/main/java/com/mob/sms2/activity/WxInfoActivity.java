package com.mob.sms2.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.mob.sms2.R;
import com.mob.sms2.application.MyApplication;
import com.mob.sms2.base.BaseActivity;
import com.mob.sms2.network.RetrofitHelper;
import com.mob.sms2.rx.LoginEvent;
import com.mob.sms2.rx.RxBus;
import com.mob.sms2.utils.SPConstant;
import com.mob.sms2.utils.SPUtils;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class WxInfoActivity extends BaseActivity {
    @BindView(R.id.userhead)
    ImageView mUserhead;
    @BindView(R.id.username)
    TextView mUsername;
    private String uid;
    private String name;
    private String iconurl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wx_info);
        ButterKnife.bind(this);

        uid = getIntent().getStringExtra("uid");
        name = getIntent().getStringExtra("name");
        iconurl = getIntent().getStringExtra("iconurl");

        Glide.with(this).load(iconurl).into(mUserhead);
        mUsername.setText(name);
        UMShareAPI.get(this).deleteOauth(this, SHARE_MEDIA.WEIXIN, null);
    }

    @OnClick({R.id.close, R.id.refuse, R.id.agree, R.id.add})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.close:
                finish();
                break;
            case R.id.refuse:
                finish();
                break;
            case R.id.agree:
                authLogin(iconurl, "2", name, uid, MyApplication.Channel);
                break;
            case R.id.add:
                break;
        }
    }

    private void authLogin(String avatar, String loginType, String nickName, String openid, String originate) {
        RetrofitHelper.getApi().authLogin(avatar, loginType, nickName, openid, originate).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loginBean -> {
                    if (loginBean != null && loginBean.code == 200) {
                        Toast.makeText(WxInfoActivity.this, "登录成功", Toast.LENGTH_LONG).show();
                        SPUtils.put(SPConstant.SP_USER_TOKEN, loginBean.token);
                        SPUtils.putGlobal(SPConstant.SP_USER_LOGIN_TYPE, "wx");
                        SPUtils.put(SPConstant.SP_USER_NAME, name);
                        SPUtils.put(SPConstant.SP_USER_HEAD, iconurl);
                        RxBus.getInstance().post(new LoginEvent());
                        startActivity(new Intent(WxInfoActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(WxInfoActivity.this, loginBean.msg, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    Toast.makeText(WxInfoActivity.this, "登录失败", Toast.LENGTH_LONG).show();
                    throwable.printStackTrace();
                });
    }

}
