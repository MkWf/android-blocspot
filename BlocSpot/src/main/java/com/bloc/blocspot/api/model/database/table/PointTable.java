package com.bloc.blocspot.api.model.database.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Mark on 2/18/2015.
 */
public class PointTable extends Table {

    public static class Builder implements Table.Builder {
        ContentValues values = new ContentValues();

        public Builder setLocation(String pointLocation) {
            values.put(COLUMN_POINT_LOCATION, pointLocation);
            return this;
        }

        /*public Builder setNote(String feedURL) {
            values.put(COLUMN_POINT_NOTE, feedURL);
            return this;
        }*/

        public Builder setLatitude(double pointLatitude) {
            values.put(COLUMN_POINT_LATITUDE, pointLatitude);
            return this;
        }

        public Builder setLongitude(double pointLongitude) {
            values.put(COLUMN_POINT_LONGITUDE, pointLongitude);
            return this;
        }

        public Builder setVicinity(String pointVicinity) {
            values.put(COLUMN_POINT_VICINITY, pointVicinity);
            return this;
        }

       /* public Builder setVisited(String pointVisited) {
            values.put(COLUMN_POINT_VISITED, pointVisited);
            return this;
        }

        public Builder setCategory(String pointCategory) {
            values.put(COLUMN_POINT_CATEGORY, pointCategory);
            return this;
        }*/

        @Override
        public long insert(SQLiteDatabase writableDB) {
            return writableDB.insert(NAME, null, values);
        }
    }

    public static String getLocation(Cursor cursor) {
        return getString(cursor, COLUMN_POINT_LOCATION);
    }

    public static String getNote(Cursor cursor) {
        return getString(cursor, COLUMN_POINT_NOTE);
    }

    public static double getLatitude(Cursor cursor) {
        return getDouble(cursor, COLUMN_POINT_LATITUDE);
    }

    public static double getLongitude(Cursor cursor) {
        return getDouble(cursor, COLUMN_POINT_LONGITUDE);
    }

    public static String getVicinity(Cursor cursor) {
        return getString(cursor, COLUMN_POINT_VICINITY);
    }

    public static boolean getVisited(Cursor cursor) {
        return getBoolean(cursor, COLUMN_POINT_VISITED);
    }

    public static String getCategory(Cursor cursor) {
        return getString(cursor, COLUMN_POINT_CATEGORY);
    }

    private static final String NAME = "point_items";
    private static final String COLUMN_POINT_LOCATION= "point_location";
    private static final String COLUMN_POINT_NOTE = "point_note";
    private static final String COLUMN_POINT_LATITUDE = "point_latitude";
    private static final String COLUMN_POINT_LONGITUDE = "point_longitude";
    private static final String COLUMN_POINT_VICINITY = "point_vicinity";
    private static final String COLUMN_POINT_VISITED = "point_is_visited";
    private static final String COLUMN_POINT_CATEGORY = "point_category";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getCreateStatement() {
        return "CREATE TABLE " + getName() + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_POINT_LOCATION + " TEXT,"
                + COLUMN_POINT_NOTE + " TEXT,"
                + COLUMN_POINT_LATITUDE + " REAL,"
                + COLUMN_POINT_LONGITUDE + " REAL,"
                + COLUMN_POINT_VICINITY+ " TEXT,"
                + COLUMN_POINT_VISITED + " INTEGER DEFAULT 0,"
                + COLUMN_POINT_CATEGORY+ " TEXT)";
    }
}
