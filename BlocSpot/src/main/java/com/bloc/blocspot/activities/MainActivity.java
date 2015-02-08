package com.bloc.blocspot.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.PopupMenu;

import com.bloc.blocspot.adapters.ItemAdapter;
import com.bloc.blocspot.api.model.PointItem;
import com.bloc.blocspot.blocspot.R;

/**
 * Created by Mark on 2/6/2015.
 */
public class MainActivity extends Activity implements ItemAdapter.Delegate, PopupMenu.OnMenuItemClickListener{

    private Menu actionbarMenu;
    private ItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemAdapter = new ItemAdapter();
        itemAdapter.setDelegate(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_activity_main);
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
    public void onPopupMenuClicked(ItemAdapter itemAdapter, View view){
        PopupMenu popMenu = new PopupMenu(this, view);
        getMenuInflater().inflate(R.menu.popup_menu, popMenu.getMenu());
        popMenu.setOnMenuItemClickListener(this);
        popMenu.show();
    }

    public boolean onMenuItemClick (MenuItem item){
        if(item.getItemId() == R.id.popup_navigate){

            return true;
        }else if(item.getItemId() == R.id.popup_choose_category){

            return true;
        }else if(item.getItemId() == R.id.popup_edit_note){

            return true;
        }else if(item.getItemId() == R.id.popup_delete){

            return true;
        }

        return false;
    }



}
