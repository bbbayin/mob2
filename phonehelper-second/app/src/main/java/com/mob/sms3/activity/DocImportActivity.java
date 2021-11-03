package com.mob.sms3.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.mob.sms3.R;
import com.mob.sms3.base.BaseActivity;
import com.mob.sms3.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DocImportActivity extends BaseActivity {
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.tip)
    TextView mTip;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_import);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
        initView();
    }

    private void initView() {
        String type = getIntent().getStringExtra("type");
        mTitle.setText(type + "文档导入");
        mTip.setText(type + "格式");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                start();
            }else {
                ToastUtil.show("文件读取权限被拒绝");
            }
        }
    }

    @OnClick({R.id.back, R.id.choose})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.choose:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }else {
                    start();
                }
                break;
        }
    }

    private void start() {
        Intent intent = new Intent(DocImportActivity.this, DocSelectActivity.class);
        intent.putExtra("type", getIntent().getStringExtra("type"));
        intent.putExtra("type2", getIntent().getStringExtra("type2"));
        startActivity(intent);
        finish();
    }
}
