package com.bloc.blocspot.api.model;

/**
 * Created by Mark on 2/7/2015.
 */
public class PointItem {
    private String location;
    private String note;
    private String distance;

    public PointItem(String distance, String note, String title) {
        this.distance = distance;
        this.note = note;
        this.location = title;
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

    public void setLocation(String distance) {
        this.distance = distance;
    }

    public void setTitle(String title) {
        this.location = title;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
