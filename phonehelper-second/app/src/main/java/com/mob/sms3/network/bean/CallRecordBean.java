package com.mob.sms3.network.bean;

import java.util.List;

public class CallRecordBean {
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
            public Object timing;
            public int dialNum;
            public String dialInterval;
            public String dialType;
            public String onHang;
            public String unHang;
            public String tels;
            public int allNum;
            public int successNum;
            public String status;
            public String createTime;
            public boolean isSelect;
        }
    }
}
