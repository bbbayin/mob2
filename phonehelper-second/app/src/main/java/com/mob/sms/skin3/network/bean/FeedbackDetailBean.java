package com.mob.sms.skin3.network.bean;

import java.util.List;

public class FeedbackDetailBean {
    public String msg;
    public int code;
    public DataBean data;

    public static class DataBean {
        public int id;
        public int userId;
        public String phone;
        public String content;
        public String image;
        public int num;
        public String status;
        public String createTime;
        public String updateTime;
        public String nickName;
        public List<RecordsBean> records;

        public static class RecordsBean {
            public int id;
            public int feedbackId;
            public String content;
            public String img;
            public String userId;
            public String serverId;
            public String replyTime;
            public String isRead;
            public String serverName;
            public String userName;
        }
    }
}
