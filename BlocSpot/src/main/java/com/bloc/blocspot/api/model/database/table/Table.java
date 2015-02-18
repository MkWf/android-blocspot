package com.bloc.blocspot.api.model.database.table;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Mark on 2/18/2015.
 */
public abstract class Table {
    protected static final String COLUMN_ID = "id";

    public abstract String getName();

    public abstract String getCreateStatement();

    public void onUpgrade(SQLiteDatabase writableDatabase, int oldVersion, int newVersion) {
        // Nothing
    }
}
