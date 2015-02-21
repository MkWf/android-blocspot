package com.bloc.blocspot.api.model;

/**
 * Created by Mark on 2/20/2015.
 */
public class Category {

    private String name;
    private String color;


    public Category(String name, String color){
        setName(name);
        setColor(color);
    }

    public String getName(){
        return name;
    }

    public String getColor(){
        return color;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setColor(String color){
        this.color = color;
    }
}