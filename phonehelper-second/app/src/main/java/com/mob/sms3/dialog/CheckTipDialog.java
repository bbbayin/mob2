package com.mob.sms3.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mob.sms3.R;

public class CheckTipDialog extends Dialog {
    private View btnCancel;
    private View btnLine;
    private View btnConfirm;
    private View.OnClickListener cancelListener, confirmListener;

    public CheckTipDialog(@NonNull Context context) {
        this(context, R.style.dialogNoBg);
    }

    private CheckTipDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);

        View view = View.inflate(context, R.layout.dialog_check_tip, null);
        setContentView(view);

        btnCancel = findViewById(R.id.dialog_btn_cancel);
        btnLine = findViewById(R.id.dialog_btn_line);
        btnConfirm = findViewById(R.id.dialog_btn_confirm);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (cancelListener!=null) {
                    cancelListener.onClick(v);
                }
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (confirmListener != null) {
                    confirmListener.onClick(v);
                }
            }
        });
    }

    public void setContent(String content) {
        TextView tvcontent = findViewById(R.id.tip);
        tvcontent.setText(content);
    }

    public void setTitle(String title) {
        TextView tvTitle = findViewById(R.id.title);
        tvTitle.setText(title);
    }

    public void setCancelListener(View.OnClickListener cancelListener) {
        if (cancelListener != null) {
            this.cancelListener = cancelListener;
            btnLine.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
        }
    }
    
    public void setPositiveListener(View.OnClickListener listener){
        this.confirmListener = listener;
    }
}
