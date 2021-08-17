package com.mob.sms2.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.mob.sms2.R;
import com.mob.sms2.base.BaseActivity;
import com.mob.sms2.utils.SPConstant;
import com.mob.sms2.utils.SPUtils;
import com.mob.sms2.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditSmsActivity extends BaseActivity {
    @BindView(R.id.content)
    EditText mContentEt;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sms);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
        mContentEt.setText(SPUtils.getString(SPConstant.SP_SMS_CONTENT, ""));
    }

    @OnClick({R.id.back, R.id.confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.confirm:
                if (TextUtils.isEmpty(mContentEt.getText().toString())) {
                    ToastUtil.show("请输入内容");
                } else {
                    SPUtils.put(SPConstant.SP_SMS_CONTENT, mContentEt.getText().toString());
                    finish();
                }
                break;
        }
    }
}
