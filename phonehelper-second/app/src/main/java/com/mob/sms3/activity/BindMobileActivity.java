package com.mob.sms3.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mob.sms3.R;
import com.mob.sms3.base.BaseActivity;
import com.mob.sms3.network.RetrofitHelper;
import com.mob.sms3.utils.SPConstant;
import com.mob.sms3.utils.SPUtils;
import com.mob.sms3.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BindMobileActivity extends BaseActivity {
    @BindView(R.id.mobile_et)
    EditText mMobileEt;
    @BindView(R.id.code_et)
    EditText mCodeEt;
    @BindView(R.id.code)
    TextView mSendCode;

    private int mTime = 60;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_mobile);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
    }

    @Override
    protected boolean setTransparentStatusEnable() {
        return true;
    }

    @OnClick({R.id.back, R.id.code, R.id.confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.code:
                if (!TextUtils.isEmpty(mMobileEt.getText().toString()) && mMobileEt.getText().toString().length() == 11) {
                    sendCode();
                } else {
                    Toast.makeText(BindMobileActivity.this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.confirm:
                if (TextUtils.isEmpty(mMobileEt.getText().toString()) || mMobileEt.getText().toString().length() != 11) {
                    Toast.makeText(BindMobileActivity.this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(mCodeEt.getText().toString())){
                    Toast.makeText(BindMobileActivity.this, "请输入验证码", Toast.LENGTH_SHORT).show();
                } else {
                    bind();
                }
                break;
        }
    }

    private void bind(){
        RetrofitHelper.getApi().bindMobile(SPUtils.getString(SPConstant.SP_USER_TOKEN, ""),
                mMobileEt.getText().toString(), mCodeEt.getText().toString()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseBean -> {
                    if (baseBean != null && baseBean.code == 200) {
                        ToastUtil.show("绑定成功");
                        SPUtils.put(SPConstant.SP_USER_PHONE, mMobileEt.getText().toString());
                        finish();
                    } else {
                        ToastUtil.show(baseBean.msg);
                    }
                }, throwable -> {
                    ToastUtil.show("绑定失败");
                    throwable.printStackTrace();
                });
    }

    private void sendCode(){
        RetrofitHelper.getApi().sendSms(mMobileEt.getText().toString()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseBean -> {
                    if (baseBean != null && baseBean.code == 200) {
                        Toast.makeText(BindMobileActivity.this, R.string.send_code_success, Toast.LENGTH_LONG).show();
                        mSendCode.setText("已发送(" + mTime + ")");
                        mHandler.sendEmptyMessageDelayed(0, 1000);
                    } else {
                        Toast.makeText(BindMobileActivity.this, R.string.send_code_fail, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    Toast.makeText(BindMobileActivity.this, R.string.send_code_fail, Toast.LENGTH_LONG).show();
                    throwable.printStackTrace();
                });
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    mTime--;
                    if(mTime>0){
                        mSendCode.setText("已发送(" + mTime + ")");
                        mSendCode.setBackgroundResource(R.drawable.round_20_grey);
                        sendEmptyMessageDelayed(0, 1000);
                    } else {
                        mSendCode.setText("发送验证码");
                        mSendCode.setBackgroundResource(R.drawable.round_20_green);
                    }
                    break;
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
