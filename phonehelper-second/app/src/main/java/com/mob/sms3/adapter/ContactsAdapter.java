package com.mob.sms3.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms3.R;
import com.mob.sms3.contacts.SortModel;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> implements SectionIndexer {
    protected Context mContext;
    protected List<SortModel> mDatas;
    protected LayoutInflater mInflater;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener{
        void onClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        mOnItemClickListener = onItemClickListener;
    }

    public ContactsAdapter(Context mContext, List<SortModel> mDatas) {
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
    public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_contact, parent, false));
    }

    @Override
    public void onBindViewHolder(final ContactsAdapter.ViewHolder holder, final int position) {
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
        holder.root_ll.setOnClickListener(view -> {
            if(mOnItemClickListener!=null){
                mOnItemClickListener.onClick(position);
            }
        });
        holder.select_iv.setImageResource(mDatas.get(position).isChecked()?R.mipmap.selected_icon:R.mipmap.unselected_icon);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView catalog;
        TextView mobile;
        ImageView select_iv;
        LinearLayout root_ll;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            catalog = (TextView) itemView.findViewById(R.id.catalog);
            mobile = (TextView) itemView.findViewById(R.id.mobile);
            select_iv = (ImageView) itemView.findViewById(R.id.select_iv);
            root_ll = (LinearLayout) itemView.findViewById(R.id.root_ll);
        }
    }
}
