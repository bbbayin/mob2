package com.mob.sms3.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.mob.sms3.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SortDialog extends Dialog {
    private Unbinder bind;


    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void sort(int type);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public SortDialog(@NonNull Context context) {
        this(context, R.style.dialogNoBg);
    }

    private SortDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);

        View view = View.inflate(context, R.layout.dialog_sort, null);
        bind = ButterKnife.bind(this, view);

        setContentView(view);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @OnClick({R.id.sort1, R.id.sort2, R.id.sort3, R.id.sort4, R.id.sort5, R.id.sort6, R.id.sort7})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.sort1:
                if (mOnClickListener != null) {
                    mOnClickListener.sort(1);
                }
                break;
            case R.id.sort2:
                if (mOnClickListener != null) {
                    mOnClickListener.sort(2);
                }
                break;
            case R.id.sort3:
                if (mOnClickListener != null) {
                    mOnClickListener.sort(3);
                }
                break;
            case R.id.sort4:
                if (mOnClickListener != null) {
                    mOnClickListener.sort(4);
                }
                break;
            case R.id.sort5:
                if (mOnClickListener != null) {
                    mOnClickListener.sort(5);
                }
                break;
            case R.id.sort6:
                if (mOnClickListener != null) {
                    mOnClickListener.sort(6);
                }
                break;
            case R.id.sort7:
                if (mOnClickListener != null) {
                    mOnClickListener.sort(7);
                }
                break;
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        bind.unbind();
    }
}
