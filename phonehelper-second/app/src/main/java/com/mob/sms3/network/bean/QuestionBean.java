package com.mob.sms3.network.bean;

import java.util.List;

public class QuestionBean {

    public String msg;
    public int code;
    public List<DataBean> data;

    public static class DataBean {
        public int id;
        public String title;
        public int sort;
        public String answer;
    }
}
