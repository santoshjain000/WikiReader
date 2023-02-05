package com.example.wikireader.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String CREATE_TABLE_ARTICLEQ = " create table ARTICLE ( id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "title TEXT NOT NULL ," +
            "description TEXT NOT NULL ," +
            "pageid TEXT NOT NULL ," +
            "thumb TEXT NOT NULL ," +
            "type TEXT NOT NULL ," +
            "date DATE NOT NULL ," +
            "time TIME NOT NULL ," +
            "Create_at DATETIME DEFAULT CURRENT_TIMESTAMP ," +
            "SYNC_STATUS TEXT NOT NULL ," +
            "STATUS TEXT NOT NULL );";

    public static final String title = "title";
    public static final String description ="description";
    public static final String pageid ="pageid";
    public static final String thumb="thumb";
    public static final String date="date";
    public static final String type="type";
    public static final String time="time";
    public static final String STATUS="STATUS";
    public static final String SYNC_STATUS="SYNC_STATUS";
    public static final String id = "id";
    private static final String DB_NAME = "WIKI_ARTICLE.DB";

    public static final String TABLE_ARTICLE = "ARTICLE";

    public SQLiteHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ARTICLEQ);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS STUDENTS_TRANSACTION");
        onCreate(db);
    }
}
