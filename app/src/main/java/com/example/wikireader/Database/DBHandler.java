package com.example.wikireader.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBHandler {


    private Context context;
    private SQLiteDatabase database;
    private SQLiteHelper dbHelper;

    public DBHandler(Context context) {
        this.context = context;
    }

    public DBHandler open() throws SQLException {
        this.dbHelper = new SQLiteHelper(this.context);
        this.database = this.dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        this.dbHelper.close();
    }

    public void insert(String title, String description, String pageid, String thumb, String date, String time, String STATUS, String TYPE) {
        ContentValues contentValue = new ContentValues();

        contentValue.put(SQLiteHelper.title, title);
        contentValue.put(SQLiteHelper.description, description);
        contentValue.put(SQLiteHelper.pageid, pageid);
        contentValue.put(SQLiteHelper.thumb, thumb);
        contentValue.put(SQLiteHelper.date, date);
        contentValue.put(SQLiteHelper.time, time);
        contentValue.put(SQLiteHelper.SYNC_STATUS,  "YES");
        contentValue.put(SQLiteHelper.type,  TYPE);
        contentValue.put(SQLiteHelper.STATUS ,   STATUS);

        this.database.insert(SQLiteHelper.TABLE_ARTICLE, null, contentValue);
    }



    public Cursor GetAll(String type){
        // SQLiteDatabase db = this.getWritableDatabase();
        String idd="";
        String query1 = "SELECT * from "+SQLiteHelper.TABLE_ARTICLE+" where type ='"+ type +"' order by id DESC ";
        Cursor cursor = database.rawQuery(query1,null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor GetVbyID_Sync(String TXNID){
        // SQLiteDatabase db = this.getWritableDatabase();
        String idd="";
        String query1 = "SELECT * from "+SQLiteHelper.TABLE_ARTICLE+" where pageid ='"+ TXNID +"'";
        Cursor cursor = database.rawQuery(query1,null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }


}
