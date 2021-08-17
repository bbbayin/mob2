package com.mob.sms2.utils;

import android.telecom.Call;
import android.telecom.VideoProfile;

public class PhoneCallManager {
    public static Call call;

    /**
     * 接听电话
     */
    public void answer() {
        if (call != null) {
            call.answer(VideoProfile.STATE_AUDIO_ONLY);
        }
    }

    /**
     * 断开电话，包括来电时的拒接以及接听后的挂断
     */
    public void disconnect() {
        if (call != null) {
            call.disconnect();
        }
    }
}
