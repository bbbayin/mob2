package com.mob.sms2.fragment;

import android.content.Intent;
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
import com.mob.sms2.R;
import com.mob.sms2.activity.AddContactActivity;
import com.mob.sms2.adapter.ViewPagerAdapter;
import com.mob.sms2.base.BaseFragment;
import com.mob.sms2.rx.ChooseEvent;
import com.mob.sms2.rx.RxBus;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContactsFragment extends BaseFragment {
    @BindView(R.id.slidingTabLayout)
    SlidingTabLayout mSlidingTabLayout;
    @BindView(R.id.viewPager)
    ViewPager mViewPager;
    @BindView(R.id.choose)
    TextView mChoose;

    private String[] mTitles = new String[]{"联系人", "通话记录"};
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private boolean mIsSelect;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView(){
        mFragments.clear();
        mFragments.add(new TxlFragment());
        mFragments.add(new CallRecordsFragment());
        mViewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), mFragments));
        mSlidingTabLayout.setViewPager(mViewPager, mTitles);
    }

    @OnClick({R.id.add, R.id.choose})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.add:
                startActivity(new Intent(getContext(), AddContactActivity.class));
                break;
            case R.id.choose:
                mIsSelect = !mIsSelect;
                RxBus.getInstance().post(new ChooseEvent(mIsSelect?1:0));
                mChoose.setText(mIsSelect?"完成":"选择");
                break;
        }
    }
}
