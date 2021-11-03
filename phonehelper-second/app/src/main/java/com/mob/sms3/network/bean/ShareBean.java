package com.mob.sms3.network.bean;

public class ShareBean {
    public String msg;
    public int code;
    public DataBean data;

    public static class DataBean {
        public int id;
        public String title;
        public String content;
        public String logo;
        public String url;
    }
}
