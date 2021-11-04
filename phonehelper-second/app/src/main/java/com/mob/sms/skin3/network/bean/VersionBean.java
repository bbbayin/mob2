package com.mob.sms.skin3.network.bean;

import java.util.List;

public class VersionBean {
    public String msg;
    public int code;
    public List<DataBean> data;

    public static class DataBean {
        public int id;
        public String name;
        public String no;
        public String remark;
        public String url;
        public String createTime;
    }
}
