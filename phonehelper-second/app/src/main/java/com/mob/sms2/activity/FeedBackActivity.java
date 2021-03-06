package com.mob.sms2.activity;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.mob.sms2.R;
import com.mob.sms2.base.BaseActivity;
import com.mob.sms2.network.RetrofitHelper;
import com.mob.sms2.utils.ToastUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class FeedBackActivity extends BaseActivity {
    @BindView(R.id.mobile_et)
    EditText mMobileEt;
    @BindView(R.id.content_et)
    EditText mContentEt;
    @BindView(R.id.add_iv)
    ImageView mAddIv;
    private String mImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
    }

    @OnClick({R.id.back, R.id.history_tv, R.id.add_iv, R.id.send})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.history_tv:
                startActivity(new Intent(this, FeedBackHistoryActivity.class));
                break;
            case R.id.add_iv:
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");
                startActivityForResult(intent, 2);
                break;
            case R.id.send:
                if (TextUtils.isEmpty(mMobileEt.getText().toString())) {
                    ToastUtil.show("???????????????????????????");
                    return;
                } else if (TextUtils.isEmpty(mContentEt.getText().toString())) {
                    ToastUtil.show("?????????????????????");
                    return;
                }
                addFeedback();
                break;
        }
    }

    private void addFeedback() {
        RetrofitHelper.getApi().addFeedback(mContentEt.getText().toString(), mImage,
                mMobileEt.getText().toString()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseBean -> {
                    if (baseBean != null && baseBean.code == 200) {
                        Toast.makeText(FeedBackActivity.this, "????????????", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(FeedBackActivity.this, baseBean.msg, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    Toast.makeText(FeedBackActivity.this, "??????????????????????????????", Toast.LENGTH_LONG).show();
                    throwable.printStackTrace();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data==null){
            return;
        }
        Uri uri = data.getData();
        String path = "";
        if(DocumentsContract.isDocumentUri(this,uri)){
            //?????????document?????????Uri????????????document id??????
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];  //????????????????????????id
                String selection = MediaStore.Images.Media._ID+"="+id;
                path = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public downloads"),Long.valueOf(docId));
                path = getImagePath(contentUri,null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            //?????????file?????????Uri?????????????????????????????????
            path = getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            //?????????file?????????Uri?????????????????????????????????
            path = uri.getPath();
        }
        uploadPic(new File(path));
    }

    private void uploadPic(File file){
        Map<String, RequestBody> map = new HashMap<>();
        RequestBody  body=RequestBody.create(MediaType.parse("multipart/form-data;charset=UTF-8"),file);
        map.put("file\"; filename=\"" + file.getName(), body);
        RetrofitHelper.getApi().upload(map).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(uploadBean -> {
                    if (uploadBean != null && uploadBean.code == 200) {
                        mImage = uploadBean.url;
                        Glide.with(mContext).load(mImage).into(mAddIv);
                    } else {
                        ToastUtil.show(uploadBean.msg);
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }

    //??????????????????Uri???????????????
    private String getImagePath(Uri uri,String selection){
        String path = null;
        //??????Uri???selection??????????????????????????????
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if(cursor!= null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
}
