package com.bloc.blocspot.api;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;

import com.bloc.blocspot.BlocSpotApplication;
import com.bloc.blocspot.api.model.Category;
import com.bloc.blocspot.api.model.PointItem;
import com.bloc.blocspot.api.model.database.DatabaseOpenHelper;
import com.bloc.blocspot.api.model.database.table.CategoryTable;
import com.bloc.blocspot.api.model.database.table.PointTable;
import com.bloc.blocspot.blocspot.BuildConfig;
import com.bloc.blocspot.blocspot.R;
import com.bloc.blocspot.places.Place;
import com.bloc.blocspot.places.PlacesService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Mark on 2/7/2015.
 */
public class DataSource {

    public static int MAX_CATEGORIES = 7;
    private final String API_KEY = "AIzaSyAhYD6RyZbvacqp8ZOpG4bOUozZDN-5zP0";

    private LocationManager mLocationManager;
    private Context mContext;
    private ExecutorService mExecutorService;
    private Location mUserLocation;
    private List<Place> mPlaces;
    private List<PointItem> mPointItems = new ArrayList<>();
    private List<PointItem> mBackupPointItems = new ArrayList<>();
    private List<Category> mCategories;
    private List<String> mCategoryColors = new ArrayList<>();
    private DatabaseOpenHelper mDatabaseOpenHelper;
    private CategoryTable mCategoryTable;
    private PointTable mPointTable;
    private PlacesService mPlacesService;
    private SQLiteDatabase mWritableDatabase;


    public static interface Callback<Result> {
        public void onSuccess(Result result);
        public void onError(String errorMessage);
    }

    void submitTask(Runnable task) {
        if (mExecutorService.isShutdown() || mExecutorService.isTerminated()) {
            mExecutorService = Executors.newSingleThreadExecutor();
        }
        mExecutorService.submit(task);
    }

    public DataSource(Context context) {
        this.mContext = context;
        mExecutorService = Executors.newSingleThreadExecutor();

        initDatabaseTables();
        initCategoryColors();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG && false) {
                    //BlocSpotApplication.getSharedInstance().deleteDatabase("blocspot_db");
                }
            }
        }).start();
    }

    private void initDatabaseTables() {
        mCategoryTable = new CategoryTable();
        mPointTable = new PointTable();
        mDatabaseOpenHelper = new DatabaseOpenHelper(BlocSpotApplication.getSharedInstance(), mCategoryTable, mPointTable);
    }

    private void initCategoryColors() {
        mCategoryColors.add(mContext.getResources().getString(R.string.categ_white));
        mCategoryColors.add(mContext.getResources().getString(R.string.categ_red));
        mCategoryColors.add(mContext.getResources().getString(R.string.categ_green));
        mCategoryColors.add(mContext.getResources().getString(R.string.categ_blue));
        mCategoryColors.add(mContext.getResources().getString(R.string.categ_yellow));
        mCategoryColors.add(mContext.getResources().getString(R.string.categ_aqua));
        mCategoryColors.add(mContext.getResources().getString(R.string.categ_magenta));
    }

    public Location getLocation(){ return mUserLocation; }
    public Context getContext(){ return mContext;}
    public List<Place> getPlaces(){ return mPlaces; }
    public List<Category> getCategories(){ return mCategories; }
    public List<PointItem> getPoints(){ return mPointItems; }
    public List<String> getCategoryColors(){ return mCategoryColors; }

    public List<String> getCategoryNames(){
        List<String> names = new ArrayList<String>();
        for(int i = 0; i<getCategories().size(); i++){
            names.add(getCategories().get(i).getName());
        }
        return names;
    }

    public String getCategoryColor(String category){
        if(mCategories != null){
            for(int i = 0; i< mCategories.size(); i++){
                if(mCategories.get(i).getName().equals(category)){
                    return mCategories.get(i).getColor();
                }
            }
            return "White";
        }
        return "";
    }

    public void filterPointsByCategory(String category){
        if(category.equals("All")){
            mPointItems.removeAll(mPointItems);
            mPointItems.addAll(mBackupPointItems);
            Collections.sort(mPointItems, new PointItem());
        }else{
            mPointItems.removeAll(mPointItems);
            mPointItems.addAll(mBackupPointItems);
            List<PointItem> filter = new ArrayList<>();
            for(int i = 0; i< mPointItems.size(); i++){
                if(mPointItems.get(i).getCategory().equals(category)){
                    filter.add(mPointItems.get(i));
                }
            }
            mPointItems.removeAll(mPointItems);
            mPointItems.addAll(filter);
        }
    }

    public void fetchPointItemPlaces(final Callback<List<PointItem>> callback) {
        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {
                //currentLocation();
                mPlacesService = new PlacesService(API_KEY);
                mPlaces = mPlacesService.findPlaces(mUserLocation.getLatitude(), mUserLocation.getLongitude());
                if (mPlaces.size() == 0) {
                    return;
                }

                mWritableDatabase = mDatabaseOpenHelper.getWritableDatabase();
                for (int i = 0; i < mPlaces.size(); i++) {
                    if (mPlaces.get(i) != null) {
                        mPointItems.add(new PointItem());
                        mPointItems.get(i).setLocation(mPlaces.get(i).getName());
                        double pointDistance = distBetweenGPSPointsInMiles(mUserLocation.getLatitude(), mUserLocation.getLongitude(), mPlaces.get(i).getLatitude(), mPlaces.get(i).getLongitude());
                        int dist = (int) pointDistance + 1;

                        mPointItems.get(i).setDistance("< " + Integer.toString(dist) + " mi");
                        mPointItems.get(i).setDistanceValue(dist);
                        mPointItems.get(i).setLat(mPlaces.get(i).getLatitude());
                        mPointItems.get(i).setLon(mPlaces.get(i).getLongitude());
                        mPointItems.get(i).setVicinity(mPlaces.get(i).getVicinity());
                    }
                }
                mBackupPointItems.addAll(mPointItems);

                if(!searchForCategory("All")) {
                    new CategoryTable.Builder()
                            .setName("All")
                            .setColor(mContext.getResources().getString(R.string.categ_white))
                            .insert(mWritableDatabase);
                }

                mCategories = new ArrayList<Category>();
                mCategories.addAll(fetchCategories());

                Collections.sort(mPointItems, new PointItem());

                final List<PointItem> finalItems = mPointItems;
                callbackThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(finalItems);
                    }
                });
            }
        });
    }

    public void fetchUpdatedPlaces(final Callback<List<PointItem>> callback) {
        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {
                mPlaces = mPlacesService.findPlaces(mUserLocation.getLatitude(), mUserLocation.getLongitude());

                mPointItems.clear();
                if (mPlaces.size() == 0) {
                    return;
                }

                for (int i = 0; i < mPlaces.size(); i++) {
                    if (mPlaces.get(i) != null) {
                        mPointItems.add(new PointItem());
                        mPointItems.get(i).setLocation(mPlaces.get(i).getName());
                        double pointDistance = distBetweenGPSPointsInMiles(mUserLocation.getLatitude(), mUserLocation.getLongitude(), mPlaces.get(i).getLatitude(), mPlaces.get(i).getLongitude());
                        int dist = (int) pointDistance + 1;

                        mPointItems.get(i).setDistance("< " + Integer.toString(dist) + " mi");
                        mPointItems.get(i).setDistanceValue(dist);
                        mPointItems.get(i).setLat(mPlaces.get(i).getLatitude());
                        mPointItems.get(i).setLon(mPlaces.get(i).getLongitude());
                        mPointItems.get(i).setVicinity(mPlaces.get(i).getVicinity());
                    }
                }

                mBackupPointItems.clear();
                mBackupPointItems.addAll(mPointItems);

                Collections.sort(mPointItems, new PointItem());

                final List<PointItem> finalItems = mPointItems;
                callbackThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(finalItems);
                    }
                });
            }
        });
    }

    public List<Category> fetchCategories() {
        final Cursor c = mWritableDatabase.rawQuery("SELECT * FROM categories", null);
        if(c.getCount() == 0){
            return null;
        }
        List<Category> list = new ArrayList<>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            list.add(new Category(CategoryTable.getCategoryName(c), CategoryTable.getCategoryColor(c)));
            c.moveToNext();
        }
        c.close();
        return list;
    }

    public boolean searchForCategory(String category){
        Cursor c = mWritableDatabase.query(mCategoryTable.getName(), null, "category_name = ?", new String[]{category}, null, null, null);
        if(c.getCount() == 0){
            return false;
        }
        return true;
    }

    public boolean searchForColor(String color){
        Cursor c = mWritableDatabase.query(mCategoryTable.getName(), null, "category_color = ?", new String[]{color}, null, null, null);
        if(c.getCount() == 0){
            return false;
        }
        return true;
    }

    public void setLocation(Location location){ mUserLocation = location; }

    public void insertPoint(PointItem item){
        new PointTable.Builder()
                .setLocation(item.getLocation())
                .setLatitude(item.getLat())
                .setLongitude(item.getLon())
                .setVicinity(item.getVicinity())
                .insert(mWritableDatabase);
    }

    public void insertCategory(String category, String color){
        new CategoryTable.Builder()
                .setName(category)
                .setColor(color)
                .insert(mWritableDatabase);
        mCategories.add(new Category(category, color));
    }

    public void removeCategory(String category){
        for(int i=0; i< mCategories.size(); i++){
            if(mCategories.get(i).getName().equals(category)){
                mCategories.remove(i);
            }
        }
        mWritableDatabase.delete(mCategoryTable.getName(), "category_name = ?", new String[]{category});
    }

    static PointItem itemFromCursor(Cursor cursor) {
        return new PointItem(PointTable.getLocation(cursor), PointTable.getNote(cursor),
                PointTable.getLatitude(cursor), PointTable.getLongitude(cursor),
                PointTable.getVicinity(cursor), PointTable.getVisited(cursor),
                PointTable.getCategory(cursor));
    }

    public double distBetweenGPSPointsInMiles(double lat1, double lng1, double lat2, double lng2) {
        int r = 3963;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = r * c;
        return d;
    }
}

