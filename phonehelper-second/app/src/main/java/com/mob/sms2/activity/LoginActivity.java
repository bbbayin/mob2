package com.mob.sms2.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gyf.immersionbar.ImmersionBar;
import com.mob.sms2.R;
import com.mob.sms2.application.MyApplication;
import com.mob.sms2.base.BaseActivity;
import com.mob.sms2.dialog.UserAgreementDialog;
import com.mob.sms2.network.RetrofitHelper;
import com.mob.sms2.policy.PolicyActivity;
import com.mob.sms2.rx.LoginEvent;
import com.mob.sms2.rx.RxBus;
import com.mob.sms2.sdk.SDKManager;
import com.mob.sms2.utils.SPConstant;
import com.mob.sms2.utils.SPUtils;
import com.mob.sms2.utils.ToastUtil;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginActivity extends BaseActivity {
    @BindView(R.id.mobile_et)
    EditText mMobileEt;
    @BindView(R.id.pwd_et)
    EditText mPwdEt;
    @BindView(R.id.eye_icon)
    ImageView mEyeIcon;
    @BindView(R.id.login)
    TextView mBtnLogin;
    @BindView(R.id.wx_tip)
    TextView mWxTip;
    @BindView(R.id.qq_tip)
    TextView mQqTip;
    @BindView(R.id.select_iv)
    ImageView mSelectIv;
    @BindView(R.id.login_tv_policy)
    TextView tvPolicy;

    private boolean mCanSeePwd;

    private boolean mTxMobile;
    private boolean mTxPwd;
    private boolean mCanLogin;

    private Subscription mSub;
    private boolean mSelectAgreement;
    private SDKManager sdkManager;
    private UMShareAPI umShareAPI;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.white));
        initView();
        mSub = RxBus.getInstance().toObserverable(LoginEvent.class)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(this::finishPage);
        initPolicy();
        showPolicyDialog();
        sdkManager = new SDKManager();
        sdkManager.addObserver(MyApplication.mApplication);
        umShareAPI = UMShareAPI.get(this);
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
                end1 + 1,
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
                        toPolicy(LoginActivity.this, PolicyActivity.TYPE_USER);
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
                        toPolicy(LoginActivity.this, PolicyActivity.TYPE_SECRET);
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

    private void finishPage(LoginEvent event) {
        finish();
    }

    private void initView() {
        mMobileEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence.toString())) {
                    mTxMobile = true;
                } else {
                    mTxMobile = false;
                }
                if (mTxMobile && mTxPwd && mSelectAgreement) {
                    mCanLogin = true;
                } else {
                    mCanLogin = false;
                }
                mBtnLogin.setEnabled(mCanLogin);
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
                if (!TextUtils.isEmpty(charSequence.toString())) {
                    mTxPwd = true;
                } else {
                    mTxPwd = false;
                }
                if (mTxMobile && mTxPwd && mSelectAgreement) {
                    mCanLogin = true;
                } else {
                    mCanLogin = false;
                }
                mBtnLogin.setEnabled(mCanLogin);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        if ("qq".equals(SPUtils.getGlobal(SPConstant.SP_USER_LOGIN_TYPE))) {
            mQqTip.setVisibility(View.VISIBLE);
            mWxTip.setVisibility(View.INVISIBLE);
        } else if ("wx".equals(SPUtils.getGlobal(SPConstant.SP_USER_LOGIN_TYPE))) {
            mQqTip.setVisibility(View.INVISIBLE);
            mWxTip.setVisibility(View.VISIBLE);
        } else {
            mQqTip.setVisibility(View.INVISIBLE);
            mWxTip.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected boolean setTransparentStatusEnable() {
        return true;
    }


    @OnClick({R.id.register, R.id.forget, R.id.eye_icon, R.id.login, R.id.wx_login, R.id.qq_login, R.id.select_iv})
    public void onViewClicked(View view) {

        switch (view.getId()) {
            case R.id.register:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
            case R.id.forget:
                startActivity(new Intent(this, ForgetPwdActivity.class));
                break;
            case R.id.eye_icon:
                mCanSeePwd = !mCanSeePwd;
                mPwdEt.setTransformationMethod(mCanSeePwd ? PasswordTransformationMethod.getInstance() : HideReturnsTransformationMethod.getInstance());
                mEyeIcon.setBackgroundResource(mCanSeePwd ? R.mipmap.eye_icon : R.mipmap.eye2_icon);
                break;
            case R.id.wx_login:
//                IWXAPI wxapi = WXAPIFactory.createWXAPI(this, "wx22e1a70e838f3267");
//                SendAuth.Req req = new SendAuth.Req();
//                req.scope = "snsapi_userinfo";
//                req.state = "wechat_sdk_demo_test";
//                wxapi.sendReq(req);
                if (!mSelectAgreement) {
                    ToastUtil.show("请勾选用户隐私协议");
                    return;
                }

                umShareAPI.getPlatformInfo(this, SHARE_MEDIA.WEIXIN, new UMAuthListener() {
                    @Override
                    public void onStart(SHARE_MEDIA share_media) {

                    }

                    @Override
                    public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
                        umShareAPI.release();
                        String avatarUrl = "";
                        if (TextUtils.isEmpty(map.get("iconurl"))) {
                            avatarUrl = "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fitem%2F201912%2F25%2F20191225224833_zloky.thumb.400_0.jpg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1629380778&t=7bf1b0b77b81a074e4c318a71dfb3b09";
                        } else {
                            avatarUrl = map.get("iconurl");
                        }
                        Intent intent = new Intent(LoginActivity.this, WxInfoActivity.class);
                        intent.putExtra("uid", map.get("uid"));
                        intent.putExtra("name", map.get("name"));
                        intent.putExtra("iconurl", avatarUrl);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {

                    }

                    @Override
                    public void onCancel(SHARE_MEDIA share_media, int i) {

                    }
                });
                break;
            case R.id.qq_login:
                if (!mSelectAgreement) {
                    ToastUtil.show("请勾选用户隐私协议");
                    return;
                }
                umShareAPI.getPlatformInfo(this, SHARE_MEDIA.QQ, new UMAuthListener() {
                    @Override
                    public void onStart(SHARE_MEDIA share_media) {
                    }

                    @Override
                    public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
                        SPUtils.put(SPConstant.SP_USER_NAME, map.get("name"));
                        SPUtils.put(SPConstant.SP_USER_HEAD, map.get("iconurl"));
                        SPUtils.put(SPConstant.SP_USER_ID, map.get("uid"));
                        authLogin(map.get("iconurl"), "3", map.get("name"), map.get("uid"), MyApplication.Channel);
                    }

                    @Override
                    public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA share_media, int i) {
                    }
                });
                break;
            case R.id.login:
                if (mCanLogin) {
                    login();
                }
                break;
            case R.id.select_iv:
                enablePermission(!mSelectAgreement);
                break;
        }
    }

    private void enablePermission(boolean enable) {
        mSelectAgreement = enable;
        mSelectIv.setBackgroundResource(enable ? R.mipmap.selected_icon : R.mipmap.unselected_icon);
        if (mTxMobile && mTxPwd && enable) {
            mCanLogin = true;
        } else {
            mCanLogin = false;
        }
        mBtnLogin.setEnabled(mCanLogin);
    }

    private void showPolicyDialog() {
        UserAgreementDialog userAgreementDialog = new UserAgreementDialog(this);
        userAgreementDialog.show();
        userAgreementDialog.setOnClickListener(new UserAgreementDialog.OnClickListener() {
            @Override
            public void agree() {

                userAgreementDialog.dismiss();

                if (mTxMobile && mTxPwd && mSelectAgreement) {
                    mCanLogin = true;
                } else {
                    mCanLogin = false;
                }
                mBtnLogin.setEnabled(mCanLogin);
                SPUtils.put(SPConstant.SP_USER_PERMISSION_OK, true);
                sdkManager.init();
            }

            @Override
            public void refuse() {
                userAgreementDialog.dismiss();

                if (mTxMobile && mTxPwd && mSelectAgreement) {
                    mCanLogin = true;
                } else {
                    mCanLogin = false;
                }
                mBtnLogin.setEnabled(mCanLogin);
            }
        });
    }

    private void authLogin(String avatar, String loginType, String nickName, String openid, String originate) {
        RetrofitHelper.getApi().authLogin(avatar, loginType, nickName, openid, originate).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loginBean -> {
                    if (loginBean != null && loginBean.code == 200) {
                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_LONG).show();
                        Log.i("jqt", "loginBean.data: " + loginBean.token);
                        SPUtils.put(SPConstant.SP_USER_TOKEN, loginBean.token);
                        SPUtils.putGlobal(SPConstant.SP_USER_LOGIN_TYPE, "qq");
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, loginBean.msg, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_LONG).show();
                    throwable.printStackTrace();
                });
    }

    private void login() {
        RetrofitHelper.getApi().login(mMobileEt.getText().toString(), mPwdEt.getText().toString()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loginBean -> {
                    if (loginBean != null && loginBean.code == 200) {
                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_LONG).show();
                        SPUtils.put(SPConstant.SP_USER_TOKEN, loginBean.data);
                        SPUtils.putGlobal(SPConstant.SP_USER_LOGIN_TYPE, "mobile");
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, loginBean.msg, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_LONG).show();
                    throwable.printStackTrace();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSub != null && !mSub.isUnsubscribed()) {
            mSub.unsubscribe();
        }
    }
}
