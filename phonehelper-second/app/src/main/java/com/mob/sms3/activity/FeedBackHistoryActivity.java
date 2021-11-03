package com.mob.sms3.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mob.sms3.R;
import com.mob.sms3.adapter.FeedBackHistoryAdapter;
import com.mob.sms3.base.BaseActivity;
import com.mob.sms3.network.RetrofitHelper;
import com.mob.sms3.network.bean.HistoryFeedBackBean;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class FeedBackHistoryActivity extends BaseActivity {
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private int mPage = 1;
    private int mPageSize = 20;
    private ArrayList<HistoryFeedBackBean.DataBean.RowsBean> mDatas = new ArrayList<>();
    private boolean mHasMore;
    private FeedBackHistoryAdapter mFeedBackHistoryAdapter;

    private boolean isSlidingUpward = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_history);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));

        mFeedBackHistoryAdapter = new FeedBackHistoryAdapter(this, mDatas);
        mRecyclerView.setAdapter(mFeedBackHistoryAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mFeedBackHistoryAdapter.setOnItemClickLsitener(new FeedBackHistoryAdapter.OnItemClickListener() {
            @Override
            public void onclick(int position) {
                Intent intent = new Intent(FeedBackHistoryActivity.this, FeedBackDetailActivity.class);
                intent.putExtra("id", mDatas.get(position).id);
                startActivity(intent);
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPage = 1;
                getData();
            }
        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                // 当不滑动时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //获取最后一个完全显示的itemPosition
                    int lastItemPosition = manager.findLastCompletelyVisibleItemPosition();
                    int itemCount = manager.getItemCount();

                    // 判断是否滑动到了最后一个item，并且是向上滑动
                    if (lastItemPosition == (itemCount - 1) && isSlidingUpward) {
                        //加载更多
                        onLoadMore();
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                isSlidingUpward = dy > 0;
            }
        });
        getData();
    }

    private void onLoadMore() {
        if (mHasMore) {
            mPage++;
            getData();
        }
    }

    private void getData(){
        RetrofitHelper.getApi().getFeedback(mPage, mPageSize).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseBean -> {
                    if (baseBean != null && baseBean.code == 200) {
                        if (mPage == 1) {
                            mDatas.clear();
                        }
                        mDatas.addAll(baseBean.data.rows);
                        if (mDatas.size() < baseBean.data.total) {
                            mHasMore = true;
                        } else {
                            mHasMore = false;
                        }
                        mSwipeRefreshLayout.setRefreshing(false);
                        mFeedBackHistoryAdapter.notifyDataSetChanged();
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
