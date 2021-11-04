package com.mob.sms.skin3.wxapi;

import android.app.Activity;

import com.mob.sms.skin3.utils.ToastUtil;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        if (baseResp.errCode == 0) {
            ToastUtil.show("支付成功");
            finish();
        } else {
            ToastUtil.show(baseResp.errStr);
            finish();
        }
    }
}
