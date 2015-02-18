package com.bloc.blocspot.api.model.database.table;

/**
 * Created by Mark on 2/18/2015.
 */
public class Point extends Table {
    private static final String COLUMN_POINT_LOCATION= "point_location";
    private static final String COLUMN_POINT_NOTE = "point_note";
    private static final String COLUMN_POINT_LATITUDE = "point_latitude";
    private static final String COLUMN_POINT_LONGITUDE = "point_longitude";
    private static final String COLUMN_POINT_VICINITY = "point_vicinity";
    private static final String COLUMN_POINT_VISITED = "point_is_visited";
    private static final String COLUMN_POINT_CATEGORY = "point_category";

    @Override
    public String getName() {
        return "Points";
    }

    @Override
    public String getCreateStatement() {
        return "CREATE TABLE " + getName() + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_POINT_LOCATION + " TEXT,"
                + COLUMN_POINT_NOTE + " TEXT,"
                + COLUMN_POINT_LATITUDE + " REAL,"
                + COLUMN_POINT_LONGITUDE + " REAL,"
                + COLUMN_POINT_VICINITY+ " TEXT,"
                + COLUMN_POINT_VISITED + " INTEGER DEFAULT 0,"
                + COLUMN_POINT_CATEGORY+ " TEXT)";
    }
}
