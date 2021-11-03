package com.mob.sms3.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mob.sms3.R;
import com.mob.sms3.bean.BannerBean;
import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

public class SplashBannerAdapter extends BannerAdapter<String, SplashBannerAdapter.BannerViewHolder> {
    private Context mContext;
    private List<BannerBean> mLists;

    public SplashBannerAdapter(Context context, List<String> mDatas, List<BannerBean> lists) {
        //设置数据，也可以调用banner提供的方法,或者自己在adapter中实现
        super(mDatas);
        mContext = context;
        mLists = lists;
    }

    @Override
    public int getItemCount() {
        if (mLists != null && !mLists.isEmpty()) return mLists.size();
        return 0;
    }

    //创建ViewHolder，可以用viewType这个字段来区分不同的ViewHolder
    @Override
    public BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_banner_splash, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindView(BannerViewHolder holder, String img, int position, int size) {
        BannerBean bannerBean = mLists.get(position);
        Glide.with(mContext).load(bannerBean.img).into(holder.imageView);
    }


    class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public BannerViewHolder(View view) {
            super(view);
            this.imageView = view.findViewById(R.id.image);
        }
    }
}
