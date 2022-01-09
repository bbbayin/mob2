package com.mob.sms2.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gyf.immersionbar.ImmersionBar;
import com.mob.sms2.R;
import com.mob.sms2.base.BaseActivity;
import com.mob.sms2.dialog.UserAgreementDialog;
import com.mob.sms2.network.RetrofitHelper;
import com.mob.sms2.policy.PolicyActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RegisterActivity extends BaseActivity {
    @BindView(R.id.mobile_et)
    EditText mMobileEt;
    @BindView(R.id.code_et)
    EditText mCodeEt;
    @BindView(R.id.send_code)
    TextView mSendCode;
    @BindView(R.id.pwd_et)
    EditText mPwdEt;
    @BindView(R.id.eye_icon)
    ImageView mEyeIcon;
    @BindView(R.id.pwd_et2)
    EditText mPwdEt2;
    @BindView(R.id.eye2_icon)
    ImageView mEye2Icon;
    @BindView(R.id.select_iv)
    ImageView mSelectIv;
    @BindView(R.id.register)
    TextView mBtnRegister;
    @BindView(R.id.register_policy)
    TextView tvPolicy;

    private boolean mCanSeePwd;
    private boolean mCanSeePwd2;

    private boolean mSelectAgreement;
    private boolean mTxMobile;
    private boolean mTxCode;
    private boolean mTxPwd;
    private boolean mTxPwd2;

    private boolean mCanRegister;
    private int mTime = 60;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.white));
        initView();
        initPolicy();
        ImmersionBar.with(this).statusBarColor(R.color.colorPrimary).init();
    }
    private void initPolicy() {
        String content = "我已阅读并同意《用户协议》和《隐私政策》";
        SpannableString spannableString = new SpannableString(content);
        int start1 = content.indexOf("《");
        int end1 = content.indexOf("》");
        int start2 = content.lastIndexOf("《");
        int end2 = content.length();
        int color = Color.parseColor("#33C197");
        spannableString.setSpan(new ForegroundColorSpan(color),
                start1,
                end1+1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(color),
                start2,
                end2,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(
                new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        // 用户协议
                        toPolicy(RegisterActivity.this, PolicyActivity.TYPE_USER);
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        ds.setUnderlineText(false);
                    }
                },
                start1,
                end1,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spannableString.setSpan(
                new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        // 隐私政策
                        toPolicy(RegisterActivity.this, PolicyActivity.TYPE_SECRET);
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        ds.setUnderlineText(false);
                    }
                },
                start2,
                end2,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        tvPolicy.setMovementMethod(LinkMovementMethod.getInstance());
        tvPolicy.setText(spannableString);
    }

    private void toPolicy(Context context, int type) {
        Intent intent = new Intent(context, PolicyActivity.class);
        intent.putExtra(PolicyActivity.KEY_POLICY_TYPE, type);
        context.startActivity(intent);
    }

    private void initView(){
        mMobileEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!TextUtils.isEmpty(charSequence.toString())){
                    mTxMobile = true;
                } else {
                    mTxMobile = false;
                }
                if(mTxMobile && mTxCode && mTxPwd && mTxPwd2 && mSelectAgreement){
                    mCanRegister = true;
                } else {
                    mCanRegister = false;
                }
                mBtnRegister.setEnabled(mCanRegister);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mCodeEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!TextUtils.isEmpty(charSequence.toString())){
                    mTxCode = true;
                } else {
                    mTxCode = false;
                }
                if(mTxMobile && mTxCode && mTxPwd && mTxPwd2 && mSelectAgreement){
                    mCanRegister = true;
                } else {
                    mCanRegister = false;
                }
                mBtnRegister.setEnabled(mCanRegister);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mPwdEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!TextUtils.isEmpty(charSequence.toString())){
                    mTxPwd = true;
                } else {
                    mTxPwd = false;
                }
                if(mTxMobile && mTxCode && mTxPwd && mTxPwd2 && mSelectAgreement){
                    mCanRegister = true;
                } else {
                    mCanRegister = false;
                }
                mBtnRegister.setEnabled(mCanRegister);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mPwdEt2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!TextUtils.isEmpty(charSequence.toString())){
                    mTxPwd2 = true;
                } else {
                    mTxPwd2 = false;
                }
                if(mTxMobile && mTxCode && mTxPwd && mTxPwd2 && mSelectAgreement){
                    mCanRegister = true;
                } else {
                    mCanRegister = false;
                }
                mBtnRegister.setEnabled(mCanRegister);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    @Override
    protected boolean setTransparentStatusEnable() {
        return true;
    }

    @OnClick({R.id.send_code, R.id.login, R.id.eye_icon, R.id.eye2_icon, R.id.select_iv, R.id.register})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.send_code:
                if (!TextUtils.isEmpty(mMobileEt.getText().toString()) && mMobileEt.getText().toString().length() == 11) {
                    sendCode();
                } else {
                    Toast.makeText(RegisterActivity.this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.select_iv:
                checked(!mSelectAgreement);
                break;
            case R.id.eye_icon:
                mCanSeePwd = !mCanSeePwd;
                mPwdEt.setTransformationMethod(mCanSeePwd? PasswordTransformationMethod.getInstance(): HideReturnsTransformationMethod.getInstance());
                mEyeIcon.setBackgroundResource(mCanSeePwd?R.mipmap.eye_icon:R.mipmap.eye2_icon);
                break;
            case R.id.eye2_icon:
                mCanSeePwd2 = !mCanSeePwd2;
                mPwdEt2.setTransformationMethod(mCanSeePwd2? PasswordTransformationMethod.getInstance(): HideReturnsTransformationMethod.getInstance());
                mEye2Icon.setBackgroundResource(mCanSeePwd2?R.mipmap.eye_icon:R.mipmap.eye2_icon);
                break;
            case R.id.login:
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
            case R.id.register:
                if (mCanRegister) {
                    if (!mPwdEt.getText().toString().equals(mPwdEt2.getText().toString())) {
                        Toast.makeText(RegisterActivity.this, "两次密码输入不一致", Toast.LENGTH_SHORT).show();
                    } else {
                        register();
                    }
                }
                break;
        }
    }

    private void checked(boolean enable) {
        mSelectAgreement = enable;
        if (mSelectAgreement) {
            mSelectIv.setBackgroundResource(R.mipmap.selected_icon);
        }else {
            mSelectIv.setBackgroundResource(R.mipmap.unselected_icon);
        }
        if(mTxMobile && mTxCode && mTxPwd && mTxPwd2 && mSelectAgreement){
            mCanRegister = true;
        } else {
            mCanRegister = false;
        }
        mBtnRegister.setEnabled(mCanRegister);
    }

    private void showDialog() {
        UserAgreementDialog userAgreementDialog = new UserAgreementDialog(this);
        userAgreementDialog.show();
        userAgreementDialog.setOnClickListener(new UserAgreementDialog.OnClickListener() {
            @Override
            public void agree() {
                mSelectAgreement = true;
                mSelectIv.setBackgroundResource(R.mipmap.selected_icon);
                userAgreementDialog.dismiss();

                if(mTxMobile && mTxCode && mTxPwd && mTxPwd2 && mSelectAgreement){
                    mCanRegister = true;
                } else {
                    mCanRegister = false;
                }
                mBtnRegister.setEnabled(mCanRegister);
            }

            @Override
            public void refuse() {
                mSelectAgreement = false;
                mSelectIv.setBackgroundResource(R.mipmap.unselected_icon);
                userAgreementDialog.dismiss();

                if(mTxMobile && mTxCode && mTxPwd && mTxPwd2 && mSelectAgreement){
                    mCanRegister = true;
                } else {
                    mCanRegister = false;
                }
                mBtnRegister.setEnabled(mCanRegister);
            }
        });
    }

    private void register(){
        RetrofitHelper.getApi().register(mCodeEt.getText().toString(), mPwdEt2.getText().toString(),
                "", mPwdEt.getText().toString(), mMobileEt.getText().toString()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseBean -> {
                    if (baseBean != null && baseBean.code == 200) {
                        Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, baseBean.msg, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_LONG).show();
                    throwable.printStackTrace();
                });
    }

    private void sendCode(){
        RetrofitHelper.getApi().sendSms(mMobileEt.getText().toString()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseBean -> {
                    if (baseBean != null && baseBean.code == 200) {
                        Toast.makeText(RegisterActivity.this, R.string.send_code_success, Toast.LENGTH_LONG).show();
                        mSendCode.setText("已发送(" + mTime + ")");
                        mHandler.sendEmptyMessageDelayed(0, 1000);
                    } else {
                        Toast.makeText(RegisterActivity.this, R.string.send_code_fail, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    Toast.makeText(RegisterActivity.this, R.string.send_code_fail, Toast.LENGTH_LONG).show();
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
