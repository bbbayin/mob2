package com.mob.sms2.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mob.sms2.R;
import com.mob.sms2.activity.ImageViewerActivity;
import com.mob.sms2.network.bean.FeedbackDetailBean;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FeedbackDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<FeedbackDetailBean.DataBean.RecordsBean> mDatas;

    public FeedbackDetailAdapter(Context context, ArrayList<FeedbackDetailBean.DataBean.RecordsBean> datas) {
        this.mContext = context;
        this.mDatas = datas;
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        VHolder vHolder = (VHolder) viewHolder;
        FeedbackDetailBean.DataBean.RecordsBean recordsBean = mDatas.get(position);
        if (TextUtils.isEmpty(recordsBean.userId)) {
            vHolder.content.setText("客服回复: " + recordsBean.content);
        } else {
            vHolder.content.setText("用户回复: " + recordsBean.content);
        }
        if(TextUtils.isEmpty(recordsBean.img)){
            vHolder.image.setVisibility(View.GONE);
        }else {
            vHolder.image.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(recordsBean.img).into(vHolder.image);
        }
        vHolder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageViewerActivity.launch(mContext, recordsBean.img);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_feedback_detail, parent, false);
        RecyclerView.ViewHolder holder = new VHolder(view);
        return holder;
    }

    class VHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.content)
        TextView content;
        @BindView(R.id.image)
        ImageView image;
        public VHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }
    }

}
