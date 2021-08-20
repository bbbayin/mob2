package com.mob.sms2.policy;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.gyf.immersionbar.ImmersionBar;
import com.mob.sms2.R;
import com.mob.sms2.base.BaseActivity;
import com.mob.sms2.network.RetrofitHelper;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 隐私政策，用户协议
 * 1用户协议 2隐私政策 3关于我们
 * http://dial.shengzewang.cn//prod-api/restApi/rule/getRule/1
 */
public class PolicyActivity extends BaseActivity {

    public static final String KEY_POLICY_TYPE = "key_policy_type";
    public static final int TYPE_USER = 1;
    public static final int TYPE_SECRET = 2;
    public static final int TYPE_ABOUT_US = 3;
    private WebView webview;
    private TextView tvTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy_activity);
        findViewById(R.id.policy_bt_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvTitle = findViewById(R.id.policy_tv_title);
        webview = findViewById(R.id.policy_webview);
        ImmersionBar.with(this).statusBarColor(R.color.colorPrimary).init();
        initData();
    }

    private void initData() {
        int type = getIntent().getIntExtra(KEY_POLICY_TYPE, TYPE_ABOUT_US);
        if (type == TYPE_SECRET) {
            tvTitle.setText("隐私政策");
        } else if (type == TYPE_USER) {
            tvTitle.setText("用户协议");
        } else {
            tvTitle.setText("关于我们");
        }
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        RetrofitHelper.getApi().getRule(type).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ruleBean -> {
                    if (ruleBean != null && ruleBean.code == 200 && ruleBean.data != null) {
//                        tvTitle.setText(ruleBean.data.name);
                        webview.loadDataWithBaseURL(null, ruleBean.data.content, "text/html", "utf-8", null);
                    }
                }, Throwable::printStackTrace);
    }
}
