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
    ArrayAdapter<String> adapter;

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

        adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);

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
        if(item.getItemId() == R.id.main_action_filter){
            AlertDialog.Builder categBuilder = new AlertDialog.Builder(this);
            adapter.clear();
            adapter.addAll(BlocSpotApplication.getSharedDataSource().getCategoryNames());
            categBuilder.setTitle("Filter By Category");
            categBuilder.setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    BlocSpotApplication.getSharedDataSource().filterPointsByCategory(adapter.getItem(which));
                    itemAdapter.notifyDataSetChanged();
                }
            });
            categBuilder.show();
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
                AlertDialog.Builder categBuilder = new AlertDialog.Builder(this);

                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.category_dialog, null);

                ImageButton add = (ImageButton) dialogView.findViewById(R.id.category_dialog_add_button);
                ImageButton minus = (ImageButton) dialogView.findViewById(R.id.category_dialog_minus_button);
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder newCateg = new AlertDialog.Builder(MainActivity.this);
                        newCateg.setTitle("Add a new category");

                        final EditText input = new EditText(MainActivity.this);
                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                        newCateg.setView(input);

                        newCateg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            if(adapter.getCount() == 7){
                                Toast.makeText(getApplicationContext(), "Maximum number of categories reached. Remove a category first", Toast.LENGTH_LONG).show();
                            }
                            if(input.getText().toString().isEmpty()){
                                Toast.makeText(getApplicationContext(), "Category must have a name", Toast.LENGTH_LONG).show();
                            }else{
                                if(BlocSpotApplication.getSharedDataSource().searchForCategory(input.getText().toString())){
                                    Toast.makeText(getApplicationContext(), "Category already exists", Toast.LENGTH_LONG).show();
                                }else{
                                    for(int i = 0; i<BlocSpotApplication.getSharedDataSource().getCategoryColors().size(); i++){
                                        if(BlocSpotApplication.getSharedDataSource().searchForColor(BlocSpotApplication.getSharedDataSource().getCategoryColors().get(i))){
                                            continue;
                                        }
                                        else{
                                            BlocSpotApplication.getSharedDataSource().insertCategory(input.getText().toString(), BlocSpotApplication.getSharedDataSource().getCategoryColors().get(i));
                                            //clickedItem.setCategory(input.getText().toString());
                                            //itemAdapter.notifyDataSetChanged();
                                            adapter.add(input.getText().toString());
                                            adapter.notifyDataSetChanged();
                                            break;
                                        }
                                    }
                                    dialog.dismiss();
                                }
                            }
                            }
                        });
                        newCateg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        newCateg.show();
                    }
                });
                minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(clickedItem.getCategory().equals("All")){
                            Toast.makeText(getApplicationContext(), "Cannot remove this category", Toast.LENGTH_LONG).show();
                        }else{
                            int ind = adapter.getPosition(clickedItem.getCategory());
                            adapter.remove(clickedItem.getCategory());
                            BlocSpotApplication.getSharedDataSource().removeCategory(clickedItem.getCategory());
                            if(adapter.getCount() > ind){
                                clickedItem.setCategory(adapter.getItem(ind));
                            }else{
                                clickedItem.setCategory("All");
                            }
                           // if(!adapter.getItem(ind).isEmpty()){
                             //   clickedItem.setCategory(adapter.getItem(ind));
                            //}
                            //clickedItem.setCategory("All");
                            itemAdapter.notifyDataSetChanged();
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                adapter.clear();
                adapter.addAll(BlocSpotApplication.getSharedDataSource().getCategoryNames());
                categBuilder.setView(dialogView);
                categBuilder.setSingleChoiceItems(adapter, adapter.getPosition(clickedItem.getCategory()), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        clickedItem.setCategory(adapter.getItem(which));
                        itemAdapter.notifyDataSetChanged();
                    }
                });
                categBuilder.show();

                /*AlertDialog.Builder categBuilder = new AlertDialog.Builder(this, 2);
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.category_dialog, null);

                ImageButton add = (ImageButton) dialogView.findViewById(R.id.category_dialog_add_button);
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setBackgroundColor(R.color.red);
                        Toast.makeText(getApplicationContext(), "Category", Toast.LENGTH_SHORT).show();
                        adapter.add("hotels");
                    }
                });

                categBuilder.setView(dialogView);
                categBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        Toast.makeText(getApplicationContext(), "Category", Toast.LENGTH_SHORT).show();
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

    @Override protected void onResume () {
        super.onResume();
        itemAdapter.notifyDataSetChanged();
    }
}
