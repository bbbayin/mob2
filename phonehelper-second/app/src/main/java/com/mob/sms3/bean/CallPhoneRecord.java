package com.mob.sms3.bean;

public class CallPhoneRecord {
    public String name;
    public String mobile;
    public boolean isSend;

    public CallPhoneRecord(String name, String mobile, boolean isSend){
        this.name = name;
        this.mobile = mobile;
        this.isSend = isSend;
    }
}
