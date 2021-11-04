package com.mob.sms.skin3.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms.skin3.R;
import com.mob.sms.skin3.db.CallRecordsTable;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CallRecordsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<CallRecordsTable> mDatas;

    public CallRecordsAdapter(Context context, ArrayList<CallRecordsTable> datas) {
        this.mContext = context;
        this.mDatas = datas;
    }

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void call(int position);
        void sms(int position);
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
        recordHolder.name.setText(mDatas.get(position).name);
        recordHolder.mobile.setText(mDatas.get(position).mobile);
        recordHolder.time.setText(mDatas.get(position).time);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_call_records, parent, false);
        RecyclerView.ViewHolder holder = new RecordHolder(view);
        return holder;
    }

    class RecordHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.mobile)
        TextView mobile;
        @BindView(R.id.time)
        TextView time;
        public RecordHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }
    }

}
