package com.example.jarim.myapplication.AndroidSide;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by lhc on 2017-12-24.
 * Database manages a device which are selected by user.
 * After saving it,
 */

public class LandMarkDBHandler {
    private static final String DATABASE_NAME ="landmark.db";
    private static final int DATABASE_VERSION = 1;
    public static SQLiteDatabase mDB;
    private DBController mDBController;
    private Context mCtx;

    //@{
    private final class CreateDB {
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String LAND_ADDRESS = "address";
        public static final String LAND_NAME = "name";

        public static final String _TABLENAME = "landmark_lists";
        public static final String _CREATE =
                "create table "+_TABLENAME+"("
                +LATITUDE+" REAL not null, "
                +LONGITUDE+" REAL not null, "
                +LAND_ADDRESS+" text not null, "
                +LAND_NAME+" text not null)";
    }
    //@}

    //@{
    private class DBController extends SQLiteOpenHelper {

        public DBController(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
            if (!checkDbExist(mCtx, DATABASE_NAME)) {
                Log.e("LHC", "Landmark DB is not exist");
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
            Log.e("LHC", "DROP: "+ CreateDB._CREATE);
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
        public void insert(SQLiteDatabase sqLiteDatabase, double _lat, double _lng,
                           String address, String name) {
            String lat = Double.toString(_lat);
            String lng = Double.toString(_lng);
            sqLiteDatabase.execSQL("INSERT INTO "+ CreateDB._TABLENAME+
                    " VALUES('" +lat+"','"+lng+"','"+address+"','"+name+"')");
            Log.e("LHC", "INSERT lat:"+lat+", lng:"+lng+", " +
                    " address:"+address+", name:"+name);
        }

        /*
         * delete db
         */
        public void deleteAll(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.delete(CreateDB._TABLENAME, null, null);
        }

        public void delete(SQLiteDatabase sqLiteDatabase, double lat, double lng) {
            sqLiteDatabase.delete(CreateDB._TABLENAME,
                    CreateDB.LATITUDE+"=? AND "+CreateDB.LONGITUDE+"=?",
                            new String[]{Double.toString(lat), Double.toString(lng)});
        }

        /*
         * select all data from db (for test)
         */
        public ArrayList<LandMark> select(SQLiteDatabase sqLiteDatabase) {
            String result = "";
            ArrayList<LandMark> landMarkLists = new ArrayList<LandMark>();

            Cursor cursor = mDB.rawQuery("SELECT * FROM "+ CreateDB._TABLENAME, null);
            Log.e("LHC", "LandMarkHandler: "+
                    Integer.toString(cursor.getCount())+
                    ", Position:"+Integer.toString(cursor.getPosition()));
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    LandMark tmpLand = new LandMark(cursor.getDouble(0), cursor.getDouble(1),
                                cursor.getString(2), cursor.getString(3));
                    result += cursor.getDouble(0) + "," + cursor.getDouble(1) + "," +
                            cursor.getString(2) + "," + cursor.getString(3) + "\n";
                    landMarkLists.add(tmpLand);
                } while (cursor.moveToNext());
            } else {
                result = "No landmarks exists";
            }
            Log.e("LHC", "Landmark Fetch Results:"+result);
            return landMarkLists;
        }
    }
    //@}

    public LandMarkDBHandler(Context context) {
        this.mCtx = context;
    }

    public LandMarkDBHandler open() throws SQLException{
        mDBController = new DBController(mCtx, DATABASE_NAME,
                                null, DATABASE_VERSION);
        return this;
    }

    public LandMarkDBHandler insert(LandMark lm) {
        mDB = mDBController.getWritableDatabase();
        Log.e("LHC", "DB insert:"+lm.getLat()+","+lm.getLng()+","+lm.getAddress()+
                    ","+lm.getName());
        mDBController.insert(mDB, lm.getLat(), lm.getLng(), lm.getAddress(), lm.getName());
        mDB.close();
        return this;
    }

    public LandMarkDBHandler deleteAll() {
        mDB = mDBController.getWritableDatabase();
        mDBController.deleteAll(mDB);
        mDB.close();
        return this;
    }

    public LandMarkDBHandler delete(double lat, double lng) {
        mDB = mDBController.getWritableDatabase();
        mDBController.delete(mDB, lat, lng);
        mDB.close();
        return this;
    }

    public ArrayList<LandMark> select() {
        ArrayList<LandMark> landLists = new ArrayList<LandMark>();
        mDB = mDBController.getReadableDatabase();
        landLists = mDBController.select(mDB);
        mDB.close();
        return landLists;
    }

    public void close() {
        mDB.close();
    }

}
