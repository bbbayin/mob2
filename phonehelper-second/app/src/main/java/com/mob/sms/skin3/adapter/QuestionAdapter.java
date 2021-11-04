package com.mob.sms.skin3.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms.skin3.R;
import com.mob.sms.skin3.network.bean.QuestionBean;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QuestionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<QuestionBean.DataBean> mDatas;

    public QuestionAdapter(Context context, ArrayList<QuestionBean.DataBean> datas) {
        this.mContext = context;
        this.mDatas = datas;
    }

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onclick(int position);
    }

    public void setOnItemClickLsitener(OnItemClickListener onItemClickLsitener) {
        this.mOnItemClickListener = onItemClickLsitener;
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        RecordHolder recordHolder = (RecordHolder) viewHolder;
        recordHolder.title.setText(mDatas.get(position).title);
        recordHolder.title.setOnClickListener(view -> {
            if(mOnItemClickListener!=null){
                mOnItemClickListener.onclick(position);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_question, parent, false);
        RecyclerView.ViewHolder holder = new RecordHolder(view);
        return holder;
    }

    class RecordHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title)
        TextView title;
        public RecordHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }
    }

}
