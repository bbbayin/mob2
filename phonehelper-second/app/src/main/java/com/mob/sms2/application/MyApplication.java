package com.mob.sms2.application;

import android.app.Application;
import android.content.Context;

import com.mob.sms2.utils.ChannelUtil;
import com.mob.sms2.utils.SPConstant;
import com.mob.sms2.utils.SPUtils;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;

import java.util.Observable;
import java.util.Observer;

public class MyApplication extends Application implements Observer {
    public static MyApplication mApplication;
    public static IWXAPI wxApi;
    public static String Channel = "";

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        if (SPUtils.getBoolean(SPConstant.SP_USER_PERMISSION_OK, false)) {
            initSDK();
        }
    }

    private void initSDK() {
        Channel = ChannelUtil.getChannel(this);
        UMConfigure.setLogEnabled(true);
        UMConfigure.init(this, "6099327553b6726499f68bb7", Channel, UMConfigure.DEVICE_TYPE_PHONE, "");
//        UMConfigure.init(this, "6099327553b6726499f68bb7", Channel, UMConfigure.DEVICE_TYPE_PHONE, "");
        PlatformConfig.setWeixin("wx22e1a70e838f3267", "42e372330366d521a57ed99bcb350b5b");
        PlatformConfig.setQQZone("101924228", "1166dd0fd38327bb8f4da43276b8865f");
        PlatformConfig.setQQFileProvider("com.mob.sms.fileprovider");

        wxApi = WXAPIFactory.createWXAPI(this, null);
        // 将该app注册到微信
        wxApi.registerApp("wx22e1a70e838f3267");
    }

    public static Context getContext(){
        return mApplication;
    }

    @Override
    public void update(Observable o, Object arg) {
        initSDK();
    }
}
