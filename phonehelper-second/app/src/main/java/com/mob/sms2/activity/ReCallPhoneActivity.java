package com.mob.sms2.activity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms2.R;
import com.mob.sms2.adapter.CallPhoneRecordsAdapter;
import com.mob.sms2.base.BaseActivity;
import com.mob.sms2.bean.CallPhoneRecord;
import com.mob.sms2.rx.CallEvent;
import com.mob.sms2.rx.RxBus;
import com.mob.sms2.utils.SPConstant;
import com.mob.sms2.utils.SPUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class ReCallPhoneActivity extends BaseActivity {
    @BindView(R.id.tip)
    TextView mTip;
    @BindView(R.id.mobile)
    TextView mMobile;
    @BindView(R.id.num)
    TextView mNum;
    @BindView(R.id.time)
    TextView mTime;
    @BindView(R.id.success_num)
    TextView mSuccessNumTv;
    @BindView(R.id.fail_num)
    TextView mFailNumTv;
    @BindView(R.id.pause)
    ImageView mPause;
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.record_ll)
    LinearLayout mRecoreLl;
    @BindView(R.id.pre)
    TextView mPre;
    @BindView(R.id.next)
    TextView mNext;

    private String mType;
    private String mPhone;
    private int mSendNum;//总拨打次数
    private int mSendIndex;//当前拨打序列
    private int mInterval;//拨打间隔
    private int mShowDaojishi;//显示的倒计时时间
    private CallPhoneRecordsAdapter mCallPhoneRecordsAdapter;
    private ArrayList<CallPhoneRecord> mRecords = new ArrayList<>();

    private boolean mPauseState;
    private boolean mSim1Call = true;// 双卡轮流拨打情况使用
    private TelephonyManager mTm;

    //指定SIM卡拨打
    private String[] dualSimTypes = { "subscription", "Subscription",
            "com.android.phone.extra.slot",
            "phone", "com.android.phone.DialingMode",
            "simId", "simnum", "phone_type",
            "simSlot" };

    private Subscription mSub;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_single_task_layout);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
        initView();

        mSub =  RxBus.getInstance().toObserverable(CallEvent.class)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(this::nextCall);
    }

    private void nextCall(CallEvent event) {
        if ("dhbd".equals(mType)) {
            if (mSendIndex + 1 < mSendNum) {
                mShowDaojishi = mInterval;
                mTime.setText("下一次拨打还需要" + mInterval + "s");
                mTm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
                mHandler.removeCallbacksAndMessages(null);
                mHandler.sendEmptyMessageDelayed(0, 1000);
            } else {
                mHandler.removeCallbacksAndMessages(null);
                mTime.setText("下一次拨打还需要0s");
            }
        } else if ("plbd".equals(mType)) {
            if (mSendIndex + 1 < mSendNum) {
                mShowDaojishi = mInterval;
                mTime.setText("拨打下一个号码还需要" + mInterval + "s");
                mTm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
                mHandler.removeCallbacksAndMessages(null);
                mHandler.sendEmptyMessageDelayed(1, 1000);
            } else {
                mHandler.removeCallbacksAndMessages(null);
                mTime.setText("拨打下一个号码还需要0s");
            }
        }
    }

    private void initView(){
        mTm = (TelephonyManager)getSystemService(Service.TELEPHONY_SERVICE);
        mTm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

        mType = getIntent().getStringExtra("type");
        mPhone = getIntent().getStringExtra("phone");
        mSendNum = getIntent().getIntExtra("allNum", 0);
        mInterval = getIntent().getIntExtra("interval", 0);
        if ("dhbd".equals(mType)) {
            mTip.setText("单号拨打电话");
            mShowDaojishi = mInterval;
            mMobile.setText(mPhone);
            mNum.setText("(" + 1 + "/" + mSendNum + "次)");
            callPhone(mPhone);
            if (mSendIndex < mSendNum - 1) {
                mTime.setText("下一次拨打还需要" + mShowDaojishi + "s");
                mHandler.sendEmptyMessageDelayed(0, 1000);
            } else {
                mTime.setText("下一次拨打还需要" + 0 + "s");
            }
        } else if ("plbd".equals(mType)) {
            mRecoreLl.setVisibility(View.VISIBLE);
            mPre.setVisibility(View.VISIBLE);
            mNext.setVisibility(View.VISIBLE);
            mTip.setText("批量拨打电话");

            if (!TextUtils.isEmpty(mPhone)) {
                if (mPhone.contains(",")) {
                    String[] phones = mPhone.split(",");
                    for (String phone : phones) {
                        mRecords.add(new CallPhoneRecord("无姓名", phone, false));
                    }
                } else {
                    mRecords.add(new CallPhoneRecord("无姓名", mPhone, false));
                }
            }

            mCallPhoneRecordsAdapter = new CallPhoneRecordsAdapter(this, mRecords);
            mRecyclerView.setAdapter(mCallPhoneRecordsAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            mShowDaojishi = mInterval;
            mSendNum = mRecords.size();
            mMobile.setText(mRecords.get(mSendIndex).mobile);
            mNum.setText("(" + 1 + "/" + mRecords.size() + "联系人)");
            callPhone(mRecords.get(mSendIndex).mobile);
            if (mSendIndex < mSendNum - 1) {
                mTime.setText("拨打下一个号码还需要" + mShowDaojishi + "s");
                mHandler.sendEmptyMessageDelayed(1, 1000);
            } else {
                mTime.setText("拨打下一个号码还需要" + 0 + "s");
            }
        }
    }

    private void callPhone(String mobile) {
        Log.i("jqt", "mobile: " + mobile);
        try{
            TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + mobile));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                String sksz = SPUtils.getString(SPConstant.SP_CALL_SKSZ, "sim1");
                for (int i = 0; i < dualSimTypes.length; i++) {
                    //1代表卡1,2代表卡2
                    if ("sim1".equals(sksz)) {
                        intent.putExtra(dualSimTypes[i], 1);
                    } else if ("sim2".equals(sksz)) {
                        intent.putExtra(dualSimTypes[i], 2);
                    }  else if ("sim_double".equals(sksz)) {
                        intent.putExtra(dualSimTypes[i], mSim1Call?1:2);
                        mSim1Call = !mSim1Call;
                    }

                }
                startActivity(intent);
            }
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    if (mSendIndex < mSendNum - 1) {
                        mShowDaojishi--;
                        if (mShowDaojishi > 0) {
                            mTime.setText("下一次拨打还需要" + mShowDaojishi + "s");
                            sendEmptyMessageDelayed(0, 1000);
                        } else {
                            mShowDaojishi = mInterval;
                            mSendIndex++;
                            mNum.setText("(" + (mSendIndex + 1) + "/" + mSendNum + "次)");
                            mTime.setText("下一次拨打还需要" + mInterval + "s");
                            mTm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
                            callPhone(mPhone);
                        }
                    } else {
                        mTime.setText("下一次拨打还需要" + 0 + "s");
                    }
                    break;
                case 1:
                    if (mSendIndex < mSendNum - 1) {
                        mShowDaojishi--;
                        if (mShowDaojishi > 0) {
                            mTime.setText("拨打下一个号码还需要" + mShowDaojishi + "s");
                            sendEmptyMessageDelayed(1, 1000);
                        } else {
                            mShowDaojishi = mInterval;
                            mSendIndex++;
                            mMobile.setText(mRecords.get(mSendIndex).mobile);
                            mNum.setText("(" + (mSendIndex + 1) + "/" + mRecords.size() + "联系人)");
                            mTime.setText("拨打下一个号码还需要" + mInterval + "s");
                            mTm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
                            callPhone(mRecords.get(mSendIndex).mobile);
                        }
                    } else {
                        mTime.setText("拨打下一个号码还需要" + 0 + "s");
                    }
                    break;
            }
        }
    };

    @OnClick({R.id.back, R.id.pause, R.id.stop, R.id.pre, R.id.next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.pause:
                mPauseState = !mPauseState;
                if (mPauseState) {
                    mHandler.removeMessages(0);
                } else {
                    mHandler.sendEmptyMessageDelayed(0, 1000);
                }
                break;
            case R.id.stop:
                finish();
                break;
            case R.id.pre:
                if(mSendIndex-1>=0){
                    mShowDaojishi = mInterval;
                    mSendIndex--;
                    mMobile.setText(mRecords.get(mSendIndex).mobile);
                    mNum.setText("(" + (mSendIndex + 1) + "/" + mRecords.size() + "联系人)");
                    mTime.setText("拨打下一个号码还需要" + mInterval + "s");
                    mTm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
                    callPhone(mRecords.get(mSendIndex).mobile);
                    mHandler.sendEmptyMessageDelayed(1, 1000);
                }
                break;
            case R.id.next:
                if(mSendIndex+1<mRecords.size()) {
                    mShowDaojishi = mInterval;
                    mSendIndex++;
                    mMobile.setText(mRecords.get(mSendIndex).mobile);
                    mNum.setText("(" + (mSendIndex + 1) + "/" + mRecords.size() + "联系人)");
                    mTime.setText("拨打下一个号码还需要" + mInterval + "s");
                    mTm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
                    callPhone(mRecords.get(mSendIndex).mobile);
                    mHandler.sendEmptyMessageDelayed(1, 1000);
                }
                break;
        }
    }

    private PhoneStateListener listener=new PhoneStateListener(){

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            //注意，方法必须写在super方法后面，否则incomingNumber无法获取到值。
            super.onCallStateChanged(state, incomingNumber);
            if ("plbd".equals(mType)) {
                mRecords.get(mSendIndex).isSend = true;
                mCallPhoneRecordsAdapter.notifyDataSetChanged();
            }
            mTm.listen(listener,PhoneStateListener.LISTEN_NONE);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        saveRecord();
    }

    private void saveRecord(){
//        int allNum = 0;
//        int type = 1;
//        if("dhbd".equals(mType)){
//            allNum = 1;
//            type = 1;
//        } else if("plbd".equals(mType)){
//            allNum = mRecords.size();
//            type = 2;
//        }
//        RetrofitHelper.getApi().saveRecord(SPUtils.getString(SPConstant.SP_USER_TOKEN, ""),
//                allNum, mPauseState?"0":"1", mSendIndex+1, mPhone, type).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(baseBean -> {
//                }, throwable -> {
//                    throwable.printStackTrace();
//                });
    }
}
