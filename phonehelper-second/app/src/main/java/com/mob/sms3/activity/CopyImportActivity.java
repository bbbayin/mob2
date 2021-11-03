package com.mob.sms3.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.mob.sms3.R;
import com.mob.sms3.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CopyImportActivity extends BaseActivity {
    @BindView(R.id.content_et)
    EditText mContentEt;
    @BindView(R.id.import_tv)
    TextView mImportTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_copy_import);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
        initView();

        mContentEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mImportTv.setBackgroundResource(mContentEt.getText().toString().length() > 0 ?
                        R.drawable.round_36_green : R.drawable.round_36_grey2);
            }
        });
    }

    private void initView(){
    }

    @OnClick({R.id.back, R.id.import_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.import_tv:
                if (!TextUtils.isEmpty(mContentEt.getText().toString())) {
                    Intent intent = new Intent(CopyImportActivity.this, PhoneInfoActivity.class);
                    intent.putExtra("type", 3);
                    intent.putExtra("title", "电话");
                    intent.putExtra("type2", getIntent().getStringExtra("type"));
                    intent.putExtra("copyContent", mContentEt.getText().toString());
                    startActivity(intent);
                    finish();
                }
                break;
        }
    }
}
