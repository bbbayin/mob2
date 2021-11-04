package com.mob.sms.skin3.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mob.sms.skin3.R;
import com.mob.sms.skin3.policy.PolicyActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class UserAgreementDialog extends Dialog {
    private Unbinder bind;
    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void agree();

        void refuse();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    public UserAgreementDialog(@NonNull Context context) {
        this(context, R.style.dialogNoBg);
    }

    private UserAgreementDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);

        View view = View.inflate(context, R.layout.dialog_useragreement, null);
        bind = ButterKnife.bind(this, view);

        setContentView(view);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        initView(context);
    }

    private void initView(Context context) {
        TextView tvContent = findViewById(R.id.dialog_tv_policy);
        String content = "内容请你务必审慎阅读，充分理解\"服务协议\"和\"隐私政策\"各条款，包括但不限于：为了向你提供即时通讯，内容分享等服务，我们需要收集你的设备信息，操作日志，等个人信息。" +
                "你可以在设置中查看、变更、删除个人信息并管理你的授权。\n你可阅读《服务协议》和《隐私政策》了解详细信息。如你同意，请点击\"同意\"开始接受我们的服务。";
        SpannableString spannableString = new SpannableString(content);
        int start1 = content.indexOf("《");
        int end1 = content.indexOf("》");
        int start2 = content.lastIndexOf("《");
        int end2 = content.lastIndexOf("》");
        int color = getContext().getColor(R.color.colorPrimary);
        spannableString.setSpan(new ForegroundColorSpan(color),
                start1,
                end1+1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(color),
                start2,
                end2+1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(
                new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        // 用户协议
                        toPolicy(context, PolicyActivity.TYPE_USER);
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        ds.setUnderlineText(false);
                    }
                },
                start1,
                end1,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(
                new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        // 隐私政策
                        toPolicy(context, PolicyActivity.TYPE_SECRET);
                    }
                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        ds.setUnderlineText(false);
                    }
                },
                start2,
                end2,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tvContent.setMovementMethod(LinkMovementMethod.getInstance());
        tvContent.setText(spannableString);
    }

    private void toPolicy(Context context, int type) {
        Intent intent = new Intent(context, PolicyActivity.class);
        intent.putExtra(PolicyActivity.KEY_POLICY_TYPE, type);
        context.startActivity(intent);
    }

    @OnClick({R.id.refuse, R.id.agree})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.refuse:
                if (mOnClickListener != null) {
                    mOnClickListener.refuse();
                }
                break;
            case R.id.agree:
                if (mOnClickListener != null) {
                    mOnClickListener.agree();
                }
                break;
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        bind.unbind();
    }
}
