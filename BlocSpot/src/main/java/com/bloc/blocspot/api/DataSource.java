package com.bloc.blocspot.api;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;

import com.bloc.blocspot.BlocSpotApplication;
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

        categoryTable = new CategoryTable();
        pointTable = new PointTable();

        databaseOpenHelper = new DatabaseOpenHelper(BlocSpotApplication.getSharedInstance(),
                categoryTable, pointTable);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG && false) {
                    BlocSpotApplication.getSharedInstance().deleteDatabase("blocspot_db");
                }

            }
        }).start();
    }

    public Context getContext(){
        return context;
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

    public void fetchPointItemPlaces(final Callback<List<PointItem>> callback) {
        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {
                currentLocation();
                PlacesService service = new PlacesService(
                        "AIzaSyAhYD6RyZbvacqp8ZOpG4bOUozZDN-5zP0");
                places = service.findPlaces(loc.getLatitude(), // 28.632808
                        loc.getLongitude());
                if (places.size() == 0) {
                    return;
                }
                items = new ArrayList<PointItem>(places.size());
                for (int i = 0; i < places.size(); i++) {
                    if (places.get(i) != null) {
                        items.add(new PointItem());
                        items.get(i).setLocation(places.get(i).getName());

                        double pointDistance = distBetweenGPSPointsInMiles(loc.getLatitude(), loc.getLongitude(), places.get(i).getLatitude(), places.get(i).getLongitude());
                        int dist = (int) pointDistance + 1;


                        items.get(i).setDistance("< " + Integer.toString(dist) + " mi");
                        items.get(i).setDistanceValue(dist);
                        items.get(i).setLat(places.get(i).getLatitude());
                        items.get(i).setLon(places.get(i).getLongitude());
                        items.get(i).setVicinity(places.get(i).getVicinity());

                        writableDatabase = databaseOpenHelper.getWritableDatabase();

                        ContentValues v = new ContentValues();

                        long pointItemId = new PointTable.Builder()
                                .setLocation(items.get(i).getLocation())
                                .setLatitude(items.get(i).getLat())
                                .setLongitude(items.get(i).getLon())
                                .setVicinity(items.get(i).getVicinity())
                                .insert(writableDatabase);
                    }
                }
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

    public List<Place> getPlaces(){
        return places;
    }
    public List<PointItem> getPoints(){ return items; }

    public double distBetweenGPSPointsInMiles(
            double lat1, double lng1, double lat2, double lng2) {
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

    public Location getUserPosition(){
        return loc;
    }
}

