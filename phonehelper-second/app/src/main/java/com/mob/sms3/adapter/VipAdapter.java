package com.mob.sms3.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms3.R;
import com.mob.sms3.network.bean.VipBean;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VipAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<VipBean.DataBean> mDatas;

    public VipAdapter(Context context, ArrayList<VipBean.DataBean> datas) {
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
        VHolder vHolder = (VHolder) viewHolder;
        final VipBean.DataBean dataBean = mDatas.get(position);
        vHolder.price.setText(dataBean.price + "");
        if (TextUtils.isEmpty(dataBean.memberName)) {
            vHolder.tvTitle.setText("会员套餐");
        }else {
            vHolder.tvTitle.setText(dataBean.memberName);
        }

        if (!TextUtils.isEmpty(dataBean.remark)) {
            vHolder.tvDesc.setText(dataBean.remark);
        }else {
            vHolder.tvDesc.setText("套餐说明（空）");
        }
        if (dataBean.isSelected) {
            viewHolder.itemView.setAlpha(1f);
        }else {
            viewHolder.itemView.setAlpha(0.5f);
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < mDatas.size(); i++) {
                    mDatas.get(i).isSelected = false;
                }
                dataBean.isSelected = true;
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_vip, parent, false);
        RecyclerView.ViewHolder holder = new VHolder(view);
        return holder;
    }

    class VHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.price)
        TextView price;
        @BindView(R.id.title)
        TextView tvTitle;
        @BindView(R.id.vip_desc)
        TextView tvDesc;

        public VHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }
    }

}
