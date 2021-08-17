package com.mob.sms2.contacts;

import java.io.Serializable;

public class SortModel implements Serializable {

    private String name;
    private String sortLetters;
    private String mobile;

    public SortModel(String name, String sortLetters, boolean isChecked) {
        super();
        this.name = name;
        this.sortLetters = sortLetters;
        this.isChecked = isChecked;
    }

    public SortModel() {
        super();
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }


    private boolean isChecked;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSortLetters() {
        return sortLetters;
    }

    public void setSortLetters(String sortLetters) {
        this.sortLetters = sortLetters;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMobile() {
        return mobile;
    }
}
