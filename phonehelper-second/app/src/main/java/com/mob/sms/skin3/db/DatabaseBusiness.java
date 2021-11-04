package com.mob.sms.skin3.db;


import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseBusiness {
    public static void createCallRecord(CallRecordsTable item) {
        SQLiteHelperOrm db = new SQLiteHelperOrm();
        try {
            Dao<CallRecordsTable, Long> dao = db.getDao(CallRecordsTable.class);
            dao.create(item);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db != null)
                db.close();
        }
    }

    public static void updateCallRecord(CallRecordsTable item) {
        SQLiteHelperOrm db = new SQLiteHelperOrm();
        try {
            Dao<CallRecordsTable, Long> dao = db.getDao(CallRecordsTable.class);
            dao.update(item);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db != null)
                db.close();
        }
    }

    public static List<CallRecordsTable> getCallRecords() {
        SQLiteHelperOrm db = new SQLiteHelperOrm();
        try {
            Dao<CallRecordsTable, Long> dao = db.getDao(CallRecordsTable.class);
            return dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db != null)
                db.close();
        }
        return new ArrayList<CallRecordsTable>();
    }

    public static void createSmsContact(SmsContactTable item) {
        SQLiteHelperOrm db = new SQLiteHelperOrm();
        try {
            Dao<SmsContactTable, Long> dao = db.getDao(SmsContactTable.class);
            dao.create(item);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db != null)
                db.close();
        }
    }

    public static void delSmsContact(SmsContactTable item) {
        SQLiteHelperOrm db = new SQLiteHelperOrm();
        try {
            Dao<SmsContactTable, Long> dao = db.getDao(SmsContactTable.class);
            dao.delete(item);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db != null)
                db.close();
        }
    }

    public static List<SmsContactTable> getSmsContacts() {
        SQLiteHelperOrm db = new SQLiteHelperOrm();
        try {
            Dao<SmsContactTable, Long> dao = db.getDao(SmsContactTable.class);
            return dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db != null)
                db.close();
        }
        return new ArrayList<SmsContactTable>();
    }

    public static void createCallContact(CallContactTable item) {
        SQLiteHelperOrm db = new SQLiteHelperOrm();
        try {
            Dao<CallContactTable, Long> dao = db.getDao(CallContactTable.class);
            dao.create(item);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db != null)
                db.close();
        }
    }

    public static void delCallContact(CallContactTable item) {
        SQLiteHelperOrm db = new SQLiteHelperOrm();
        try {
            Dao<CallContactTable, Long> dao = db.getDao(CallContactTable.class);
            dao.delete(item);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db != null)
                db.close();
        }
    }

    public static List<CallContactTable> getCallContacts() {
        SQLiteHelperOrm db = new SQLiteHelperOrm();
        try {
            Dao<CallContactTable, Long> dao = db.getDao(CallContactTable.class);
            return dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db != null)
                db.close();
        }
        return new ArrayList<CallContactTable>();
    }
}
