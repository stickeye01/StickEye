package com.example.jarim.myapplication;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by lhc on 2017-12-24.
 * Database manages a device which are selected by user.
 * After saving it,
 */

public class DBHandler {
    private static final String DATABASE_NAME ="bluetooth.device";
    private static final int DATABASE_VERSION = 1;
    public static SQLiteDatabase mDB;
    private DBController mDBController;
    private Context mCtx;

    //@{
    private final class CreateDB {
        public static final String REGISTERED_DATE = "date";
        public static final String DEVICE_NAME = "device";
        public static final String DEVICE_ADDRESS = "address";
        public static final String _TABLENAME = "selected_devices";
        public static final String _CREATE =
                "create table "+_TABLENAME+"("
                +DEVICE_NAME+" text not null, "
                +DEVICE_ADDRESS+" text not null)";
                //+REGISTERED_DATE+" DATETIME DEFAULT CURRENT_TIMESTAMP";
    }
    //@}

    //@{
    private class DBController extends SQLiteOpenHelper {

        public DBController(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // Create Database for the first time
        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CreateDB._CREATE);
        }

        // Recreate Database whenever the version is updated
        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int old_ver, int new_ver) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ CreateDB._TABLENAME);
            onCreate(sqLiteDatabase);
        }
    }
    //@}

    public DBHandler(Context context) {
        this.mCtx = context;
    }

    public DBHandler open() throws SQLException{
        mDBController = new DBController(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBController.getWritableDatabase();
        Log.e("LHC", mDB.toString());
        return this;
    }

    public void close() {
        mDB.close();
    }

}
