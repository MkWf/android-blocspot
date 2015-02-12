package com.bloc.blocspot.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.bloc.blocspot.blocspot.R;
import com.bloc.blocspot.places.Place;
import com.bloc.blocspot.places.PlacesService;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
public class MapActivity extends Activity {
    private String API_KEY = "AIzaSyAhYD6RyZbvacqp8ZOpG4bOUozZDN-5zP0";
    private final String TAG = getClass().getSimpleName();
    private GoogleMap mMap;
    //private String[] places;
    private LocationManager locationManager;
    private Location loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initCompo();
        currentLocation();

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

    private class GetPlaces extends AsyncTask<Void, Void, ArrayList<Place>> {

        private ProgressDialog dialog;
        private Context context;
      //  private String places;

        public GetPlaces(Context context){//{, String places) {
            this.context = context;
           // this.places = places;
        }

        @Override
        protected void onPostExecute(ArrayList<Place> result) {
            super.onPostExecute(result);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if(result.size() == 0){
                return;
            }

            List<Marker> markers = new ArrayList<Marker>();
            for (int i = 0; i < result.size(); i++) {
                if(result.get(i) != null){
                    markers.add(mMap.addMarker(new MarkerOptions()
                            .title(result.get(i).getName())
                            .position(
                                    new LatLng(result.get(i).getLatitude(), result
                                            .get(i).getLongitude()))
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.pin))
                            .snippet(result.get(i).getVicinity())));
                }
            }
            displayAllPointsInView(markers);

           /* CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(result.get(1).getLatitude(), result
                            .get(1).getLongitude())) // Sets the center of the map to
                            // Mountain View
                    .zoom(14) // Sets the zoom
                    .tilt(30) // Sets the tilt of the camera to 30 degrees
                    .build(); // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));*/
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setCancelable(false);
            dialog.setMessage("Loading Map..");
            dialog.isIndeterminate();
            dialog.show();
        }

        @Override
        protected ArrayList<Place> doInBackground(Void... arg0) {
            PlacesService service = new PlacesService(
                    API_KEY);
            ArrayList<Place> findPlaces = service.findPlaces(loc.getLatitude(), // 28.632808
                    loc.getLongitude()); // 77.218276
            return findPlaces;
        }

    }

    private void initCompo() {
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();
    }

    private void currentLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        String provider = locationManager
                .getBestProvider(new Criteria(), false);

        Location location = locationManager.getLastKnownLocation(provider);

        if (location == null) {
            locationManager.requestLocationUpdates(provider, 0, 0, listener);
        } else {
            loc = location;
            new GetPlaces(MapActivity.this).execute();
            //Log.e(TAG, "location : " + location);
        }

    }

    public void displayAllPointsInView(List<Marker> items){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (Marker marker : items) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.animateCamera(cu);
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
            Log.e(TAG, "location update : " + location);
            loc = location;
            locationManager.removeUpdates(listener);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu, menu);
        return true;
    }
}