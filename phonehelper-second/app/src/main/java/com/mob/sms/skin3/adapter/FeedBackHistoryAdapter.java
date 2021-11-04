package com.mob.sms.skin3.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms.skin3.R;
import com.mob.sms.skin3.network.bean.HistoryFeedBackBean;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FeedBackHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<HistoryFeedBackBean.DataBean.RowsBean> mDatas;

    public FeedBackHistoryAdapter(Context context, ArrayList<HistoryFeedBackBean.DataBean.RowsBean> datas) {
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
        HistoryHolder historyHolder = (HistoryHolder) viewHolder;
        historyHolder.title.setText(mDatas.get(position).content);
        historyHolder.time.setText(mDatas.get(position).createTime);
        historyHolder.root_ll.setOnClickListener(view -> {
            if(mOnItemClickListener!=null){
                mOnItemClickListener.onclick(position);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_feedback_history, parent, false);
        RecyclerView.ViewHolder holder = new HistoryHolder(view);
        return holder;
    }

    class HistoryHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.root_ll)
        LinearLayout root_ll;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.time)
        TextView time;
        @BindView(R.id.tip)
        TextView tip;
        public HistoryHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }
    }

}
