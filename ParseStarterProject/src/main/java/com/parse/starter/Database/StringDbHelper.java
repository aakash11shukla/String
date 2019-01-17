package com.parse.starter.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StringDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE = "string.db";
    private static final int DATABASE_VERSION = 1;

    public StringDbHelper(Context context){
        super(context, DATABASE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_USER_TABLE = "CREATE TABLE " +
                StringContract.User.TABLE_NAME + " (" +
                StringContract.User._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                StringContract.User.COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                StringContract.User.COLUMN_OBJECTID + " TEXT NOT NULL, " +
                StringContract.User.COLUMN_NAME + " TEXT NOT NULL, " +
                StringContract.User.COLUMN_PHONE + " TEXT NOT NULL, " +
                StringContract.User.COLUMN_PROFILE_PHOTO + " BLOB DEFAULT NULL" +
                " );";

        final String SQL_CREATE_MESSAGE_TABLE = "CREATE TABLE " +
                StringContract.Message.TABLE_NAME + " (" +
                StringContract.Message._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                StringContract.Message.COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                StringContract.Message.COLUMN_SENDER + " TEXT NOT NULL," +
                StringContract.Message.COLUMN_RECIPIENT + " TEXT NOT NULL," +
                StringContract.Message.COLUMN_MESSAGE + " TEXT DEFAULT NULL," +
                StringContract.Message.COLUMN_PHOTO + " BLOB DEFAULT NULL" +
                " );";


        sqLiteDatabase.execSQL(SQL_CREATE_USER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MESSAGE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + StringContract.User.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + StringContract.Message.TABLE_NAME);
    }
}
