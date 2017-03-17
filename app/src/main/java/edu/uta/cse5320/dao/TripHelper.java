package edu.uta.cse5320.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Akshay on 3/16/2017.
 */

public class TripHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "trips.db";
    public static final String TABLE_NAME = "trip_data";
    public static final String COL1 = "ID";
    public static final String COL2 = "tripName";
    public static final String COL3 = "tripStartDate";
    public static final String COL4 = "tripEndDate";
    public static final String COL5 = "tripAirlineName";
    public static final String COL6 = "tripDetails";

    public TripHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL2 + " TEXT," +
                COL3 + " TEXT," +
                COL4 + " TEXT," +
                COL5 + " TEXT," +
                COL6 + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addData( String tripName, String tripStartDate, String tripEndDate, String tripAirlineName, String tripDetails) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, tripName);
        contentValues.put(COL3, tripStartDate);
        contentValues.put(COL4, tripEndDate);
        contentValues.put(COL5, tripAirlineName);
        contentValues.put(COL6, tripDetails);

        long result = db.insert(TABLE_NAME, null, contentValues);
        return result;
    }

    public void addDataCompleteSync(long id, String tripName, String tripStartDate, String tripEndDate, String tripAirlineName, String tripDetails){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, id);
        contentValues.put(COL2, tripName);
        contentValues.put(COL3, tripStartDate);
        contentValues.put(COL4, tripEndDate);
        contentValues.put(COL5, tripAirlineName);
        contentValues.put(COL6, tripDetails);

        db.insert(TABLE_NAME, null, contentValues);
    }

    public boolean updateDetails(long rowId, String tripName, String tripStartDate, String tripEndDate, String tripAirlineName, String tripDetails)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, tripName);
        contentValues.put(COL3, tripStartDate);
        contentValues.put(COL4, tripEndDate);
        contentValues.put(COL5, tripAirlineName);
        contentValues.put(COL6, tripDetails);
        int i =  db.update(TABLE_NAME, contentValues, COL1+" =" + rowId, null);
        return i > 0;
    }

    //query for 1 week repeats
    public Cursor getListContents() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return data;
    }

    public int getListContent(long rowId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME+ " WHERE "+COL1+" =" + rowId, null);
        return data.getCount();
    }

    public boolean deleteContent(long rowId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int row_affected = db.delete(TABLE_NAME, COL1 + " ="+ rowId, null);
        if(row_affected>0){
            return true;
        }
        return false;
    }
}
