package com.mob.sms3.application;

import android.app.Application;
import android.content.Context;

import com.mob.sms3.Constants;
import com.mob.sms3.utils.ChannelUtil;
import com.mob.sms3.utils.SPConstant;
import com.mob.sms3.utils.SPUtils;
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
        Channel = ChannelUtil.getChannel(this);
        System.out.println("渠道-"+Channel);
        //APP ID：101924228
        //APP Key：1166dd0fd38327bb8f4da43276b8865f
        PlatformConfig.setQQZone("101924228", "1166dd0fd38327bb8f4da43276b8865f");
        if (SPUtils.getBoolean(SPConstant.SP_USER_PERMISSION_OK, false)) {
            initSDK();
        }else {
            UMConfigure.preInit(this, "6124a28310c4020b03eaf9e5", Channel);
        }
    }

    private void initSDK() {
        UMConfigure.setLogEnabled(true);
        UMConfigure.init(this, "6124a28310c4020b03eaf9e5", Channel, UMConfigure.DEVICE_TYPE_PHONE, "");
//        UMConfigure.init(this, "6099327553b6726499f68bb7", Channel, UMConfigure.DEVICE_TYPE_PHONE, "");
        PlatformConfig.setWeixin(Constants.WX_APPID, Constants.WX_APPKEY);
        PlatformConfig.setQQZone(Constants.QQ_APPID, Constants.QQ_APPKEY);
        PlatformConfig.setQQFileProvider("com.mob.sms3.fileprovider");

        wxApi = WXAPIFactory.createWXAPI(this, null);
        // 将该app注册到微信
        wxApi.registerApp(Constants.WX_APPID);
    }

    public static Context getContext(){
        return mApplication;
    }

    @Override
    public void update(Observable o, Object arg) {
        initSDK();
    }
}
