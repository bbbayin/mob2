package com.mob.sms3.activity;

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
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms3.R;
import com.mob.sms3.adapter.SmsRecordsAdapter;
import com.mob.sms3.base.BaseActivity;
import com.mob.sms3.bean.SendSmsRecord;
import com.mob.sms3.db.DatabaseBusiness;
import com.mob.sms3.db.SmsContactTable;
import com.mob.sms3.network.RetrofitHelper;
import com.mob.sms3.utils.SPConstant;
import com.mob.sms3.utils.SPUtils;
import com.mob.sms3.utils.ToastUtil;
import com.mob.sms3.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AutoSendSmsActivity extends BaseActivity {
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
    private static PendingIntent sentPI;
    private static String SENT_SMS_ACTION = "SENT_SMS_ACTION";
    private int mSendNum;//总发送次数
    private int mSendIndex = 0;//当前发送序列
    private int mSuccessNum;//成功次数
    private int mFailNum;//失败次数
    private String mDsfsTime;//定时发送
    private int mInterval;//发送间隔
    private int mCountdownTime;//显示的倒计时时间
    private ArrayList<SmsContactTable> mDatas = new ArrayList<>();//批量发送联系人
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private <T> T get(List<T> list, int index) {
        if (list != null) {
            if (index >= 0) {
                if (list.size() > index) {
                    return list.get(index);
                }
            }
        }
        return null;
    }

    private void initView() {
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        sentPI = PendingIntent.getBroadcast(this, 0, sentIntent, 0);
        registerReceiver(sendMessageReceiver, new IntentFilter(SENT_SMS_ACTION));
        mType = getIntent().getStringExtra("type");
        mInterval = SPUtils.getInt(SPConstant.SP_SMS_FSJG, 0);
        if ("dhfs".equals(mType)) {
            mTip.setText("单号发送短信");
            mMobile.setText(SPUtils.getString(SPConstant.SP_SMS_SRHM, ""));
            mSendNum = SPUtils.getInt(SPConstant.SP_SMS_FSCS, 1);
            mDsfsTime = SPUtils.getString(SPConstant.SP_SMS_DSFS, "");

            if (mSendIndex < mSendNum) {
                if (TextUtils.isEmpty(mDsfsTime)) {
                    mNum.setText("(" + 1 + "/" + mSendNum + "次)");
                    mSendIndex++;
                    sendSms(SPUtils.getString(SPConstant.SP_SMS_SRHM, ""), SPUtils.getString(SPConstant.SP_SMS_CONTENT, ""));
                    mCountdownTime = mInterval;
                    mTime.setText("下一次发送还需要" + mCountdownTime + "s");
                    //无定时发送
                    mHandler.sendEmptyMessageDelayed(SINGLE_SMS, 1000);
                } else if (mDsfsTime.contains("秒")) {
                    mNum.setText("(1/" + mSendNum + "次)");
                    Log.i("jqt", "mDsfsTime: " + mDsfsTime);
                    int hour = Integer.parseInt(mDsfsTime.split("时")[0]);
                    int min = Integer.parseInt(mDsfsTime.split("时")[1].split("分")[0]);
                    int sec = Integer.parseInt(mDsfsTime.split("时")[1].split("分")[1].split("秒")[0]);

                    long time = ((hour * 60 + min) * 60 + sec) * 1000;
                    mCountdownTime = (int) (time / 1000);
                    mTime.setText("下一次发送还需要" + mCountdownTime + "s");
                    mHandler.sendEmptyMessageDelayed(SINGLE_SMS, 1000);
                } else {
                    mNum.setText("(1/" + mSendNum + "次)");
                    if (mDsfsTime.contains("上午")) {
                        String date = mDsfsTime.split("上午")[0];
                        int hour = Integer.parseInt(mDsfsTime.split("上午")[1].split("时")[0]);
                        int min = Integer.parseInt(mDsfsTime.split("上午")[1].split("时")[1].split("分")[0]);
                        String sendDate = Utils.getYear() + "-" + date + " " + hour + ":" + min + ":" + "00";

                        long time = Utils.getTime(sendDate);
                        mCountdownTime = (int) (time / 1000);
                        mTime.setText("下一次发送还需要" + mCountdownTime + "s");
                        mHandler.sendEmptyMessageDelayed(SINGLE_SMS, 1000);
                    } else if (mDsfsTime.contains("下午")) {
                        String date = mDsfsTime.split("下午")[0];
                        int hour = Integer.parseInt(mDsfsTime.split("下午")[1].split("时")[0]);
                        int min = Integer.parseInt(mDsfsTime.split("下午")[1].split("时")[1].split("分")[0]);
                        String sendDate = Utils.getYear() + "-" + date + " " + (hour + 12) + ":" + min + ":" + "00";

                        long time = Utils.getTime(sendDate);
                        mCountdownTime = (int) (time / 1000);
                        mTime.setText("下一次发送还需要" + mCountdownTime + "s");
                        mHandler.sendEmptyMessageDelayed(SINGLE_SMS, 1000);
                    }
                }
            } else {
                mTime.setText("下一次发送还需要" + 0 + "s");
            }
        } else if ("plfs".equals(mType)) {
            mRecoreLl.setVisibility(View.VISIBLE);
            mPre.setVisibility(View.VISIBLE);
            mNext.setVisibility(View.VISIBLE);
            mTip.setText("批量发送短信");
            mDatas.addAll(DatabaseBusiness.getSmsContacts());

            for (SmsContactTable smsContactTable : mDatas) {
                mRecords.add(new SendSmsRecord(smsContactTable.name, smsContactTable.mobile, false));
            }
            mSmsRecordsAdapter = new SmsRecordsAdapter(this, mRecords);
            mRecyclerView.setAdapter(mSmsRecordsAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


            mCountdownTime = mInterval;
            mSendNum = mDatas.size();
            mMobile.setText(mDatas.get(mSendIndex).mobile);
            mNum.setText("(" + 1 + "/" + mDatas.size() + "联系人)");
//            sendSms(mDatas.get(mSendIndex).mobile, SPUtils.getString(SPConstant.SP_SMS_CONTENT, ""));

            if (mSendIndex < mSendNum) {
                mTime.setText("发送下一个号码还需要" + mCountdownTime + "s");
                mHandler.sendEmptyMessageDelayed(MULTI_SMS, 1000);
            } else {
                mTime.setText("发送下一个号码还需要" + 0 + "s");
            }
        }
    }

    private void sendSms(String mobile, String content) {
        Log.i("jqt", "mobile: " + mobile + "," + content);
        try {
            String sksz = SPUtils.getString(SPConstant.SP_SMS_SKSZ, "sim1");
            Log.i("jqt", "sksz: " + sksz);
            SubscriptionInfo sInfo = null;
            final SubscriptionManager sManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            List<SubscriptionInfo> list = sManager.getActiveSubscriptionInfoList();
            if (list.size() == 2) {// double card
                if ("sim1".equals(sksz)) {
                    sInfo = list.get(0);
                } else if ("sim2".equals(sksz)) {
                    sInfo = list.get(1);
                } else {
                    sInfo = list.get(mSim1Send ? 0 : 1);
                    mSim1Send = !mSim1Send;
                }
            } else {//single card
                sInfo = list.get(0);
            }
            int subId = sInfo.getSubscriptionId();
            SmsManager manager = SmsManager
                    .getSmsManagerForSubscriptionId(subId);
            manager.sendTextMessage(mobile, null, content, sentPI, null);
            // test
//            ToastUtil.show("发送成功tst");
//            mHandler.sendEmptyMessageDelayed(MULTI_SMS, 1000);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private final int SINGLE_SMS = 0;
    private final int MULTI_SMS = 1;

    private Handler mHandler =
            new Handler() {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case SINGLE_SMS:
                            if (mSendIndex < mSendNum) {
                                mCountdownTime--;
                                if (mCountdownTime > 0) {
                                    mTime.setText("下一次发送还需要" + mCountdownTime + "s");
                                    sendEmptyMessageDelayed(SINGLE_SMS, 1000);
                                } else {
                                    mCountdownTime = mInterval;
                                    mNum.setText("(" + (mSendIndex+1) + "/" + mSendNum + "次)");
                                    mTime.setText("下一次发送还需要" + mInterval + "s");
                                    sendSms(SPUtils.getString(SPConstant.SP_SMS_SRHM, ""), SPUtils.getString(SPConstant.SP_SMS_CONTENT, ""));
                                }
                            } else {
                                mTime.setText("下一次发送还需要" + 0 + "s");
                            }
                            break;
                        case MULTI_SMS:
                            if (mSendIndex < mSendNum) {
                                mCountdownTime--;
                                if (mCountdownTime > 0) {
                                    mTime.setText("发送下一个号码还需要" + mCountdownTime + "s");
                                    sendEmptyMessageDelayed(MULTI_SMS, 1000);
                                } else {
                                    mCountdownTime = mInterval;
                                    SmsContactTable smsContactTable = get(mDatas, mSendIndex);
                                    if (smsContactTable != null) {
                                        mMobile.setText(smsContactTable.mobile);
                                        mNum.setText("(" + (mSendIndex+1) + "/" + mDatas.size() + "联系人)");
                                        mTime.setText("发送下一个号码还需要" + mInterval + "s");
                                        sendSms(smsContactTable.mobile, SPUtils.getString(SPConstant.SP_SMS_CONTENT, ""));
                                    }
                                }
                            } else {
                                mTime.setText("发送下一个号码还需要" + 0 + "s");
                            }
                            break;
                    }
                }
            };

    private BroadcastReceiver sendMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //判断短信是否发送成功
            Log.i("jqt", "getResultCode(): " + getResultCode());
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    ToastUtil.show("发送短信成功！");
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
            // 开始下个计时
            int what = SINGLE_SMS;
            if ("plfs".equals(mType)) {
                if (mSendIndex >= mRecords.size()) {
                    return;
                }
                mRecords.get(mSendIndex).isSend = true;
                mSmsRecordsAdapter.notifyDataSetChanged();
                what = MULTI_SMS;
            }
            mSendIndex++;
            mCountdownTime = mInterval;
            mHandler.sendEmptyMessageDelayed(what, 1000);
        }
    };

    @OnClick({R.id.back, R.id.pause, R.id.stop, R.id.pre, R.id.next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
            case R.id.stop:
                finish();
                break;
            case R.id.pause:
                mPauseState = !mPauseState;
                if (mPauseState) {
                    mHandler.removeMessages(0);
                } else {
                    mHandler.sendEmptyMessageDelayed(SINGLE_SMS, 1000);
                }
                break;
            case R.id.pre:
                if (mSendIndex - 1 >= 0) {
                    mHandler.removeCallbacksAndMessages(null);
                    mCountdownTime = mInterval;
                    mSendIndex--;
                    mMobile.setText(mDatas.get(mSendIndex).mobile);
                    mNum.setText("(" + (mSendIndex+1) + "/" + mDatas.size() + "联系人)");
                    mTime.setText("发送下一个号码还需要" + mInterval + "s");
                    sendSms(mDatas.get(mSendIndex).mobile, SPUtils.getString(SPConstant.SP_SMS_CONTENT, ""));
                    mHandler.sendEmptyMessageDelayed(MULTI_SMS, 1000);
                }
                break;
            case R.id.next:
                if (mSendIndex + 1 < mDatas.size()) {
                    mHandler.removeCallbacksAndMessages(null);
                    mCountdownTime = mInterval;
                    mSendIndex++;
                    mMobile.setText(mDatas.get(mSendIndex).mobile);
                    mNum.setText("(" + (mSendIndex+1) + "/" + mDatas.size() + "联系人)");
                    mTime.setText("发送下一个号码还需要" + mInterval + "s");
                    sendSms(mDatas.get(mSendIndex).mobile, SPUtils.getString(SPConstant.SP_SMS_CONTENT, ""));
                    mHandler.sendEmptyMessageDelayed(MULTI_SMS, 1000);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        saveRecord();
    }

    private void saveRecord() {
        int allNum = 0;
        String tel = "";
        if ("dhfs".equals(mType)) {
            allNum = 1;
            tel = SPUtils.getString(SPConstant.SP_SMS_SRHM, "");
        } else if ("plfs".equals(mType)) {
            allNum = mDatas.size();
            for (SmsContactTable smsContactTable : mDatas) {
                if (TextUtils.isEmpty(tel)) {
                    tel = smsContactTable.mobile;
                } else {
                    tel = tel + "," + smsContactTable.mobile;
                }
            }
        }
        String status;
        if (mSendIndex == 0) {
            status = "-1";
        } else if (mSendIndex < mSendNum) {
            status = "0";
        } else {
            status = "1";
        }

        String time = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss").format(new Date(System.currentTimeMillis()));
        if ("dhfs".equals(mType)) {
            RetrofitHelper.getApi().saveSmsRecord(allNum, "plfs".equals(mType) ? "1" : "0", SPUtils.getString(SPConstant.SP_SMS_CONTENT, ""), time,
                    "dhfs".equals(mType) ? "1" : "0", mSendNum, status,
                    mSuccessNum, tel, mDsfsTime).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(baseBean -> {
                    }, throwable -> {
                        throwable.printStackTrace();
                    });
        } else if ("plfs".equals(mType)) {
            RetrofitHelper.getApi().saveSmsRecord(allNum, "plfs".equals(mType) ? "1" : "0", SPUtils.getString(SPConstant.SP_SMS_CONTENT, ""), time,
                    "dhfs".equals(mType) ? "1" : "0", 0, status,
                    mSuccessNum, tel, "").subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(baseBean -> {
                    }, throwable -> {
                        throwable.printStackTrace();
                    });
        }
    }
}
