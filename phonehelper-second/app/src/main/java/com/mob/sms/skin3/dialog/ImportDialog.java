package com.mob.sms.skin3.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.mob.sms.skin3.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ImportDialog extends Dialog {
    private Unbinder bind;


    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void wordImport();
        void txlImport();
        void copyImport();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public ImportDialog(@NonNull Context context) {
        this(context, R.style.dialogNoBg);
    }

    private ImportDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);

        View view = View.inflate(context, R.layout.dialog_import, null);
        bind = ButterKnife.bind(this, view);

        setContentView(view);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @OnClick({R.id.cancel, R.id.word_ll, R.id.txl_ll, R.id.copy_ll})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                dismiss();
                break;
            case R.id.word_ll:
                if(mOnClickListener!=null){
                    mOnClickListener.wordImport();
                }
                break;
            case R.id.txl_ll:
                if(mOnClickListener!=null){
                    mOnClickListener.txlImport();
                }
                break;
            case R.id.copy_ll:
                if(mOnClickListener!=null){
                    mOnClickListener.copyImport();
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
