package capgemini.infrateam.hackaton;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by harriewijnans on 22/09/2017.
 */

public class MeetingDB {
    private Context ctx;
    private SQLiteDatabase myDB;
    private boolean transOpen = false;
    private static String lastvalue = "last";

    MeetingDB(Context c) {
        ctx = c;
    }

    private void getDB() {
        if (myDB!=null && myDB.isOpen()) return;
        myDB = new MeetingDbOpenHelper(ctx).getWritableDatabase();
        transOpen = false;
    }

    public void startTransaction() {
        startTransaction(true);
    }

    public void startTransaction(boolean rw) {
        getDB();
        if (!transOpen) {
            if (rw) {
                myDB.beginTransaction();
            } else {
                myDB.beginTransactionNonExclusive();
            }
            transOpen = true;
        }
    }

    public void commit() {
        if (!transOpen) return;
        myDB.setTransactionSuccessful();
        myDB.endTransaction();
        transOpen = false;
    }

    public void rollback() {
        if (!transOpen) return;
        myDB.endTransaction();
        transOpen = false;
    }

    public MeetMeSettings getSettings() {
        getDB();
        MeetMeSettings r = new MeetMeSettings();
        boolean inTrans = myDB.inTransaction();
        if (! inTrans) {
            startTransaction(false);
        }
        Cursor c = myDB.rawQuery("SELECT "+MeetingDbOpenHelper.set_value + " FROM "+MeetingDbOpenHelper.tbl_settings+
        " WHERE "+MeetingDbOpenHelper.set_name+"=?", new String[] {lastvalue});
        if (c.moveToFirst()) {
            r.lastMeeting = Integer.decode(c.getString(0));
        }
        c.close();
        if (!inTrans) {
            commit();
        }
        return r;
    }

    public Meeting getMeeting(int i) {
        getDB();
        Meeting r = null;
        boolean inTrans = myDB.inTransaction();
        if (! inTrans) {
            startTransaction(false);
        }
        Cursor c = myDB.rawQuery("SELECT "+MeetingDbOpenHelper.ms_name+" FROM "+
        MeetingDbOpenHelper.tbl_meetserver+" WHERE "+MeetingDbOpenHelper.ms_id+"=?",
                new String[]{Integer.toString(i)});
        if (c.moveToFirst()) {
            r = new Meeting();
            r.setId(i);
            r.setName(c.getString(0));
        }
        c.close();
        return r;
    }

    public void insertContact(int mid, Contact c) {
        if (c.lastMeeting == null) {
            c.lastMeeting = new Date();
        }
        getDB();
        boolean inTrans = myDB.inTransaction();
        if (! inTrans) {
            startTransaction(true);
        }
        myDB.execSQL("DELETE FROM "+MeetingDbOpenHelper.tbl_contacts+" WHERE "+
            MeetingDbOpenHelper.cnt_name+"=? AND "+
            MeetingDbOpenHelper.ms_id+"=?");
        ContentValues cv = new ContentValues(3);
        cv.put(MeetingDbOpenHelper.cnt_name, c.name);
        cv.put(MeetingDbOpenHelper.cnt_ts, c.lastMeeting.getTime());
        cv.put(MeetingDbOpenHelper.ms_id, mid);
        myDB.insert(MeetingDbOpenHelper.tbl_contacts, null, cv);
        if (! inTrans) {
            commit();
        }
    }

    public ArrayList<Contact> getContacts(int mid) {
        ArrayList<Contact> r = null;
        Contact ct;
        getDB();
        boolean inTrans = myDB.inTransaction();
        if (! inTrans) {
            startTransaction(false);
        }
        Cursor c = myDB.rawQuery("SELECT "+MeetingDbOpenHelper.cnt_name+","+
            MeetingDbOpenHelper.cnt_ts+" FROM "+MeetingDbOpenHelper.tbl_contacts+
            " WHERE "+MeetingDbOpenHelper.ms_id+"=? ORDER BY "+MeetingDbOpenHelper.cnt_ts+
            " DESCENDING", new String[]{Integer.toString(mid)});
        if (c.moveToFirst()) {
            r = new ArrayList<>();
            do {
                ct = new Contact();
                ct.lastMeeting = new Date(c.getLong(1));
                ct.name = c.getString(0);
                r.add(ct);
            } while (c.moveToNext());
        }
        c.close();
        if (! inTrans) {
            commit();
        }
        return r;
    }

    public int setMeeting(Meeting m) {
        getDB();
        boolean inTrans = myDB.inTransaction();
        if (! inTrans) {
            startTransaction(true);
        }
        ContentValues cv = new ContentValues(2);
        cv.put(MeetingDbOpenHelper.ms_name, m.getName());
        if (m.getId() >= 0) {
            cv.put(MeetingDbOpenHelper.ms_id, m.getId());
        }
        long mid = myDB.insert(MeetingDbOpenHelper.tbl_meetserver, null, cv);
        if (! inTrans) {
            commit();
        }
        return (int) mid;
    }

    public void setLast(int i) {
        getDB();
        boolean inTrans = myDB.inTransaction();
        if (! inTrans) {
            startTransaction();
        }
        ContentValues cv = new ContentValues(2);
        cv.put(MeetingDbOpenHelper.set_name, lastvalue);
        cv.put(MeetingDbOpenHelper.set_value, Integer.toString(i));
        myDB.delete(MeetingDbOpenHelper.tbl_settings,MeetingDbOpenHelper.ms_name+"=?",
                new String[]{lastvalue});
        myDB.insert(MeetingDbOpenHelper.tbl_settings,null,cv);
        if (! inTrans) {
            commit();
        }
    }

    public void close() {
        if (myDB==null) return;
        myDB.close();
        myDB = null;
    }

    public int countMeetings() {
        getDB();
        int r = 0;
        boolean inTrans = myDB.inTransaction();
        if (! inTrans) {
            startTransaction(false);
        }
        Cursor c = myDB.rawQuery("SELECT COUNT(*) FROM "+MeetingDbOpenHelper.tbl_meetserver,null);
        if (c.moveToFirst()) {
            r = c.getInt(0);
        }
        c.close();
        if (! inTrans) {
            commit();
        }
        return r;
    }

    public ArrayList<Meeting> getMeetings() {
        getDB();
        Meeting mm;
        ArrayList<Meeting> r = new ArrayList<>();
        boolean inTrans = myDB.inTransaction();
        if (! inTrans) {
            startTransaction(false);
        }
        Cursor c = myDB.rawQuery("SELECT "+MeetingDbOpenHelper.ms_id+","+MeetingDbOpenHelper.ms_name+
        " FROM "+MeetingDbOpenHelper.tbl_meetserver+" ORDER BY 1",null);
        if (c.moveToFirst()) {
            do {
                mm = new Meeting();
                mm.setId(c.getInt(0));
                mm.setName(c.getString(1));
                r.add(mm);
            } while (c.moveToNext());
        }
        c.close();
        if (! inTrans) {
            commit();
        }
        return r;
    }
}
