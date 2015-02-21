package com.bloc.blocspot.api.model.database.table;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Mark on 2/18/2015.
 */
public abstract class Table {

    public static interface Builder {
        public long insert(SQLiteDatabase writableDB);
    }

    protected static final String COLUMN_ID = "_id";

    public abstract String getName();

    public abstract String getCreateStatement();

    public void onUpgrade(SQLiteDatabase writableDatabase, int oldVersion, int newVersion) {
        // Nothing
    }

    public Cursor fetchRow(SQLiteDatabase readonlyDatabase, long rowId) {
        return readonlyDatabase.query(true, getName(), null, COLUMN_ID + " = ?",
                new String[] {String.valueOf(rowId)}, null, null, null, null);
    }

    protected static String getString(Cursor cursor, String column) {
        int columnIndex = cursor.getColumnIndex(column);
        if (columnIndex == -1) {
            return "";
        }
        return cursor.getString(columnIndex);
    }

    protected static double getDouble(Cursor cursor, String column) {
        int columnIndex = cursor.getColumnIndex(column);
        if (columnIndex == -1) {
            return -1d;
        }
        return cursor.getDouble(columnIndex);
    }

    protected static long getLong(Cursor cursor, String column) {
        int columnIndex = cursor.getColumnIndex(column);
        if (columnIndex == -1) {
            return -1l;
        }
        return cursor.getLong(columnIndex);
    }

    protected static boolean getBoolean(Cursor cursor, String column) {
        return getLong(cursor, column) == 1l;
    }

    protected static int getInteger(Cursor cursor, String column) {
        int columnIndex = cursor.getColumnIndex(column);
        if (columnIndex == -1) {
            return -1;
        }
        return cursor.getInt(columnIndex);
    }
}
