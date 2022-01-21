package com.mob.sms2.auto;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.mob.sms2.R;
import com.mob.sms2.activity.SetSecretInfoActivity;
import com.mob.sms2.base.BaseActivity;
import com.mob.sms2.databinding.ActivityAutoSingleTaskLayoutBinding;
import com.mob.sms2.network.RetrofitHelper;
import com.mob.sms2.pns.BaiduPnsServiceImpl;
import com.mob.sms2.receiver.PhoneStateReceiver;
import com.mob.sms2.rx.CallEvent;
import com.mob.sms2.rx.RxBus;
import com.mob.sms2.utils.BindXUtils;
import com.mob.sms2.utils.CallLogBean;
import com.mob.sms2.utils.Constants;
import com.mob.sms2.utils.PhoneUtils;
import com.mob.sms2.utils.SPConstant;
import com.mob.sms2.utils.SPUtils;
import com.mob.sms2.utils.ToastUtil;
import com.mob.sms2.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 单一任务：单号拨打/发短信
 */
public class SingleAutoTaskActivity extends BaseActivity {

    public static boolean phoneIdleFlag = false;

    private ActivityAutoSingleTaskLayoutBinding binding;
    private String mDestNumber;// 要拨打的电话
    private int mTotalCallTimes;// 拨打次数
    private int mInterval;
    // 当前页面任务类型
    public static String KEY_TASK = "key+task";
    public final static int VALUE_TASK_DIAL = 1;// 打电话
    public final static int VALUE_TASK_SEND_MSG = 2;// 发短信
    private int mTaskType;// 打电话/发短信
    private long mDelay;// 延迟开始
    private int mCurrentCount = 1;// 当前第几次任务
    private CountDownTimer timer;
    private boolean isRunning = false;// 倒计时是否在执行
    private boolean isManualStop = false;// 手动停止
    private int mSim;// 设置的sim卡
    private boolean mIsSecretDial;// 使用使用隐私拨打
    private ProgressDialog progressDialog;
    private TelephonyManager telephonyManager;
    private final String TAG = "【SingleAutoTask】";
    private boolean exchangeSendSms = false;// 交替卡发短信
    private String mSmsContent;// 短信内容
    private int currentSim = 0;// 交替发短信时的卡
    private List<CallLogBean> reportedRecord = new ArrayList<>();

    private PhoneStateListener listener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            final TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            Log.i("拨号页面", "state: " + state + "," + incomingNumber);
            if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                // 判断是否自动挂断
                Boolean autoFinish = SPUtils.getBoolean(SPConstant.SP_CALL_GD, false);
                if (autoFinish) {
                    handler.postDelayed(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.P)
                        @Override
                        public void run() {
                            if (ActivityCompat.checkSelfPermission(SingleAutoTaskActivity.this,
                                    Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            telecomManager.endCall();
                        }
                    }, 30*1000);
                }
            } else if (state == TelephonyManager.CALL_STATE_IDLE) {


            }
        }
    };
    private Subscription mSub;
    private long remainTime;

    /**
     * 上报隐私拨号时间，单位：分钟
     *
     * @param duration
     */
    private void reportDuration(int duration) {
        Log.i(TAG, "上报打电话时长");
        if (duration <= 60) duration = 1;
        else {
            duration = duration / 60 + 1;
        }
        Log.i(TAG, "通话时长："+duration+"分钟");
        RetrofitHelper.getApi().chargeCloudDial(duration)
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

        }
    };
    private PendingIntent sentPI;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // viewBinding
        binding = ActivityAutoSingleTaskLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setStatusBar(getResources().getColor(R.color.green));
        // init
        initView();
        initClick();
        initData();
        mSub = RxBus.getInstance().toObserverable(CallEvent.class)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(this::nextCall);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    private void nextCall(CallEvent event) {
        startTimer(mInterval);
    }

    private void initData() {
        mTaskType = getIntent().getIntExtra(KEY_TASK, VALUE_TASK_DIAL);
        if (mTaskType == VALUE_TASK_DIAL) {
            binding.tip.setText("单号拨打电话");
            // 判断是否隐私拨打
            String type = SPUtils.getString(SPConstant.SP_SIM_CARD_TYPE, Constants.SIM_TYPE_SIM_1);
            if (Constants.SIM_TYPE_SECRET.equals(type)) {
                // 隐私拨号
                mIsSecretDial = true;
                mSim = SPUtils.getInt(SPConstant.SP_SECRET_SIM_NO, 0);
                bindSecretNumber();
            } else {
                // 正常拨打
                mIsSecretDial = false;
                if (Constants.SIM_TYPE_SIM_1.equals(type)) {
                    mSim = 0;
                } else {
                    mSim = 1;
                }
                startTimer(mDelay);
            }
        } else {
            binding.tip.setText("单号发短信");
            mDestNumber = SPUtils.getString(SPConstant.SP_SMS_SRHM, "");
            mSmsContent = SPUtils.getString(SPConstant.SP_SMS_CONTENT, "");
            String smsSim = SPUtils.getString(SPConstant.SP_SMS_SKSZ, "");
            if (TextUtils.isEmpty(smsSim)) {
                exchangeSendSms = true;
            } else {
                exchangeSendSms = false;
                mSim = Constants.SIM_TYPE_SIM_1.equals(smsSim) ? 0 : 1;
            }
            Intent sentIntent = new Intent("SENT_SMS_ACTION");
            sentPI = PendingIntent.getBroadcast(this, 0, sentIntent, 0);
        }
    }

    /**
     * 获取隐私号
     */
    private void bindSecretNumber() {
        String phone = SPUtils.getString(SPConstant.SP_USER_PHONE, "");
        if (TextUtils.isEmpty(phone)) {
            Utils.showDialog(this, "请先设置隐私拨号信息", "提示",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(SingleAutoTaskActivity.this, SetSecretInfoActivity.class));
                        }
                    },
                    null);
        } else {
            String callNumber = SPUtils.getString(SPConstant.SP_CALL_SRHM, "");
            BindXUtils.bindX(this, phone, callNumber, new BindXUtils.BindCallBack() {
                @Override
                public void bindSuccess(String telX) {
                    bindTelxSuccess(telX);
                }

                @Override
                public void bindFailed(String msg) {
                    bindTelxFailed(msg);
                }
            });
        }
    }

    private void bindTelxSuccess(String telX) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDestNumber = telX;
                ToastUtil.show("隐私号绑定成功");
                startTimer(mDelay);
            }
        });
    }

    private void bindTelxFailed(String errorMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showDialog(SingleAutoTaskActivity.this, "隐私号码绑定失败，请重试，错误信息："+errorMsg,
                        "提示", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bindSecretNumber();
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
            }
        });
    }

    private void showProgress(String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        if (!progressDialog.isShowing()) {
            progressDialog.setTitle(msg);
            if (isDestroyed() || isFinishing()) {
                return;
            }
            progressDialog.show();
        }
    }

    private void hideProgress() {
        runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        });
    }

    private synchronized void startTimer(long duration) {
        if (isRunning && isManualStop) return;
        if (mCurrentCount > mTotalCallTimes) {
            binding.time.setText("拨打结束");
            return;
        }
        binding.num.setText(String.format("(%s/%s次)", mCurrentCount, mTotalCallTimes));
        System.out.println(String.format("开启任务，%s/%s", mCurrentCount, mTotalCallTimes));
        release();

        timer = new CountDownTimer(duration * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainTime = millisUntilFinished / 1000 + 1;
                binding.time.setText(String.format("下一次拨打还需要%ss", remainTime));
            }

            @Override
            public void onFinish() {
                // 执行任务
                executeTask();
                mCurrentCount++;
            }
        };
        timer.start();
        isRunning = true;
    }

    /**
     * 拨打电话/发短信
     */
    private void executeTask() {
        if (mTaskType == VALUE_TASK_DIAL) {
            callPhone();
        } else {
            sendMsg();
        }
    }


    /**
     * 打电话
     */
    private void callPhone() {
        try {
            // 1. 获取拨打的sim卡
            if (isFinishing() || isDestroyed() || isPageFinished) return;
            TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + mDestNumber));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                List<PhoneAccountHandle> phoneAccountHandleList = Utils.getAccountHandles(this);
                if (phoneAccountHandleList.size() > mSim) {
                    intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandleList.get(mSim));
                }
                startActivityForResult(intent, 888);
                isRunning = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMsg() {
        try {
            SubscriptionInfo sInfo = null;
            final SubscriptionManager sManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            List<SubscriptionInfo> list = sManager.getActiveSubscriptionInfoList();
            if (list.size() == 2) {// double card
                if (exchangeSendSms) {
                    sInfo = list.get(currentSim);
                    currentSim ^= 0;
                    Log.w(TAG, "当前电话卡" + currentSim);
                } else {
                    sInfo = list.get(mSim);
                }
            } else {//single card
                sInfo = list.get(0);
            }
            int subId = sInfo.getSubscriptionId();
            SmsManager manager = SmsManager
                    .getSmsManagerForSubscriptionId(subId);
            manager.sendTextMessage(mDestNumber, null, mSmsContent, sentPI, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        telephonyManager = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
        mDestNumber = SPUtils.getString(SPConstant.SP_CALL_SRHM, "");
        binding.tip.setText("单号拨打电话");
        binding.mobile.setText(mDestNumber);
        // 总共拨打次数
        mTotalCallTimes = SPUtils.getInt(SPConstant.SP_CALL_NUM, 1);
        // 定时时间
        String delayTimeStr = SPUtils.getString(SPConstant.SP_CALL_TIMING, "");
        // 时间间隔
        mInterval = SPUtils.getInt(SPConstant.SP_CALL_INTERVAL, 20);
        binding.num.setText(String.format("(%s/%s次)", mCurrentCount, mTotalCallTimes));
        mDelay = 0;
        if (delayTimeStr.contains("秒")) {
            int hour = Integer.parseInt(delayTimeStr.split("时")[0]);
            int min = Integer.parseInt(delayTimeStr.split("时")[1].split("分")[0]);
            int sec = Integer.parseInt(delayTimeStr.split("时")[1].split("分")[1].split("秒")[0]);
            long time = ((hour * 60 + min) * 60 + sec) * 1000;
            // 下次拨打倒计时
            mDelay = time / 1000;
            binding.time.setText(String.format("下一次拨打还需要%ss", mDelay));
        } else {
            if (delayTimeStr.contains("上午")) {
                String date = delayTimeStr.split("上午")[0];
                int hour = Integer.parseInt(delayTimeStr.split("上午")[1].split("时")[0]);
                int min = Integer.parseInt(delayTimeStr.split("上午")[1].split("时")[1].split("分")[0]);
                String sendDate = Utils.getYear() + "-" + date + " " + hour + ":" + min + ":" + "00";

                long time = Utils.getTime(sendDate);
                mDelay = (time / 1000);
                binding.time.setText(String.format("下一次拨打还需要%ss", mDelay));
            } else if (delayTimeStr.contains("下午")) {
                String date = delayTimeStr.split("下午")[0];
                int hour = Integer.parseInt(delayTimeStr.split("下午")[1].split("时")[0]);
                int min = Integer.parseInt(delayTimeStr.split("下午")[1].split("时")[1].split("分")[0]);
                String sendDate = Utils.getYear() + "-" + date + " " + (hour + 12) + ":" + min + ":" + "00";

                long time = Utils.getTime(sendDate);
                mDelay = time / 1000;
                binding.time.setText(String.format("下一次拨打还需要%ss", mDelay));
            }
        }
        // 注册电话广播监听
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        intentFilter.setPriority(Integer.MAX_VALUE);
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        intentFilter.addAction(TelecomManager.ACTION_INCOMING_CALL);
        registerReceiver(new PhoneStateReceiver(), intentFilter);

        RxBus.getInstance().toObserverable(CallEvent.class)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onReceiveCallEvent);
    }

    public void onReceiveCallEvent(CallEvent event) {
        if (event.status.equals("IDLE")) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 上报记录，扣减时长
                    synchronized (SingleAutoTaskActivity.this) {
                        if (mIsSecretDial) {
                            // 扣减时长
                            List<CallLogBean> callLog = PhoneUtils.INSTANCE.getCallLog(1, SingleAutoTaskActivity.this);
                            Log.d(TAG, callLog.toString());
                            if (!callLog.isEmpty()) {
                                CallLogBean logBean = callLog.get(0);
                                if (logBean.getNumber().equals(mDestNumber) && logBean.getDuration() > 0) {
                                    // 上报时长
                                    if (!reportedRecord.contains(logBean)) {
                                        reportedRecord.add(logBean);
                                        reportDuration(logBean.getDuration());
                                    }
                                }
                            }
                        }
                    }
                }
            }, 3000);

            // TODO：上报记录

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("收到结果：" + requestCode + "   res=" + resultCode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
    }

    private void release() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        isRunning = false;
    }

    private boolean isPageFinished = false;
    private void back() {
        isPageFinished = true;
        finish();
        saveRecord();
    }

    @Override
    public void onBackPressed() {

    }

    private void saveRecord() {
        String status;
        if (mCurrentCount == 0) {
            status = "-1";
        } else if (mCurrentCount < mTotalCallTimes) {
            status = "0";
        } else {
            status = "1";
        }
        String time = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss").format(new Date(System.currentTimeMillis()));
        String mDsbdTime = SPUtils.getString(SPConstant.SP_CALL_TIMING, "");
        Boolean gd = SPUtils.getBoolean(SPConstant.SP_CALL_GD, false);
        RetrofitHelper.getApi().saveCallRecord(1, time, mInterval + "", mTotalCallTimes,
                mSim == 0 ? Constants.SIM_TYPE_SIM_1 : Constants.SIM_TYPE_SIM_2,
                gd ? "1" : "0", status,
                mCurrentCount + 1, mDestNumber, mDsbdTime, gd ? "0" : "1").subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseBean -> {
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }

    private void initClick() {
        binding.back.setOnClickListener(v -> back());
        binding.stop.setOnClickListener(v -> back());
        binding.pause.setOnClickListener(v -> {
            // 手动暂停
            if (isManualStop) {
                startTimer(remainTime);
            } else {
                release();
                binding.time.setText("暂停拨打");
            }
            isManualStop = !isManualStop;
        });
    }
}
