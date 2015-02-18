package com.bloc.blocspot.api.model.database.table;

/**
 * Created by Mark on 2/18/2015.
 */
public class Category extends Table {
    private static final String COLUMN_CATEGORY_NAME = "category_name";
    private static final String COLUMN_CATEGORY_COLOR = "category_color";

    @Override
    public String getName() {
        return "categories";
    }

    @Override
    public String getCreateStatement() {
        return "CREATE TABLE " + getName() + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CATEGORY_NAME + " TEXT,"
                + COLUMN_CATEGORY_COLOR + " TEXT)";
    }
}
