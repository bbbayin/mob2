package com.mob.sms.skin3.activity;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.mob.sms.skin3.R;
import com.mob.sms.skin3.base.BaseActivity;
import com.mob.sms.skin3.network.RetrofitHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AgreementActivity extends BaseActivity {
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.content)
    TextView mContent;

    private int mType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));

        mTitle.setText(getIntent().getStringExtra("title"));
        mType = getIntent().getIntExtra("type", 1);
        getRule();
    }

    private void getRule(){
        RetrofitHelper.getApi().getRule(mType).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ruleBean -> {
                    if (ruleBean != null && ruleBean.code == 200 && ruleBean.data!=null) {
                        mContent.setText(Html.fromHtml(ruleBean.data.content));
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }

    @OnClick({R.id.back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
        }
    }
}
