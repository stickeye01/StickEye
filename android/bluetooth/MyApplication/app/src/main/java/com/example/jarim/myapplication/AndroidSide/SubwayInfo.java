package com.example.jarim.myapplication.AndroidSide;

/**
 * Created by lhc on 2018-04-29.
 */

public class SubwayInfo {
    private String name;
    private String phoneNum;
    private double lat;
    private double lng;

    public SubwayInfo(String name, String phoneNum, String lat, String lng) {
        this.name = name;
        this.phoneNum = phoneNum;
        this.lat = Double.parseDouble(lat);
        this.lng = Double.parseDouble(lng);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
