package com.mob.sms.skin3.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mob.sms.skin3.R;
import com.mob.sms.skin3.bean.MsgBean;
public class MsgViewFactory {
    public static View create(Context context, MsgBean bean, ViewGroup parent) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_msg_layout, parent, false);
        TextView content = inflate.findViewById(R.id.msg_content);
        content.setText("系统消息："+bean.content);
//        inflate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
        return inflate;
    }

    public static View create(Context context, ViewGroup parent) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_msg_layout, parent, false);
        TextView content = inflate.findViewById(R.id.msg_content);
        content.setText("系统消息：您的会员即将过期，请及时续费");
//        inflate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
        return inflate;
    }
}
