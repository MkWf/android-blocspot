package com.bloc.blocspot.api.model;

import java.util.Comparator;

/**
 * Created by Mark on 2/7/2015.
 */
public class PointItem implements Comparator<PointItem> {
    private String location;
    private String note;
    private String distance;
    private String category;
    private boolean visited;
    private int distanceValue;

    public PointItem(){
        this.distance = "";
        this.note = "Add a note";
        this.location = "";
        this.category = "";
        this.visited = false;
        this.distanceValue = 0;
    }

    public String getDistance() {
        return distance;
    }

    public String getNote() {
        return note;
    }

    public String getLocation() {
        return location;
    }

    public String getCategory() {return category;}

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDistance(String distance) {this.distance = distance;}

    public void setNote(String note) {
        this.note = note;
    }

    public void setCategory(String category) { this.category = category;}

    public void setDistanceValue(int distanceValue) { this.distanceValue = distanceValue;}

    public int getDistanceValue() { return distanceValue; }

    public boolean isVisited() { return visited; }

    public void setVisited(boolean visited) {this.visited = visited;}

    @Override
    public int compare(PointItem lhs, PointItem rhs) {
        return lhs.getDistanceValue() - rhs.getDistanceValue();
    }
}
