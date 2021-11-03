package com.mob.sms3.activity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms3.R;
import com.mob.sms3.adapter.ContactsAdapter;
import com.mob.sms3.base.BaseActivity;
import com.mob.sms3.bean.ContactsBean;
import com.mob.sms3.contacts.CharacterParser;
import com.mob.sms3.contacts.PinyinComparator;
import com.mob.sms3.contacts.SideBar;
import com.mob.sms3.contacts.SortModel;
import com.mob.sms3.db.CallContactTable;
import com.mob.sms3.db.DatabaseBusiness;
import com.mob.sms3.db.SmsContactTable;
import com.mob.sms3.utils.ToastUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 底部弹框的通讯录导入
 */
public class ImportContactsActivity extends BaseActivity {
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.sidebar)
    SideBar mSideBar;
    @BindView(R.id.top_tv)
    TextView mTopTv;
    @BindView(R.id.select_num)
    TextView mSelectNumTv;
    @BindView(R.id.import_contacts_select_all)
    TextView btSelectAll;
    @BindView(R.id.contacts_import_et_search)
    EditText etSearch;
    @BindView(R.id.import_contacts_empty_layout)
    View emptyLayout;

    private ContactsAdapter mContactsAdapter;
    private ArrayList<ContactsBean> mDatas = new ArrayList<>();
    private CharacterParser mCharacterParser;
    private PinyinComparator mPinyinComparator;
    private ArrayList<SortModel> mOriginList = new ArrayList<>();
    private ArrayList<SortModel> mSearchResults = new ArrayList<>();

    private int lastFirstVisibleItem = -1;

    // 号码
    public final static String NUM = ContactsContract.CommonDataKinds.Phone.NUMBER;
    // 联系人姓名
    public final static String NAME = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
    private static Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

    private String mType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_contacts);
        setStatusBar(getResources().getColor(R.color.green));
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mType = getIntent().getStringExtra("type");

        mCharacterParser = CharacterParser.getInstance();
        mPinyinComparator = new PinyinComparator();

        getContacts();

        filledData(mDatas);
        Collections.sort(mOriginList, mPinyinComparator);

        mContactsAdapter = new ContactsAdapter(this, mOriginList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mContactsAdapter);
        mContactsAdapter.setOnItemClickListener(new ContactsAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                List<SortModel> activeData = mContactsAdapter.getData();
                activeData.get(position).setChecked(!activeData.get(position).isChecked());
                mContactsAdapter.notifyItemChanged(position);
                updateSelectNum(activeData);
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                    int firstItemPosition = linearManager.findFirstVisibleItemPosition();

                    int section = mContactsAdapter.getSectionForPosition(firstItemPosition);
                    if (firstItemPosition != lastFirstVisibleItem) {
                        mTopTv.setText(String.valueOf((char) section));
                    }
                    lastFirstVisibleItem = firstItemPosition;
                }
            }
        });

        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                int position = mContactsAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mRecyclerView.scrollToPosition(position);
                }

            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String keywords = s.toString();
                if (TextUtils.isEmpty(keywords)) {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    // 原始数据
                    mContactsAdapter.updateData(mOriginList);
                    emptyLayout.setVisibility(View.GONE);
                } else {
                    filterData(keywords);
                    if (mSearchResults.isEmpty()) {
                        mRecyclerView.setVisibility(View.GONE);
                        emptyLayout.setVisibility(View.VISIBLE);
                    } else {
                        mRecyclerView.setVisibility(View.VISIBLE);
                        emptyLayout.setVisibility(View.GONE);
                        mContactsAdapter.updateData(mSearchResults);
                    }
                }
            }
        });
    }

    private void updateSelectNum(List<SortModel> activeData) {
        int selectNum = 0;
        for (int i = 0; i < activeData.size(); i++) {
            if (activeData.get(i).isChecked()) {
                selectNum++;
            }
        }
        mSelectNumTv.setText("已选择(" + selectNum + ")");
    }

    /**
     * 搜索
     *
     * @param keyword
     */
    private void filterData(String keyword) {
        mSearchResults.clear();
        for (int i = 0; i < mOriginList.size(); i++) {
            if (mOriginList.get(i).getName().contains(keyword)) {
                mSearchResults.add(mOriginList.get(i));
            }
        }
    }

    private void getContacts() {
        try {
            ContentResolver cr = getContentResolver();
            Cursor cursor = cr.query(phoneUri, new String[]{NUM, NAME}, null, null, null);
            while (cursor.moveToNext()) {
                mDatas.add(new ContactsBean(cursor.getString(cursor.getColumnIndex(NUM)), cursor.getString(cursor.getColumnIndex(NAME))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filledData(ArrayList<ContactsBean> datas) {
        for (int i = 0; i < datas.size(); i++) {
            SortModel sortModel = new SortModel();
            sortModel.setName(datas.get(i).name);
            sortModel.setMobile(datas.get(i).mobile);
            String pinyin = mCharacterParser.getSelling(datas.get(i).name);
            String sortString = pinyin.substring(0, 1).toUpperCase();

            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase());
            } else {
                sortModel.setSortLetters("#");
            }

            mOriginList.add(sortModel);
        }
    }

    boolean isSelectAll = false;

    @OnClick({R.id.back, R.id.import_tv, R.id.import_contacts_select_all})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.import_contacts_select_all:
                List<SortModel> activeList = mContactsAdapter.getData();
                for (int i = 0; i < activeList.size(); i++) {
                    activeList.get(i).setChecked(!isSelectAll);
                }
                isSelectAll = !isSelectAll;
                mContactsAdapter.notifyDataSetChanged();
                updateSelectNum(activeList);
                break;
            case R.id.back:
                finish();
                break;
            case R.id.import_tv:
                ArrayList<SortModel> sortModels = new ArrayList<>();
                List<SortModel> activeData = mContactsAdapter.getData();
                for (int i = 0; i < activeData.size(); i++) {
                    if (activeData.get(i).isChecked()) {
                        sortModels.add(activeData.get(i));
                    }
                }
                if (sortModels.size() > 0) {
                    multImportContacts(sortModels);
                } else {
                    ToastUtil.show("请选择联系人");
                }
                break;
        }
    }

    private void multImportContacts(ArrayList<SortModel> sortModels) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("请稍后...");
        progressDialog.show();
        new Thread() {
            @Override
            public void run() {
                if ("call".equals(mType)) {
                    //批量拨打导入
                    List<CallContactTable> callContactTables = DatabaseBusiness.getCallContacts();
                    if (callContactTables.size() > 0) {
                        for (CallContactTable callContactTable : callContactTables) {
                            DatabaseBusiness.delCallContact(callContactTable);
                        }
                    }
                    for (int i = 0; i < sortModels.size(); i++) {
                        CallContactTable callContactTable = new CallContactTable(sortModels.get(i).getName(), sortModels.get(i).getMobile());
                        DatabaseBusiness.createCallContact(callContactTable);
                    }
                } else if ("sms".equals(mType)) {
                    //批量发送短信导入
                    List<SmsContactTable> smsContactTables = DatabaseBusiness.getSmsContacts();
                    if (smsContactTables.size() > 0) {
                        for (SmsContactTable smsContactTable : smsContactTables) {
                            DatabaseBusiness.delSmsContact(smsContactTable);
                        }
                    }
                    for (int i = 0; i < sortModels.size(); i++) {
                        SmsContactTable smsContactTable = new SmsContactTable(sortModels.get(i).getName(), sortModels.get(i).getMobile());
                        DatabaseBusiness.createSmsContact(smsContactTable);
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        ToastUtil.show("导入成功！");
                    }
                });
                finish();
            }
        }.start();

    }
}
