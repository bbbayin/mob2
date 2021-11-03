package com.mob.sms3.network.bean;

public class EnterpriseBean {

    public String msg;
    public int code;
    public DataBean data;

    public static class DataBean {
        public int id;
        public String name;
        public String brief;
        public int num;
        public String status;
        public String link;
    }
}
