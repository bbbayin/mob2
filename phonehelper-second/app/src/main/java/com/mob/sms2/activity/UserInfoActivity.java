package com.mob.sms2.activity;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.mob.sms2.R;
import com.mob.sms2.base.BaseActivity;
import com.mob.sms2.network.RetrofitHelper;
import com.mob.sms2.utils.SPConstant;
import com.mob.sms2.utils.SPUtils;
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

public class UserInfoActivity extends BaseActivity {
    @BindView(R.id.avatar)
    ImageView mAvatar;
    @BindView(R.id.name_et)
    EditText mNameEt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
        initView();
    }

    private void initView(){
        Glide.with(this).load(SPUtils.getString(SPConstant.SP_USER_HEAD, "")).into(mAvatar);
        mNameEt.setText(SPUtils.getString(SPConstant.SP_USER_NAME, ""));
    }

    @OnClick({R.id.back, R.id.avatar, R.id.edit_iv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.avatar:
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");
                startActivityForResult(intent, 2);
                break;
            case R.id.edit_iv:
                updateUser(SPUtils.getString(SPConstant.SP_USER_HEAD, ""), mNameEt.getText().toString());
                break;
        }
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
            //如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];  //解析出数字格式的id
                String selection = MediaStore.Images.Media._ID+"="+id;
                path = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public downloads"),Long.valueOf(docId));
                path = getImagePath(contentUri,null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            //如果是file类型的Uri，直接获取图片路径即可
            path = getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            //如果是file类型的Uri，直接获取图片路径即可
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
                        updateUser(uploadBean.url, SPUtils.getString(SPConstant.SP_USER_NAME, ""));
                    } else {
                        ToastUtil.show(uploadBean.msg);
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }

    private void updateUser(String avatar, String name){
        RetrofitHelper.getApi().updateUser(Uri.encode(avatar),name).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseBean -> {
                    if (baseBean != null && baseBean.code == 200) {
                        SPUtils.put(SPConstant.SP_USER_HEAD, avatar);
                        SPUtils.put(SPConstant.SP_USER_NAME, name);
                        initView();
                        hideKeyboard(mNameEt);
                        ToastUtil.show("修改个人信息成功");
                    } else {
                        ToastUtil.show(baseBean.msg);
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }

    public static void hideKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    //将选择的图片Uri转换为路径
    private String getImagePath(Uri uri,String selection){
        String path = null;
        //通过Uri和selection来获取真实的图片路径
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
