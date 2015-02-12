package com.bloc.blocspot.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupMenu;

import com.bloc.blocspot.BlocSpotApplication;
import com.bloc.blocspot.adapters.ItemAdapter;
import com.bloc.blocspot.api.DataSource;
import com.bloc.blocspot.api.model.PointItem;
import com.bloc.blocspot.blocspot.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mark on 2/6/2015.
 */
public class MainActivity extends Activity implements ItemAdapter.Delegate,
        PopupMenu.OnMenuItemClickListener,
        ItemAdapter.DataSource {

    private Menu actionbarMenu;
    private ItemAdapter itemAdapter;
    private String API_KEY = "AIzaSyAhYD6RyZbvacqp8ZOpG4bOUozZDN-5zP0";
    private LocationManager locationManager;
    private Location loc;
    private List<PointItem> items = new ArrayList<PointItem>();
    private ProgressDialog dialog;
    private PointItem editNote;
    private View noteView;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BlocSpotApplication.getSharedDataSource().getPointItemPlaces(new DataSource.Callback<List<PointItem>>() {
            @Override
            public void onSuccess(List<PointItem> pointItems) {
                if (!pointItems.isEmpty()) {
                    items.addAll(0, pointItems);
                    itemAdapter.notifyItemRangeInserted(0, pointItems.size());
                }
            }

            @Override
            public void onError(String errorMessage) {
            }
        });


        itemAdapter = new ItemAdapter();
        itemAdapter.setDelegate(this);
        itemAdapter.setDataSource(this);

        recyclerView = (RecyclerView) findViewById(R.id.rv_activity_main);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(itemAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu, menu);
        this.actionbarMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_map) {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClicked(ItemAdapter itemAdapter, CheckBox visitedBox, PointItem item){
        if(visitedBox.isChecked()){
            visitedBox.setChecked(false);
            item.setVisited(false);
        }else{
            visitedBox.setChecked(true);
            item.setVisited(true);
        }
    }

    @Override
    public void onPopupMenuClicked(ItemAdapter itemAdapter, View view, PointItem item){
        int pos = items.indexOf(item);
        View point = recyclerView.getLayoutManager().findViewByPosition(pos);

        editNote = item;
        noteView = point;

        PopupMenu popMenu = new PopupMenu(this, view);
        getMenuInflater().inflate(R.menu.popup_menu, popMenu.getMenu());
        popMenu.setOnMenuItemClickListener(this);
        popMenu.show();
    }

    @Override
    public boolean onMenuItemClick (MenuItem item){
        switch (item.getItemId()) {
            case R.id.popup_navigate :
                break;
            case R.id.popup_choose_category :
                break;

            case R.id.popup_edit_note :
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Note for " + editNote.getLocation());

                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editNote.setNote(input.getText().toString());
                        itemAdapter.notifyDataSetChanged();
                        //noteView.
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                break;

            case R.id.popup_delete :
                break;
        }
        return false;
    }

    @Override
    public PointItem getPointItem(ItemAdapter itemAdapter, int position) {
        if(items.size() != 0){
            if(dialog != null && dialog.isShowing()){
                dialog.dismiss();
            }
            return items.get(position);
        }else{
            dialog = new ProgressDialog(this);
            dialog.setCancelable(false);
            dialog.setMessage("Loading..");
            dialog.isIndeterminate();
            dialog.show();
        }
        return null;
    }

    @Override
    public int getItemCount(ItemAdapter itemAdapter) {
        if(items == null){
            return 0;
        }
        if(items.size() != 0){
            return items.size();
        }
        return 0;
    }
}
