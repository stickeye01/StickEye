package com.example.jarim.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

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
            if (!checkDbExist(mCtx, DATABASE_NAME)) {
                Log.e("LHC", "Weird!");
            }
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
            Log.e("LHC", "DROP: "+CreateDB._CREATE);
        }

        /*
         *  Check whether database already exists or not
         */
        private boolean checkDbExist(Context context, String dbName) {
            File dbFile = context.getDatabasePath(dbName);
            Log.e("LHC", "DB CHECK!:"+dbFile.toString());
            Log.e("LHC", "results:"+Boolean.toString(dbFile.exists()));
            return dbFile.exists();
        }

        /*
         * Insert db
         */
        public void insert(SQLiteDatabase sqLiteDatabase, String device_name, String device_address) {
            sqLiteDatabase.execSQL("INSERT INTO "+CreateDB._TABLENAME+" VALUES('" +device_name+
                                "', '"+device_address+"')");
            Log.e("LHC", "INSERT DEVICE_NAME:"+device_name+", DEVICE_ADDRESS:"+device_address);
        }

        /*
         * delete db
         */
        public void deleteAll(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.delete(CreateDB._TABLENAME, null, null);
        }

        /*
         * select all data from db (for test)
         */
        public String select(SQLiteDatabase sqLiteDatabase) {
            String result = "";

            Cursor cursor = mDB.rawQuery("SELECT address FROM "+CreateDB._TABLENAME+" where device='target'", null);
            Log.e("LHC", "DBHandler: "+Integer.toString(cursor.getCount())+", Position:"+Integer.toString(cursor.getPosition()));
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                result += cursor.getString(0) + "\n";
            } else {
                result = "No device exists";
            }

            Log.e("LHC", "DBHandler: "+cursor.toString()+":"+result);
            return result;
        }
    }
    //@}

    public DBHandler(Context context) {
        this.mCtx = context;
    }

    public DBHandler open() throws SQLException{
        mDBController = new DBController(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        return this;
    }

    public DBHandler insert(String device_name, String device_address) {
        mDB = mDBController.getWritableDatabase();
        Log.e("LHC", "DB insert:"+device_address+", LENGHT:"+device_address.length());
        mDBController.insert(mDB, device_name, device_address);
        mDB.close();
        return this;
    }

    public DBHandler deleteAll() {
        mDB = mDBController.getWritableDatabase();
        mDBController.deleteAll(mDB);
        mDB.close();
        return this;
    }

    public String select() {
        String results = "";
        mDB = mDBController.getReadableDatabase();
        results = mDBController.select(mDB);
        mDB.close();
        Log.e("LHC", "DB select:"+results);
        return results;
    }

    public void close() {
        mDB.close();
    }

}
