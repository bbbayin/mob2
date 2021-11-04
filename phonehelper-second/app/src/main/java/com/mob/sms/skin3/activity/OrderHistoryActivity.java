package com.mob.sms.skin3.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms.skin3.R;
import com.mob.sms.skin3.adapter.OrderHistoryAdapter;
import com.mob.sms.skin3.base.BaseActivity;
import com.mob.sms.skin3.network.RetrofitHelper;
import com.mob.sms.skin3.network.bean.OrderHistoryBean;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class OrderHistoryActivity extends BaseActivity {
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private OrderHistoryAdapter mOrderHistoryAdapter;
    private ArrayList<OrderHistoryBean.DataBean> mDatas = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));

        mOrderHistoryAdapter = new OrderHistoryAdapter(this, mDatas);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mOrderHistoryAdapter);
        getData();
    }

    private void getData() {
        RetrofitHelper.getApi().getOrderHistory().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(orderHistoryBean -> {
                    if (orderHistoryBean != null && orderHistoryBean.code == 200 && orderHistoryBean.data != null) {
                        mDatas.addAll(orderHistoryBean.data);
                        mOrderHistoryAdapter.notifyDataSetChanged();
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
