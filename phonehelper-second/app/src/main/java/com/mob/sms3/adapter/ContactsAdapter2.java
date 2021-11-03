package com.mob.sms3.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms3.R;
import com.mob.sms3.contacts.SortModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactsAdapter2 extends RecyclerView.Adapter<ContactsAdapter2.ContactHolder2> implements SectionIndexer {
    protected Context mContext;
    protected List<SortModel> mDatas;
    protected LayoutInflater mInflater;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        mOnItemClickListener = onItemClickListener;
    }

    public ContactsAdapter2(Context mContext, List<SortModel> mDatas) {
        this.mContext = mContext;
        this.mDatas = mDatas;
        mInflater = LayoutInflater.from(mContext);
    }

    public List<SortModel> getData() {
        return mDatas;
    }

    public void updateData(List<SortModel> list) {
        if (list != null && !list.isEmpty()) {
            this.mDatas = list;
            notifyDataSetChanged();
        }
    }

    @Override
    public ContactsAdapter2.ContactHolder2 onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContactHolder2(mInflater.inflate(R.layout.item_contacts_person, parent, false));
    }

    @Override
    public void onBindViewHolder(final ContactsAdapter2.ContactHolder2 holder, final int position) {
        final SortModel sortModel = mDatas.get(position);
        holder.name.setText(sortModel.getName());
        holder.mobile.setText(sortModel.getMobile());

        int section = getSectionForPosition(position);

        if (position == getPositionForSection(section)) {
            holder.catalog.setVisibility(View.VISIBLE);
            holder.catalog.setText(sortModel.getSortLetters());
        } else {
            holder.catalog.setVisibility(View.GONE);
        }
        holder.call_iv.setOnClickListener(view -> {
            if(mOnItemClickListener!=null){
                mOnItemClickListener.call(position);
            }
        });
        holder.sms_iv.setOnClickListener(view -> {
            if(mOnItemClickListener!=null){
                mOnItemClickListener.sms(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas != null ? mDatas.size() : 0;
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    @Override
    public int getPositionForSection(int position) {
        for (int i = 0; i < mDatas.size(); i++) {
            String sortStr = mDatas.get(i).getSortLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == position) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getSectionForPosition(int position) {
        return mDatas.get(position).getSortLetters().charAt(0);
    }

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void select(int position);
        void call(int position);
        void sms(int position);
    }

    static class ContactHolder2 extends RecyclerView.ViewHolder {
        @BindView(R.id.catalog)
        TextView catalog;
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
        public ContactHolder2(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
            select_iv.setVisibility(View.GONE);
        }
    }
}
