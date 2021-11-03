package com.mob.sms3.bean;

public class SendSmsRecord {
    public String name;
    public String mobile;
    public boolean isSend;

    public SendSmsRecord(String name, String mobile, boolean isSend){
        this.name = name;
        this.mobile = mobile;
        this.isSend = isSend;
    }
}
