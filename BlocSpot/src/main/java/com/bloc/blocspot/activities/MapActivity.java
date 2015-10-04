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


public class MapActivity extends ActionBarActivity implements GoogleMap.OnMarkerClickListener, View.OnClickListener {
    private GoogleMap mMap;
    private Toolbar mToolBar;
    private List<Marker> mListPlaceMarkers = new ArrayList<>();
    private Marker mMarkerUserPosition;
    private int mNavIndex = 0;
    private AlertDialog.Builder mDialogMarker;
    private AlertDialog mDialogDestroyer;
    private PointItem mClickedPointItem;
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
                    mListPlaceMarkers.get(mNavIndex).showInfoWindow();
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
                    displayAllPointsInView(mListPlaceMarkers);
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
                mListPlaceMarkers.add(mMap.addMarker(new MarkerOptions()
                        .title(result.get(i).getLocation())
                        .position(
                                new LatLng(result.get(i).getLat(), result
                                        .get(i).getLon()))
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.pin))
                        .snippet(result.get(i).getVicinity())));
                setNonVisitedMarkers(BlocSpotApplication.getSharedDataSource().getCategoryColor(result.get(i).getCategory()), i);
            }else if(result.get(i) != null && result.get(i).isVisited()){
                mListPlaceMarkers.add(mMap.addMarker(new MarkerOptions()
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
        mMarkerUserPosition = mMap.addMarker(new MarkerOptions()
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
            mMarkerUserPosition.showInfoWindow();
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(mMarkerUserPosition.getPosition())
                    .zoom(18)
                    .tilt(90)
                    .build();
            mMap.moveCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.equals(mMarkerUserPosition)){
            return false;
        }
        mClickedMarkerItemPosition = mListPlaceMarkers.indexOf(marker);
        mClickedPointItem = BlocSpotApplication.getSharedDataSource().getPoints().get(mClickedMarkerItemPosition);

        mDialogMarker = new AlertDialog.Builder(MapActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.point_item_map_alert, null);
        mDialogMarker.setView(dialogView);

        TextView location = (TextView) dialogView.findViewById(R.id.map_dialog_location);
        TextView note = (TextView) dialogView.findViewById(R.id.map_dialog_note);
        CheckBox visited = (CheckBox) dialogView.findViewById(R.id.map_dialog_checkbox);
        Button category = (Button) dialogView.findViewById(R.id.map_dialog_category_button);
        ImageButton navigate = (ImageButton) dialogView.findViewById(R.id.map_dialog_navigate);
        ImageButton share = (ImageButton) dialogView.findViewById(R.id.map_dialog_share);
        ImageButton delete = (ImageButton) dialogView.findViewById(R.id.map_dialog_delete);

        category.setText(mClickedPointItem.getCategory());
        String color = BlocSpotApplication.getSharedDataSource().getCategoryColor(mClickedPointItem.getCategory());

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

        location.setText(mClickedPointItem.getLocation());
        note.setText(mClickedPointItem.getNote());

        if(mClickedPointItem.isVisited()){
            visited.setChecked(true);
        }else{
            visited.setChecked(false);
        }

        visited.setOnClickListener(this);
        navigate.setOnClickListener(this);
        share.setOnClickListener(this);
        delete.setOnClickListener(this);

        mDialogDestroyer = mDialogMarker.show();

        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.map_dialog_checkbox:
                if(mClickedPointItem.isVisited()){
                    mClickedPointItem.setVisited(false);
                    setNonVisitedMarkers(BlocSpotApplication.getSharedDataSource().getCategoryColor(mClickedPointItem.getCategory()), mClickedMarkerItemPosition);
                }else{
                    mClickedPointItem.setVisited(true);
                    setVisitedMarkers(BlocSpotApplication.getSharedDataSource().getCategoryColor(mClickedPointItem.getCategory()), mClickedMarkerItemPosition);
                }
                break;
            case R.id.map_dialog_category_button:

                break;
            case R.id.map_dialog_navigate:
                mDialogDestroyer.dismiss();
                mListPlaceMarkers.get(BlocSpotApplication.getSharedDataSource().getPoints().indexOf(mClickedPointItem)).showInfoWindow();
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(BlocSpotApplication.getSharedDataSource().getPoints().get(BlocSpotApplication.getSharedDataSource().getPoints().indexOf(mClickedPointItem)).getLat(),
                                BlocSpotApplication.getSharedDataSource().getPoints().get(BlocSpotApplication.getSharedDataSource().getPoints().indexOf(mClickedPointItem)).getLon()))
                        .zoom(18)
                        .tilt(90)
                        .build();
                mMap.moveCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
                break;
            case R.id.map_dialog_share:
                mDialogDestroyer.dismiss();
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        String.format("Check this place out: %s, %s ", mClickedPointItem.getLocation(), mClickedPointItem.getVicinity()));
                shareIntent.setType("text/plain");
                Intent chooser = Intent.createChooser(shareIntent, getString(R.string.share_chooser_title));
                startActivity(chooser);
                break;
            case R.id.map_dialog_delete:
                mDialogDestroyer.dismiss();
                mListPlaceMarkers.remove(mClickedMarkerItemPosition).remove();
                BlocSpotApplication.getSharedDataSource().getPoints().remove(mClickedPointItem);
                break;
        }
    }

    public void setVisitedMarkers(String color, int index){
        if(color.equals("Red")){
            mListPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.red_visited_pin));
        }else if(color.equals("Green")){
            mListPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.green_visited_pin));
        }else if(color.equals("Blue")){
            mListPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.blue_visited_pin));
        }else if(color.equals("Yellow")){
            mListPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.yellow_visited_pin));
        }else if(color.equals("Aqua")){
            mListPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.aqua_visited_pin));
        }else if(color.equals("Magenta")){
            mListPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.magenta_visited_pin));
        }else{
            mListPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.visited_pin));
        }
    }

    public void setNonVisitedMarkers(String color, int index){
        if(color.equals("Red")){
            mListPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.red_pin));
        }else if(color.equals("Green")){
            mListPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.green_pin));
        }else if(color.equals("Blue")){
            mListPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.blue_pin));
        }else if(color.equals("Yellow")){
            mListPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.yellow_pin));
        }else if(color.equals("Aqua")){
            mListPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.aqua_pin));
        }else if(color.equals("Magenta")){
            mListPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.magenta_pin));
        }else{
            mListPlaceMarkers.get(index).setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.pin));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_actionbar_menu, menu);
        return true;
    }
}
