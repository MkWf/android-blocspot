package com.bloc.blocspot.api;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
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
    private final String API_KEY = BlocSpotApplication.getSharedInstance().getString(R.string.API_KEY);

    private Context mContext;
    private ExecutorService mExecutorService;
    private Location mUserLocation;
    private List<Place> mListPlaces;
    private List<PointItem> mListPointItems = new ArrayList<>();
    private List<PointItem> mListBackupPointItems = new ArrayList<>();
    private List<Category> mListCategories;
    private List<String> mListCategoryColors = new ArrayList<>();
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
        mListCategoryColors.add(mContext.getResources().getString(R.string.categ_white));
        mListCategoryColors.add(mContext.getResources().getString(R.string.categ_red));
        mListCategoryColors.add(mContext.getResources().getString(R.string.categ_green));
        mListCategoryColors.add(mContext.getResources().getString(R.string.categ_blue));
        mListCategoryColors.add(mContext.getResources().getString(R.string.categ_yellow));
        mListCategoryColors.add(mContext.getResources().getString(R.string.categ_aqua));
        mListCategoryColors.add(mContext.getResources().getString(R.string.categ_magenta));
    }

    public Location getLocation(){ return mUserLocation; }
    public Context getContext(){ return mContext;}
    public List<Place> getPlaces(){ return mListPlaces; }
    public List<Category> getCategories(){ return mListCategories; }
    public List<PointItem> getPoints(){ return mListPointItems; }
    public List<PointItem> getBackupPoints(){ return mListBackupPointItems; }
    public List<String> getCategoryColors(){ return mListCategoryColors; }

    public List<String> getCategoryNames(){
        List<String> names = new ArrayList<String>();
        for(int i = 0; i<getCategories().size(); i++){
            names.add(getCategories().get(i).getName());
        }
        return names;
    }

    public String getCategoryColor(String category){
        if(mListCategories != null){
            for(int i = 0; i< mListCategories.size(); i++){
                if(mListCategories.get(i).getName().equals(category)){
                    return mListCategories.get(i).getColor();
                }
            }
            return "White";
        }
        return "";
    }

    public void filterPointsByCategory(String category){
        if(category.equals("All")){
            mListPointItems.clear();
            mListPointItems.addAll(mListBackupPointItems);
            Collections.sort(mListPointItems, new PointItem());
        }else{
            List<PointItem> filter = new ArrayList<>();
            for(int i = 0; i< mListBackupPointItems.size(); i++){
                if(mListBackupPointItems.get(i).getCategory().equals(category)){
                    filter.add(mListPointItems.get(i));
                }
            }
            mListPointItems.clear();
            mListPointItems.addAll(filter);
        }
    }

    public void deletePoint(int mClickedItemPosition) {
        mListPointItems.remove(mClickedItemPosition);
        mListBackupPointItems.remove(mClickedItemPosition);
    }

    public void fetchPointItemPlaces(final Callback<List<PointItem>> callback) {
        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {
                mPlacesService = new PlacesService(API_KEY);
                mListPlaces = mPlacesService.findPlaces(mUserLocation.getLatitude(), mUserLocation.getLongitude());
                if (mListPlaces.size() == 0) {
                    return;
                }

                mWritableDatabase = mDatabaseOpenHelper.getWritableDatabase();
                for (int i = 0; i < mListPlaces.size(); i++) {
                    if (mListPlaces.get(i) != null) {
                        mListPointItems.add(new PointItem());
                        mListPointItems.get(i).setLocation(mListPlaces.get(i).getName());
                        double pointDistance = distBetweenGPSPointsInMiles(mUserLocation.getLatitude(), mUserLocation.getLongitude(), mListPlaces.get(i).getLatitude(), mListPlaces.get(i).getLongitude());
                        int dist = (int) pointDistance + 1;

                        mListPointItems.get(i).setDistance("< " + Integer.toString(dist) + " mi");
                        mListPointItems.get(i).setDistanceValue(dist);
                        mListPointItems.get(i).setLat(mListPlaces.get(i).getLatitude());
                        mListPointItems.get(i).setLon(mListPlaces.get(i).getLongitude());
                        mListPointItems.get(i).setVicinity(mListPlaces.get(i).getVicinity());
                    }
                }

                if (!searchForCategory("All")) {
                    new CategoryTable.Builder()
                            .setName("All")
                            .setColor(mContext.getResources().getString(R.string.categ_white))
                            .insert(mWritableDatabase);
                }

                mListCategories = new ArrayList<Category>();
                mListCategories.addAll(fetchCategories());

                Collections.sort(mListPointItems, new PointItem());
                mListBackupPointItems.addAll(mListPointItems);

                final List<PointItem> finalItems = mListPointItems;
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
                mListPlaces = mPlacesService.findPlaces(mUserLocation.getLatitude(), mUserLocation.getLongitude());

                mListPointItems.clear();
                if (mListPlaces.size() == 0) {
                    return;
                }

                for (int i = 0; i < mListPlaces.size(); i++) {
                    if (mListPlaces.get(i) != null) {
                        mListPointItems.add(new PointItem());
                        mListPointItems.get(i).setLocation(mListPlaces.get(i).getName());
                        double pointDistance = distBetweenGPSPointsInMiles(mUserLocation.getLatitude(), mUserLocation.getLongitude(), mListPlaces.get(i).getLatitude(), mListPlaces.get(i).getLongitude());
                        int dist = (int) pointDistance + 1;

                        mListPointItems.get(i).setDistance("< " + Integer.toString(dist) + " mi");
                        mListPointItems.get(i).setDistanceValue(dist);
                        mListPointItems.get(i).setLat(mListPlaces.get(i).getLatitude());
                        mListPointItems.get(i).setLon(mListPlaces.get(i).getLongitude());
                        mListPointItems.get(i).setVicinity(mListPlaces.get(i).getVicinity());
                    }
                }

                Collections.sort(mListPointItems, new PointItem());

                mListBackupPointItems.clear();
                mListBackupPointItems.addAll(mListPointItems);

                final List<PointItem> finalItems = mListPointItems;
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

    public void searchForCategoryInBackground(final String category, final Callback<Boolean> callback){
        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {
                Cursor c = mWritableDatabase.query(mCategoryTable.getName(), null, "category_name = ?", new String[]{category}, null, null, null);
                if (c.getCount() == 0) {
                    callbackThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(false);
                        }
                    });
                }else{
                    callbackThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(true);
                        }
                    });
                }
            }
        });
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
        boolean isVisited = false;
        if(item.isVisited()){
            isVisited = true;
        }
        new PointTable.Builder()
                .setLocation(item.getLocation())
                .setLatitude(item.getLat())
                .setLongitude(item.getLon())
                .setVicinity(item.getVicinity())
                .setVisited(isVisited)
                .setNote(item.getNote())
                .insert(mWritableDatabase);
    }

    public void insertCategory(final String category, final String color, final Callback<Void> callback){
        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {
                new CategoryTable.Builder()
                        .setName(category)
                        .setColor(color)
                        .insert(mWritableDatabase);
                callbackThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(null);
                    }
                });
            }
        });

        mListCategories.add(new Category(category, color));
    }

    public void removeCategory(String category){
        for(int i=0; i< mListCategories.size(); i++){
            if(mListCategories.get(i).getName().equals(category)){
                mListCategories.remove(i);
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

