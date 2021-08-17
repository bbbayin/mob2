package com.mob.sms2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms2.R;
import com.mob.sms2.bean.DocBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DocAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<DocBean> mDatas;

    public DocAdapter(Context context, List<DocBean> datas) {
        this.mContext = context;
        this.mDatas = datas;
    }

    public  List<DocBean> getData() {
        return mDatas;
    }

    public void updateList(List<DocBean> list) {
        if (list!=null && !list.isEmpty()) {
            this.mDatas = list;
            notifyDataSetChanged();
        }
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
        DocHolder docHolder = (DocHolder) viewHolder;
        docHolder.name.setText(mDatas.get(position).fileName);
        docHolder.time.setText(mDatas.get(position).modifyDate);
        if ("word".equals(mDatas.get(position).docType)) {
            docHolder.icon.setImageResource(R.mipmap.word_icon2);
        } else if ("excel".equals(mDatas.get(position).docType)) {
            docHolder.icon.setImageResource(R.mipmap.excel_icon2);
        } else if ("txt".equals(mDatas.get(position).docType)) {
            docHolder.icon.setImageResource(R.mipmap.txt_icon2);
        }

        docHolder.select_iv.setImageResource(mDatas.get(position).isSelected ? R.mipmap.selected_icon : R.mipmap.unselected_icon);
        docHolder.root_ll.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onclick(position);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_doc, parent, false);
        RecyclerView.ViewHolder holder = new DocHolder(view);
        return holder;
    }

    class DocHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.root_ll)
        LinearLayout root_ll;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.time)
        TextView time;
        @BindView(R.id.icon)
        ImageView icon;
        @BindView(R.id.select_iv)
        ImageView select_iv;

        public DocHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }
    }

}
