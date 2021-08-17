package com.mob.sms2.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.mob.sms2.auto.SingleAutoTaskActivity;
import com.mob.sms2.rx.CallEvent;
import com.mob.sms2.rx.RxBus;

public class PhoneStateReceiver extends BroadcastReceiver {
    private String TAG = "电话广播";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: " + intent.getAction() + "--------");
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        Log.d(TAG, "电话状态：" + state);
        if ("IDLE".equals(state)) {
            RxBus.getInstance().post(new CallEvent(state));
            SingleAutoTaskActivity.phoneIdleFlag = true;
        }
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            // 去电，可以用定时挂断
            if ("IDLE".equals(state)) {
                SingleAutoTaskActivity.phoneIdleFlag = true;
            }
        } else {
            //来电
            if (TelephonyManager.EXTRA_STATE_RINGING.equalsIgnoreCase(state)) {
                Log.i(TAG, "PhoneStateReceiver onReceive endCall");
            }
        }
    }

    public static class PreciseCallState {
        public static final int PRECISE_CALL_STATE_IDLE = 0; //通话空闲
        public static final int PRECISE_CALL_STATE_ACTIVE = 1; //正在通话(活动中)
        public static final int PRECISE_CALL_STATE_HOLDING = 2; //通话挂起(例如我和多个人通话,其中一个通话在活动,而其它通话就会进入挂起状态)
        public static final int PRECISE_CALL_STATE_DIALING = 3; //拨号开始
        public static final int PRECISE_CALL_STATE_ALERTING = 4; //正在呼出(提醒对方接电话)
        public static final int PRECISE_CALL_STATE_INCOMING = 5; //对方来电
        public static final int PRECISE_CALL_STATE_WAITING = 6; //第三方来电等待(例如我正在和某人通话,而其他人打入时就会就进入等待状态)
        public static final int PRECISE_CALL_STATE_DISCONNECTED = 7; //挂断完成
        public static final int PRECISE_CALL_STATE_DISCONNECTING = 8; //正在挂断
    }
}
