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

    private final int CURRENT_LOCATION = 0;
    private final int REGISTER_LOCATION = 1;
    private final int CLOSE_LANDMARK = 2;
    private final int REMOVE_LOCATION = 3;
    private final int GO_TO_MAINMENU = 4;
    private final int NORMAL_MODE = 5;
    private final int LM_REG_MODE = 6;

    private int horizontal_index = 0;
    private int register_landmark = 0;
    private int check_dist_dir_land= 0;
    private int no_degree = NORMAL_MODE;

    private LandMarkDBHandler landMarkDBHandler;
    private LandMark shortestLandMark = null;
    private LandMark currentLandMark = null;

    private EditText input_etext;
    private String lm_name = "";

    public NavigationBean(String _name, String _intentName, TtsService _tts, Context _ctx,
                          BrailleKeyboard _bKey) {
        super(_name, _intentName, _tts, _ctx, _bKey);
        input_etext = mActivity.findViewById(R.id.test_input);
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

    /**
     * find the shortest landmark
     * @param lat
     * @param lng
     * @return
     */
    private LandMark findShortestLandmark(double lat, double lng) {
        if (landmarkList.size() > 0) {
            int shortestIndex = -1;
            double shortestDistance = 999999999f;
            double tmp = 0.0f;
            int idx = 0;
            // Shortest
            for (LandMark landMark : landmarkList) {
               tmp = distance(landMark.getLat(), landMark.getLng(), lat, lng, 'K');
               if (tmp <= shortestDistance) {
                   shortestIndex = idx;
                   shortestDistance = tmp;
               }
               idx ++;
           }

           return landmarkList.get(shortestIndex);
        } else {
            return null;
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        String address = getAddress(lat, lng);
        String name = address.split(" ")[0];
        Log.e("LHC", "Address" + address);
        tts.sspeak("현재 위치는 "+ getAddress(lat, lng)+ " 입니다.");
        Log.e("LHC", "*********** on LocationChanged ***********");

        // 현재 위치 미리 저장
        currentLandMark = new LandMark(lat, lng, address, name);
        // 가장 가까운 랜드마크 찾기
        shortestLandMark = findShortestLandmark(lat, lng);
        selectAllLandMarkLists();
        printLandMarkLists();

        if (register_landmark == 1) {
            currentLandMark.setName(lm_name);
            landMarkDBHandler.insert(currentLandMark);
            register_landmark = 0;
        }

        if (check_dist_dir_land == 1) {
            calcDDFrmShortestLM();
            check_dist_dir_land = 0;
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

    /**
     *  fetch landmark data from DB
     */
    private void selectAllLandMarkLists() {
        landmarkList = landMarkDBHandler.select();
    }

    /**
     * print all the landmarks
     */
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
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        if (horizontal_index == CURRENT_LOCATION) {
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
            if (no_degree == NORMAL_MODE) {
                tts.sspeak("현재 위치를 랜드마크로 등록합니다.");
                tts.sspeak("등록할 이름을 작성하고 다시 클릭하세요.");

                // Turn on the braille keyboard
                input_etext.setText("");
                input_etext.requestFocus();
                bKey.clearString();
                bKey.turnOnBrailleKB();
                // Next level
                no_degree = LM_REG_MODE;
            } else if (no_degree == LM_REG_MODE) {
                // Turn off the keyboard
                lm_name = input_etext.getText().toString();
                input_etext.setText("");
                bKey.clearString();

                // 이 경우에는 랜드마크 등록 기능을 선택한 후에 주소를
                // 검색하였을 경우이다. DB와 list를 갱신 후에 register_landmark를 원래 값으로 돌린다.
                // 따라서 이 이후에는 DB에 추가되지 않는다.
                if (currentLandMark != null) {
                    currentLandMark.setName(lm_name);
                    landMarkDBHandler.insert(currentLandMark); // add data into DB
                } else {    // 미리 저장된 값이 없을 경우 이벤트 강제 호출
                    register_landmark = 1;
                    lm.removeUpdates(this);
                    lm.requestLocationUpdates(locationProvider, 0, 0, this);
                }
                lm.removeUpdates(this);
                selectAllLandMarkLists();
                printLandMarkLists();
                // return to the level
                no_degree = NORMAL_MODE;
                tts.sspeak("랜드마크 "+lm_name+"이 등록되었습니다.");
                shortestLandMark =
                        findShortestLandmark(currentLandMark.getLat(),
                                            currentLandMark.getLng());
            }
        } else if (horizontal_index == REMOVE_LOCATION) {
            tts.ispeak("가까운 랜드마크를 제거합니다.");
            if (shortestLandMark != null) {
                landMarkDBHandler.delete(shortestLandMark.getLat(), shortestLandMark.getLng());
                shortestLandMark = null;
            } else {
                tts.ispeak("가까운 랜드마크가 존재하지 않습니다.");
            }
            selectAllLandMarkLists();
            printLandMarkLists();
        } else if (horizontal_index == CLOSE_LANDMARK) {
            tts.sspeak("가까운 랜드마크까지의 방향과 거리를 안내합니다.");
            //////////////////////////////////////////////
            /// 리스너 제거 후 다시 추가하면서 event 시작.
            //////////////////////////////////////////////

            if (shortestLandMark == null) {
                tts.ispeak("가까운 랜드마크가 없습니다.");
                check_dist_dir_land = 0;
                return;
            }

            if (currentLandMark != null) {
                Log.e("LHC", "Shortest Landmark:" + shortestLandMark.getAddress());
                calcDDFrmShortestLM();
            } else {
                check_dist_dir_land = 1;
                lm.removeUpdates(this);
                lm.requestLocationUpdates(locationProvider, 0, 0, this);
            }
        } else if (horizontal_index == GO_TO_MAINMENU) {
            tts.sspeak("메인 메뉴로 돌아갑니다.");
            landMarkDBHandler.close();
            Constants.MENU_LEVEL = Constants.MAIN_MENU_MODE;
        }
    }

    /**
     *  calculate distance and direction from the shortest landmark.
     */
    private void calcDDFrmShortestLM() {
        double curLat = currentLandMark.getLat();
        double curLng = currentLandMark.getLng();
        double distance = distance(curLat, curLng,
                shortestLandMark.getLat(),
                shortestLandMark.getLng(), 'K');
        String distStr = String.format("%.4f", distance);
        String direction =
                checkDirection(bearingP1toP2(curLat, curLng,
                        shortestLandMark.getLat(),
                        shortestLandMark.getLng()));
        tts.ispeak("가까운 랜드마크까지 " + direction +
                " 방향으로 " + distStr + "KM 거리에 있습니다");
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

    /**
     * calculate the distance between lat/longitude
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @param unit
     * @return
     */
    public double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) +
                    Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == 'K') { // KM
            dist = dist * 1.609344;
        } else if (unit == 'N') { // Mile
            dist = dist * 0.8684;
        }
        return (dist);
    }

    /**
     * converts decimal degrees to radians
     * @param deg
     * @return
     */
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /**
     * converts radians to decimal degrees
     * @param rad
     * @return
     */
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    /**
     * calculate bearing (0: North, 90: West, 180: South, 270: East)
     * @param P1_latitude
     * @param P1_longitude
     * @param P2_latitude
     * @param P2_longitude
     * @return
     */
    public short bearingP1toP2(double P1_latitude, double P1_longitude, double P2_latitude, double P2_longitude)
    {
        // 현재 위치 : 위도나 경도는 지구 중심을 기반으로 하는 각도이기 때문에 라디안 각도로 변환한다.
        double Cur_Lat_radian = P1_latitude * (3.141592 / 180);
        double Cur_Lon_radian = P1_longitude * (3.141592 / 180);
        // 목표 위치 : 위도나 경도는 지구 중심을 기반으로 하는 각도이기 때문에 라디안 각도로 변환한다.
        double Dest_Lat_radian = P2_latitude * (3.141592 / 180);
        double Dest_Lon_radian = P2_longitude * (3.141592 / 180);
        // radian distance
        double radian_distance = 0;
        radian_distance = Math.acos(Math.sin(Cur_Lat_radian) * Math.sin(Dest_Lat_radian)
                + Math.cos(Cur_Lat_radian) *
                Math.cos(Dest_Lat_radian) * Math.cos(Cur_Lon_radian - Dest_Lon_radian));

        // 목적지 이동 방향을 구한다.
        // (현재 좌표에서 다음 좌표로 이동하기 위해서는 방향을 설정해야 한다. 라디안값이다.
        double radian_bearing = Math.acos((Math.sin(Dest_Lat_radian) - Math.sin(Cur_Lat_radian)
                * Math.cos(radian_distance)) / (Math.cos(Cur_Lat_radian) * Math.sin(radian_distance)));
        // acos의 인수로 주어지는 x는 360분법의 각도가 아닌 radian(호도)값이다.
        double true_bearing = 0;
        if (Math.sin(Dest_Lon_radian - Cur_Lon_radian) < 0)
        {
            true_bearing = radian_bearing * (180 / 3.141592);
            true_bearing = 360 - true_bearing;
        }
        else
        {
            true_bearing = radian_bearing * (180 / 3.141592);
        }
        return (short)true_bearing;
    }

    /**
     * Check direction
     * @param bearing
     * @return
     */
    private String checkDirection(short bearing) {
        String direction = "";
        if (0 <= bearing && bearing < 20) {
            Log.e("LHC", "To landmark dir: North");
            return "북쪽";
        } else if (20 <= bearing && bearing < 70) {
            Log.e("LHC", "To landmark dir: North West");
            return "북서쪽";
        } else if (70 <= bearing && bearing < 110) {
            Log.e("LHC", "To landmark dir: West");
            return "서쪽";
        } else if (110 <= bearing && bearing < 160) {
            Log.e("LHC", "To landmark dir: South West");
            return "남서쪽";
        } else if (160 <= bearing && bearing < 200) {
            Log.e("LHC", "To landmark dir: South");
            return "남쪽";
        } else if (200 <= bearing && bearing < 250) {
            Log.e("LHC", "To landmark dir: South East");
            return "남동쪽";
        } else if (250 <= bearing && bearing < 290) {
            Log.e("LHC", "To landmark dir: East");
            return "동쪽";
        } else if (290 <= bearing && bearing < 340) {
            Log.e("LHC", "To landmark dir: North East");
            return "북동쪽";
        } else {
            Log.e("LHC", "To landmark dir: North");
            return "북쪽";
        }
    }


}
