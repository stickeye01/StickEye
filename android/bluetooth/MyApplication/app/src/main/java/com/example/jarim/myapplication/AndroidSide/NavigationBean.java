package com.example.jarim.myapplication.AndroidSide;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
    private ArrayList<String> subMenus;
    private ArrayList<LandMark> landmarkList;

    private int horizontal_index = 0;
    private int register_landmark = 0;

    private final int CURRENT_LOCATION = 0;
    private final int REGISTER_LOCATION = 1;
    private final int CLOSE_LANDMARK = 2;
    private final int REMOVE_LOCATION = 3;
    private final int GO_TO_MAINMENU = 4;

    private LandMarkDBHandler landMarkDBHandler;

    public NavigationBean(String _name, String _intentName, TtsService _tts, Context _ctx,
                          BrailleKeyboard _bKey) {
        super(_name, _intentName, _tts, _ctx, _bKey);
    }


    @Override
    public boolean start(Object o) {
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
            return false;
        }
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                0, 0, this);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0, 0, this);

        landMarkDBHandler = new LandMarkDBHandler(mContext);

        subMenus = new ArrayList<String>();
        subMenus.add("현재 위치");
        subMenus.add("현재 위치 등록");
        subMenus.add("가까운 랜드마크");
        subMenus.add("가까운 랜드마크 삭제");
        subMenus.add("메인 메뉴로 돌아가기");

        tts.sspeak("네비게이션 기능입니다.");
        Constants.MENU_LEVEL = Constants.SUB_MENU_MODE;
        landMarkDBHandler.open();
        selectAllLandMarkLists(); // Fetch all landmarks data
        printLandMarkLists();
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        String address = getAddress(lat, lng);
        String name = address.split(" ")[0];
        Log.e("LHC", "Address" + address);
        tts.sspeak("현재 위치는 "+ getAddress(lat, lng)+ " 입니다.");
        if (register_landmark == 1) {// 이 경우에는 랜드마크 등록 기능을 선택한 후에 주소를
            // 검색하였을 경우이다. DB와 list를 갱신 후에 register_landmark를 원래 값으로 돌린다.
            // 따라서 이 이후에는 DB에 추가되지 않는다.
            LandMark lm = new LandMark(lat, lng, address, name);
            register_landmark = 0;  // turn off landmark add option
            landMarkDBHandler.insert(lm); // add data into DB
            selectAllLandMarkLists(); // update landmark lists
            printLandMarkLists();
        }
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

    private void selectAllLandMarkLists() {
        landmarkList = landMarkDBHandler.select();
    }

    private void printLandMarkLists() {
        int i = 0;
        for (LandMark land: landmarkList)
            Log.e("LHC", "landmarks:"+landmarkList.get(i++).getAddress()+"\n");
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
        if (horizontal_index == CURRENT_LOCATION) {
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
        } else if (horizontal_index == REGISTER_LOCATION) {
            String locationProvider = LocationManager.NETWORK_PROVIDER;
            tts.sspeak("현재 위치를 랜드마크로 등록합니다.");
            register_landmark = 1; // 이것을 1로 등록하면서 event에 landmark 갱신 후 추가.
            lm.removeUpdates(this);
            lm.requestLocationUpdates(locationProvider, 0, 0, this);
        } else if (horizontal_index == REMOVE_LOCATION) {
            tts.sspeak("가까운 랜드마크를 제거합니다.");
            landMarkDBHandler.deleteAll();
            selectAllLandMarkLists();
            printLandMarkLists();
        } else if (horizontal_index == CLOSE_LANDMARK) {
            tts.sspeak("가까운 랜드마크까지의 방향과 거리를 안내합니다.");
            if(landmarkList.size() > 0)
                tts.ispeak(landmarkList.get(0).getAddress());
        } else if (horizontal_index == GO_TO_MAINMENU) {
            tts.sspeak("메인 메뉴로 돌아갑니다.");
            landMarkDBHandler.close();
            Constants.MENU_LEVEL = Constants.MAIN_MENU_MODE;
        }
    }


    @Override
    public void left() {
        horizontal_index --;
        if (horizontal_index < 0) horizontal_index = subMenus.size() - 1;
        tts.ispeak(subMenus.get(horizontal_index));
    }

    @Override
    public void right() {
        horizontal_index ++;
        if (horizontal_index >= subMenus.size()) horizontal_index = 0;
        tts.ispeak(subMenus.get(horizontal_index));
    }
}
