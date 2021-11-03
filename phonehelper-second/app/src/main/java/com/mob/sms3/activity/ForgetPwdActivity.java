package com.mob.sms3.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mob.sms3.R;
import com.mob.sms3.base.BaseActivity;
import com.mob.sms3.network.RetrofitHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ForgetPwdActivity extends BaseActivity {
    @BindView(R.id.title_rl)
    RelativeLayout mTitleRl;
    @BindView(R.id.back)
    ImageView mBack;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.mobile_et)
    EditText mMobileEt;
    @BindView(R.id.code_et)
    EditText mCodeEt;
    @BindView(R.id.pwd_et)
    EditText mPwdEt;
    @BindView(R.id.pwd2_et)
    EditText mPwd2Et;
    @BindView(R.id.code)
    TextView mSendCode;
    @BindView(R.id.eye_icon)
    ImageView mEyeIcon;
    @BindView(R.id.eye2_icon)
    ImageView mEye2Icon;

    private int mTime = 60;
    private boolean mCanSeePwd;
    private boolean mCanSeePwd2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pwd);
        ButterKnife.bind(this);
        if ("modify".equals(getIntent().getStringExtra("type"))) {
            mBack.setImageResource(R.mipmap.back_white);
            setStatusBar(getResources().getColor(R.color.green));
            mTitle.setText("修改密码");
            mTitle.setTextColor(Color.parseColor("#ffffff"));
            mTitleRl.setBackgroundColor(getResources().getColor(R.color.green));
        } else {
            mBack.setImageResource(R.mipmap.back_black);
            setStatusBar(getResources().getColor(R.color.white));
            mTitle.setText("忘记密码");
            mTitle.setTextColor(Color.parseColor("#454545"));
            mTitleRl.setBackgroundColor(getResources().getColor(R.color.white));
        }
    }

    @Override
    protected boolean setTransparentStatusEnable() {
        return true;
    }

    @OnClick({R.id.back, R.id.code, R.id.eye_icon, R.id.eye2_icon, R.id.confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.code:
                if (!TextUtils.isEmpty(mMobileEt.getText().toString()) && mMobileEt.getText().toString().length() == 11) {
                    sendCode();
                } else {
                    Toast.makeText(ForgetPwdActivity.this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.eye_icon:
                mCanSeePwd = !mCanSeePwd;
                mPwdEt.setTransformationMethod(mCanSeePwd? PasswordTransformationMethod.getInstance(): HideReturnsTransformationMethod.getInstance());
                mEyeIcon.setBackgroundResource(mCanSeePwd?R.mipmap.eye_icon:R.mipmap.eye2_icon);
                break;
            case R.id.eye2_icon:
                mCanSeePwd2 = !mCanSeePwd2;
                mPwd2Et.setTransformationMethod(mCanSeePwd2? PasswordTransformationMethod.getInstance(): HideReturnsTransformationMethod.getInstance());
                mEye2Icon.setBackgroundResource(mCanSeePwd2?R.mipmap.eye_icon:R.mipmap.eye2_icon);
                break;
            case R.id.confirm:
                if (TextUtils.isEmpty(mMobileEt.getText().toString()) || mMobileEt.getText().toString().length() != 11) {
                    Toast.makeText(ForgetPwdActivity.this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(mCodeEt.getText().toString())){
                    Toast.makeText(ForgetPwdActivity.this, "请输入验证码", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(mPwdEt.getText().toString())){
                    Toast.makeText(ForgetPwdActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(mPwd2Et.getText().toString())){
                    Toast.makeText(ForgetPwdActivity.this, "请输入确认密码", Toast.LENGTH_SHORT).show();
                } else if (!mPwdEt.getText().toString().equals(mPwd2Et.getText().toString())) {
                    Toast.makeText(ForgetPwdActivity.this, "两次密码输入不一致", Toast.LENGTH_SHORT).show();
                } else {
                    forgetPwd();
                }
                break;
        }
    }

    private void forgetPwd(){
        RetrofitHelper.getApi().forgetPwd(mCodeEt.getText().toString(), mPwd2Et.getText().toString(),
                 mPwdEt.getText().toString(), mMobileEt.getText().toString()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseBean -> {
                    if (baseBean != null && baseBean.code == 200) {
                        Toast.makeText(ForgetPwdActivity.this, "修改密码成功", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(ForgetPwdActivity.this, baseBean.msg, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    Toast.makeText(ForgetPwdActivity.this, "修改密码失败", Toast.LENGTH_LONG).show();
                    throwable.printStackTrace();
                });
    }

    private void sendCode(){
        RetrofitHelper.getApi().sendSms(mMobileEt.getText().toString()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseBean -> {
                    if (baseBean != null && baseBean.code == 200) {
                        Toast.makeText(ForgetPwdActivity.this, R.string.send_code_success, Toast.LENGTH_LONG).show();
                        mSendCode.setText("已发送(" + mTime + ")");
                        mHandler.sendEmptyMessageDelayed(0, 1000);
                    } else {
                        Toast.makeText(ForgetPwdActivity.this, R.string.send_code_fail, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    Toast.makeText(ForgetPwdActivity.this, R.string.send_code_fail, Toast.LENGTH_LONG).show();
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
