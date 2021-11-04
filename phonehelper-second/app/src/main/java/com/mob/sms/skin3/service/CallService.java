package com.mob.sms.skin3.service;

import android.telecom.Call;
import android.telecom.InCallService;
import android.util.Log;

import com.mob.sms.skin3.rx.CallEvent;
import com.mob.sms.skin3.rx.RxBus;
import com.mob.sms.skin3.utils.PhoneCallManager;
import com.mob.sms.skin3.utils.SPConstant;
import com.mob.sms.skin3.utils.SPUtils;

public class CallService extends InCallService {
    private String TAG = "[CallService]";

    private Call.Callback callback = new Call.Callback() {
        @Override
        public void onStateChanged(Call call, int state) {
            super.onStateChanged(call, state);
            Log.i(TAG, "state: " + state);
            switch (state) {
                case Call.STATE_ACTIVE: {
                    //接通后自动挂断
                    if (SPUtils.getBoolean(SPConstant.SP_CALL_GD, false)) {
                        call.disconnect();
                        RxBus.getInstance().post(new CallEvent());
                    }
                    break; // 通话中
                }
                case Call.STATE_DISCONNECTED: {

                    break; // 通话结束
                }
            }
        }
    };

    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);
        Log.i(TAG, "onCallAdded");
        call.registerCallback(callback);
        PhoneCallManager.call = call; // 传入call

        Log.i(TAG, "call.getState(): " + call.getState());
        if (call.getState() == Call.STATE_RINGING) {
        } else if (call.getState() == Call.STATE_CONNECTING) {
            //没有接通自动挂断
//            if (!SPUtils.getBoolean(SPConstant.SP_CALL_GD, true)) {
//                call.disconnect();
//                RxBus.getInstance().post(new CallEvent());
//            }
        }
    }

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);
        Log.i(TAG, "onCallRemoved");
        call.unregisterCallback(callback);
        PhoneCallManager.call = null;
    }
}
