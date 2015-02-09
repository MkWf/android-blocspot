package com.bloc.blocspot.api.model;

/**
 * Created by Mark on 2/7/2015.
 */
public class PointItem {
    private String location;
    private String note;
    private String distance;
    private String category;
    private boolean visited;

    public PointItem(String distance, String note, String title) {
        this.distance = distance;
        this.note = note;
        this.location = title;
        this.category = "";
        this.visited = false;
    }

    public PointItem(){

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

    public void setLocation(String distance) {
        this.distance = distance;
    }

    public void setTitle(String title) {
        this.location = title;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setCategory(String category) { this.category = category;}

    public boolean isVisited() { return visited; }

    public void setVisited(boolean visited) {this.visited = visited;}

}
