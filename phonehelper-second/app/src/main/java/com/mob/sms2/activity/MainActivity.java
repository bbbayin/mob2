package com.mob.sms2.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.mob.sms2.R;
import com.mob.sms2.ShareManager;
import com.mob.sms2.adapter.ViewPagerAdapter;
import com.mob.sms2.base.BaseActivity;
import com.mob.sms2.base.SimpleObserver;
import com.mob.sms2.bean.OneDialBean;
import com.mob.sms2.config.GlobalConfig;
import com.mob.sms2.dialog.CheckTipDialog;
import com.mob.sms2.fragment.ContactsFragment;
import com.mob.sms2.fragment.HomeFragment;
import com.mob.sms2.fragment.MineFragment;
import com.mob.sms2.fragment.RecordFragment;
import com.mob.sms2.network.RetrofitHelper;
import com.mob.sms2.network.bean.BaseResponse;
import com.mob.sms2.rx.ExitEvent;
import com.mob.sms2.rx.RxBus;
import com.mob.sms2.service.UpdateService;
import com.mob.sms2.utils.ToastUtil;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class MainActivity extends BaseActivity {
    @BindView(R.id.viewPager)
    ViewPager mViewPager;
    @BindView(R.id.magicIndicator)
    MagicIndicator mMagicIndicator;

    private ArrayList<Fragment> fragmentsList = new ArrayList<>();
    private int[] IMG_ICON_ON;
    private int[] IMG_ICON_OFF;
    private String[] NAIGATIONVIEW_TEXT;

    private Subscription mSub;

    private String[] mPermissions = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ANSWER_PHONE_CALLS,
            Manifest.permission.READ_CALL_LOG

    };
    private ArrayList<String> mPermissionList = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));

        for (int i = 0; i < mPermissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, mPermissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(mPermissions[i]);//添加还未授予的权限
            }
        }

        if (mPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(this, mPermissions, 1);
        }
        initView();
        mSub = RxBus.getInstance().toObserverable(ExitEvent.class)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(this::finishPage);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
            intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                    getPackageName());
            startActivity(intent);
        }

        startService(new Intent(this, UpdateService.class));
    }

    private void finishPage(ExitEvent event) {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected boolean setTransparentStatusEnable() {
        return true;
    }

    private void initView() {
        fragmentsList.add(new HomeFragment());
        fragmentsList.add(new RecordFragment());
        fragmentsList.add(new ContactsFragment());
        fragmentsList.add(new MineFragment());
        mViewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), fragmentsList));
        mViewPager.setOffscreenPageLimit(1);
        initBottomNavigationBar();
        initData();
    }

    private void initData() {
//        RetrofitHelper.getApi().getAllMarket().subscribe();

        RetrofitHelper.getApi().getOneDialTimes().subscribe(new SimpleObserver<BaseResponse<OneDialBean>>() {
            @Override
            public void onNext(BaseResponse<OneDialBean> response) {
                if (response != null && response.code == 200) {
                    GlobalConfig.oneDialTimes = response.data.endNum;
                }
            }
        });
    }

    private void initBottomNavigationBar() {
        NAIGATIONVIEW_TEXT = new String[]{"首页", "记录", "通讯录", "我的"};
        IMG_ICON_ON = new int[]{R.mipmap.home_on_icon, R.mipmap.record_on_icon, R.mipmap.txl_on_icon, R.mipmap.mine_on_icon};
        IMG_ICON_OFF = new int[]{R.mipmap.home_off_icon, R.mipmap.record_off_icon, R.mipmap.txl_off_icon, R.mipmap.mine_off_icon};

        CommonNavigator commonNavigator = new CommonNavigator(this);
        commonNavigator.setAdjustMode(true);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return NAIGATIONVIEW_TEXT.length;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                CommonPagerTitleView commonPagerTitleView = new CommonPagerTitleView(context);
                // load custom layout
                View customLayout = LayoutInflater.from(context).inflate(R.layout.home_tab_layout, null);
                final ImageView titleImg = customLayout.findViewById(R.id.image);
                titleImg.setImageResource(IMG_ICON_ON[index]);
                TextView tab = customLayout.findViewById(R.id.tab);
                tab.setText(NAIGATIONVIEW_TEXT[index]);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER;
                commonPagerTitleView.setContentView(customLayout, layoutParams);
                commonPagerTitleView.setOnPagerTitleChangeListener(new CommonPagerTitleView.OnPagerTitleChangeListener() {

                    @Override
                    public void onSelected(int index, int totalCount) {
                        titleImg.setImageResource(IMG_ICON_ON[index]);
                        tab.setTextColor(getResources().getColor(R.color.color_33c197));
                    }

                    @Override
                    public void onDeselected(int index, int totalCount) {
                        titleImg.setImageResource(IMG_ICON_OFF[index]);
                        tab.setTextColor(getResources().getColor(R.color.color_454545));
                    }

                    @Override
                    public void onLeave(int index, int totalCount, float leavePercent,
                                        boolean leftToRight) {

                    }

                    @Override
                    public void onEnter(int index, int totalCount, float enterPercent,
                                        boolean leftToRight) {

                    }

                });
                commonPagerTitleView.setOnClickListener(v -> {
                    mViewPager.setCurrentItem(index);
                });

                return commonPagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                return null;
            }
        });
        mMagicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(mMagicIndicator, mViewPager);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSub != null && !mSub.isUnsubscribed()) {
            mSub.unsubscribe();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permissionDenied = false;//有权限没有通过
        if (1 == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionDenied = true;
                }
            }
            if (permissionDenied) {
                CheckTipDialog dialog = new CheckTipDialog(this);
                dialog.setTitle("提示");
                dialog.setContent("您未授予app必须权限，将无法正常使用app的功能，是否现在授权？");
                dialog.setCancelListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToastUtil.show("后续您可以在应用设置中进行授权");
                    }
                });
                dialog.setPositiveListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.mob.sms"));
                        startActivity(intent);
                    }
                });
                dialog.show();
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ShareManager.getInstance().onActivityResult(this, requestCode,resultCode,data);
    }
}
