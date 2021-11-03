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

public class DocImportDialog extends Dialog {
    private Unbinder bind;


    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void wordImport();
        void excelImport();
        void txtImport();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public DocImportDialog(@NonNull Context context) {
        this(context, R.style.dialogNoBg);
    }

    private DocImportDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);

        View view = View.inflate(context, R.layout.dialog_doc_import, null);
        bind = ButterKnife.bind(this, view);

        setContentView(view);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @OnClick({R.id.cancel, R.id.word_tv, R.id.excel_tv, R.id.txt_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                dismiss();
                break;
            case R.id.word_tv:
                if(mOnClickListener!=null){
                    mOnClickListener.wordImport();
                }
                break;
            case R.id.excel_tv:
                if(mOnClickListener!=null){
                    mOnClickListener.excelImport();
                }
                break;
            case R.id.txt_tv:
                if(mOnClickListener!=null){
                    mOnClickListener.txtImport();
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
