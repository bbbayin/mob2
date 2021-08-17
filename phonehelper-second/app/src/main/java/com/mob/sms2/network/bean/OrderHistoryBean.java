package com.mob.sms2.network.bean;

import java.util.List;

public class OrderHistoryBean {
    public String msg;
    public int code;
    public List<DataBean> data;

    public static class DataBean {
        public int id;
        public int userId;
        public int relationId;
        public Object orderNo;
        public String status;
        public float price;
        public String createTime;
        public Object payType;
        public int duration;
        public String isAll;
        public String title;
        public Object nickName;
        public Object searchTime;
    }
}
