package com.example.jarim.myapplication.AndroidSide;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.jarim.myapplication.BrailleKeyboard.BrailleKeyboard;
import com.example.jarim.myapplication.Constants;
import com.example.jarim.myapplication.R;
import com.example.jarim.myapplication.TtsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Locale.*;

/**
 * Created by lhc on 2018-03-07.
 */

public class NavigationBean extends AppBean implements View.OnClickListener, LocationListener {
    private LocationManager lm = null;
    private String provider = null;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;

    public NavigationBean(String _name, String _intentName, TtsService _tts, Context _ctx,
                          BrailleKeyboard _bKey) {
        super(_name, _intentName, _tts, _ctx, _bKey);

        lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        // Check whether GPS provider is enabled or not.
        isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // Check whether a network provider is enabled or not.
        isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.e("LHC", "isGPSEnabled=" + isGPSEnabled);
        Log.e("LHC", "isNetworkEnabled=" + isNetworkEnabled);

        if (ActivityCompat.
                checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                0, 0, this);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0, 0, this);
    }


    @Override
    public boolean start(Object o) {
        tts.sspeak("네비게이션 기능입니다.");
        Constants.MENU_LEVEL = Constants.SUB_MENU_MODE;
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        Log.e("LHC", "Address" + getAddress(lat, lng));
        tts.sspeak("현재 위치는 "+ getAddress(lat, lng)+ " 입니다.");
        lm.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onClick(View view) {

    }


    /** 위도와 경도 기반으로 주소를 리턴하는 메서드*/
    public String getAddress(double lat, double lng) {
        String address = null;

        //위치정보를 활용하기 위한 구글 API 객체
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

        //주소 목록을 담기 위한 HashMap
        List<Address> list = null;

        try {
            list = geocoder.getFromLocation(lat, lng, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (list == null) {
            Log.e("getAddress", "주소 데이터 얻기 실패");
            return null;
        }

        if (list.size() > 0) {
            Address addr = list.get(0);
            address = addr.getCountryName() + " "
                    + addr.getPostalCode() + " "
                    + addr.getLocality() + " "
                    + addr.getThoroughfare() + " "
                    + addr.getFeatureName();
        }

        return address;
    }

    @Override
    public void click() {
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        Log.e("LHC", "clicked!");
        if (ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.
                    checkSelfPermission(mContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)

            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.removeUpdates(this);
        lm.requestLocationUpdates(locationProvider, 0, 0, this);
    }
}