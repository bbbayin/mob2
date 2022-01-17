package com.mob.sms.skin3.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms.skin3.R;
import com.mob.sms.skin3.adapter.CallPhoneRecordsAdapter;
import com.mob.sms.skin3.base.BaseActivity;
import com.mob.sms.skin3.bean.CallPhoneRecord;
import com.mob.sms.skin3.db.CallContactTable;
import com.mob.sms.skin3.db.DatabaseBusiness;
import com.mob.sms.skin3.network.RetrofitHelper;
import com.mob.sms.skin3.pns.BaiduPnsServiceImpl;
import com.mob.sms.skin3.utils.BindXUtils;
import com.mob.sms.skin3.utils.CollectionUtils;
import com.mob.sms.skin3.utils.Constants;
import com.mob.sms.skin3.utils.SPConstant;
import com.mob.sms.skin3.utils.SPUtils;
import com.mob.sms.skin3.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AutoCallPhoneActivity extends BaseActivity {
    @BindView(R.id.tip)
    TextView mTip;
    @BindView(R.id.mobile)
    TextView mMobile;
    @BindView(R.id.num)
    TextView mTvProgress;
    @BindView(R.id.time)
    TextView mTime;
    @BindView(R.id.success_num)
    TextView mSuccessNumTv;
    @BindView(R.id.fail_num)
    TextView mFailNumTv;
    @BindView(R.id.pause)
    TextView mPause;
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.record_ll)
    LinearLayout mRecoreLl;
    @BindView(R.id.pre)
    TextView mPre;
    @BindView(R.id.next)
    TextView mNext;

    private String mType;
    private int mTotalCallTimes;//总拨打次数
    private int mSendIndex;//当前拨打序列
    private int mInterval;//拨打间隔
    private String mDsbdTime;//定时拨打

    private ArrayList<CallContactTable> mDatas = new ArrayList<>();//批量拨打联系人
    private CallPhoneRecordsAdapter mCallPhoneRecordsAdapter;
    private ArrayList<CallPhoneRecord> mRecords = new ArrayList<>();

    private boolean mPauseState;
    private boolean mSim1Call = true;// 双卡轮流拨打情况使用
    private TelephonyManager mTm;
    private String mSimCard;// 用的sim卡
    private String dialNumber;// 要拨打的电话
    private boolean isOutCalling = false;// 是否已经拨出去电话，用此标志位拦截多次收到挂断广播执行下次拨号的问题

    //指定SIM卡拨打
    private String[] dualSimTypes = {"subscription", "Subscription",
            "com.android.phone.extra.slot",
            "phone", "com.android.phone.DialingMode",
            "simId", "simnum", "phone_type",
            "simSlot"};

    private ProgressDialog progressDialog;
    private boolean isRandomInterval;
    private CountDownTimer countDownTimer;
    private CountDownTimer callTimer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_single_task_layout);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
        initView();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void bindSecretNumber(final String mobile) {
        String phone = SPUtils.getString(SPConstant.SP_USER_PHONE, "");
        if (TextUtils.isEmpty(phone)) {
            Utils.showDialog(this, "请先绑定手机号", "提示",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(AutoCallPhoneActivity.this, SetSecretInfoActivity.class));
                        }
                    },
                    null);
        } else {
            BindXUtils.bindX(this, phone, mobile, new BindXUtils.BindCallBack() {
                @Override
                public void bindSuccess(String telX) {
                    bindTelxSuccess(telX);
                }

                @Override
                public void bindFailed(String msg) {
                    bindTelxFailed(mobile, msg);
                }
            });
        }
    }

    private void bindTelxSuccess(String telX) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callPhoneX(telX);
            }
        });
    }

    private void callPhoneX(String telX) {

        try {
            TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + telX));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                String simType = SPUtils.getString(SPConstant.SP_CALL_SKSZ, Constants.SIM_TYPE_SIM_1);
                for (int i = 0; i < dualSimTypes.length; i++) {
                    //0代表卡1,1代表卡2
                    if (Constants.SIM_TYPE_SIM_1.equals(simType)) {
                        intent.putExtra(dualSimTypes[i], 0);
                    } else if (Constants.SIM_TYPE_SIM_2.equals(simType)) {
                        intent.putExtra(dualSimTypes[i], 1);
                    } else if (Constants.SIM_TYPE_SIM_MIX.equals(simType)) {
                        intent.putExtra(dualSimTypes[i], mSim1Call ? 0 : 1);
                        mSim1Call = !mSim1Call;
                    } else if (Constants.SIM_TYPE_SECRET.equals(simType)) {
                        intent.putExtra(dualSimTypes[i], 0);
                    }
                }
//                List<PhoneAccountHandle> phoneAccountHandleList = telecomManager.getCallCapablePhoneAccounts();
                List<PhoneAccountHandle> phoneAccountHandleList = Utils.getAccountHandles(this);
                if (phoneAccountHandleList.size() > 0) {
                    if (Constants.SIM_TYPE_SIM_1.equals(simType)) {
                        intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandleList.get(0));
                    } else if (Constants.SIM_TYPE_SIM_2.equals(simType)) {
                        intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandleList.get(1));
                    } else if (Constants.SIM_TYPE_SIM_MIX.equals(simType)) {
                        intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandleList.get(mSim1Call ? 1 : 0));
                    } else if (Constants.SIM_TYPE_SECRET.equals(simType)) {
                        intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandleList.get(1));
                    }
                }
                startActivity(intent);
                isOutCalling = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindTelxFailed(String mobile, String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showDialog(AutoCallPhoneActivity.this, msg,
                        "提示", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bindSecretNumber(mobile);
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

    private void nextCall() {
        if (!isOutCalling) return;
        // 重置标志位
        isOutCalling = false;
        // 更新UI
        mRecords.get(mSendIndex).isSend = true;
        mCallPhoneRecordsAdapter.notifyItemChanged(mSendIndex);

        increaseIndex();
        if (isRandomInterval) {
            mInterval = Utils.generateRandomInterval();
        }
        if (mSendIndex < mTotalCallTimes) {
            CallContactTable data = CollectionUtils.get(mDatas, mSendIndex);
            if (data != null) {
                mMobile.setText(data.mobile);
                mTvProgress.setText(String.format("(%s/%s联系人)", mSendIndex, mDatas.size()));
                startCallCounter();
            }
        }
    }

    private void initView() {
        mTm = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
        mTm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        dialNumber = SPUtils.getString(SPConstant.SP_CALL_SRHM, "");
        mType = getIntent().getStringExtra("type");
        if (Constants.CALL_STYLE_MULTI.equals(mType)) {
            mRecoreLl.setVisibility(View.VISIBLE);
            mPre.setVisibility(View.VISIBLE);
            mNext.setVisibility(View.VISIBLE);
            mTip.setText("批量拨打电话");
            mDatas.addAll(DatabaseBusiness.getCallContacts());

            for (CallContactTable callContactTable : mDatas) {
                mRecords.add(new CallPhoneRecord(callContactTable.name, callContactTable.mobile, false));
            }
            mCallPhoneRecordsAdapter = new CallPhoneRecordsAdapter(this, mRecords);
            mRecyclerView.setAdapter(mCallPhoneRecordsAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            String interval = SPUtils.getString(SPConstant.SP_CALL_JGSZ, "");
            if (interval.contains("-")) {
                isRandomInterval = true;
                // 随机
                mInterval = Utils.generateRandomInterval();
            } else {
                mInterval = Integer.parseInt(interval);
            }
            mTotalCallTimes = mDatas.size();
            mMobile.setText(mDatas.get(mSendIndex).mobile);
            mTvProgress.setText("(" + 1 + "/" + mDatas.size() + "联系人)");

            callPhone(mDatas.get(mSendIndex).mobile);
        }
    }

    private void callPhone(String mobile) {
        bindSecretNumber(mobile);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onFinish();
    }

    @OnClick({R.id.back, R.id.pause, R.id.stop, R.id.pre, R.id.next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
            case R.id.stop:
                saveRecord();
                finish();
                onFinish();
                break;
            case R.id.pause:
                mPauseState = !mPauseState;
                // TODO
                break;
            case R.id.pre:
                if (mSendIndex - 1 >= 0) {
                    mSendIndex--;
                    mMobile.setText(mDatas.get(mSendIndex).mobile);
                    mTvProgress.setText("(" + (mSendIndex + 1) + "/" + mDatas.size() + "联系人)");
                    mTime.setText("拨打下一个号码还需要" + mInterval + "s");
                    callPhone(mDatas.get(mSendIndex).mobile);
                }
                break;
            case R.id.next:
                if (mSendIndex + 1 < mDatas.size()) {
                    mMobile.setText(mDatas.get(mSendIndex).mobile);
                    mTvProgress.setText("(" + (mSendIndex + 1) + "/" + mDatas.size() + "联系人)");
                    mTime.setText("拨打下一个号码还需要" + mInterval + "s");
                    callPhone(mDatas.get(mSendIndex).mobile);
                }
                break;
        }
    }

    private synchronized void increaseIndex() {
        mSendIndex++;
    }

    private int currentPhoneState = TelephonyManager.CALL_STATE_IDLE;
    private final PhoneStateListener listener = new PhoneStateListener() {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            //注意，方法必须写在super方法后面，否则incomingNumber无法获取到值。
            super.onCallStateChanged(state, incomingNumber);
            Log.d("电话监听", "当前状态：" + getState(state));
            if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                // 判断是否自动挂断
                Boolean autoFinish = SPUtils.getBoolean(SPConstant.SP_CALL_PL_GD, false);
                if (autoFinish) {
                    startAutoEnd();
                }
            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                if (currentPhoneState != state) {
                    releaseCounter();
                    nextCall();
                }
            }
            currentPhoneState = state;
        }
    };

    private void onFinish() {
        releaseCounter();
        if (mTm != null) {
            mTm.listen(listener, PhoneStateListener.LISTEN_NONE);
        }
    }

    private void releaseCounter() {
        if (callTimer != null) {
            callTimer.cancel();
        }
        callTimer = null;
    }

    private String getState(int state) {
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                return "空闲idle";
            case TelephonyManager.CALL_STATE_OFFHOOK:
                return "offHook，";
            case TelephonyManager.CALL_STATE_RINGING:
                return "响铃：";
            default:
                return "空闲 defalut" + state;
        }
    }

    private void startCallCounter() {
        if (isFinishing()) return;
        callTimer = new CountDownTimer(mInterval * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long remain = millisUntilFinished / 1000;
                mTime.setText(String.format("拨打下一个号码还需要 %s s", remain));
            }

            @Override
            public void onFinish() {
                callPhone(mDatas.get(mSendIndex).mobile);
            }
        };
        callTimer.start();
    }

    private void startAutoEnd() {
        countDownTimer = new CountDownTimer(10 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                // 执行任务
                if (ActivityCompat.checkSelfPermission(AutoCallPhoneActivity.this,
                        Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
                    telecomManager.endCall();
                }
            }
        };
        countDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onFinish();
    }

    private void saveRecord() {
        int allNum = 0;
        String tel = "";
        if ("dhbd".equals(mType)) {
            allNum = 1;
            tel = SPUtils.getString(SPConstant.SP_CALL_SRHM, "");
        } else if ("plbd".equals(mType)) {
            allNum = mDatas.size();
            for (CallContactTable callContactTable : mDatas) {
                if (TextUtils.isEmpty(tel)) {
                    tel = callContactTable.mobile;
                } else {
                    tel = tel + "," + callContactTable.mobile;
                }
            }
        }
        String status;
        if (mSendIndex == 0) {
            status = "-1";
        } else if (mSendIndex < mTotalCallTimes) {
            status = "0";
        } else {
            status = "1";
        }

        String time = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss").format(new Date(System.currentTimeMillis()));
        if ("dhbd".equals(mType)) {
            RetrofitHelper.getApi().saveCallRecord(allNum, time, mInterval + "", mTotalCallTimes, SPUtils.getString(SPConstant.SP_SIM_CARD_TYPE, ""),
                    SPUtils.getBoolean(SPConstant.SP_CALL_GD, false) ? "1" : "0", status,
                    mSendIndex + 1, tel, mDsbdTime, SPUtils.getBoolean(SPConstant.SP_CALL_GD, false) ? "0" : "1").subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(baseBean -> {
                    }, throwable -> {
                        throwable.printStackTrace();
                    });
        } else if ("plbd".equals(mType)) {
            RetrofitHelper.getApi().savePlCallRecord(allNum, SPUtils.getString(SPConstant.SP_CALL_SKSZ, Constants.SIM_TYPE_SIM_1), time,
                    mInterval + "",
                    SPUtils.getBoolean(SPConstant.SP_CALL_PL_GD, false) ? "1" : "0", status,
                    mSendIndex + 1, tel, SPUtils.getBoolean(SPConstant.SP_CALL_PL_GD, false) ? "0" : "1").subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(baseBean -> {
                    }, throwable -> {
                        throwable.printStackTrace();
                    });
        }
    }
}
