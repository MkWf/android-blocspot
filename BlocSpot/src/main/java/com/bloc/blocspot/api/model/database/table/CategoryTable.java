package com.bloc.blocspot.api.model.database.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Mark on 2/18/2015.
 */
public class CategoryTable extends Table {

    public static class Builder implements Table.Builder {
        ContentValues values = new ContentValues();

        public Builder setName(String categoryName) {
            values.put(COLUMN_CATEGORY_NAME, categoryName);
            return this;
        }

        public Builder setColor(String color) {
            values.put(COLUMN_CATEGORY_COLOR, color);
            return this;
        }

        @Override
        public long insert(SQLiteDatabase writableDB) {
            return writableDB.insert(NAME, null, values);
        }
    }

    public static String getCategoryName(Cursor cursor) {
        return getString(cursor, COLUMN_CATEGORY_NAME);
    }

    public static String getCategoryColor(Cursor cursor) {
        return getString(cursor, COLUMN_CATEGORY_COLOR);
    }

    private static final String NAME = "categories";
    private static final String COLUMN_CATEGORY_NAME = "category_name";
    private static final String COLUMN_CATEGORY_COLOR= "category_color";


    @Override
    public String getName() {
        return NAME;
    }

    public String getColumnCategoryName(){
        return COLUMN_CATEGORY_NAME;
    }

    @Override
    public String getCreateStatement() {
        return "CREATE TABLE " + getName() + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_CATEGORY_NAME + " TEXT,"
                + COLUMN_CATEGORY_COLOR + " TEXT)";
    }
}
