package com.bloc.blocspot.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bloc.blocspot.BlocSpotApplication;
import com.bloc.blocspot.adapters.ItemAdapter;
import com.bloc.blocspot.api.DataSource;
import com.bloc.blocspot.api.model.PointItem;
import com.bloc.blocspot.blocspot.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;

/**
 * Created by Mark on 2/6/2015.
 */
public class MainActivity extends ActionBarActivity implements ItemAdapter.Delegate,
        PopupMenu.OnMenuItemClickListener,
        ItemAdapter.DataSource,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private Menu mToolbarMenu;
    private ItemAdapter mItemAdapter;
    private ProgressDialog mPdDialog;
    private PointItem mClickedItem;
    private int mClickedItemPosition;
    private RecyclerView mRecyclerView;
    private Toolbar mToolbar;
    private ArrayAdapter<String> mCategoryAdapter;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCategoryAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);

        mToolbar = (Toolbar) findViewById(R.id.tb_activity_main);
        setSupportActionBar(mToolbar);

        mItemAdapter = new ItemAdapter();
        mItemAdapter.setDelegate(this);
        mItemAdapter.setDataSource(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_activity_main);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mItemAdapter);

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1000);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mItemAdapter != null){
            mItemAdapter.notifyDataSetChanged();
        }
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(location == null){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }else{
            BlocSpotApplication.getSharedDataSource().setLocation(location);
            if(BlocSpotApplication.getSharedDataSource().getPoints().isEmpty()){
                BlocSpotApplication.getSharedDataSource().fetchPointItemPlaces(new DataSource.Callback<List<PointItem>>() {
                    @Override
                    public void onSuccess(List<PointItem> pointItems) {
                        if (!pointItems.isEmpty()) {
                            mItemAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                    }
                });
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Do nothing
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_actionbar_menu, menu);
        this.mToolbarMenu = menu;

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.main_action_map:
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
                break;
            case R.id.main_action_filter:
                AlertDialog.Builder categBuilder = new AlertDialog.Builder(this);
                mCategoryAdapter.clear();
                mCategoryAdapter.addAll(BlocSpotApplication.getSharedDataSource().getCategoryNames());
                categBuilder.setTitle(getString(R.string.filter_by_category));
                categBuilder.setSingleChoiceItems(mCategoryAdapter, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        BlocSpotApplication.getSharedDataSource().filterPointsByCategory(mCategoryAdapter.getItem(which));
                        mItemAdapter.notifyDataSetChanged();
                    }
                });
                categBuilder.show();
            case R.id.main_action_search:
                BlocSpotApplication.getSharedDataSource().fetchUpdatedPlaces(new DataSource.Callback<List<PointItem>>() {
                    @Override
                    public void onSuccess(List<PointItem> pointItems) {
                        if (!pointItems.isEmpty()) {
                            mItemAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {}
                });
                break;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClicked(ItemAdapter itemAdapter, CheckBox visitedBox, PointItem item){
        if(visitedBox.isChecked()){
            visitedBox.setChecked(false);
            item.setVisited(false);
        }else{
            Toast.makeText(this, getString(R.string.marked_as_visited), Toast.LENGTH_SHORT).show();
            visitedBox.setChecked(true);
            item.setVisited(true);
        }
    }

    @Override
    public void onPopupMenuClicked(ItemAdapter itemAdapter, View view, PointItem item){
        mClickedItemPosition = BlocSpotApplication.getSharedDataSource().getPoints().indexOf(item);
        mClickedItem = item;

        PopupMenu popMenu = new PopupMenu(this, view);
        getMenuInflater().inflate(R.menu.popup_menu, popMenu.getMenu());
        popMenu.setOnMenuItemClickListener(this);
        popMenu.show();
    }

    @Override
    public boolean onMenuItemClick (MenuItem item){
        switch (item.getItemId()) {
            case R.id.popup_navigate :
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                intent.putExtra(getString(R.string.navigate), mClickedItemPosition);
                startActivity(intent);
                break;
            case R.id.popup_choose_category :
                AlertDialog.Builder categBuilder = new AlertDialog.Builder(this);

                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.category_dialog, null);

                ImageButton add = (ImageButton) dialogView.findViewById(R.id.category_dialog_add_button);
                ImageButton minus = (ImageButton) dialogView.findViewById(R.id.category_dialog_minus_button);
                add.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       if (BlocSpotApplication.getSharedDataSource().getCategories().size() == DataSource.MAX_CATEGORIES) {
                           Toast.makeText(BlocSpotApplication.getSharedInstance(), getString(R.string.max_num_categories_reached), Toast.LENGTH_SHORT).show();
                       } else {
                           AlertDialog.Builder newCateg = new AlertDialog.Builder(MainActivity.this);
                           newCateg.setTitle(getString(R.string.add_new_category));

                           final EditText input = new EditText(MainActivity.this);
                           input.setInputType(InputType.TYPE_CLASS_TEXT);
                           newCateg.setView(input);

                           newCateg.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(final DialogInterface dialog, int which) {
                                   if(BlocSpotApplication.getSharedDataSource().searchForCategory(input.getText().toString())) {
                                       Toast.makeText(getApplicationContext(), getString(R.string.category_already_exists), Toast.LENGTH_LONG).show();
                                   }else if(input.getText().toString().isEmpty()){
                                       Toast.makeText(getApplicationContext(), getString(R.string.enter_category_name), Toast.LENGTH_LONG).show();
                                   }else{
                                       for(int i = 0; i<BlocSpotApplication.getSharedDataSource().getCategoryColors().size(); i++){
                                           if(BlocSpotApplication.getSharedDataSource().searchForColor(BlocSpotApplication.getSharedDataSource().getCategoryColors().get(i))){
                                               continue;
                                           }
                                           else{
                                               BlocSpotApplication.getSharedDataSource().insertCategory(input.getText().toString(), BlocSpotApplication.getSharedDataSource().getCategoryColors().get(i));
                                               mCategoryAdapter.add(input.getText().toString());
                                               mCategoryAdapter.notifyDataSetChanged();
                                               break;
                                           }
                                       }
                                       dialog.dismiss();
                                   }
                               }
                           });
                           newCateg.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialog, int which) {
                                   dialog.cancel();
                               }
                           });
                           newCateg.show();
                       }
                   }
               });

                minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mClickedItem.getCategory().equals(getString(R.string.all))){
                            Toast.makeText(getApplicationContext(), getString(R.string.cannot_remove_category), Toast.LENGTH_LONG).show();
                        }else{
                            int ind = mCategoryAdapter.getPosition(mClickedItem.getCategory());
                            mCategoryAdapter.remove(mClickedItem.getCategory());
                            BlocSpotApplication.getSharedDataSource().removeCategory(mClickedItem.getCategory());
                            if(mCategoryAdapter.getCount() > ind){
                                mClickedItem.setCategory(mCategoryAdapter.getItem(ind));
                            }else{
                                mClickedItem.setCategory(getString(R.string.all));
                            }
                            mItemAdapter.notifyDataSetChanged();
                            mCategoryAdapter.notifyDataSetChanged();
                        }
                    }
                });

                mCategoryAdapter.clear();
                mCategoryAdapter.addAll(BlocSpotApplication.getSharedDataSource().getCategoryNames());
                categBuilder.setView(dialogView);
                categBuilder.setSingleChoiceItems(mCategoryAdapter, mCategoryAdapter.getPosition(mClickedItem.getCategory()), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mClickedItem.setCategory(mCategoryAdapter.getItem(which));
                        mItemAdapter.notifyDataSetChanged();
                    }
                });
                categBuilder.show();
                break;

            case R.id.popup_edit_note :
                AlertDialog.Builder noteBuilder = new AlertDialog.Builder(this);
                noteBuilder.setTitle(getString(R.string.note_for) + mClickedItem.getLocation());

                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(mClickedItem.getNote());
                noteBuilder.setView(input);

                noteBuilder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mClickedItem.setNote(input.getText().toString());
                        mItemAdapter.notifyDataSetChanged();
                    }
                });
                noteBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                noteBuilder.show();
                break;

            case R.id.popup_save:
                BlocSpotApplication.getSharedDataSource().insertPoint(mClickedItem);
                Toast.makeText(this, mClickedItem.getLocation() + getString(R.string.has_been_saved), Toast.LENGTH_SHORT).show();
                break;

            case R.id.popup_delete :
                BlocSpotApplication.getSharedDataSource().getPoints().remove(mClickedItemPosition);
                mItemAdapter.notifyItemRemoved(mClickedItemPosition);
                break;
        }
        return false;
    }
    @TargetApi(21)
    @SuppressWarnings("deprecation")
    public void AlertDialog(){
        AlertDialog.Builder categBuilder = new AlertDialog.Builder(this, 2);
        categBuilder.setView(R.layout.category_dialog);
        categBuilder.show();
    }

    @Override
    public PointItem getPointItem(ItemAdapter itemAdapter, int position) {
        if(BlocSpotApplication.getSharedDataSource().getPoints().size() != 0){
            if(mPdDialog != null && mPdDialog.isShowing()){
                mPdDialog.dismiss();
            }
            return BlocSpotApplication.getSharedDataSource().getPoints().get(position);
        }else{
            mPdDialog = new ProgressDialog(this);
            mPdDialog.setCancelable(false);
            mPdDialog.setMessage(getString(R.string.loading));
            mPdDialog.isIndeterminate();
            mPdDialog.show();
        }
        return null;
    }

    @Override
    public int getItemCount(ItemAdapter itemAdapter) {
        if(BlocSpotApplication.getSharedDataSource().getPoints().size() != 0){
            return BlocSpotApplication.getSharedDataSource().getPoints().size();
        }
        return 0;
    }

    @Override
    public void onLocationChanged(Location location) {
        BlocSpotApplication.getSharedDataSource().setLocation(location);
    }
}
