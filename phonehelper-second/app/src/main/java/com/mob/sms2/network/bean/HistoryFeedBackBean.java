package com.mob.sms2.network.bean;

import java.util.List;

public class HistoryFeedBackBean {
    public String msg;
    public int code;
    public DataBean data;

    public static class DataBean {
        public int total;
        public int code;
        public String msg;
        public List<RowsBean> rows;

        public static class RowsBean {
            public int id;
            public int userId;
            public String phone;
            public String content;
            public Object image;
            public int num;
            public String status;
            public String createTime;
            public Object updateTime;
            public Object records;
            public Object nickName;
        }
    }
}
