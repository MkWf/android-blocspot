package com.bloc.blocspot.api.model.database.table;

import android.content.ContentValues;
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

        public Builder setColor(int red, int green, int blue) {
            values.put(COLUMN_CATEGORY_COLOR_RED, red);
            values.put(COLUMN_CATEGORY_COLOR_GREEN, green);
            values.put(COLUMN_CATEGORY_COLOR_BLUE, blue);
            return this;
        }

        @Override
        public long insert(SQLiteDatabase writableDB) {
            return writableDB.insert(NAME, null, values);
        }
    }

    private static final String NAME = "categories";
    private static final String COLUMN_CATEGORY_NAME = "category_name";
    private static final String COLUMN_CATEGORY_COLOR_RED = "category_color_red";
    private static final String COLUMN_CATEGORY_COLOR_GREEN = "category_color_green";
    private static final String COLUMN_CATEGORY_COLOR_BLUE = "category_color_blue";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getCreateStatement() {
        return "CREATE TABLE " + getName() + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CATEGORY_NAME + " TEXT,"
                + COLUMN_CATEGORY_COLOR_RED + " INTEGER,"
                + COLUMN_CATEGORY_COLOR_GREEN + " INTEGER,"
                + COLUMN_CATEGORY_COLOR_BLUE + " INTEGER)";
    }
}
