package com.mob.sms2.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alipay.sdk.app.PayTask;
import com.bumptech.glide.Glide;
import com.mob.sms2.R;
import com.mob.sms2.adapter.VipAdapter;
import com.mob.sms2.base.BaseActivity;
import com.mob.sms2.config.GlobalConfig;
import com.mob.sms2.network.RetrofitHelper;
import com.mob.sms2.network.bean.VipBean;
import com.mob.sms2.utils.PayResult;
import com.mob.sms2.utils.SPConstant;
import com.mob.sms2.utils.SPUtils;
import com.mob.sms2.utils.ToastUtil;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class VipActivity extends BaseActivity {
    @BindView(R.id.avatar)
    ImageView mAvatar;
    @BindView(R.id.username)
    TextView mUsername;
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.tv_tip)
    TextView tvTip;

    private VipAdapter mVipAdapter;
    private ArrayList<VipBean.DataBean> mDatas = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.black));
        initView();
        getData();
    }

    private void initView() {
        Glide.with(this).load(SPUtils.getString(SPConstant.SP_USER_HEAD, "")).into(mAvatar);
        mUsername.setText(SPUtils.getString(SPConstant.SP_USER_NAME, ""));
        tvTip.setVisibility(GlobalConfig.isVip? View.GONE: View.VISIBLE);
        mVipAdapter = new VipAdapter(this, mDatas);
        mRecyclerView.setAdapter(mVipAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private <T> T get(List<T> list, int pos) {
        if (list != null && !list.isEmpty() && list.size() > pos) {
            return list.get(pos);
        }
        return null;
    }

    private void getData() {
        RetrofitHelper.getApi().getVip().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(vipBean -> {
                    if (vipBean != null && vipBean.code == 200) {
                        mDatas.addAll(vipBean.data);
                        mDatas.get(0).isSelected = true;
                        mVipAdapter.notifyDataSetChanged();
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }

    @OnClick({R.id.back, R.id.ali_pay, R.id.wx_pay})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.ali_pay:
                aliPay();
                break;
            case R.id.wx_pay:
                wxPay();
                break;
        }
    }

    private int getSelectedOrder() {
        for (int i = 0; i < mDatas.size(); i++) {
            if (mDatas.get(i).isSelected) return i;
        }
        return 0;
    }

    private void aliPay() {
        int selectedOrder = getSelectedOrder();
        RetrofitHelper.getApi().createOrder(mDatas.get(selectedOrder).memberId).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(orderBean -> {
                    if (orderBean != null && orderBean.code == 200) {
                        pay(orderBean.data, "1");
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }

    private void wxPay() {
        final IWXAPI msgApi = WXAPIFactory.createWXAPI(this, null);
        // 将该app注册到微信
        msgApi.registerApp("wx5fe8deafb48e5513");
        RetrofitHelper.getApi().createOrder(mDatas.get(getSelectedOrder()).memberId).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(orderBean -> {
                    if (orderBean != null && orderBean.code == 200) {
                        pay(orderBean.data, "2");
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }

    private void pay(int orderId, String payType) {
        RetrofitHelper.getApi().pay(orderId, payType).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseBean -> {
                    if (baseBean != null && baseBean.code == 200) {
                        if ("1".equals(payType)) {
                            final String orderInfo = baseBean.msg;   // 订单信息
                            Runnable payRunnable = new Runnable() {

                                @Override
                                public void run() {
                                    PayTask alipay = new PayTask(VipActivity.this);
                                    Map<String, String> result = alipay.payV2(orderInfo, true);

                                    Message msg = new Message();
                                    msg.what = 1901;
                                    msg.obj = result;
                                    mHandler.sendMessage(msg);
                                }
                            };
                            // 必须异步调用
                            Thread payThread = new Thread(payRunnable);
                            payThread.start();
                        } else if ("2".equals(payType)) {
                            try {
                                JSONObject jsonObject = new JSONObject(baseBean.msg);
                                String packageValue = jsonObject.getString("package");
                                String appid = jsonObject.getString("appid");
                                String sign = jsonObject.getString("sign");
                                String partnerid = jsonObject.getString("partnerid");
                                String prepayid = jsonObject.getString("prepayid");
                                String noncestr = jsonObject.getString("noncestr");
                                String timestamp = jsonObject.getString("timestamp");

                                IWXAPI api = WXAPIFactory.createWXAPI(VipActivity.this, null);
                                api.registerApp(appid);
                                PayReq request = new PayReq();
                                request.appId = appid;
                                request.partnerId = partnerid;
                                request.prepayId = prepayid;
                                request.packageValue = packageValue;
                                request.nonceStr = noncestr;
                                request.timeStamp = timestamp;
                                request.sign = sign;
                                api.sendReq(request);
                            } catch (Exception e) {
                                Log.i("jqt", "e: " + e);
                                e.printStackTrace();
                            }
                        }
                    }
                }, throwable -> {
                    Log.i("jqt", "throwable: " + throwable);
                    throwable.printStackTrace();
                });
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1901:  //支付宝

                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        ToastUtil.show("支付成功");
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        ToastUtil.show("支付失败");
                    }

//                    if (msg.obj.equals("9000")) {
//                        ToastUtil.show("支付成功");
//                    } else {
//                        ToastUtil.show("支付失败");
//                    }
                    break;
            }
        }

        ;
    };
}
