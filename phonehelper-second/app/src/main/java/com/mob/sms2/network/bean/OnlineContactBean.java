package com.mob.sms2.network.bean;

import java.util.List;

public class OnlineContactBean {

    public String msg;
    public int code;
    public List<DataBean> data;

    public static class DataBean {
        public int id;
        public String name;
        public String tel;
        public int userId;
        public boolean isSelect;
    }
}
