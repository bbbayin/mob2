package com.mob.sms.skin3.network.bean;

public class RuleBean {
    public String msg;
    public int code;
    public DataBean data;

    public static class DataBean {
        public int id;
        public String name;
        public String content;
        public String updateTime;
    }
}
