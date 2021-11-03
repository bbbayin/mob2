package com.mob.sms3.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mob.sms3.R;
import com.zyyoona7.wheel.WheelView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SetCallTimingDialog extends Dialog {
    private Unbinder bind;
    @BindView(R.id.wheelview)
    WheelView mWheelView;
    @BindView(R.id.wheelview2)
    WheelView mWheelView2;
    @BindView(R.id.wheelview3)
    WheelView mWheelView3;
    @BindView(R.id.dsbh_tv)
    TextView dsbh_tv;
    @BindView(R.id.zdybh_tv)
    TextView zdybh_tv;
    @BindView(R.id.tip)
    TextView tip;
    @BindView(R.id.dsbh_ll)
    LinearLayout dsbh_ll;
    @BindView(R.id.wheelview4)
    WheelView mWheelView4;
    @BindView(R.id.wheelview5)
    WheelView mWheelView5;
    @BindView(R.id.wheelview6)
    WheelView mWheelView6;
    @BindView(R.id.wheelview7)
    WheelView mWheelView7;
    @BindView(R.id.zdybh_ll)
    LinearLayout zdybh_ll;

    private String mHourValue;
    private String mMinValue;
    private String mSecondValue;
    private OnClickListener mOnClickListener;

    private String mDateValue;
    private String mDateValue2;
    private String mHourValue2;
    private String mMinValue2;

    public interface OnClickListener {
        void confirm(String value);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public SetCallTimingDialog(@NonNull Context context, String type) {
        this(context, R.style.dialogNoBg, type);
    }

    private SetCallTimingDialog(@NonNull Context context, int themeResId, String type) {
        super(context, themeResId);

        View view = View.inflate(context, R.layout.dialog_set_calltiming, null);
        bind = ButterKnife.bind(this, view);

        setContentView(view);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if ("call".equals(type)) {
            dsbh_tv.setText("定时拨号");
            zdybh_tv.setText("自定义拨号");
            tip.setText("后拨打");
        } else if ("sms".equals(type)) {
            dsbh_tv.setText("定时发送");
            zdybh_tv.setText("自定义发送");
            tip.setText("后发送");
        }

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            if (i < 10) {
                list.add("0" + i);
            } else {
                list.add(i + "");
            }
        }
        mWheelView.setData(list);
        mWheelView.setSelectedItemPosition(0);
        mHourValue = list.get(0);

        List<String> list6 = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            if (i < 10) {
                list6.add("0" + i);
            } else {
                list6.add(i + "");
            }
        }
        mWheelView6.setData(list6);
        mWheelView6.setSelectedItemPosition(0);
        mHourValue2 = list6.get(0);

        List<String> list2 = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            if (i < 10) {
                list2.add("0" + i);
            } else {
                list2.add(i + "");
            }
        }
        mWheelView2.setData(list2);
        mWheelView2.setSelectedItemPosition(0);
        mMinValue = list2.get(0);
        mWheelView7.setData(list2);
        mWheelView7.setSelectedItemPosition(0);
        mMinValue2 = list2.get(0);

        mWheelView3.setData(list2);
        mWheelView3.setSelectedItemPosition(6);
        mSecondValue = list2.get(6);

        List<String> list3 = new ArrayList<>();
        long time = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            long time1 = time + 24 * 60 * 60 * 1000 * i;
            String timestr = new SimpleDateFormat("MM-dd").format(new Date(time1));
            list3.add(timestr);
        }
        mWheelView4.setData(list3);
        mWheelView4.setSelectedItemPosition(2);
        mDateValue = list3.get(2);

        List<String> list4 = new ArrayList<>();
        list4.add("上午");
        list4.add("下午");
        list4.add("");
        mWheelView5.setData(list4);
        mDateValue2 = list4.get(0);

        mWheelView.setOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelView wheelView, Object data, int position) {
                mHourValue = list.get(position);
            }
        });
        mWheelView2.setOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelView wheelView, Object data, int position) {
                mMinValue = list2.get(position);
            }
        });
        mWheelView3.setOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelView wheelView, Object data, int position) {
                mSecondValue = list2.get(position);
            }
        });

        mWheelView4.setOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelView wheelView, Object data, int position) {
                mDateValue = list3.get(position);
            }
        });

        mWheelView5.setOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelView wheelView, Object data, int position) {
                mDateValue2 = list4.get(position);
            }
        });

        mWheelView6.setOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelView wheelView, Object data, int position) {
                mHourValue2 = list6.get(position);
            }
        });
        mWheelView7.setOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelView wheelView, Object data, int position) {
                mMinValue2 = list2.get(position);
            }
        });
    }

    @OnClick({R.id.cancel, R.id.confirm, R.id.dsbh_tv, R.id.zdybh_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                dismiss();
                break;
            case R.id.confirm:
                if (mOnClickListener != null) {
                    if(dsbh_ll.getVisibility() == View.VISIBLE){
                        mOnClickListener.confirm(mHourValue + "时" + mMinValue + "分" + mSecondValue + "秒");
                    } else {
                        mOnClickListener.confirm(mDateValue + mDateValue2 + mHourValue2 + "时" + mMinValue2 + "分");
                    }
                }
                dismiss();
                break;
            case R.id.dsbh_tv:
                dsbh_ll.setVisibility(View.VISIBLE);
                zdybh_ll.setVisibility(View.GONE);
                dsbh_tv.setTextColor(Color.parseColor("#454545"));
                zdybh_tv.setTextColor(Color.parseColor("#A6A6A6"));
                break;
            case R.id.zdybh_tv:
                dsbh_ll.setVisibility(View.GONE);
                zdybh_ll.setVisibility(View.VISIBLE);
                zdybh_tv.setTextColor(Color.parseColor("#454545"));
                dsbh_tv.setTextColor(Color.parseColor("#A6A6A6"));
                break;
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        bind.unbind();
    }
}
