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
    private String vicinity;
    private int distanceValue;
    private double lat;
    private double lon;

    public PointItem(){
        this.distance = "";
        this.note = "Add a note";
        this.location = "";
        this.category = "All";
        this.visited = false;
        this.distanceValue = 0;
        this.lat = 0;
        this.lon = 0;
        this.vicinity = "";
    }

    public PointItem(String location, String note, double lat, double lon, String vicinity, boolean visited, String category){
        this.location = location;
        this.note = note;
        this.lat = lat;
        this.lon = lon;
        this.vicinity = vicinity;
        this.visited = visited;
        this.category = category;
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

    public double getLon() { return lon; }

    public double getLat() { return lat; }

    public String getCategory() {return category;}

    public int getDistanceValue() { return distanceValue; }

    public String getVicinity() { return vicinity; }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDistance(String distance) {this.distance = distance;}

    public void setNote(String note) {
        this.note = note;
    }

    public void setCategory(String category) { this.category = category;}

    public void setDistanceValue(int distanceValue) { this.distanceValue = distanceValue;}

    public void setVisited(boolean visited) {this.visited = visited;}

    public void setLon(double lon) { this.lon = lon; }

    public void setLat(double lat) { this.lat = lat; }

    public void setVicinity(String vicinity) { this.vicinity = vicinity; }

    public boolean isVisited() { return visited; }

    @Override
    public int compare(PointItem lhs, PointItem rhs) {
        return lhs.getDistanceValue() - rhs.getDistanceValue();
    }
}
