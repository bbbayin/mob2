package com.mob.sms3.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telecom.TelecomManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms3.DialKeyBoard;
import com.mob.sms3.R;
import com.mob.sms3.activity.VipActivity;
import com.mob.sms3.adapter.ContactsAdapter2;
import com.mob.sms3.base.BaseFragment;
import com.mob.sms3.bean.ContactsBean;
import com.mob.sms3.contacts.CharacterParser;
import com.mob.sms3.contacts.PinyinComparator;
import com.mob.sms3.contacts.SideBar;
import com.mob.sms3.contacts.SortModel;
import com.mob.sms3.utils.FreeCheckUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TxlFragment extends BaseFragment {
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.sidebar)
    SideBar mSideBar;

    private ContactsAdapter2 mContactsAdapter;
    private ArrayList<ContactsBean> mDatas = new ArrayList<>();
    private CharacterParser mCharacterParser;
    private PinyinComparator mPinyinComparator;

    private int lastFirstVisibleItem = -1;

    // 号码
    public final static String NUM = ContactsContract.CommonDataKinds.Phone.NUMBER;
    // 联系人姓名
    public final static String NAME = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;

    private static Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_txl, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {
        mCharacterParser = CharacterParser.getInstance();
        mPinyinComparator = new PinyinComparator();

        getContacts();

        List<SortModel> sortModels = filledData(mDatas);
        Collections.sort(sortModels, mPinyinComparator);

        mContactsAdapter = new ContactsAdapter2(getContext(), sortModels);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mContactsAdapter);
        mContactsAdapter.setOnItemClickListener(new ContactsAdapter2.OnItemClickListener() {
            @Override
            public void select(int position) {

            }

            @Override
            public void call(int position) {
                FreeCheckUtils.check(getActivity(), false, new FreeCheckUtils.OnCheckCallback() {
                    @Override
                    public void onResult(boolean free) {
                        if (free) {
                            callPhone(mContactsAdapter.getData().get(position).getMobile());
                        } else {
                            startActivity(new Intent(getContext(), VipActivity.class));
                        }
                    }
                });
            }

            @Override
            public void sms(int position) {
                FreeCheckUtils.check(getActivity(),false, new FreeCheckUtils.OnCheckCallback() {
                    @Override
                    public void onResult(boolean free) {
                        if (free) {
                            Uri uri = Uri.parse("smsto:" + mContactsAdapter.getData().get(position).getMobile()); // 设置操作的路径
                            Intent it = new Intent();
                            it.setAction(Intent.ACTION_SENDTO); // 设置要操作的Action
                            it.setType("vnd.android-dir/mms-sms"); // 短信的MIME类型
                            it.setData(uri);// 要设置的数据
                            startActivity(it); // 执行跳转
                        } else {
                            startActivity(new Intent(getContext(), VipActivity.class));
                        }
                    }
                });
            }
        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                    int firstItemPosition = linearManager.findFirstVisibleItemPosition();

                    int section = mContactsAdapter.getSectionForPosition(firstItemPosition);
//                    if (firstItemPosition != lastFirstVisibleItem) {
//                        mTopTv.setText(String.valueOf((char) section));
//                    }
                    lastFirstVisibleItem = firstItemPosition;
                }
            }
        });

        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                int position = mContactsAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mRecyclerView.scrollToPosition(position);
                }

            }
        });
    }

    private String[] dualSimTypes = {"subscription", "Subscription",
            "com.android.phone.extra.slot",
            "phone", "com.android.phone.DialingMode",
            "simId", "simnum", "phone_type",
            "simSlot"};

    private void callPhone(String mobile) {
        try {
            TelecomManager telecomManager = (TelecomManager) getContext().getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + mobile));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                for (int i = 0; i < dualSimTypes.length; i++) {
                    //1代表卡1,2代表卡2
                    intent.putExtra(dualSimTypes[i], 1);
                }
                startActivity(intent);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void getContacts() {
        try {
            ContentResolver cr = getContext().getContentResolver();
            Cursor cursor = cr.query(phoneUri, new String[]{NUM, NAME}, null, null, null);
            while (cursor.moveToNext()) {
                mDatas.add(new ContactsBean(cursor.getString(cursor.getColumnIndex(NUM)), cursor.getString(cursor.getColumnIndex(NAME))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<SortModel> filledData(ArrayList<ContactsBean> datas) {
        List<SortModel> mSortList = new ArrayList<SortModel>();

        for (int i = 0; i < datas.size(); i++) {
            SortModel sortModel = new SortModel();
            sortModel.setName(datas.get(i).name);
            sortModel.setMobile(datas.get(i).mobile);
            String pinyin = mCharacterParser.getSelling(datas.get(i).name);
            String sortString = pinyin.substring(0, 1).toUpperCase();

            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase());
            } else {
                sortModel.setSortLetters("#");
            }

            mSortList.add(sortModel);
        }
        return mSortList;

    }

    @OnClick({R.id.btn_toggle_dial_keyboard})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_toggle_dial_keyboard) {
            new DialKeyBoard().show(getChildFragmentManager(), "dialkey");
        }
    }
}
