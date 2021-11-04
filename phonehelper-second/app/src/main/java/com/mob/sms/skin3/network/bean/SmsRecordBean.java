package com.mob.sms.skin3.network.bean;

import java.util.List;

public class SmsRecordBean {
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
            public String tels;
            public String oneSend;
            public Object timing;
            public int sendNum;
            public String batchSend;
            public String content;
            public int successNum;
            public int allNum;
            public String status;
            public String createTime;
            public boolean isSelect;
        }
    }
}
