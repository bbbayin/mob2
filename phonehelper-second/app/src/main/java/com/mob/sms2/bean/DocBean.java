package com.mob.sms2.bean;

public class DocBean {
    public String fileName;
    public String filePath;
    public String createDate;
    public String modifyDate;
    public long fileSize;
    public boolean isSelected;
    public String docType;

    public DocBean(String fileName, String filePath, String createDate, String modifyDate,
                   long fileSize, boolean isSelected, String docType){
        this.fileName = fileName;
        this.filePath = filePath;
        this.createDate = createDate;
        this.modifyDate = modifyDate;
        this.fileSize = fileSize;
        this.isSelected = isSelected;
        this.docType = docType;
    }
}
