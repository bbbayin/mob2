package com.mob.sms.skin3.network.bean;

public class UserInfoBean {
    public String msg;
    public int code;
    public DataBean data;

    public static class DataBean {
        public int userId;
        public String username;
        public String nickName;
        public String type;//1会员 0 非会员
        public int useMinute;
        public int allMinute;
        public String expTime;
        public String isAll;// 永久会员：1
        public String avatar;
        public String originate;
        public String memberName;
    }
}
