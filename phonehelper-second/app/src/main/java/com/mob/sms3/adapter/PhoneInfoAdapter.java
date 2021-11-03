package com.mob.sms3.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms3.R;
import com.mob.sms3.bean.PhoneInfoBean;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhoneInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<PhoneInfoBean> mDatas;

    public PhoneInfoAdapter(Context context, ArrayList<PhoneInfoBean> datas) {
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
        PhoneHolder phoneHolder = (PhoneHolder) viewHolder;
        phoneHolder.name.setText(mDatas.get(position).name);
        phoneHolder.phone.setText(mDatas.get(position).phone);

        phoneHolder.select_iv.setImageResource(mDatas.get(position).isSelected ? R.mipmap.selected_icon : R.mipmap.unselected_icon);
        phoneHolder.root_ll.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onclick(position);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_phone_info, parent, false);
        RecyclerView.ViewHolder holder = new PhoneHolder(view);
        return holder;
    }

    class PhoneHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.root_ll)
        LinearLayout root_ll;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.phone)
        TextView phone;
        @BindView(R.id.select_iv)
        ImageView select_iv;

        public PhoneHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }
    }

}
