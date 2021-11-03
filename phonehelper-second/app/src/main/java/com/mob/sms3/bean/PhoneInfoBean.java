package com.mob.sms3.bean;

public class PhoneInfoBean {
    public String phone;
    public String name;
    public boolean isSelected;

    public PhoneInfoBean(String phone, String name, boolean isSelected){
        this.phone = phone;
        this.name = name;
        this.isSelected = isSelected;
    }
}
