package capgemini.infrateam.hackaton;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by harriewijnans on 22/09/2017.
 *
 */

public class MeetingDbOpenHelper extends SQLiteOpenHelper {
    private static String dbname = "meetings";
    private static int version = 2;

    public static String tbl_settings = "settings";
    public static String set_name = "name";
    public static String set_value = "value";
    public static String tbl_meetserver = "meetserver";
    public static String ms_id = "id";
    public static String ms_name = "name";
    public static String tbl_contacts = "contacts";
    public static String cnt_name = "name";
    public static String cnt_ts = "time";

    MeetingDbOpenHelper (Context context)  {
        super(context, dbname, null, version);
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+
        tbl_settings + "("+
        set_name + " TEXT," +
        set_value + " TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS "+
        tbl_meetserver + "(" +
        ms_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"+
        ms_name + " TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS "+
        tbl_contacts+"("+
                cnt_name+ " TEXT,"+
                cnt_ts+ " INTEGER,"+
                ms_id+ " INTEGER)"
        );
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion<2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS "+
                    tbl_contacts+"("+
                    cnt_name+ " TEXT,"+
                    cnt_ts+ " INTEGER,"+
                    ms_id+ " INTEGER)"
            );
        }
    }
}
