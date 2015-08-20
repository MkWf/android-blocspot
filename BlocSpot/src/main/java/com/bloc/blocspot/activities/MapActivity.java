package com.bloc.blocspot.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

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
 *
 *
 * @author Karn Shah
 * @Date   10/3/2013
 *
 */
public class MapActivity extends ActionBarActivity implements GoogleMap.OnMarkerClickListener, View.OnClickListener {
    private GoogleMap mMap;
    private Toolbar mToolBar;
    private List<Marker> mPlaceMarkers = new ArrayList<>();
    private Marker mUserPositionMarker;
    private int mNavIndex = 0;
    private AlertDialog.Builder mADMarker;
    private AlertDialog mADDestroyer;
    private PointItem mClickedMarkerItem;
    private int mClickedMarkerItemPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        mNavIndex = intent.getIntExtra("navigate", -1);

        initMap();
        loadMap(BlocSpotApplication.getSharedDataSource().getPlaces());

        mToolBar = (Toolbar) findViewById(R.id.tb_activity_main);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initMap() {
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();
        mMap.setOnMarkerClickListener(this);

    }

    public void loadMap(List<Place> place){
        setMapPoints();
        setUserPoint();

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                //If user used the 'Navigate To...' option on a POI
                if(mNavIndex != -1){
                    mPlaceMarkers.get(mNavIndex).showInfoWindow();
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(BlocSpotApplication.getSharedDataSource().getPoints().get(mNavIndex).getLat(),
                                    BlocSpotApplication.getSharedDataSource().getPoints().get(mNavIndex).getLon()))

                            .zoom(18)
                            .tilt(90)
                            .build();
                    mMap.moveCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition));
                //If Map icon was clicked
                }else{
                    displayAllPointsInView(mPlaceMarkers);
                }
            }
        });
    }

    public void setMapPoints() {
        List<PointItem> result = BlocSpotApplication.getSharedDataSource().getPoints();
        if (result == null || result.size() == 0) {
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Loading Map..");
        dialog.isIndeterminate();
        dialog.show();

        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) != null && !result.get(i).isVisited()){
                mPlaceMarkers.add(mMap.addMarker(new MarkerOptions()
                        .title(result.get(i).getLocation())
                        .position(
                                new LatLng(result.get(i).getLat(), result
                                        .get(i).getLon()))
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.pin))
                        .snippet(result.get(i).getVicinity())));
                setNonVisitedMarkers(BlocSpotApplication.getSharedDataSource().getCategoryColor(result.get(i).getCategory()), i);
            }else if(result.get(i) != null && result.get(i).isVisited()){
                mPlaceMarkers.add(mMap.addMarker(new MarkerOptions()
                        .title(result.get(i).getLocation())
                        .position(
                                new LatLng(result.get(i).getLat(), result
                                        .get(i).getLon()))
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.visited_pin))
                        .snippet(result.get(i).getVicinity())));
                setVisitedMarkers(BlocSpotApplication.getSharedDataSource().getCategoryColor(result.get(i).getCategory()), i);
            }
        }
        dialog.dismiss();
    }

    public void setUserPoint(){
        mUserPositionMarker = mMap.addMarker(new MarkerOptions()
                .title("Your position")
                .position(
                        new LatLng(BlocSpotApplication.getSharedDataSource().getLocation().getLatitude(),
                                BlocSpotApplication.getSharedDataSource().getLocation().getLongitude()))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_pin)));
    }

    public void displayAllPointsInView(List<Marker> items) {
        if(!items.isEmpty()){
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (Marker marker : items) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();

            int padding = 100;
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

            mMap.animateCamera(cu);
        }else{
            mUserPositionMarker.showInfoWindow();
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(mUserPositionMarker.getPosition())
                    .zoom(18)
                    .tilt(90)
                    .build();
            mMap.moveCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.equals(mUserPositionMarker)){
            return false;
        }
        mClickedMarkerItemPosition = mPlaceMarkers.indexOf(marker);
        mClickedMarkerItem = BlocSpotApplication.getSharedDataSource().getPoints().get(mClickedMarkerItemPosition);

        mADMarker = new AlertDialog.Builder(MapActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.point_item_map_alert, null);
        mADMarker.setView(dialogView);

        TextView location = (TextView) dialogView.findViewById(R.id.map_dialog_location);
        TextView note = (TextView) dialogView.findViewById(R.id.map_dialog_note);
        CheckBox visited = (CheckBox) dialogView.findViewById(R.id.map_dialog_checkbox);
        Button category = (Button) dialogView.findViewById(R.id.map_dialog_category_button);
        ImageButton navigate = (ImageButton) dialogView.findViewById(R.id.map_dialog_navigate);
        ImageButton share = (ImageButton) dialogView.findViewById(R.id.map_dialog_share);
        ImageButton delete = (ImageButton) dialogView.findViewById(R.id.map_dialog_delete);

        category.setText(mClickedMarkerItem.getCategory());
        String color = BlocSpotApplication.getSharedDataSource().getCategoryColor(mClickedMarkerItem.getCategory());

        if(color.equals(getResources().getString(R.string.categ_white))){
            category.setTextColor(getResources().getColor(R.color.white));
        }else  if(color.equals(getResources().getString(R.string.categ_red))){
            category.setTextColor(getResources().getColor(R.color.red));
        }else  if(color.equals(getResources().getString(R.string.categ_green))){
            category.setTextColor(getResources().getColor(R.color.green));
        }else  if(color.equals(getResources().getString(R.string.categ_blue))){
            category.setTextColor(getResources().getColor(R.color.blue));
        }else if(color.equals(getResources().getString(R.string.categ_yellow))){
            category.setTextColor(getResources().getColor(R.color.yellow));
        }else if(color.equals(getResources().getString(R.string.categ_aqua))){
            category.setTextColor(getResources().getColor(R.color.aqua));
        }else  if(color.equals(getResources().getString(R.string.categ_magenta))){
            category.setTextColor(getResources().getColor(R.color.magenta));
        }

        location.setText(mClickedMarkerItem.getLocation());
        note.setText(mClickedMarkerItem.getNote());

        if(mClickedMarkerItem.isVisited()){
            visited.setChecked(true);
        }else{
            visited.setChecked(false);
        }

        visited.setOnClickListener(this);
        navigate.setOnClickListener(this);
        share.setOnClickListener(this);
        delete.setOnClickListener(this);

        mADDestroyer = mADMarker.show();

        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.map_dialog_checkbox:
                if(mClickedMarkerItem.isVisited()){
                    mClickedMarkerItem.setVisited(false);
                    setNonVisitedMarkers(BlocSpotApplication.getSharedDataSource().getCategoryColor(mClickedMarkerItem.getCategory()), mClickedMarkerItemPosition);
                }else{
                    mClickedMarkerItem.setVisited(true);
                    setVisitedMarkers(BlocSpotApplication.getSharedDataSource().getCategoryColor(mClickedMarkerItem.getCategory()), mClickedMarkerItemPosition);
                }
                break;
            case R.id.map_dialog_category_button:

                break;
            case R.id.map_dialog_navigate:
                mADDestroyer.dismiss();
                mPlaceMarkers.get(BlocSpotApplication.getSharedDataSource().getPoints().indexOf(mClickedMarkerItem)).showInfoWindow();
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(BlocSpotApplication.getSharedDataSource().getPoints().get(BlocSpotApplication.getSharedDataSource().getPoints().indexOf(mClickedMarkerItem)).getLat(),
                                BlocSpotApplication.getSharedDataSource().getPoints().get(BlocSpotApplication.getSharedDataSource().getPoints().indexOf(mClickedMarkerItem)).getLon()))
                        .zoom(18)
                        .tilt(90)
                        .build();
                mMap.moveCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
                break;
            case R.id.map_dialog_share:
                mADDestroyer.dismiss();
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        String.format("Check this place out: %s, %s ", mClickedMarkerItem.getLocation(), mClickedMarkerItem.getVicinity()));
                shareIntent.setType("text/plain");
                Intent chooser = Intent.createChooser(shareIntent, getString(R.string.share_chooser_title));
                startActivity(chooser);
                break;
            case R.id.map_dialog_delete:
                mADDestroyer.dismiss();
                mPlaceMarkers.remove(mClickedMarkerItemPosition).remove();
                BlocSpotApplication.getSharedDataSource().getPoints().remove(mClickedMarkerItem);
                break;
        }
    }

    public void setVisitedMarkers(String color, int index){
        if(color.equals("Red")){
            mPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.red_visited_pin));
        }else if(color.equals("Green")){
            mPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.green_visited_pin));
        }else if(color.equals("Blue")){
            mPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.blue_visited_pin));
        }else if(color.equals("Yellow")){
            mPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.yellow_visited_pin));
        }else if(color.equals("Aqua")){
            mPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.aqua_visited_pin));
        }else if(color.equals("Magenta")){
            mPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.magenta_visited_pin));
        }else{
            mPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.visited_pin));
        }
    }

    public void setNonVisitedMarkers(String color, int index){
        if(color.equals("Red")){
            mPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.red_pin));
        }else if(color.equals("Green")){
            mPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.green_pin));
        }else if(color.equals("Blue")){
            mPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.blue_pin));
        }else if(color.equals("Yellow")){
            mPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.yellow_pin));
        }else if(color.equals("Aqua")){
            mPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.aqua_pin));
        }else if(color.equals("Magenta")){
            mPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.magenta_pin));
        }else{
            mPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.pin));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_actionbar_menu, menu);
        return true;
    }
}
