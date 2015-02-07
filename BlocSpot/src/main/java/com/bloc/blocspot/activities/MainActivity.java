package com.bloc.blocspot.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bloc.blocspot.blocspot.R;

/**
 * Created by Mark on 2/6/2015.
 */
public class MainActivity extends Activity {

    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.point_item);

        TextView location = (TextView) findViewById(R.id.point_item_location);
        TextView note = (TextView) findViewById(R.id.point_item_note);
        TextView distance = (TextView) findViewById(R.id.point_item_distance);
        CheckBox checkbox = (CheckBox) findViewById(R.id.point_item_checkbox);

        location.setText("Grill Baby, Grill");
        note.setText("Looks like fun, Karen said she had a blast at this place");
        distance.setText("<1 mi");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
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
}
