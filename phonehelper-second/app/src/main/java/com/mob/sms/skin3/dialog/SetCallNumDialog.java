package com.mob.sms.skin3.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mob.sms.skin3.R;
import com.mob.sms.skin3.config.GlobalConfig;
import com.zyyoona7.wheel.WheelView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SetCallNumDialog extends Dialog {
    private Unbinder bind;
    @BindView(R.id.wheelview)
    WheelView mWheelView;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.tv_max_call_times)
    TextView mTvMaxCall;

    private int mSelectNum = 1;
    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void confirm(int num);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public SetCallNumDialog(@NonNull Context context, String type) {
        this(context, R.style.dialogNoBg, type);
    }

    private SetCallNumDialog(@NonNull Context context, int themeResId, String type) {
        super(context, themeResId);

        View view = View.inflate(context, R.layout.dialog_set_callnum, null);
        bind = ButterKnife.bind(this, view);

        setContentView(view);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mTitle.setText("call".equals(type)?"拨打次数":"发送次数");

        List<Integer> list = new ArrayList<>();
        int max = 100;
        if ("call".equals(type)) {
            mTvMaxCall.setText(String.format("最多拨打%s次", GlobalConfig.oneDialTimes));
            max = GlobalConfig.oneDialTimes;
        }
        for (int i = 1; i <= max; i++) {
            list.add(i);
        }
        mWheelView.setData(list);
        mWheelView.setSelectedItemPosition(0);

        mWheelView.setOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelView wheelView, Object data, int position) {
                mSelectNum = position + 1;
            }
        });
    }

    @OnClick({R.id.cancel, R.id.confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                dismiss();
                break;
            case R.id.confirm:
                if (mOnClickListener != null) {
                    mOnClickListener.confirm(mSelectNum);
                }
                dismiss();
                break;
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        bind.unbind();
    }
}
