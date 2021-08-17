package com.mob.sms2.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telecom.TelecomManager;
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

import com.mob.sms2.DialKeyBoard;
import com.mob.sms2.R;
import com.mob.sms2.activity.VipActivity;
import com.mob.sms2.adapter.ContactsPersonAdapter;
import com.mob.sms2.base.BaseFragment;
import com.mob.sms2.contacts.SideBar;
import com.mob.sms2.network.RetrofitHelper;
import com.mob.sms2.network.bean.OnlineContactBean;
import com.mob.sms2.rx.ChooseEvent;
import com.mob.sms2.rx.ContactEvent;
import com.mob.sms2.rx.RxBus;
import com.mob.sms2.utils.FreeCheckUtils;
import com.mob.sms2.utils.ToastUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ContactsPersonFragment extends BaseFragment {
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.bottom_rl)
    RelativeLayout mBottomRl;
    @BindView(R.id.select_iv)
    ImageView mSelectIv;
    @BindView(R.id.delete)
    TextView mDelete;
    @BindView(R.id.fragment_contacts_sidebar)
    SideBar mSideBar;


    private ContactsPersonAdapter mContactsPersonAdapter;
    private ArrayList<OnlineContactBean.DataBean> mDatas = new ArrayList<>();

    // 号码
    public final static String NUM = ContactsContract.CommonDataKinds.Phone.NUMBER;
    // 联系人姓名
    public final static String NAME = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;

    private static Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

    private Subscription mSub;
    private Subscription mSub1;
    private boolean mAllSelect;
    private int mSelectNum;
    private boolean isCreated = false;

    private String[] dualSimTypes = {"subscription", "Subscription",
            "com.android.phone.extra.slot",
            "phone", "com.android.phone.DialingMode",
            "simId", "simnum", "phone_type",
            "simSlot"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts_person, container, false);
        ButterKnife.bind(this, view);
        initView();
        getContacts();
        return view;
    }

    private void initView() {
        mContactsPersonAdapter = new ContactsPersonAdapter(getContext(), mDatas);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mContactsPersonAdapter);
        mContactsPersonAdapter.setOnItemClickLsitener(new ContactsPersonAdapter.OnItemClickListener() {
            @Override
            public void select(int position) {
                if (mContactsPersonAdapter.getType() == 1) {
                    mDatas.get(position).isSelect = !mDatas.get(position).isSelect;
                    if (mDatas.get(position).isSelect) {
                        mSelectNum++;
                    } else {
                        mSelectNum--;
                    }
                    mDelete.setText("删除(" + mSelectNum + ")");
                    mContactsPersonAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void call(int position) {
                FreeCheckUtils.check(getActivity(),false, new FreeCheckUtils.OnCheckCallback() {
                    @Override
                    public void onResult(boolean free) {
                        if (free) {
                            callPhone(mDatas.get(position).tel);
//                            CallRecordsTable callRecordsTable = new CallRecordsTable(mDatas.get(position).name,
//                                    mDatas.get(position).tel, System.currentTimeMillis());
//                            DatabaseBusiness.createCallRecord(callRecordsTable);
                        }else {
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
                            Uri uri = Uri.parse("smsto:" + mDatas.get(position).tel); // 设置操作的路径
                            Intent it = new Intent();
                            it.setAction(Intent.ACTION_SENDTO) ; // 设置要操作的Action
                            it.setType("vnd.android-dir/mms-sms") ; // 短信的MIME类型
                            it.setData(uri) ;// 要设置的数据
                            startActivity(it) ; // 执行跳转
                        }
                    }
                });
            }
        });
        mSub = RxBus.getInstance().toObserverable(ContactEvent.class)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(this::refresh);
        mSub1 = RxBus.getInstance().toObserverable(ChooseEvent.class)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(this::choose);
    }

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

    private void refresh(ContactEvent event) {
        getContacts();
    }

    private void choose(ChooseEvent event) {
        if (event.type == 0) {
            mBottomRl.setVisibility(View.GONE);
            mContactsPersonAdapter.setType(0);
            for (OnlineContactBean.DataBean bean : mDatas) {
                bean.isSelect = false;
            }
            mContactsPersonAdapter.notifyDataSetChanged();
        } else if (event.type == 1) {
            mBottomRl.setVisibility(View.VISIBLE);
            mContactsPersonAdapter.setType(1);
            mContactsPersonAdapter.notifyDataSetChanged();
        }
    }

    @OnClick({R.id.select_iv, R.id.delete, R.id.btn_toggle_dial_keyboard})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.select_iv:
                mAllSelect = !mAllSelect;
                mSelectIv.setImageResource(mAllSelect ? R.mipmap.selected_icon : R.mipmap.unselected_icon);
                for (OnlineContactBean.DataBean bean : mDatas) {
                    bean.isSelect = mAllSelect;
                }
                mContactsPersonAdapter.notifyDataSetChanged();
                if (mAllSelect) {
                    mSelectNum = mDatas.size();
                } else {
                    mSelectNum = 0;
                }
                mDelete.setText("删除(" + mSelectNum + ")");
                break;
            case R.id.delete:
                if (mSelectNum == 0) {
                    ToastUtil.show("请选择要删除的联系人");
                } else {
                    String ids = "";
                    for (OnlineContactBean.DataBean bean : mDatas) {
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
            case R.id.btn_toggle_dial_keyboard:
                new DialKeyBoard().show(getChildFragmentManager(), "dialkey");
                break;
        }
    }

    private void delete(String ids) {
        RetrofitHelper.getApi().deleteContacts(ids).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseBean -> {
                    if (baseBean != null && baseBean.code == 200) {
                        getContacts();
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }


    private void getContacts() {
//        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
//        progressDialog.setTitle("加载中...");
//        progressDialog.show();
        Observable.just("")
                .doOnNext(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        ContentResolver cr = getContext().getContentResolver();
                        Cursor cursor = cr.query(phoneUri, new String[]{NUM, NAME}, null, null, null);
                        while (cursor.moveToNext()) {
                            OnlineContactBean.DataBean dataBean = new OnlineContactBean.DataBean();
                            dataBean.name = cursor.getString(cursor.getColumnIndex(NAME));
                            dataBean.tel = cursor.getString(cursor.getColumnIndex(NUM));
                            mDatas.add(dataBean);
                        }
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
//                        if (progressDialog != null && progressDialog.isShowing()) {
//                            progressDialog.dismiss();
//                        }
                        mContactsPersonAdapter.notifyDataSetChanged();
                    }
                });

//        RetrofitHelper.getApi().getContacts().subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(onlineContactBean -> {
//                    if (onlineContactBean != null && onlineContactBean.code == 200) {
//                        mDatas.clear();
//                        mDatas.addAll(onlineContactBean.data);
//                        mContactsPersonAdapter.notifyDataSetChanged();
//                    }
//                }, throwable -> {
//                    throwable.printStackTrace();
//                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSub != null && !mSub.isUnsubscribed()) {
            mSub.unsubscribe();
        }
        if (mSub1 != null && !mSub1.isUnsubscribed()) {
            mSub1.unsubscribe();
        }
    }
}
