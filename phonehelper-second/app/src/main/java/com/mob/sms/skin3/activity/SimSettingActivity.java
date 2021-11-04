package com.mob.sms.skin3.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.mob.sms.skin3.R;
import com.mob.sms.skin3.base.BaseActivity;
import com.mob.sms.skin3.utils.Constants;
import com.mob.sms.skin3.utils.SPConstant;
import com.mob.sms.skin3.utils.SPUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SimSettingActivity extends BaseActivity {
    @BindView(R.id.sim1_iv)
    ImageView mSim1Iv;
    @BindView(R.id.sim1_tv)
    TextView mSim1Tv;
    @BindView(R.id.sim2_iv)
    ImageView mSim2Iv;
    @BindView(R.id.sim2_tv)
    TextView mSim2Tv;
    @BindView(R.id.sim_double_iv)
    ImageView mSimDoubleIv;
    @BindView(R.id.sim_double_tv)
    TextView mSimDoubleTv;
    @BindView(R.id.sim_double_rl1)
    RelativeLayout mSimDoubleRl1;
    @BindView(R.id.sim_double_rl2)
    RelativeLayout mSimDoubleRl2;
    @BindView(R.id.divider1)
    View mDivider1;
    @BindView(R.id.divider2)
    View mDivider2;
    @BindView(R.id.sim1_num)
    TextView tvSim1Num;
    @BindView(R.id.sim2_num)
    TextView tvSim2Num;

    private String mType;
    private String mSimType;

//    private final String SIM1 = "sim1";
//    private final String SIM2 = "sim2";
//    private final String SIM_DOUBLE = "sim_double";

    private int sim1Count = 1, sim2Count = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sim_setting);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
        initView();
    }

    private void initView() {
        mType = getIntent().getStringExtra("type");
        if ("call".equals(mType)) {
            mSimType = SPUtils.getString(SPConstant.SP_CALL_SKSZ, Constants.SIM_TYPE_SIM_1);
            mSim1Tv.setText("使用卡1拨打");
            mSim2Tv.setText("使用卡2拨打");
            mSimDoubleTv.setText("双卡轮流拨打");
        } else if ("sms".equals(mType)) {
            mSimType = SPUtils.getString(SPConstant.SP_SMS_SKSZ, "SIM1");
            mSim1Tv.setText("使用卡1发送");
            mSim2Tv.setText("使用卡2发送");
            mSimDoubleTv.setText("双卡轮流发送");
        }
        if (Constants.SIM_TYPE_SIM_1.equals(mSimType)) {
            mSim1Iv.setImageResource(R.mipmap.switch_on);
            mSim2Iv.setImageResource(R.mipmap.switch_off);
            mSimDoubleIv.setImageResource(R.mipmap.switch_off);
            mSimDoubleRl1.setVisibility(View.GONE);
            mSimDoubleRl2.setVisibility(View.GONE);
            mDivider1.setVisibility(View.GONE);
            mDivider2.setVisibility(View.GONE);
        } else if (Constants.SIM_TYPE_SIM_2.equals(mSimType)) {
            mSim1Iv.setImageResource(R.mipmap.switch_off);
            mSim2Iv.setImageResource(R.mipmap.switch_on);
            mSimDoubleIv.setImageResource(R.mipmap.switch_off);
            mSimDoubleRl1.setVisibility(View.GONE);
            mSimDoubleRl2.setVisibility(View.GONE);
            mDivider1.setVisibility(View.GONE);
            mDivider2.setVisibility(View.GONE);
        } else if (Constants.SIM_TYPE_SIM_MIX.equals(mSimType)) {
            mSim1Iv.setImageResource(R.mipmap.switch_off);
            mSim2Iv.setImageResource(R.mipmap.switch_off);
            mSimDoubleIv.setImageResource(R.mipmap.switch_on);
            mSimDoubleRl1.setVisibility(View.VISIBLE);
            mSimDoubleRl2.setVisibility(View.VISIBLE);
            mDivider1.setVisibility(View.VISIBLE);
            mDivider2.setVisibility(View.VISIBLE);

            String sim1count = SPUtils.getString(SPConstant.SP_SIM_1_CALL_COUNT, "1");
            String sim2count = SPUtils.getString(SPConstant.SP_SIM_2_CALL_COUNT, "1");
            tvSim1Num.setText(sim1count);
            tvSim2Num.setText(sim2count);
            sim1Count = Integer.parseInt(sim1count);
            sim2Count = Integer.parseInt(sim2count);
        }
    }

    @OnClick({R.id.back, R.id.sim1_iv, R.id.sim2_iv, R.id.sim_double_iv,
            R.id.sim1_subtract, R.id.sim1_plus, R.id.sim2_subtract, R.id.sim2_plus})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.sim1_subtract:// 卡1减
                if (sim1Count > 1) {
                    sim1Count--;
                    tvSim1Num.setText(String.valueOf(sim1Count));
                    SPUtils.put(SPConstant.SP_SIM_1_CALL_COUNT, String.valueOf(sim1Count));
                }
                break;
            case R.id.sim2_subtract:// 卡2减
                if (sim2Count > 1) {
                    sim2Count--;
                    tvSim2Num.setText(String.valueOf(sim2Count));
                    SPUtils.put(SPConstant.SP_SIM_2_CALL_COUNT, String.valueOf(sim2Count));
                }
                break;
            case R.id.sim1_plus:// 卡1 加
                sim1Count++;
                tvSim1Num.setText(String.valueOf(sim1Count));
                SPUtils.put(SPConstant.SP_SIM_1_CALL_COUNT, String.valueOf(sim1Count));
                break;
            case R.id.sim2_plus:// 卡2 加
                sim2Count++;
                tvSim2Num.setText(String.valueOf(sim2Count));
                SPUtils.put(SPConstant.SP_SIM_2_CALL_COUNT, String.valueOf(sim2Count));
                break;
            case R.id.back:
                finish();
                break;
            case R.id.sim1_iv:
                mSim1Iv.setImageResource(R.mipmap.switch_on);
                mSim2Iv.setImageResource(R.mipmap.switch_off);
                mSimDoubleIv.setImageResource(R.mipmap.switch_off);
                mSimDoubleRl1.setVisibility(View.GONE);
                mSimDoubleRl2.setVisibility(View.GONE);
                mDivider1.setVisibility(View.GONE);
                mDivider2.setVisibility(View.GONE);
                if ("call".equals(mType)) {
                    SPUtils.put(SPConstant.SP_CALL_SKSZ, Constants.SIM_TYPE_SIM_1);
                } else if ("sms".equals(mType)) {
                    SPUtils.put(SPConstant.SP_SMS_SKSZ, Constants.SIM_TYPE_SIM_1);
                }
                break;
            case R.id.sim2_iv:
                mSim1Iv.setImageResource(R.mipmap.switch_off);
                mSim2Iv.setImageResource(R.mipmap.switch_on);
                mSimDoubleIv.setImageResource(R.mipmap.switch_off);
                mSimDoubleRl1.setVisibility(View.GONE);
                mSimDoubleRl2.setVisibility(View.GONE);
                mDivider1.setVisibility(View.GONE);
                mDivider2.setVisibility(View.GONE);
                if ("call".equals(mType)) {
                    SPUtils.put(SPConstant.SP_CALL_SKSZ, Constants.SIM_TYPE_SIM_2);
                } else if ("sms".equals(mType)) {
                    SPUtils.put(SPConstant.SP_SMS_SKSZ, Constants.SIM_TYPE_SIM_2);
                }
                break;
            case R.id.sim_double_iv:
                mSim1Iv.setImageResource(R.mipmap.switch_off);
                mSim2Iv.setImageResource(R.mipmap.switch_off);
                mSimDoubleIv.setImageResource(R.mipmap.switch_on);
                mSimDoubleRl1.setVisibility(View.VISIBLE);
                mSimDoubleRl2.setVisibility(View.VISIBLE);
                mDivider1.setVisibility(View.VISIBLE);
                mDivider2.setVisibility(View.VISIBLE);
                if ("call".equals(mType)) {
                    SPUtils.put(SPConstant.SP_CALL_SKSZ, Constants.SIM_TYPE_SIM_MIX);
                } else if ("sms".equals(mType)) {
                    SPUtils.put(SPConstant.SP_SMS_SKSZ, Constants.SIM_TYPE_SIM_MIX);
                }
                break;
        }
    }

}
