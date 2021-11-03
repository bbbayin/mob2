package com.mob.sms3.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.flyco.tablayout.SlidingTabLayout;
import com.mob.sms3.R;
import com.mob.sms3.adapter.ViewPagerAdapter;
import com.mob.sms3.base.BaseFragment;
import com.mob.sms3.rx.ChooseRecordEvent;
import com.mob.sms3.rx.RxBus;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RecordFragment extends BaseFragment {
    @BindView(R.id.slidingTabLayout)
    SlidingTabLayout mSlidingTabLayout;
    @BindView(R.id.viewPager)
    ViewPager mViewPager;
    @BindView(R.id.choose)
    TextView mChooseTv;

    private String[] mTitles = new String[]{"拨号记录", "群拨记录", "短信记录"};
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private boolean mIsSelect;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView(){
        mFragments.clear();
        mFragments.add(new BhjlFragment());
        mFragments.add(new QbjlFragment());
        mFragments.add(new DxjlFragment());
        mViewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), mFragments));
        mSlidingTabLayout.setViewPager(mViewPager, mTitles);
        mViewPager.setOffscreenPageLimit(2);
    }

    @OnClick({R.id.choose})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.choose:
                mIsSelect = !mIsSelect;
                RxBus.getInstance().post(new ChooseRecordEvent(mIsSelect?1:0));
                mChooseTv.setText(mIsSelect?"完成":"选择");
                break;
        }
    }
}
