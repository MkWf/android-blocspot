package com.bloc.blocspot.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import com.bloc.blocspot.blocspot.R;

/**
 * Created by Mark on 2/6/2015.
 */
public class MainActivity extends Activity {

    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }
}
