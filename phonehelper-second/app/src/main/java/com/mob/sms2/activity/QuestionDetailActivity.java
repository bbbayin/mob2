package com.mob.sms2.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.mob.sms2.R;
import com.mob.sms2.base.BaseActivity;
import com.mob.sms2.network.RetrofitHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class QuestionDetailActivity extends BaseActivity {
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.answer)
    TextView mAnswer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));

        getData(getIntent().getIntExtra("id", 0));
    }

    private void getData(int id) {
        RetrofitHelper.getApi().getQuestionDetail(id).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(questionDetailBean -> {
                    if (questionDetailBean != null && questionDetailBean.code == 200) {
                        mTitle.setText(questionDetailBean.data.title);
                        mAnswer.setText(questionDetailBean.data.answer);
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
