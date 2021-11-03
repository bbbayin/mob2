package com.mob.sms3.activity;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.mob.sms3.R;
import com.mob.sms3.base.BaseActivity;
import com.mob.sms3.network.RetrofitHelper;
import com.mob.sms3.rx.ContactEvent;
import com.mob.sms3.rx.RxBus;
import com.mob.sms3.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddContactActivity extends BaseActivity {
    @BindView(R.id.name_et)
    EditText mNameEt;
    @BindView(R.id.mobile_et)
    EditText mMobileEt;
    @BindView(R.id.tongbu_iv)
    ImageView mTongbuIv;

    private boolean mTongbu = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
    }

    @OnClick({R.id.back, R.id.tongbu_ll, R.id.cancel, R.id.save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.tongbu_ll:
                mTongbu = !mTongbu;
                mTongbuIv.setImageResource(mTongbu?R.mipmap.selected_icon:R.mipmap.unselected_icon);
                break;
            case R.id.cancel:
                finish();
                break;
            case R.id.save:
                if (TextUtils.isEmpty(mNameEt.getText().toString())) {
                    ToastUtil.show("请输入姓名");
                } else if (TextUtils.isEmpty(mMobileEt.getText().toString())) {
                    ToastUtil.show("请输入手机号");
                } else {
                    save();
                }
                break;
        }
    }

    private void save(){
        RetrofitHelper.getApi().addContact(mNameEt.getText().toString(), mMobileEt.getText().toString()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseBean -> {
                    if (baseBean != null && baseBean.code == 200) {
                        Toast.makeText(AddContactActivity.this, "添加成功", Toast.LENGTH_LONG).show();
                        if (mTongbu) {
                            addContact(mNameEt.getText().toString(), mMobileEt.getText().toString());
                        }
                        RxBus.getInstance().post(new ContactEvent());
                        finish();
                    } else {
                        Toast.makeText(AddContactActivity.this, baseBean.msg, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    Toast.makeText(AddContactActivity.this, "添加失败，请稍后再试", Toast.LENGTH_LONG).show();
                    throwable.printStackTrace();
                });
    }

    // 一个添加联系人信息的例子
    private void addContact(String name, String phoneNumber) {
        // 创建一个空的ContentValues
        ContentValues values = new ContentValues();

        // 向RawContacts.CONTENT_URI空值插入，
        // 先获取Android系统返回的rawContactId
        // 后面要基于此id插入值
        Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);
        values.clear();

        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        // 内容类型
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        // 联系人名字
        values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name);
        // 向联系人URI添加联系人名字
        getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
        values.clear();

        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        // 联系人的电话号码
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);
        // 电话类型
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        // 向联系人电话号码URI添加电话号码
        getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
        values.clear();
    }
}
