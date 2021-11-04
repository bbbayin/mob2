package com.mob.sms.skin3.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
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

import com.mob.sms.skin3.R;
import com.mob.sms.skin3.adapter.SmsRecordsAdapter;
import com.mob.sms.skin3.base.BaseActivity;
import com.mob.sms.skin3.bean.SendSmsRecord;
import com.mob.sms.skin3.network.RetrofitHelper;
import com.mob.sms.skin3.utils.SPConstant;
import com.mob.sms.skin3.utils.SPUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ReSendSmsActivity extends BaseActivity {
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
    private static PendingIntent sentPI;
    private static String SENT_SMS_ACTION = "SENT_SMS_ACTION";
    private int mSendNum;//总发送次数
    private int mSendIndex;//当前发送序列
    private int mSuccessNum;//成功次数
    private int mFailNum;//失败次数
    private int mInterval;//发送间隔
    private int mShowDaojishi;//显示的倒计时时间
    private SmsRecordsAdapter mSmsRecordsAdapter;
    private ArrayList<SendSmsRecord> mRecords = new ArrayList<>();

    private boolean mPauseState;
    private boolean mSim1Send = true;// 双卡轮流发送情况使用

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_send_sms);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
        initView();
    }

    private void initView(){
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        sentPI = PendingIntent.getBroadcast(this, 0, sentIntent, 0);
        registerReceiver(sendMessage, new IntentFilter(SENT_SMS_ACTION));
        mType = getIntent().getStringExtra("type");
        mPhone = getIntent().getStringExtra("phone");
        mSendNum = getIntent().getIntExtra("allNum", 0);
        mInterval = getIntent().getIntExtra("interval", 0);
        if ("dhfs".equals(mType)) {
            mTip.setText("单号发送短信");
            mMobile.setText(mPhone);
            mShowDaojishi = mInterval;
            mNum.setText("(" + 1 + "/" + mSendNum + "次)");
            sendSms(mPhone, SPUtils.getString(SPConstant.SP_SMS_CONTENT, ""));
            if (mSendIndex < mSendNum - 1) {
                mTime.setText("下一次发送还需要" + mShowDaojishi + "s");
                mHandler.sendEmptyMessageDelayed(0, 1000);
            } else {
                mTime.setText("下一次发送还需要" + 0 + "s");
            }
        } else if ("plfs".equals(mType)) {
            mRecoreLl.setVisibility(View.VISIBLE);
            mPre.setVisibility(View.VISIBLE);
            mNext.setVisibility(View.VISIBLE);
            mTip.setText("批量发送短信");

            if (!TextUtils.isEmpty(mPhone)) {
                if (mPhone.contains(",")) {
                    String[] phones = mPhone.split(",");
                    for (String phone : phones) {
                        mRecords.add(new SendSmsRecord("", phone, false));
                    }
                } else {
                    mRecords.add(new SendSmsRecord("", mPhone, false));
                }
            }

            mSmsRecordsAdapter = new SmsRecordsAdapter(this, mRecords);
            mRecyclerView.setAdapter(mSmsRecordsAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            mInterval = SPUtils.getInt(SPConstant.SP_SMS_FSJG, 0);
            mShowDaojishi = mInterval;
            mSendNum = mRecords.size();
            mMobile.setText(mRecords.get(mSendIndex).mobile);
            mNum.setText("(" + 1 + "/" + mRecords.size() + "联系人)");
            sendSms(mRecords.get(mSendIndex).mobile, SPUtils.getString(SPConstant.SP_SMS_CONTENT, ""));
            if (mSendIndex < mSendNum - 1) {
                mTime.setText("发送下一个号码还需要" + mShowDaojishi + "s");
                mHandler.sendEmptyMessageDelayed(1, 1000);
            } else {
                mTime.setText("发送下一个号码还需要" + 0 + "s");
            }
        }
    }

    private void sendSms(String mobile, String content) {
        Log.i("jqt", "mobile: " + mobile + "," + content);
        try {
            SmsManager manager = SmsManager.getDefault();
            String sksz = SPUtils.getString(SPConstant.SP_SMS_SKSZ, "");
            Class smClass = SmsManager.class;
            //通过反射查到了SmsManager有个叫做mSubId的属性
            Field field = smClass.getDeclaredField("mSubId");
            field.setAccessible(true);
            if ("sim1".equals(sksz)) {
                field.set(manager, 0);//0:默认卡1发送；1：默认卡2发送
            } else if ("sim2".equals(sksz)) {
                field.set(manager, 1);//0:默认卡1发送；1：默认卡2发送
            }  else if ("sim_double".equals(sksz)) {
                field.set(manager, mSim1Send?0:1);//0:默认卡1发送；1：默认卡2发送
                mSim1Send = !mSim1Send;
            }

            manager.sendTextMessage(mobile, null, content, sentPI, null);
        } catch (Exception e) {
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
                            mTime.setText("下一次发送还需要" + mShowDaojishi + "s");
                        } else {
                            mShowDaojishi = mInterval;
                            mSendIndex++;
                            mNum.setText("(" + (mSendIndex + 1) + "/" + mSendNum + "次)");
                            mTime.setText("下一次发送还需要" + mInterval + "s");
                            sendSms(mPhone, SPUtils.getString(SPConstant.SP_SMS_CONTENT, ""));
                        }
                        sendEmptyMessageDelayed(0, 1000);
                    } else {
                        mTime.setText("下一次发送还需要" + 0 + "s");
                    }
                    break;
                case 1:
                    if (mSendIndex < mSendNum - 1) {
                        mShowDaojishi--;
                        if (mShowDaojishi > 0) {
                            mTime.setText("发送下一个号码还需要" + mShowDaojishi + "s");
                        } else {
                            mShowDaojishi = mInterval;
                            mSendIndex++;
                            mMobile.setText(mRecords.get(mSendIndex).mobile);
                            mNum.setText("(" + (mSendIndex + 1) + "/" + mRecords.size() + "联系人)");
                            mTime.setText("发送下一个号码还需要" + mInterval + "s");
                            sendSms(mRecords.get(mSendIndex).mobile, SPUtils.getString(SPConstant.SP_SMS_CONTENT, ""));
                        }
                        sendEmptyMessageDelayed(1, 1000);
                    } else {
                        mTime.setText("发送下一个号码还需要" + 0 + "s");
                    }
                    break;
            }
        }
    };

    private BroadcastReceiver sendMessage = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //判断短信是否发送成功
            Log.i("jqt", "getResultCode(): " + getResultCode());
            if ("plfs".equals(mType)) {
                mRecords.get(mSendIndex).isSend = true;
                mSmsRecordsAdapter.notifyDataSetChanged();
            }
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    //成功
                    mSuccessNum++;
                    mSuccessNumTv.setText(mSuccessNum + "");
                    break;
                default:
                    //失败
                    mFailNum++;
                    mFailNumTv.setText(mFailNum + "");
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
                break;
            case R.id.next:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        saveRecord();
    }

    private void saveRecord(){
        int allNum = 0;
        int type = 3;
        if("dhfs".equals(mType)){
            allNum = 1;
            type = 3;
        } else if("plfs".equals(mType)){
            allNum = mRecords.size();
            type = 4;
        }
        RetrofitHelper.getApi().saveRecord(allNum, mPauseState?"0":"1", mSendIndex+1, mPhone, type).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseBean -> {
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }
}
