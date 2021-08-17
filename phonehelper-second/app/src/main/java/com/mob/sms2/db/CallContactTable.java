package com.mob.sms2.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "call_contact")
public class CallContactTable {
    @DatabaseField(generatedId = true)
    public long table_id;
    @DatabaseField
    public String name;
    @DatabaseField
    public String mobile;

    public CallContactTable() {

    }

    public CallContactTable(String name, String mobile) {
        this.name = name;
        this.mobile = mobile;
    }

}
