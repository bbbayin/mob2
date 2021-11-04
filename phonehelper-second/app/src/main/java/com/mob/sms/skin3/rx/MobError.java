package com.mob.sms.skin3.rx;

import androidx.annotation.Nullable;

public class MobError extends Throwable{

    private int code;
    private String errorMsg;

    public MobError(@Nullable String message, int code, String errorMsg) {
        super(message);
        this.code = code;
        this.errorMsg = errorMsg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
