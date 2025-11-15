// File: DatabaseHelper.java
package com.example.nazarick;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "quanlytaphoa.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS tbHangHoa(" +
                "tenSanPham TEXT PRIMARY KEY, soLuongTon INTEGER, giaBan INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS tbHoaDon(" +
                "maHoaDon TEXT PRIMARY KEY, thoiGian TEXT, chiTiet TEXT, tongTien INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tbHangHoa");
        db.execSQL("DROP TABLE IF EXISTS tbHoaDon");
        onCreate(db);
    }
}