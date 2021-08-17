package com.mob.sms2.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms2.R;
import com.mob.sms2.adapter.DocAdapter;
import com.mob.sms2.base.BaseActivity;
import com.mob.sms2.bean.DocBean;
import com.mob.sms2.dialog.SortDialog;
import com.mob.sms2.utils.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.leefeng.promptlibrary.PromptDialog;

public class DocSelectActivity extends BaseActivity {
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.search_et)
    EditText etSearch;
    @BindView(R.id.doc_select_empty_layout)
    View mEmptyLayout;

    private List<DocBean> mOriginDataList = Collections.synchronizedList(new ArrayList<>());
    private ArrayList<DocBean> mFilterList = new ArrayList<>();
    private DocAdapter mDocAdapter;
    private PromptDialog mPromptDialog;
    private String fileType;
    private ExecutorService executorService;
    private int lastSize = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_select);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
        initView();
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mOriginDataList.isEmpty()) {
                    return;
                }
                String keyword = s.toString();
                if (TextUtils.isEmpty(keyword)) {
                    mDocAdapter.updateList(mOriginDataList);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mEmptyLayout.setVisibility(View.GONE);
                } else {
                    filterData(keyword);
                    if (mFilterList.isEmpty()) {
                        mRecyclerView.setVisibility(View.GONE);
                        mEmptyLayout.setVisibility(View.VISIBLE);
                    } else {
                        mRecyclerView.setVisibility(View.VISIBLE);
                        mEmptyLayout.setVisibility(View.GONE);
                        mDocAdapter.updateList(mFilterList);
                    }
                }
            }
        });
    }

    private void filterData(String keyword) {
        mFilterList.clear();
        for (int i = 0; i < mOriginDataList.size(); i++) {
            DocBean docBean = mOriginDataList.get(i);
            if (docBean.fileName.contains(keyword)) {
                mFilterList.add(docBean);
            }
        }
    }

    private long lastTime = 0;

    private void initView() {
        fileType = getIntent().getStringExtra("type");
        mDocAdapter = new DocAdapter(this, mOriginDataList);
        mRecyclerView.setAdapter(mDocAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDocAdapter.setOnItemClickLsitener(new DocAdapter.OnItemClickListener() {
            @Override
            public void onclick(int position) {
                List<DocBean> data = mDocAdapter.getData();
                for (int i = 0; i < data.size(); i++) {
                    if (i == position) {
                        data.get(position).isSelected = !data.get(position).isSelected;
                    } else {
                        data.get(i).isSelected = false;
                    }
                }
                mDocAdapter.notifyDataSetChanged();
            }
        });

        liveData.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                long l = System.currentTimeMillis();
                if (l - lastTime > 1000) {
                    mDocAdapter.notifyDataSetChanged();
                    lastTime = l;
                }
            }
        });

        // 遍历文件
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            mPromptDialog = new PromptDialog(this);
            mPromptDialog.showLoading("正在加载...");
            File root = Environment.getExternalStorageDirectory();
            executorService = Executors.newFixedThreadPool(8);
            executorService.execute(new FileFilterThread(root));

            new Thread() {
                @Override
                public void run() {
                    while (true) {
                        if (System.currentTimeMillis() - lastTime > 5000) {
                            mHandler.sendEmptyMessageDelayed(0, 100);
                            this.interrupt();
                            break;
                        }
                        try {
                            if (!isInterrupted()) {
                                sleep(1000);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        } else {
            System.out.println("目录不存在。。。");
        }
    }

    private MutableLiveData<Boolean> liveData = new MutableLiveData<Boolean>();

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            mPromptDialog.dismissImmediately();
            mDocAdapter.notifyDataSetChanged();
        }
    };

    private class FileFilterThread implements Runnable {
        File file;

        public FileFilterThread(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            if (file.exists()) {
                if (file.isDirectory()) {
                    File[] fileArray = file.listFiles();
                    if (fileArray == null) return;
                    for (File f : fileArray) {
                        if (f.isDirectory()) {
                            executorService.execute(new FileFilterThread(f));
                        } else {
                            try {
                                addFileToList(fileType, f);
                                liveData.postValue(true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private String getFileTime(File file) {
        Date date = new Date(file.lastModified());
        return date.toLocaleString();
    }

    private void addFileToList(String type, File f) throws Exception {
        if ("word".equals(type)) {
            if (f.getName().endsWith(".docx") || f.getName().endsWith(".doc")) {
                try {
                    String time = getFileTime(f);
                    DocBean bean = new DocBean(f.getName(), f.getAbsolutePath(), time, time, f.length(), false, type);
                    mOriginDataList.add(bean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if ("excel".equals(type)) {
            if (f.getName().endsWith(".xls") || f.getName().endsWith(".xlsx")) {
                try {
                    String time = getFileTime(f);
                    DocBean bean = new DocBean(f.getName(), f.getAbsolutePath(), time, time, f.length(), false, type);
                    mOriginDataList.add(bean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if ("txt".equals(type)) {
            if (f.getName().endsWith(".txt")) {
                try {
                    String time = getFileTime(f);
                    DocBean bean = new DocBean(f.getName(), f.getAbsolutePath(), time, time, f.length(), false, type);
                    mOriginDataList.add(bean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @OnClick({R.id.back, R.id.search_iv, R.id.import_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.search_iv:
                SortDialog sortDialog = new SortDialog(DocSelectActivity.this);
                sortDialog.show();
                sortDialog.setOnClickListener(new SortDialog.OnClickListener() {
                    @Override
                    public void sort(int type) {
                        sortDialog.dismiss();
                    }
                });
                break;
            case R.id.import_tv:
                boolean mIsSelected = false;
                DocBean docBean = null;
                for (int i = 0; i < mDocAdapter.getData().size(); i++) {
                    if (mDocAdapter.getData().get(i).isSelected) {
                        docBean = mDocAdapter.getData().get(i);
                        mIsSelected = true;
                    }
                }
                if (!mIsSelected) {
                    ToastUtil.show("请选择文档");
                } else {
                    Intent intent = new Intent(DocSelectActivity.this, PhoneInfoActivity.class);
                    if ("word".equals(getIntent().getStringExtra("type"))) {
                        intent.putExtra("type", 0);
                    } else if ("excel".equals(getIntent().getStringExtra("type"))) {
                        intent.putExtra("type", 1);
                    } else if ("txt".equals(getIntent().getStringExtra("type"))) {
                        intent.putExtra("type", 2);
                    }
                    intent.putExtra("title", docBean.fileName);
                    intent.putExtra("filePath", docBean.filePath);
                    intent.putExtra("type2", getIntent().getStringExtra("type2"));
                    startActivity(intent);
                    finish();
                }
                break;
        }
    }
}
