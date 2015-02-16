package com.bloc.blocspot.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.bloc.blocspot.BlocSpotApplication;
import com.bloc.blocspot.api.model.PointItem;
import com.bloc.blocspot.blocspot.R;
import com.bloc.blocspot.places.Place;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 *  This class is used to search places using Places API using keywords like police,hospital etc.
 *
 * @author Karn Shah
 * @Date   10/3/2013
 *
 */
public class MapActivity extends ActionBarActivity {
    private String API_KEY = "AIzaSyAhYD6RyZbvacqp8ZOpG4bOUozZDN-5zP0";
    private final String TAG = getClass().getSimpleName();
    private GoogleMap mMap;
    //private String[] places;
    private LocationManager locationManager;
    private Location loc;
    private Toolbar toolbar;
    List<Marker> placeMarkers;
    ArrayList<Integer> deletions;
    private int nav = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        deletions = intent.getIntegerArrayListExtra("data");

        nav = intent.getIntExtra("navigate", -1);

        initMap();
        loadMap(BlocSpotApplication.getSharedDataSource().getPlaces());

        toolbar = (Toolbar) findViewById(R.id.tb_activity_main);
        setSupportActionBar(toolbar);

        //places = getResources().getStringArray(R.array.places);
        //currentLocation();
        /*final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(ArrayAdapter.createFromResource(
                        this, R.array.places, android.R.layout.simple_list_item_1),
                new ActionBar.OnNavigationListener() {

                    @Override
                    public boolean onNavigationItemSelected(int itemPosition,
                                                            long itemId) {
                        Log.e(TAG,
                                places[itemPosition].toLowerCase().replace("-",
                                        "_"));
                        if (loc != null) {
                            mMap.clear();
                            new GetPlaces(MapActivity.this,
                                    places[itemPosition].toLowerCase().replace(
                                            "-", "_").replace(" ", "_")).execute();
                        }
                        return true;
                    }

                });*/
    }

    public void loadMap(List<Place> place){
        setMapPoints();
        setUserPoint();

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if(nav != -1){
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(BlocSpotApplication.getSharedDataSource().getPoints().get(nav).getLat(),
                                    BlocSpotApplication.getSharedDataSource().getPoints().get(nav).getLon()))

                            .zoom(18)
                            .tilt(90)
                            .build();
                    mMap.moveCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition));
                   // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(BlocSpotApplication.getSharedDataSource().getPoints().get(nav).getLat(),
                            //BlocSpotApplication.getSharedDataSource().getPoints().get(nav).getLon()), 15));
                }else{
                    displayAllPointsInView(placeMarkers);
                }
            }
        });
    }

    public void setMapPoints() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Loading Map..");
        dialog.isIndeterminate();
        dialog.show();

        List<PointItem> result = BlocSpotApplication.getSharedDataSource().getPoints();
        if (result == null || result.size() == 0) {
            return;
        }

        placeMarkers = new ArrayList<Marker>();
        for (int i = 0; i < result.size(); i++) {
           // if (result.get(i) != null) {
                placeMarkers.add(mMap.addMarker(new MarkerOptions()
                        .title(result.get(i).getLocation())
                        .position(
                                new LatLng(result.get(i).getLat(), result
                                        .get(i).getLon()))
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.pin))
                        .snippet(result.get(i).getVicinity())));
          //  }
        }

      /*  if(deletions != null){
            for(int i = 0; i < deletions.size(); i++){
                int k = deletions.get(i);
                placeMarkers.get(k).remove();
                placeMarkers.remove(k);
            }
        }*/
        dialog.dismiss();
    }

    public void setUserPoint(){
        mMap.addMarker(new MarkerOptions()
                .title("Your position")
                .position(
                        new LatLng(BlocSpotApplication.getSharedDataSource().getUserPosition().getLatitude(),
                                BlocSpotApplication.getSharedDataSource().getUserPosition().getLongitude()))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_pin)));
    }


           /* CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(result.get(1).getLatitude(), result
                            .get(1).getLongitude())) // Sets the center of the map to
                            // Mountain View
                    .zoom(14) // Sets the zoom
                    .tilt(30) // Sets the tilt of the camera to 30 degrees
                    .build(); // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));*/
    //}


    private void initMap() {
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();
    }

    public void displayAllPointsInView(List<Marker> items) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (Marker marker : items) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.animateCamera(cu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_actionbar_menu, menu);
        return true;
    }
}
