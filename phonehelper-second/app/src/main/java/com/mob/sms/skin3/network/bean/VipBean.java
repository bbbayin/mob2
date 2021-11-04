package com.mob.sms.skin3.network.bean;

import java.util.List;

public class VipBean {
    public String msg;
    public int code;
    public List<DataBean> data;

    public static class DataBean {
        public int memberId;
        public String memberName;
        public float price;
        public int duration;
        public String isAll;
        public int minutes;
        public String status;
        public boolean isSelected;
        public String remark;
    }
}
