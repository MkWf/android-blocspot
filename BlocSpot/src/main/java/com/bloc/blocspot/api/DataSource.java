package com.bloc.blocspot.api;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;

import com.bloc.blocspot.BlocSpotApplication;
import com.bloc.blocspot.api.model.Category;
import com.bloc.blocspot.api.model.PointItem;
import com.bloc.blocspot.api.model.database.DatabaseOpenHelper;
import com.bloc.blocspot.api.model.database.table.CategoryTable;
import com.bloc.blocspot.api.model.database.table.PointTable;
import com.bloc.blocspot.blocspot.BuildConfig;
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

    private String API_KEY = "AIzaSyAhYD6RyZbvacqp8ZOpG4bOUozZDN-5zP0";
    private LocationManager locationManager;
    private ProgressDialog dialog;
    private Context context;
    private ExecutorService executorService;
    private Location loc;
    private List<Place> places;
    private List<PointItem> items;
    private List<PointItem> backupItems;
    private List<Category> categories;
    private List<String> colors = new ArrayList<String>();
    private DatabaseOpenHelper databaseOpenHelper;
    private CategoryTable categoryTable;
    private PointTable pointTable;
    SQLiteDatabase writableDatabase;

    public static interface Callback<Result> {
        public void onSuccess(Result result);
        public void onError(String errorMessage);
    }

    void submitTask(Runnable task) {
        if (executorService.isShutdown() || executorService.isTerminated()) {
            executorService = Executors.newSingleThreadExecutor();
        }
        executorService.submit(task);
    }

    public DataSource(Context context) {
        this.context = context;
        executorService = Executors.newSingleThreadExecutor();

        //loc.setLatitude(40.54992600000001);
        //loc.setLongitude(-74.20030700000001);

        categoryTable = new CategoryTable();
        pointTable = new PointTable();

        databaseOpenHelper = new DatabaseOpenHelper(BlocSpotApplication.getSharedInstance(),
                categoryTable, pointTable);

        colors = new ArrayList<String>();

        colors.add("White");
        colors.add("Red");
        colors.add("Green");
        colors.add("Blue");
        colors.add("Yellow");
        colors.add("Aqua");
        colors.add("Magenta");

        backupItems = new ArrayList<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG && false) {
                    //BlocSpotApplication.getSharedInstance().deleteDatabase("blocspot_db");
                }
            }
        }).start();
    }

    public Location getLocation(){
        return loc;
    }

    public Context getContext(){
        return context;
    }

    public List<Place> getPlaces(){ return places; }

    public List<Category> getCategories(){
        return categories;
    }

    public List<PointItem> getPoints(){ return items; }

    public List<String> getCategoryColors(){ return colors; }

    public List<String> getCategoryNames(){
        List<String> names = new ArrayList<String>();
        for(int i = 0; i<getCategories().size(); i++){
            names.add(getCategories().get(i).getName());
        }
        return names;
    }

    public String getCategoryColor(String category){
        if(categories != null){
            for(int i = 0; i<categories.size(); i++){
                if(categories.get(i).getName().equals(category)){
                    return categories.get(i).getColor();
                }
            }
            return "White";
        }
        return "";
    }

    public void filterPointsByCategory(String category){
        if(category.equals("All")){
            items.removeAll(items);
            items.addAll(backupItems);
            Collections.sort(items, new PointItem());
        }else{
            items.removeAll(items);
            items.addAll(backupItems);
            List<PointItem> filter = new ArrayList<>();
            for(int i = 0; i<items.size(); i++){
                if(items.get(i).getCategory().equals(category)){
                    filter.add(items.get(i));
                }
            }
            items.removeAll(items);
            items.addAll(filter);
        }
    }

    public void fetchPointItemPlaces(final Callback<List<PointItem>> callback) {
        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {
                //currentLocation();
                PlacesService service = new PlacesService("AIzaSyAhYD6RyZbvacqp8ZOpG4bOUozZDN-5zP0");
                places = service.findPlaces(40.54992600000001, -74.20030700000001);
                if (places.size() == 0) {
                    return;
                }

                items = new ArrayList<PointItem>();
                writableDatabase = databaseOpenHelper.getWritableDatabase();
                for (int i = 0; i < places.size(); i++) {
                    if (places.get(i) != null) {
                        items.add(new PointItem());
                        items.get(i).setLocation(places.get(i).getName());
                        //loc.setLatitude(40.54992600000001);
                        //loc.setLongitude(-74.20030700000001);
                        double pointDistance = distBetweenGPSPointsInMiles(40.54992600000001, -74.20030700000001, places.get(i).getLatitude(), places.get(i).getLongitude());
                        int dist = (int) pointDistance + 1;

                        items.get(i).setDistance("< " + Integer.toString(dist) + " mi");
                        items.get(i).setDistanceValue(dist);
                        items.get(i).setLat(places.get(i).getLatitude());
                        items.get(i).setLon(places.get(i).getLongitude());
                        items.get(i).setVicinity(places.get(i).getVicinity());
                    }
                }
                backupItems.addAll(items);

                if(!searchForCategory("All")) {
                    new CategoryTable.Builder()
                            .setName("All")
                            .setColor("White")
                            .insert(writableDatabase);
                }

                categories = new ArrayList<Category>();
                categories.addAll(fetchCategories());

                /*searchForCategory("All", new Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        if(!aBoolean){
                            new CategoryTable.Builder()
                                    .setName("All")
                                    .setColor("White")
                                    .insert(writableDatabase);
                        }
                        categories = new ArrayList<Category>();
                        categories.addAll(fetchCategories());
                    }

                    @Override
                    public void onError(String errorMessage) {

                    }
                });*/



                Collections.sort(items, new PointItem());

                final List<PointItem> finalItems = items;
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
        final Cursor c = writableDatabase.rawQuery("SELECT * FROM categories", null);
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
        Cursor c = writableDatabase.query(categoryTable.getName(), null, "category_name = ?", new String[] {category}, null, null, null);
        if(c.getCount() == 0){
            return false;
        }
        return true;
    }

    public void searchForCategory(final String category, final Callback<Boolean> callback){
        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {
                final boolean b;
                Cursor c = writableDatabase.query(categoryTable.getName(), null, "category_name = ?", new String[]{category}, null, null, null);
                if (c.getCount() == 0) {
                    b = false;
                }else{
                    b = true;
                }
                callbackThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(b);
                    }
                });
            }
        });
    }

    public boolean searchForCat(String category){
        for(int i = 0; i<categories.size(); i++){
            if(categories.get(i).getName().equals(category)){
                return true;
            }
        }
        return false;
    }

    public boolean searchForColor(String color){
        Cursor c = writableDatabase.query(categoryTable.getName(), null, "category_color = ?", new String[] {color}, null, null, null);
        if(c.getCount() == 0){
            return false;
        }
        return true;
    }

    public void insertPoint(PointItem item){
        new PointTable.Builder()
                .setLocation(item.getLocation())
                .setLatitude(item.getLat())
                .setLongitude(item.getLon())
                .setVicinity(item.getVicinity())
                .insert(writableDatabase);
    }

    public void insertCategory(String category, String color){
        new CategoryTable.Builder()
                .setName(category)
                .setColor(color)
                .insert(writableDatabase);
        categories.add(new Category(category, color));
    }

    public void removeCategory(String category){
        for(int i=0; i<categories.size(); i++){
            if(categories.get(i).getName().equals(category)){
                categories.remove(i);
            }
        }
        writableDatabase.delete(categoryTable.getName(), "category_name = ?", new String[] {category});
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

    private void currentLocation() {
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        String provider = locationManager.getBestProvider(new Criteria(), false);

        Location location = locationManager.getLastKnownLocation(provider);

        if (location == null) {
            locationManager.requestLocationUpdates(provider, 0, 0, listener);
        } else {
            loc = location;
        }
    }

    private LocationListener listener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onLocationChanged(Location location) {
            loc = location;
            locationManager.removeUpdates(listener);
        }
    };
}

