package com.mob.sms2.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mob.sms2.R;
import com.mob.sms2.activity.ReSendSmsActivity;
import com.mob.sms2.adapter.DxjlAdapter;
import com.mob.sms2.base.BaseFragment;
import com.mob.sms2.network.RetrofitHelper;
import com.mob.sms2.network.bean.SmsRecordBean;
import com.mob.sms2.rx.ChooseRecordEvent;
import com.mob.sms2.rx.RxBus;
import com.mob.sms2.utils.ToastUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DxjlFragment extends BaseFragment {
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.bottom_rl)
    RelativeLayout mBottomRl;
    @BindView(R.id.select_iv)
    ImageView mSelectIv;
    @BindView(R.id.delete)
    TextView mDelete;

    private int mPage = 1;
    private int mPageSize = 20;
    private ArrayList<SmsRecordBean.DataBean.RowsBean> mDatas = new ArrayList<>();
    private boolean mHasMore;
    private DxjlAdapter mDxjlAdapter;

    private boolean isSlidingUpward = false;
    private Subscription mSub1;
    private boolean mAllSelect;
    private int mSelectNum;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jl, container, false);
        ButterKnife.bind(this, view);
        initView();
        mSub1 =  RxBus.getInstance().toObserverable(ChooseRecordEvent.class)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(this::choose);
        return view;
    }

    private void initView(){
        mDxjlAdapter = new DxjlAdapter(getContext(), mDatas);
        mRecyclerView.setAdapter(mDxjlAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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

        mDxjlAdapter.setOnItemClickLsitener(new DxjlAdapter.OnItemClickListener() {
            @Override
            public void select(int position) {
                if (mDxjlAdapter.getType() == 1) {
                    mDatas.get(position).isSelect = !mDatas.get(position).isSelect;
                    if (mDatas.get(position).isSelect) {
                        mSelectNum++;
                    } else {
                        mSelectNum--;
                    }
                    mDelete.setText("删除(" + mSelectNum + ")");
                    mDxjlAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void jump(int position) {
                Intent intent = new Intent(getContext(), ReSendSmsActivity.class);
                if ("0".equals(mDatas.get(position).batchSend)) {
                    intent.putExtra("type", "dhfs");
                } else  if ("1".equals(mDatas.get(position).batchSend)) {
                    intent.putExtra("type", "plfs");
                }
                intent.putExtra("phone", mDatas.get(position).tels);
                intent.putExtra("allNum", mDatas.get(position).allNum);
                intent.putExtra("interval", 15);
                startActivity(intent);
            }
        });
        getData();
    }

    private void choose(ChooseRecordEvent event) {
        if (event.type == 0) {
            mBottomRl.setVisibility(View.GONE);
            mDxjlAdapter.setType(0);
            for (SmsRecordBean.DataBean.RowsBean bean : mDatas) {
                bean.isSelect = false;
            }
            mDxjlAdapter.notifyDataSetChanged();
        } else if (event.type == 1) {
            mBottomRl.setVisibility(View.VISIBLE);
            mDxjlAdapter.setType(1);
            mDxjlAdapter.notifyDataSetChanged();
        }
    }

    private void onLoadMore() {
        if (mHasMore) {
            mPage++;
            getData();
        }
    }

    private void getData(){
        RetrofitHelper.getApi().getSmsRecords(mPage, mPageSize).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(smsRecordBean -> {
                    if (smsRecordBean != null && smsRecordBean.code == 200) {
                        if (mPage == 1) {
                            mDatas.clear();
                        }
                        mDatas.addAll(smsRecordBean.data.rows);
                        if (mDatas.size() < smsRecordBean.data.total) {
                            mHasMore = true;
                        } else {
                            mHasMore = false;
                        }
                        mSwipeRefreshLayout.setRefreshing(false);
                        mDxjlAdapter.notifyDataSetChanged();
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }

    @OnClick({R.id.select_iv, R.id.delete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.select_iv:
                mAllSelect = !mAllSelect;
                mSelectIv.setImageResource(mAllSelect ? R.mipmap.selected_icon : R.mipmap.unselected_icon);
                for (SmsRecordBean.DataBean.RowsBean bean : mDatas) {
                    bean.isSelect = mAllSelect;
                }
                mDxjlAdapter.notifyDataSetChanged();
                if (mAllSelect) {
                    mSelectNum = mDatas.size();
                } else {
                    mSelectNum = 0;
                }
                mDelete.setText("删除(" + mSelectNum + ")");
                break;
            case R.id.delete:
                if (mSelectNum == 0) {
                    ToastUtil.show("请选择要删除的记录");
                } else {
                    String ids = "";
                    for (SmsRecordBean.DataBean.RowsBean bean : mDatas) {
                        if (bean.isSelect) {
                            if (TextUtils.isEmpty(ids)) {
                                ids = bean.id + "";
                            } else {
                                ids = ids + "," + bean.id;
                            }
                        }
                    }
                    delete(ids);
                }
                break;
        }
    }

    private void delete(String ids){
        RetrofitHelper.getApi().deleteSmsRecord(ids).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseBean -> {
                    if (baseBean != null && baseBean.code == 200) {
                        getData();
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSub1 != null && !mSub1.isUnsubscribed()) {
            mSub1.unsubscribe();
        }
    }
}
