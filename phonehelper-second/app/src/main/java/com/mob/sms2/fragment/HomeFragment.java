package com.mob.sms2.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms2.R;
import com.mob.sms2.activity.AutoCallPhoneActivity;
import com.mob.sms2.activity.AutoSendSmsActivity;
import com.mob.sms2.activity.CallTypeActivity;
import com.mob.sms2.activity.ContactsActivity;
import com.mob.sms2.activity.CopyImportActivity;
import com.mob.sms2.activity.DocImportActivity;
import com.mob.sms2.activity.EditSmsActivity;
import com.mob.sms2.activity.ImportContactsActivity;
import com.mob.sms2.activity.SimSettingActivity;
import com.mob.sms2.activity.VipActivity;
import com.mob.sms2.application.MyApplication;
import com.mob.sms2.auto.SingleAutoTaskActivity;
import com.mob.sms2.base.BaseFragment;
import com.mob.sms2.base.SimpleObserver;
import com.mob.sms2.bean.ChannelChargeBean;
import com.mob.sms2.bean.CloudPermissionBean;
import com.mob.sms2.bean.HomeFuncBean;
import com.mob.sms2.config.GlobalConfig;
import com.mob.sms2.db.CallContactTable;
import com.mob.sms2.db.DatabaseBusiness;
import com.mob.sms2.db.SmsContactTable;
import com.mob.sms2.dialog.CheckTipDialog;
import com.mob.sms2.dialog.DocImportDialog;
import com.mob.sms2.dialog.ImportDialog;
import com.mob.sms2.dialog.SetCallIntervalDialog;
import com.mob.sms2.dialog.SetCallNumDialog;
import com.mob.sms2.dialog.SetCallTimingDialog;
import com.mob.sms2.dialog.SetMultiCallIntervalDialog;
import com.mob.sms2.network.RetrofitHelper;
import com.mob.sms2.network.bean.BaseResponse;
import com.mob.sms2.network.bean.UserInfoBean;
import com.mob.sms2.utils.Constants;
import com.mob.sms2.utils.FreeCheckUtils;
import com.mob.sms2.utils.MsgViewFactory;
import com.mob.sms2.utils.SPConstant;
import com.mob.sms2.utils.SPUtils;
import com.mob.sms2.utils.ToastUtil;
import com.mob.sms2.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class HomeFragment extends BaseFragment {
    @BindView(R.id.bddh_ll)
    LinearLayout bddh_ll;
    @BindView(R.id.plbd_ll)
    LinearLayout plbd_ll;
    @BindView(R.id.dxds_ll)
    LinearLayout dxds_ll;
    //??????
    @BindView(R.id.call_mobile_et)
    EditText mCallMobileEt;
    @BindView(R.id.dsbh_tv)
    TextView mCallDsbhTv;
    @BindView(R.id.bdcs_tv)
    TextView mCallBdcsTv;
    @BindView(R.id.bdjg_tv)
    TextView mCallBdjgTv;
    @BindView(R.id.bdfs_tv)
    TextView mCallBdfsTv;
    @BindView(R.id.gd_switch)
    ImageView mGdSwitch;
    @BindView(R.id.home_btn_single_call_now)
    TextView mCallTv;
    //????????????
    @BindView(R.id.call_hmdr_tip)
    TextView mCallHmdrTip;
    @BindView(R.id.call_sksz_tip)
    TextView mCallSkszTip;
    @BindView(R.id.call_jgsz_tip)
    TextView mCallJgszTip;
    @BindView(R.id.pl_switch_gd)
    ImageView mPlGdSwitch;
    @BindView(R.id.pl_call_tv)
    TextView mPlCallTv;
    //??????
    @BindView(R.id.dhfs_switch)
    ImageView dhfs_switch;
    @BindView(R.id.dhfs_ll)
    LinearLayout dhfs_ll;
    @BindView(R.id.sms_mobile_et)
    EditText mSmsMobileEt;
    @BindView(R.id.sms_dsfs_tip)
    TextView mSmsDsfsTip;
    @BindView(R.id.sms_fscs_tip)
    TextView mSmsFscsTip;
    @BindView(R.id.plfs_switch)
    ImageView plfs_switch;
    @BindView(R.id.plfs_ll)
    LinearLayout plfs_ll;
    @BindView(R.id.sms_hmdr_tip)
    TextView mSmsHmdrTip;
    @BindView(R.id.sms_sksz_tip)
    TextView mSmsSkszTip;
    @BindView(R.id.sms_fsjg_tip)
    TextView mSmsFsjgTip;
    @BindView(R.id.bjdx_tip)
    TextView mBjdxTip;
    @BindView(R.id.sms_ljfs)
    TextView mSmsLjfs;
    @BindView(R.id.home_single_btn_clear_phone)
    ImageView ivClearSinglePhoneNumber;
    @BindView(R.id.home_single_btn_clear_time)
    ImageView ivClearTime;
    @BindView(R.id.multi_iv_clear_interval)
    ImageView ivMultiClearInterval;
    @BindView(R.id.sms_iv_clear_phone)
    ImageView ivClearSmsPhone;
    //    @BindView(R.id.sms_iv_clear_timeout)
//    ImageView ivClearSmsTimeout;
    @BindView(R.id.multi_btn_clear_phone)
    ImageView ivMultiCLearPhone;
    @BindView(R.id.sms_btn_clear_phone)
    ImageView ivClearSmsImportPhone;
//    @BindView(R.id.banner)
//    Banner banner;
    @BindView(R.id.home_error_layout)
    View homeErrorLayout;
    @BindView(R.id.home_tv_error_msg)
    TextView tvErrorMsg;
    @BindView(R.id.single_call_auto_finish_layout)
    View singleCallSwitchLayout;// ????????????????????????
    @BindView(R.id.multi_call_auto_finish_layout)
    View multiCallSwitchLayout;// ??????????????????????????????
    @BindView(R.id.sms_multi_send_layout)
    View smsMultiSendSwitch;// ????????????????????????
    @BindView(R.id.sms_fscs_rl)
    View smsSendTimesLayout;// ????????????????????????
    @BindView(R.id.sms_single_send_switch)
    View smsSingleSendSwitch;// ??????????????????
    @BindView(R.id.msg_flipper)
    LinearLayout msgContainer;
    @BindView(R.id.home_radio_group)
    RadioGroup radioGroup;
    @BindView(R.id.home_radio_call_phone)
    RadioButton rbCallPhone;
    @BindView(R.id.home_radio_multi_call)
    RadioButton rbMultiCall;
    @BindView(R.id.home_radio_send_msg)
    RadioButton rbSendMsg;


    private final int REQUEST_CODE_TAB1_SRHM = 1;
    private final int REQUEST_CODE_TAB1_CALL_TYPE = 2;
    private boolean sms_dhfs_open = true;

    private int mVisibleTab = 0;//???????????????tab
    private boolean mCallGd = false;//???????????????????????????
    private boolean mPlCallGd = false;//???????????????????????????
    private boolean isShowSendTimes = true;// ??????????????????????????????

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        initView();
        initData();
        return view;
    }

    private void initMsg() {
        msgContainer.setVisibility(View.VISIBLE);
        msgContainer.addView(MsgViewFactory.create(getContext(), msgContainer));
    }

    private void initData() {
//        RetrofitHelper.getApi().getImage(3)
//                .subscribe(new BaseObserver<List<BannerBean>>() {
//                    @Override
//                    protected void onSuccess(List<BannerBean> list) {
//                        initBanner(list);
//                    }
//
//                    @Override
//                    protected void onFailed(MobError error) {
//
//                    }
//                });
        RetrofitHelper.getApi().getUserInfo().subscribe(new SimpleObserver<UserInfoBean>() {
            @Override
            public void onNext(UserInfoBean userInfoBean) {
                if (userInfoBean.data != null) {
                    GlobalConfig.isVip = TextUtils.equals(userInfoBean.data.type, "1");
                    if (GlobalConfig.isVip) {
                        // ??????????????????????????????
                        try {
                            String expTime = userInfoBean.data.expTime;
                            if (!TextUtils.isEmpty(expTime)) {
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:ss", Locale.CHINA);
                                Date expdate = simpleDateFormat.parse(expTime);
                                Calendar calendar = Calendar.getInstance();
                                Date current = calendar.getTime();
                                if (current.after(expdate)) {
                                    //???????????????

                                } else {
                                    calendar.add(Calendar.DAY_OF_YEAR, 30);// 30??????
                                    Calendar instance = Calendar.getInstance();
                                    instance.setTime(expdate);
                                    if (calendar.after(instance)) {
                                        initMsg();
                                    }
                                }
                            }

                        } catch (Exception e) {

                        }
                    }
                }
            }
        });

        // 3?????????????????????
        RetrofitHelper.getApi().getHomeSetting().subscribe(new SimpleObserver<BaseResponse<List<HomeFuncBean>>>() {
            @Override
            public void onNext(BaseResponse<List<HomeFuncBean>> response) {
                if (response != null) {
                    if (response.code == 200 && response.data != null) {
                        int activeId = -1;
                        homeErrorLayout.setVisibility(View.GONE);
                        for (HomeFuncBean config : response.data) {
                            // ???????????????????????????????????????
                            initHiddenSetting(config.id);

                            switch (config.id) {
                                case 1:// ??????
                                    if (TextUtils.equals(config.status, "1")) {
                                        rbCallPhone.setVisibility(View.VISIBLE);
                                        activeId = 1;
                                        // ??????????????????

                                    } else {
                                        rbCallPhone.setVisibility(View.GONE);
                                        bddh_ll.setVisibility(View.GONE);
                                    }
                                    break;
                                case 2:// ??????
                                    if (TextUtils.equals(config.status, "1")) {
                                        rbMultiCall.setVisibility(View.VISIBLE);
                                        activeId = (activeId < 0 ? 2 : activeId);
                                    } else {
                                        rbMultiCall.setVisibility(View.GONE);
                                        plbd_ll.setVisibility(View.GONE);
                                    }
                                    break;
                                case 3:// ??????
                                    if (TextUtils.equals(config.status, "1")) {
                                        rbSendMsg.setVisibility(View.VISIBLE);
                                        activeId = (activeId < 0 ? 3 : activeId);
                                    } else {
                                        rbSendMsg.setVisibility(View.GONE);
                                        dxds_ll.setVisibility(View.GONE);
                                    }
                                    break;
                            }
                        }
                        selectFunc(activeId);
                    } else {
                        // error
                        errorLayout(response.msg);
                    }
                } else {
                    //error
                    errorLayout(response.msg);
                }
            }
        });
    }

    /**
     * ????????????
     *
     * @param type 1????????????2????????????3?????????
     */
    private void initHiddenSetting(final int type) {
        RetrofitHelper.getApi().getHiddenSetting(type).subscribe(new SimpleObserver<BaseResponse<List<HomeFuncBean>>>() {
            @Override
            public void onNext(BaseResponse<List<HomeFuncBean>> response) {
                if (response != null && response.code == 200) {
                    for (HomeFuncBean bean : response.data) {
                        // 3??????????????????????????????????????????????????????????????????
                        if (bean.type == 1 || bean.type == 2) {// ?????????????????????
                            showSettings(bean.type, TextUtils.equals(bean.status, "1"), false);
                        } else {
                            // ????????????
                            if (bean.id == 4) {
                                // ????????????
                                isShowSendTimes = false;
                                smsSendTimesLayout.setVisibility(TextUtils.equals(bean.status, "1") ? View.VISIBLE : View.GONE);
                            } else {
                                // ????????????
                                smsMultiSendSwitch.setVisibility(TextUtils.equals(bean.status, "1") ? View.VISIBLE : View.GONE);
                                smsSingleSendSwitch.setVisibility(TextUtils.equals(bean.status, "1") ? View.VISIBLE : View.GONE);
                            }
                        }
                    }
                } else {
                    // ??????
                    showSettings(type, false, false);
                }
            }
        });
    }

    /**
     * ???????????????????????????????????????????????????????????????
     *
     * @param type
     */
    private void showSettings(int type, boolean show1, boolean show2) {
        if (type == 1) {
            singleCallSwitchLayout.setVisibility(show1 ? View.VISIBLE : View.GONE);
        } else if (type == 2) {
            multiCallSwitchLayout.setVisibility(show1 ? View.VISIBLE : View.GONE);
        }
    }

    private void errorLayout(String errorMsg) {
        homeErrorLayout.setVisibility(View.VISIBLE);
        tvErrorMsg.setText(errorMsg);
        dxds_ll.setVisibility(View.GONE);
        plbd_ll.setVisibility(View.GONE);
        bddh_ll.setVisibility(View.GONE);
    }

//    private void initBanner(List<BannerBean> list) {
//        if (list != null && !list.isEmpty()) {
//            banner.setVisibility(View.VISIBLE);
//            banner.setAdapter(new BannerAdapter<BannerBean, BannerHolder>(list) {
//                @Override
//                public BannerHolder onCreateHolder(ViewGroup parent, int viewType) {
//                    LayoutInflater from = LayoutInflater.from(parent.getContext());
//                    View item = from.inflate(R.layout.banner_image_layout, parent, false);
//                    return new BannerHolder(item);
//                }
//
//                @Override
//                public void onBindView(BannerHolder holder, BannerBean data, int position, int size) {
//                    Glide.with(holder.itemView.getContext())
//                            .load(data.img)
//                            .apply(RequestOptions.fitCenterTransform()
//                                    .error(R.drawable.ic_launcher_background)
//                                    .placeholder(R.drawable.ic_prompt_loading))
//                            .into(holder.image);
//                    if (!TextUtils.isEmpty(data.url)) {
//                        holder.itemView.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                Intent intent = new Intent(Intent.ACTION_VIEW);
//                                intent.setData(Uri.parse(data.url));
//                                startActivity(intent);
//                            }
//                        });
//
//                    }
//                }
//            });
//            banner.setDatas(list);
//            banner.start();
//        } else {
//            banner.setVisibility(View.GONE);
//        }
//    }

    private static class BannerHolder extends RecyclerView.ViewHolder {

        private final ImageView image;

        public BannerHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.banner_image);
        }
    }

    private void initView() {
        bddh_ll.setVisibility(View.VISIBLE);
        plbd_ll.setVisibility(View.GONE);
        dxds_ll.setVisibility(View.GONE);

        mCallHmdrTip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String timeout = s.toString();
                if (TextUtils.isEmpty(timeout)) {
                    ivMultiCLearPhone.setVisibility(View.GONE);
                } else {
                    ivMultiCLearPhone.setVisibility(View.VISIBLE);
                }
            }
        });

        mSmsHmdrTip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String timeout = s.toString();
                if (TextUtils.isEmpty(timeout)) {
                    ivClearSmsImportPhone.setVisibility(View.GONE);
                } else {
                    ivClearSmsImportPhone.setVisibility(View.VISIBLE);
                }
            }
        });

        mCallGd = (boolean) SPUtils.get(SPConstant.SP_CALL_GD, false);
        mGdSwitch.setImageResource(mCallGd ? R.mipmap.switch_on : R.mipmap.switch_off);

        mPlCallGd = (boolean) SPUtils.get(SPConstant.SP_CALL_PL_GD, false);
        mPlGdSwitch.setImageResource(mPlCallGd ? R.mipmap.switch_on : R.mipmap.switch_off);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.home_radio_call_phone) {
                    mVisibleTab = 0;
                    bddh_ll.setVisibility(View.VISIBLE);
                    plbd_ll.setVisibility(View.GONE);
                    dxds_ll.setVisibility(View.GONE);
                } else if (checkedId == R.id.home_radio_multi_call) {
                    mVisibleTab = 1;
                    bddh_ll.setVisibility(View.GONE);
                    plbd_ll.setVisibility(View.VISIBLE);
                    dxds_ll.setVisibility(View.GONE);
                } else if (checkedId == R.id.home_radio_send_msg) {
                    mVisibleTab = 2;
                    bddh_ll.setVisibility(View.GONE);
                    plbd_ll.setVisibility(View.GONE);
                    dxds_ll.setVisibility(View.VISIBLE);
                    changeSmsUi();
                }
            }
        });
    }

    private void setBdfs() {
        String call_type = SPUtils.getString(SPConstant.SP_SIM_CARD_TYPE, Constants.SIM_TYPE_SIM_1);
        if (Constants.SIM_TYPE_SIM_1.equals(call_type)) {
            mCallBdfsTv.setText("???????????????1??????");
        } else if (Constants.SIM_TYPE_SIM_2.equals(call_type)) {
            mCallBdfsTv.setText("???????????????2??????");
        } else if (Constants.SIM_TYPE_SECRET.equals(call_type)) {
            mCallBdfsTv.setText("?????????????????????");
        }
    }

    private void selectFunc(int id) {
        if (id == 1) {
            radioGroup.check(R.id.home_radio_call_phone);
            bddh_ll.setVisibility(View.VISIBLE);
            plbd_ll.setVisibility(View.GONE);
            dxds_ll.setVisibility(View.GONE);
        } else if (id == 2) {
            radioGroup.check(R.id.home_radio_multi_call);
            bddh_ll.setVisibility(View.GONE);
            plbd_ll.setVisibility(View.VISIBLE);
            dxds_ll.setVisibility(View.GONE);
        } else if (id == 3) {
            radioGroup.check(R.id.home_radio_send_msg);
            bddh_ll.setVisibility(View.GONE);
            plbd_ll.setVisibility(View.GONE);
            dxds_ll.setVisibility(View.VISIBLE);
        }
    }

    @OnClick({R.id.txl_iv, R.id.dsbh_rl, R.id.bdcs_rl, R.id.bdjg_rl, R.id.bdfs_rl,
            R.id.gd_switch, R.id.home_btn_single_call_now,
            R.id.hmdr_rl, R.id.skzs_rl, R.id.jgsz_rl, R.id.pl_switch_gd, R.id.pl_call_tv,
            R.id.dhfs_switch, R.id.sms_txl_iv, R.id.sms_dsfs_rl, R.id.sms_fscs_rl, R.id.plfs_switch,
            R.id.sms_hmdr_rl, R.id.sms_sksz_rl, R.id.sms_fsjg_rl, R.id.bjdx_rl, R.id.sms_ljfs, R.id.home_single_btn_clear_phone,
            R.id.home_single_btn_clear_time, R.id.multi_iv_clear_interval, R.id.sms_iv_clear_phone,
            R.id.multi_btn_clear_phone, R.id.sms_btn_clear_phone, R.id.home_btn_reload})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.home_btn_reload:
                initData();
                break;
            case R.id.sms_btn_clear_phone:// ????????????????????????
                ivClearSmsImportPhone.setVisibility(View.GONE);
                mSmsHmdrTip.setText("");
                changeSmsUi();
                break;
            case R.id.multi_btn_clear_phone:// ????????????????????????
                mCallHmdrTip.setText("");
                ivMultiCLearPhone.setVisibility(View.GONE);
                List<CallContactTable> callContactTables = DatabaseBusiness.getCallContacts();
                if (callContactTables.size() > 0) {
                    for (CallContactTable callContactTable : callContactTables) {
                        DatabaseBusiness.delCallContact(callContactTable);
                    }
                }
                changePlCallUi();
                break;

            case R.id.sms_iv_clear_phone:// ????????????????????????
                mSmsMobileEt.setText("");
                changeSmsUi();
                break;
            case R.id.multi_iv_clear_interval:// ???????????????????????????
                mCallJgszTip.setText("");
                SPUtils.remove(SPConstant.SP_CALL_JGSZ);
                ivMultiClearInterval.setVisibility(View.GONE);
                changePlCallUi();
                break;
            case R.id.home_single_btn_clear_time:// ????????????
                SPUtils.remove(SPConstant.SP_CALL_TIMING);
                mCallDsbhTv.setText("");
                ivClearTime.setVisibility(View.GONE);
                changeCallUi();
                break;
            case R.id.home_single_btn_clear_phone:// ???????????????
                SPUtils.remove(SPConstant.SP_CALL_SRHM);
                mCallMobileEt.setText("");
                ivClearSinglePhoneNumber.setVisibility(View.GONE);
                changeCallUi();
                break;
            case R.id.txl_iv:
                Intent intent = new Intent(getContext(), ContactsActivity.class);
                intent.putExtra("type", "call");
                startActivity(intent);
                break;
            case R.id.dsbh_rl:
                SetCallTimingDialog setCallTimingDialog = new SetCallTimingDialog(getContext(), "call");
                setCallTimingDialog.show();
                setCallTimingDialog.setOnClickListener(new SetCallTimingDialog.OnClickListener() {
                    @Override
                    public void confirm(String value) {
                        SPUtils.put(SPConstant.SP_CALL_TIMING, value);
                        mCallDsbhTv.setText(value + "?????????");
                        ivClearTime.setVisibility(View.VISIBLE);
                    }
                });
                break;
            case R.id.bdcs_rl:
                SetCallNumDialog setCallNumDialog = new SetCallNumDialog(getContext(), "call");
                setCallNumDialog.show();
                setCallNumDialog.setOnClickListener(new SetCallNumDialog.OnClickListener() {
                    @Override
                    public void confirm(int num) {
                        SPUtils.put(SPConstant.SP_CALL_NUM, num);
                        mCallBdcsTv.setText(num + "???");
                        changeCallUi();
                    }
                });
                break;
            case R.id.bdjg_rl:
                // ????????????
                SetCallIntervalDialog setCallIntervalDialog = new SetCallIntervalDialog(getContext(), "call");
                setCallIntervalDialog.show();
                setCallIntervalDialog.setOnClickListener(new SetCallIntervalDialog.OnClickListener() {
                    @Override
                    public void confirm(int second) {
                        SPUtils.put(SPConstant.SP_CALL_INTERVAL, second);
                        mCallBdjgTv.setText(second + "s");
                        changeCallUi();
                    }
                });
                break;
            case R.id.bdfs_rl:
                intent = new Intent(getContext(), CallTypeActivity.class);
                startActivityForResult(intent, REQUEST_CODE_TAB1_CALL_TYPE);
                break;
            case R.id.gd_switch:
                mCallGd = !mCallGd;
                mGdSwitch.setImageResource(mCallGd ? R.mipmap.switch_on : R.mipmap.switch_off);
                SPUtils.put(SPConstant.SP_CALL_GD, mCallGd);
                break;
            case R.id.home_btn_single_call_now:
                if (!TextUtils.isEmpty(mCallMobileEt.getText().toString())) {
                    FreeCheckUtils.check(getActivity(), FreeCheckUtils.isSecretCall(), new FreeCheckUtils.OnCheckCallback() {
                        @Override
                        public void onResult(boolean free) {
                            if (free) {
                                Intent intent = new Intent(getContext(), SingleAutoTaskActivity.class);
                                intent.putExtra(SingleAutoTaskActivity.KEY_TASK, SingleAutoTaskActivity.VALUE_TASK_DIAL);
                                startActivity(intent);
                            } else {
                                startActivity(new Intent(getContext(), VipActivity.class));
                            }
                        }
                    });
                } else {
                    ToastUtil.show("????????????????????????");
                }
                break;
            case R.id.hmdr_rl:
                ImportDialog importDialog = new ImportDialog(getContext());
                importDialog.show();
                importDialog.setOnClickListener(new ImportDialog.OnClickListener() {
                    @Override
                    public void wordImport() {
                        importDialog.dismiss();
                        DocImportDialog docImportDialog = new DocImportDialog(getContext());
                        docImportDialog.show();
                        docImportDialog.setOnClickListener(new DocImportDialog.OnClickListener() {
                            @Override
                            public void wordImport() {
                                Intent intent1 = new Intent(getContext(), DocImportActivity.class);
                                intent1.putExtra("type", "word");
                                intent1.putExtra("type2", "call");
                                startActivity(intent1);
                                docImportDialog.dismiss();
                            }

                            @Override
                            public void excelImport() {
                                Intent intent1 = new Intent(getContext(), DocImportActivity.class);
                                intent1.putExtra("type", "excel");
                                intent1.putExtra("type2", "call");
                                startActivity(intent1);
                                docImportDialog.dismiss();
                            }

                            @Override
                            public void txtImport() {
                                Intent intent1 = new Intent(getContext(), DocImportActivity.class);
                                intent1.putExtra("type", "txt");
                                intent1.putExtra("type2", "call");
                                startActivity(intent1);
                                docImportDialog.dismiss();
                            }
                        });
                    }

                    @Override
                    public void txlImport() {
                        Intent intent1 = new Intent(getContext(), ImportContactsActivity.class);
                        intent1.putExtra("type", "call");
                        startActivity(intent1);
                        importDialog.dismiss();
                    }

                    @Override
                    public void copyImport() {
                        Intent intent1 = new Intent(getContext(), CopyImportActivity.class);
                        intent1.putExtra("type", "call");
                        startActivity(intent1);
                        importDialog.dismiss();
                    }
                });
                break;
            case R.id.skzs_rl:
                intent = new Intent(getContext(), SimSettingActivity.class);
                intent.putExtra("type", "call");
                startActivity(intent);
                break;
            case R.id.jgsz_rl:
                SetMultiCallIntervalDialog setMultiCallIntervalDialog = new SetMultiCallIntervalDialog(getContext());
                setMultiCallIntervalDialog.show();
                setMultiCallIntervalDialog.setOnClickListener(new SetMultiCallIntervalDialog.OnClickListener() {
                    @Override
                    public void confirm(String time) {
                        SPUtils.put(SPConstant.SP_CALL_JGSZ, time);
                        mCallJgszTip.setText(Utils.getCallInterval());
                        ivMultiClearInterval.setVisibility(View.VISIBLE);
                        changePlCallUi();
                    }
                });
                break;
            case R.id.pl_switch_gd:
                mPlCallGd = !mPlCallGd;
                mPlGdSwitch.setImageResource(mPlCallGd ? R.mipmap.switch_on : R.mipmap.switch_off);
                SPUtils.put(SPConstant.SP_CALL_PL_GD, mPlCallGd);
                break;
//            case R.id.pl_switch_gd2:
//                mPlCallGd = !mPlCallGd;
//                mPlGdSwitch.setImageResource(mPlCallGd ? R.mipmap.switch_on : R.mipmap.switch_off);
//                mPlGdSwitch2.setImageResource(mPlCallGd ? R.mipmap.switch_off : R.mipmap.switch_on);
//                SPUtils.put(SPConstant.SP_CALL_PL_GD, mPlCallGd);
//                break;
            case R.id.pl_call_tv:
                if (!TextUtils.isEmpty(mCallHmdrTip.getText().toString()) &&
                        !TextUtils.isEmpty(mCallJgszTip.getText().toString())) {
                    FreeCheckUtils.check(getActivity(), false, new FreeCheckUtils.OnCheckCallback() {
                        @Override
                        public void onResult(boolean free) {
                            if (free) {
                                Intent intent = new Intent(getContext(), AutoCallPhoneActivity.class);
                                intent.putExtra("type", "plbd");
                                startActivity(intent);
                            } else {
                                startActivity(new Intent(getContext(), VipActivity.class));
                            }
                        }
                    });
                } else {
                    ToastUtil.show("?????????????????????????????????");
                }
                break;
            //????????????
            case R.id.dhfs_switch:
                sms_dhfs_open = !sms_dhfs_open;
                dhfs_switch.setImageResource(sms_dhfs_open ? R.mipmap.switch_on : R.mipmap.switch_off);
                dhfs_ll.setVisibility(sms_dhfs_open ? View.VISIBLE : View.GONE);
                plfs_switch.setImageResource(sms_dhfs_open ? R.mipmap.switch_off : R.mipmap.switch_on);
                plfs_ll.setVisibility(sms_dhfs_open ? View.GONE : View.VISIBLE);
                changeSmsUi();
                break;
            case R.id.sms_txl_iv:
                intent = new Intent(getContext(), ContactsActivity.class);
                intent.putExtra("type", "sms");
                startActivity(intent);
                break;
            case R.id.sms_dsfs_rl:
                setCallTimingDialog = new SetCallTimingDialog(getContext(), "sms");
                setCallTimingDialog.show();
                setCallTimingDialog.setOnClickListener(new SetCallTimingDialog.OnClickListener() {
                    @Override
                    public void confirm(String value) {
                        SPUtils.put(SPConstant.SP_SMS_DSFS, value);
                        mSmsDsfsTip.setText(value + "?????????");
                    }
                });
                break;
            case R.id.sms_fscs_rl:
                setCallNumDialog = new SetCallNumDialog(getContext(), "sms");
                setCallNumDialog.show();
                setCallNumDialog.setOnClickListener(new SetCallNumDialog.OnClickListener() {
                    @Override
                    public void confirm(int num) {
                        SPUtils.put(SPConstant.SP_SMS_FSCS, num);
                        mSmsFscsTip.setText(num + "???");
                        changeSmsUi();
                    }
                });
                break;
            case R.id.plfs_switch:
                sms_dhfs_open = !sms_dhfs_open;
                dhfs_switch.setImageResource(sms_dhfs_open ? R.mipmap.switch_on : R.mipmap.switch_off);
                dhfs_ll.setVisibility(sms_dhfs_open ? View.VISIBLE : View.GONE);
                plfs_switch.setImageResource(sms_dhfs_open ? R.mipmap.switch_off : R.mipmap.switch_on);
                plfs_ll.setVisibility(sms_dhfs_open ? View.GONE : View.VISIBLE);
                changeSmsUi();
                break;
            case R.id.sms_hmdr_rl:
                importDialog = new ImportDialog(getContext());
                importDialog.show();
                importDialog.setOnClickListener(new ImportDialog.OnClickListener() {
                    @Override
                    public void wordImport() {
                        importDialog.dismiss();
                        DocImportDialog docImportDialog = new DocImportDialog(getContext());
                        docImportDialog.show();
                        docImportDialog.setOnClickListener(new DocImportDialog.OnClickListener() {
                            @Override
                            public void wordImport() {
                                Intent intent1 = new Intent(getContext(), DocImportActivity.class);
                                intent1.putExtra("type", "word");
                                intent1.putExtra("type2", "sms");
                                startActivity(intent1);
                                docImportDialog.dismiss();
                            }

                            @Override
                            public void excelImport() {
                                Intent intent1 = new Intent(getContext(), DocImportActivity.class);
                                intent1.putExtra("type", "excel");
                                intent1.putExtra("type2", "sms");
                                startActivity(intent1);
                                docImportDialog.dismiss();
                            }

                            @Override
                            public void txtImport() {
                                Intent intent1 = new Intent(getContext(), DocImportActivity.class);
                                intent1.putExtra("type", "txt");
                                intent1.putExtra("type2", "sms");
                                startActivity(intent1);
                                docImportDialog.dismiss();
                            }
                        });
                    }

                    @Override
                    public void txlImport() {
                        Intent intent1 = new Intent(getContext(), ImportContactsActivity.class);
                        intent1.putExtra("type", "sms");
                        startActivity(intent1);
                        importDialog.dismiss();
                    }

                    @Override
                    public void copyImport() {
                        Intent intent1 = new Intent(getContext(), CopyImportActivity.class);
                        intent1.putExtra("type", "sms");
                        startActivity(intent1);
                        importDialog.dismiss();
                    }
                });
                break;
            case R.id.sms_sksz_rl:
                intent = new Intent(getContext(), SimSettingActivity.class);
                intent.putExtra("type", "sms");
                startActivity(intent);
                break;
            case R.id.sms_fsjg_rl:
                setCallIntervalDialog = new SetCallIntervalDialog(getContext(), "sms");
                setCallIntervalDialog.show();
                setCallIntervalDialog.setOnClickListener(new SetCallIntervalDialog.OnClickListener() {
                    @Override
                    public void confirm(int second) {
                        SPUtils.put(SPConstant.SP_SMS_FSJG, second);
                        mSmsFsjgTip.setText(second + "s");
                        changeSmsUi();
                    }
                });
                break;
            case R.id.bjdx_rl:
                startActivity(new Intent(getContext(), EditSmsActivity.class));
                break;
            case R.id.sms_ljfs:
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, 999);
                    return;
                }
                autoSendSms();
                break;
        }
    }

    private void autoSendSms() {
        final SubscriptionManager sManager = (SubscriptionManager) getContext().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        @SuppressLint("MissingPermission")
        List<SubscriptionInfo> list = sManager.getActiveSubscriptionInfoList();
        if (list == null || list.isEmpty()) {
            ToastUtil.showLong("??????????????????sim???????????????????????????");
            return;
        }
        FreeCheckUtils.check(getActivity(), false, new FreeCheckUtils.OnCheckCallback() {
            @Override
            public void onResult(boolean free) {
                if (free) {
                    toSmsSendActivity();
                } else {
                    startActivity(new Intent(getContext(), VipActivity.class));
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 999) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                autoSendSms();
            } else {
                ToastUtil.show("???????????????????????????????????????");
                CheckTipDialog dialog = new CheckTipDialog(getActivity());
                dialog.setTitle("??????");
                dialog.setContent("????????????app????????????????????????????????????app?????????????????????????????????");
                dialog.setCancelListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToastUtil.show("?????????????????????????????????????????????");
                    }
                });
                dialog.setPositiveListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.mob.sms"));
                        startActivity(intent);
                    }
                });
                dialog.show();
            }
        }
    }

    private void toSmsSendActivity() {
        Intent intent;
        if (sms_dhfs_open) {
            //????????????
            if (!TextUtils.isEmpty(mSmsMobileEt.getText().toString()) &&
                    !TextUtils.isEmpty(mBjdxTip.getText().toString())) {
                if (isShowSendTimes && TextUtils.isEmpty(mSmsFscsTip.getText().toString())) {
                    ToastUtil.show("?????????????????????");
                } else {
                    intent = new Intent(getContext(), AutoSendSmsActivity.class);
                    intent.putExtra("type", "dhfs");
                    startActivity(intent);
                }
            } else {
                ToastUtil.show("??????????????????????????????????????????");
            }
        } else {
            //????????????
            if (!TextUtils.isEmpty(mSmsHmdrTip.getText().toString()) &&
                    !TextUtils.isEmpty(mSmsFsjgTip.getText().toString()) &&
                    !TextUtils.isEmpty(mBjdxTip.getText().toString())) {
                intent = new Intent(getContext(), AutoSendSmsActivity.class);
                intent.putExtra("type", "plfs");
                startActivity(intent);
            }
        }
    }

    private void checkPermission() {
        // ???????????????
        RetrofitHelper.getApi().getMarketCharge(MyApplication.Channel)
                .subscribe(new SimpleObserver<BaseResponse<ChannelChargeBean>>() {
                    @Override
                    public void onNext(BaseResponse<ChannelChargeBean> response) {
                        if (response != null && response.data != null) {
                            switch (response.data.status) {
                                case "0":
                                    Intent intent = new Intent(getContext(), SingleAutoTaskActivity.class);
                                    intent.putExtra("type", "dhbd");
                                    startActivity(intent);
                                    break;
                                default:
                                    checkUserVip();
                                    break;
                            }
                        }

                    }
                });
    }

    private void checkUserVip() {
        RetrofitHelper.getApi().cloudDial()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<CloudPermissionBean>() {
                    @Override
                    public void onNext(CloudPermissionBean permissionBean) {
                        if (permissionBean != null && "200".equals(permissionBean.code)) {
                            // ?????????
                            Intent intent = new Intent(getContext(), SingleAutoTaskActivity.class);
                            intent.putExtra("type", "dhbd");
                            startActivity(intent);
                        } else if ("500".equals(permissionBean.code)) {
                            ToastUtil.show(permissionBean.msg);
                        } else {
                            startActivity(new Intent(getContext(), VipActivity.class));
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();

        //????????????
        String singlePhoneNumber = SPUtils.getString(SPConstant.SP_CALL_SRHM, "");
        if (TextUtils.isEmpty(singlePhoneNumber)) {
            mCallMobileEt.setText("");
            mCallMobileEt.setHint("?????????????????????");
            ivClearSinglePhoneNumber.setVisibility(View.GONE);
        } else {
            mCallMobileEt.setText(singlePhoneNumber);
            ivClearSinglePhoneNumber.setVisibility(View.VISIBLE);
        }

        if (TextUtils.isEmpty(SPUtils.getString(SPConstant.SP_CALL_TIMING, ""))) {
            mCallDsbhTv.setText("");
            ivClearTime.setVisibility(View.GONE);
        } else {
            mCallDsbhTv.setText(SPUtils.getString(SPConstant.SP_CALL_TIMING, "") + "?????????");
            ivClearTime.setVisibility(View.VISIBLE);
        }
        setBdfs();
        // ??????????????????1
        if (SPUtils.getInt(SPConstant.SP_CALL_NUM, 1) != 0) {
            mCallBdcsTv.setText(SPUtils.getInt(SPConstant.SP_CALL_NUM, 1) + "???");
        }
        // ????????????
        int callInterval = SPUtils.getInt(SPConstant.SP_CALL_INTERVAL, 20);
        if (callInterval != 0) {
            mCallBdjgTv.setText(callInterval + "s");
        }
        if (SPUtils.getBoolean(SPConstant.SP_CALL_GD, false)) {
            mGdSwitch.setImageResource(R.mipmap.switch_on);
        } else {
            mGdSwitch.setImageResource(R.mipmap.switch_off);
        }

        //??????????????????
        List<CallContactTable> callContactTables = DatabaseBusiness.getCallContacts();
        if (callContactTables.size() == 0) {
            mCallHmdrTip.setText("");
        } else {
            mCallHmdrTip.setText("?????????" + callContactTables.size() + "????????????");
        }
        String doubleSimSetting = SPUtils.getString(SPConstant.SP_CALL_SKSZ, "sim1");
        if (TextUtils.isEmpty(doubleSimSetting)) {
            mCallSkszTip.setText("");
        } else if ("sim1".equals(doubleSimSetting)) {
            mCallSkszTip.setText("?????????1??????");
        } else if ("sim2".equals(doubleSimSetting)) {
            mCallSkszTip.setText("?????????2??????");
        } else if ("sim_double".equals(doubleSimSetting)) {
            mCallSkszTip.setText("??????????????????");
        }
        // ????????????
        mCallJgszTip.setText(Utils.getCallInterval());

        //??????
        if (TextUtils.isEmpty(SPUtils.getString(SPConstant.SP_SMS_SRHM, ""))) {
            mSmsMobileEt.setText("");
            mSmsMobileEt.setHint("?????????????????????");
        } else {
            mSmsMobileEt.setText(SPUtils.getString(SPConstant.SP_SMS_SRHM, ""));
        }
        if (TextUtils.isEmpty(SPUtils.getString(SPConstant.SP_SMS_DSFS, ""))) {
            mSmsDsfsTip.setText("");
        } else {
            mSmsDsfsTip.setText(SPUtils.getString(SPConstant.SP_SMS_DSFS, "") + "?????????");
        }
        if (SPUtils.getInt(SPConstant.SP_SMS_FSCS, 0) == 0) {
            mSmsFscsTip.setText("");
        } else {
            mSmsFscsTip.setText(SPUtils.getInt(SPConstant.SP_SMS_FSCS, 0) + "");
        }

        List<SmsContactTable> smsContactTables = DatabaseBusiness.getSmsContacts();
        if (smsContactTables.size() == 0) {
            mSmsHmdrTip.setText("");
        } else {
            mSmsHmdrTip.setText("?????????" + smsContactTables.size() + "????????????");
        }
        if (TextUtils.isEmpty(SPUtils.getString(SPConstant.SP_SMS_SKSZ, ""))) {
            mSmsSkszTip.setText("");
        } else if ("sim1".equals(SPUtils.getString(SPConstant.SP_SMS_SKSZ, ""))) {
            mSmsSkszTip.setText("?????????1??????");
        } else if ("sim2".equals(SPUtils.getString(SPConstant.SP_SMS_SKSZ, ""))) {
            mSmsSkszTip.setText("?????????2??????");
        } else if ("sim_double".equals(SPUtils.getString(SPConstant.SP_SMS_SKSZ, ""))) {
            mSmsSkszTip.setText("??????????????????");
        }

        if (SPUtils.getInt(SPConstant.SP_SMS_FSJG, 0) == 0) {
            mSmsFsjgTip.setText("");
        } else {
            mSmsFsjgTip.setText(SPUtils.getInt(SPConstant.SP_SMS_FSJG, 0) + "s");
        }
        if (TextUtils.isEmpty(SPUtils.getString(SPConstant.SP_SMS_CONTENT, ""))) {
            mBjdxTip.setText("");
        } else {
            mBjdxTip.setText("?????????");
        }

        mCallMobileEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String phone = mCallMobileEt.getText().toString();
                SPUtils.put(SPConstant.SP_CALL_SRHM, phone);
                changeCallUi();
                if (TextUtils.isEmpty(phone)) {
                    ivClearSinglePhoneNumber.setVisibility(View.GONE);
                } else {
                    ivClearSinglePhoneNumber.setVisibility(View.VISIBLE);
                }
            }
        });

        mSmsMobileEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String phone = mSmsMobileEt.getText().toString();
                SPUtils.put(SPConstant.SP_SMS_SRHM, phone);
                if (TextUtils.isEmpty(phone)) {
                    ivClearSmsPhone.setVisibility(View.GONE);
                } else {
                    ivClearSmsPhone.setVisibility(View.VISIBLE);
                }
                changeSmsUi();
            }
        });


        changeCallUi();
        changePlCallUi();
        //????????????
        changeSmsUi();
    }

    private void changeCallUi() {
        //????????????
        mCallTv.setEnabled(!TextUtils.isEmpty(mCallMobileEt.getText().toString()) &&
                !TextUtils.isEmpty(mCallBdcsTv.getText().toString()) &&
                !TextUtils.isEmpty(mCallBdjgTv.getText().toString()) &&
                !TextUtils.isEmpty(mCallBdfsTv.getText().toString()));
    }

    private void changePlCallUi() {
        //??????????????????
        mPlCallTv.setEnabled(!TextUtils.isEmpty(mCallHmdrTip.getText().toString()) &&
                !TextUtils.isEmpty(mCallJgszTip.getText().toString()));
    }

    private void changeSmsUi() {
        //????????????
        if (sms_dhfs_open) {
            //????????????
            boolean b = (smsSendTimesLayout.getVisibility() == View.VISIBLE && !TextUtils.isEmpty(mSmsFscsTip.getText().toString())) || smsSendTimesLayout.getVisibility() == View.GONE;
            if (!TextUtils.isEmpty(mSmsMobileEt.getText().toString()) && b &&
                    !TextUtils.isEmpty(mBjdxTip.getText().toString())) {
                mSmsLjfs.setEnabled(true);
            } else {
                mSmsLjfs.setEnabled(false);
            }
        } else {
            //????????????
            if (!TextUtils.isEmpty(mSmsHmdrTip.getText().toString()) &&
                    !TextUtils.isEmpty(mSmsFsjgTip.getText().toString()) &&
                    !TextUtils.isEmpty(mBjdxTip.getText().toString())) {
                mSmsLjfs.setEnabled(true);
            } else {
                mSmsLjfs.setEnabled(false);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_TAB1_CALL_TYPE:
                setBdfs();
                break;
        }
    }
}
