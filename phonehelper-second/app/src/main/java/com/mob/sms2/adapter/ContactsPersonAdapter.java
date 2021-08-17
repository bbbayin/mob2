package com.mob.sms2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms2.R;
import com.mob.sms2.network.bean.OnlineContactBean;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactsPersonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<OnlineContactBean.DataBean> mDatas;
    private int mType;

    public ContactsPersonAdapter(Context context, ArrayList<OnlineContactBean.DataBean> datas) {
        this.mContext = context;
        this.mDatas = datas;
    }

    public void setType(int type){
        this.mType = type;
    }

    public int getType(){
        return mType;
    }

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void select(int position);
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
        ContactHolder contactHolder = (ContactHolder) viewHolder;
        contactHolder.name.setText(mDatas.get(position).name);
        contactHolder.mobile.setText(mDatas.get(position).tel);
        if (mType == 0) {
            contactHolder.select_iv.setVisibility(View.GONE);
        } else {
            contactHolder.select_iv.setVisibility(View.VISIBLE);
            contactHolder.select_iv.setImageResource(mDatas.get(position).isSelect?R.mipmap.selected_icon:R.mipmap.unselected_icon);
        }
        contactHolder.root_rl.setOnClickListener(view -> {
            if(mOnItemClickListener!=null){
                mOnItemClickListener.select(position);
            }
        });
        contactHolder.call_iv.setOnClickListener(view -> {
            if(mOnItemClickListener!=null){
                mOnItemClickListener.call(position);
            }
        });
        contactHolder.sms_iv.setOnClickListener(view -> {
            if(mOnItemClickListener!=null){
                mOnItemClickListener.sms(position);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_contacts_person, parent, false);
        RecyclerView.ViewHolder holder = new ContactHolder(view);
        return holder;
    }

    static class ContactHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.select_iv)
        ImageView select_iv;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.mobile)
        TextView mobile;
        @BindView(R.id.root_rl)
        RelativeLayout root_rl;
        @BindView(R.id.call_iv)
        ImageView call_iv;
        @BindView(R.id.sms_iv)
        ImageView sms_iv;
        public ContactHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }
    }

}
