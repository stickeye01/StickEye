package com.example.jarim.myapplication.AndroidSide;

/**
 * Created by lhc on 2018-03-08.
 *  Manage landmark information
 */
public class LandMark {
    private double lat, lng;
    private String address;
    private String name; // short name to explain the place easily.

    public LandMark(double _lat, double _lng, String _address, String _name) {
        lat = _lat;
        lng = _lng;
        address = _address;
        name = _name;
    }

    public double getLat() {return lat;}
    public void setLat(double _lat) {lat = _lat;}
    public double getLng() {return lng;}
    public void setLng(double _lng) {lng = _lng;}
    public String getAddress() {return address;}
    public void setAddress(String _address) {address = _address;}
    public String getName() {return name;}
    public void setName(String _name) {name = _name;}
}
