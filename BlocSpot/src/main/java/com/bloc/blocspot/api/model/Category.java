package com.bloc.blocspot.api.model;

/**
 * Created by Mark on 2/20/2015.
 */
public class Category {

    private String name;
    int [] color = new int [3];


    public Category(String name, int red, int green, int blue){
        setName(name);
        setColor(red, green, blue);
    }

    public String getName(){
        return name;
    }

    public int [] getColor(){
        return color;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setColor(int red, int green, int blue){
        color[0] = red;
        color[1] = green;
        color[2] = blue;
    }
}