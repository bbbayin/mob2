package com.mob.sms2.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms2.R;
import com.mob.sms2.adapter.QuestionAdapter;
import com.mob.sms2.base.BaseActivity;
import com.mob.sms2.network.RetrofitHelper;
import com.mob.sms2.network.bean.QuestionBean;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class QuestionActivity extends BaseActivity {
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private QuestionAdapter mQuestionAdapter;
    private ArrayList<QuestionBean.DataBean> mDatas = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));

        mQuestionAdapter = new QuestionAdapter(this, mDatas);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mQuestionAdapter);
        mQuestionAdapter.setOnItemClickLsitener(new QuestionAdapter.OnItemClickListener() {
            @Override
            public void onclick(int position) {
                Intent intent = new Intent(QuestionActivity.this, QuestionDetailActivity.class);
                intent.putExtra("id", mDatas.get(position).id);
                startActivity(intent);
            }
        });

        getData();
    }

    private void getData() {
        RetrofitHelper.getApi().getQuestion().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(questionBean -> {
                    if (questionBean != null && questionBean.code == 200 && questionBean.data != null) {
                        mDatas.addAll(questionBean.data);
                        mQuestionAdapter.notifyDataSetChanged();
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
