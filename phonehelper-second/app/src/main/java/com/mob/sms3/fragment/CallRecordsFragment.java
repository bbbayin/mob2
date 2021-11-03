package com.mob.sms3.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms3.R;
import com.mob.sms3.adapter.CallRecordsAdapter;
import com.mob.sms3.base.BaseFragment;
import com.mob.sms3.base.SimpleObserver;
import com.mob.sms3.db.CallRecordsTable;
import com.mob.sms3.db.DatabaseBusiness;
import com.mob.sms3.utils.CallLogBean;
import com.mob.sms3.utils.PhoneUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class CallRecordsFragment extends BaseFragment {
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private CallRecordsAdapter mCallRecordsAdapter;
    private ArrayList<CallRecordsTable> mDatas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call_records, container, false);
        ButterKnife.bind(this, view);
        initView();
        initData();
        return view;
    }

    private void initData() {
        Observable.create(new Observable.OnSubscribe<List<CallLogBean>>() {
                    @Override
                    public void call(Subscriber<? super List<CallLogBean>> subscriber) {
                        try {
                            subscriber.onNext(PhoneUtils.INSTANCE.getCallLog(500, getContext()));
                        }catch (Exception e){
                            System.out.println("报错啦～～～");
                            e.printStackTrace();
                        }
                    }
                })
                .map(new Func1<List<CallLogBean>, List<CallRecordsTable>>() {
                    @Override
                    public List<CallRecordsTable> call(List<CallLogBean> callLogBeans) {
                        if (callLogBeans != null && !callLogBeans.isEmpty()) {
                            for (int i = 0; i < callLogBeans.size(); i++) {
                                CallRecordsTable bean = new CallRecordsTable();
                                CallLogBean mobile = callLogBeans.get(i);
                                bean.mobile = mobile.getNumber();
                                bean.time = mobile.getDate();
                                bean.name = mobile.getName();
                                mDatas.add(bean);
                            }
                            return mDatas;
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<CallRecordsTable>>() {
                    @Override
                    public void onNext(List<CallRecordsTable> list) {
                        if (list != null && !list.isEmpty()) {
                            mCallRecordsAdapter.notifyDataSetChanged();
                        }
                    }
                });

    }

    private void initView() {
        List<CallRecordsTable> list = DatabaseBusiness.getCallRecords();
        if (list != null && list.size() > 0) {
            mDatas.clear();
            mDatas.addAll(list);
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mCallRecordsAdapter = new CallRecordsAdapter(getContext(), mDatas);
        mRecyclerView.setAdapter(mCallRecordsAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        initView();
    }
}
