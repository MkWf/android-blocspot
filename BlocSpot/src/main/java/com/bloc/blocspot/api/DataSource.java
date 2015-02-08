package com.bloc.blocspot.api;

import com.bloc.blocspot.api.model.PointItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mark on 2/7/2015.
 */
public class DataSource {

    private List<PointItem> items;

    public DataSource() {
        items = new ArrayList<PointItem>();
        createFakeData();
    }

    public List<PointItem> getItems() {
        return items;
    }

    void createFakeData() {
        for (int i = 0; i < 10; i++) {
            items.add(new PointItem("<1 mi", "I heard good reviews about this place", "Grill Baby, Grill"));
            //items.get(i).setCategory("red");
        }
    }
}
