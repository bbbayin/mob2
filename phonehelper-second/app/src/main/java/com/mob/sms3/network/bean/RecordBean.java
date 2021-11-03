package com.mob.sms3.network.bean;

import java.util.List;

public class RecordBean {
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
            public int type;
            public String createTime;
            public String status;
            public int allNum;
            public int successNum;
            public List<RelationListBean> relationList;
            public boolean isSelect;

            public static class RelationListBean {
                public int id;
                public int recordId;
                public String tel;
            }
        }
    }
}
