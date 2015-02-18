package com.bloc.blocspot.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
public class MainActivity extends ActionBarActivity implements ItemAdapter.Delegate,
        PopupMenu.OnMenuItemClickListener,
        ItemAdapter.DataSource {

    private Menu actionbarMenu;
    private ItemAdapter itemAdapter;
    private List<PointItem> items = new ArrayList<PointItem>();
    private ProgressDialog dialog;
    private PointItem clickedItem;
    int clickedItemPosition;
    private View noteView;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private ArrayList<Integer> deletions = new ArrayList<>();
    private String [] categories = {"restaurants", "bars", "stores"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BlocSpotApplication.getSharedDataSource().fetchPointItemPlaces(new DataSource.Callback<List<PointItem>>() {
            @Override
            public void onSuccess(List<PointItem> pointItems) {
                if (!pointItems.isEmpty()) {
                    //items.addAll(0, pointItems);
                    itemAdapter.notifyItemRangeInserted(0, pointItems.size());
                }
            }

            @Override
            public void onError(String errorMessage) {
            }
        });

        toolbar = (Toolbar) findViewById(R.id.tb_activity_main);
        setSupportActionBar(toolbar);

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
        getMenuInflater().inflate(R.menu.main_actionbar_menu, menu);
        this.actionbarMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.main_action_map) {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            intent.putIntegerArrayListExtra("data", deletions);
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
        clickedItemPosition = BlocSpotApplication.getSharedDataSource().getPoints().indexOf(item);
        clickedItem = item;
        //View point = recyclerView.getLayoutManager().findViewByPosition(clickedItemPosition);
        //noteView = point;

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
                intent.putExtra("navigate", clickedItemPosition);
                startActivity(intent);
                break;
            case R.id.popup_choose_category :
                /*AlertDialog.Builder categBuilder = new AlertDialog.Builder(this);
                                    //#1
                                LayoutInflater inflater = getLayoutInflater();
                                categBuilder.setView(inflater.inflate(R.layout.category_dialog, null));
                                    //#2
                                categBuilder.setView(R.layout.category_dialog);
                                    //both
                                categBuilder.setSingleChoiceItems(categories, -1, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 1:
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                });
                categBuilder.show(); */


                 /*AlertDialog.Builder categBuilder = new AlertDialog.Builder(this, 2);
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
                                adapter.add("restaurants");
                                adapter.add("bars");
                                adapter.add("stores");
                                categBuilder.setTitle("Choose Category");
                                categBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int item) {

                                    }
                                });
                categBuilder.show();*/
                break;

            case R.id.popup_edit_note :
                AlertDialog.Builder noteBuilder = new AlertDialog.Builder(this);
                noteBuilder.setTitle("Note for " + clickedItem.getLocation());

                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(clickedItem.getNote());
                noteBuilder.setView(input);

                noteBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clickedItem.setNote(input.getText().toString());
                        itemAdapter.notifyDataSetChanged();
                        //noteView.
                    }
                });
                noteBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                noteBuilder.show();
                break;

            case R.id.popup_delete :
                BlocSpotApplication.getSharedDataSource().getPoints().remove(clickedItemPosition);
               // deletions.add(clickedItemPosition);
                itemAdapter.notifyItemRemoved(clickedItemPosition);
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
            if(dialog != null && dialog.isShowing()){
                dialog.dismiss();
            }
            return BlocSpotApplication.getSharedDataSource().getPoints().get(position);
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
        if(BlocSpotApplication.getSharedDataSource().getPoints() == null){
            return 0;
        }
        if(BlocSpotApplication.getSharedDataSource().getPoints().size() != 0){
            return BlocSpotApplication.getSharedDataSource().getPoints().size();
        }
        return 0;
    }
}
