package com.mob.sms.skin3.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms.skin3.R;
import com.mob.sms.skin3.adapter.PhoneInfoAdapter;
import com.mob.sms.skin3.base.BaseActivity;
import com.mob.sms.skin3.bean.PhoneInfoBean;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 群拨号码记录页面
 */
public class RecordListActivity extends BaseActivity {

    @BindView(R.id.record_list_recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.num)
    TextView number;
    private ArrayList<PhoneInfoBean> list;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list_layout);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
        initData();
    }

    private void initData() {
        String phoneString = getIntent().getStringExtra("list");
        if (!TextUtils.isEmpty(phoneString)) {
            String[] phoneNumbers = new String[]{phoneString};
            if (phoneString.contains(",")) {
                phoneNumbers = phoneString.split(",");
            }
            number.setText(String.format("%s个号码", phoneNumbers.length));
            list = new ArrayList<>();
            for (int i = 0; i < phoneNumbers.length; i++) {
                list.add(new PhoneInfoBean(phoneNumbers[i], "无姓名", false));
            }
            PhoneInfoAdapter phoneInfoAdapter = new PhoneInfoAdapter(this, list);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(phoneInfoAdapter);
        }
    }
}
