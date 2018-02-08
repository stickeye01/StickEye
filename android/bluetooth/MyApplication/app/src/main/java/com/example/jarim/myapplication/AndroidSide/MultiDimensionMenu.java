package com.example.jarim.myapplication.AndroidSide;

import android.content.Context;
import android.util.Log;

import com.example.jarim.myapplication.TtsService;

import java.util.ArrayList;

/**
 * Created by hochan on 2018-02-04.
 */

public class MultiDimensionMenu {
    private int vertical_index;
    private int horizontal_index;
    private ArrayList<AppBean> appList;
    private AppBean curItem;
    private TtsService tts;
    private Context mContext;

    public MultiDimensionMenu(TtsService _tts, Context _ctxt) {
        tts = _tts;
        mContext = _ctxt;
        initialize();
    }

    private void initialize() {
        vertical_index = 0;
        horizontal_index = 0;
        appList = new ArrayList<AppBean>();
        PhoneCallBean callApp = new PhoneCallBean("전화번호부",
                "", tts, mContext);
        PhoneCallBean callDialApp = new PhoneCallBean("전화걸기",
                "", tts, mContext);
        PhoneCallBean callRegApp = new PhoneCallBean("전화번호등록",
                "", tts, mContext);
        callApp.addSubItem(callDialApp);
        callApp.addSubItem(callRegApp);
        appList.add(callApp);
        AppBean msgApp = new AppBean("메세지", "", tts, mContext);
        AppBean msgReadApp = new AppBean("메시지 읽기", "", tts, mContext);
        AppBean msgWriteApp = new AppBean("메시지 쓰기", "", tts, mContext);
        msgApp.addSubItem(msgReadApp);
        msgApp.addSubItem(msgWriteApp);
        appList.add(msgApp);
        AppBean mp3App = new AppBean("MP3", "", tts, mContext);
        appList.add(mp3App);
    }

    public void left() {
        Log.e("LHC", "LEFT");
        horizontal_index --;
        if (horizontal_index < 0)
            horizontal_index = appList.size()-1;
        tts.ispeak(appList.get(horizontal_index).getName());
    }

    public void right() {
        Log.e("LHC", "RIGHT");
        horizontal_index ++;
        if (horizontal_index > appList.size()-1)
            horizontal_index = 0;
        tts.ispeak(appList.get(horizontal_index).getName());
    }

    public void top() {
        Log.e("LHC", "TOP");
        curItem = appList.get(horizontal_index);
        if (curItem == null) {
            // EMPTY
            return ;
        }
        vertical_index++;
        ArrayList<AppBean> subItems = curItem.getSubItem();
        if (vertical_index > subItems.size()-1)
            vertical_index = 0;
        curItem = subItems.get(vertical_index);
        tts.ispeak(curItem.getName());
    }

    public void down() {
        Log.e("LHC", "DOWN");
        curItem = appList.get(horizontal_index);
        if (curItem == null) {
            // EMPTY
            return ;
        }
        vertical_index --;
        ArrayList<AppBean> subItems = curItem.getSubItem();
        if (vertical_index < 0)
            vertical_index = subItems.size()-1;
        curItem = subItems.get(vertical_index);
        tts.ispeak(curItem.getName());
    }

    public void click() {
        AppBean selectedApp = appList.get(horizontal_index);
        if (selectedApp == null) return;
        selectedApp = selectedApp.getSubItem().get(vertical_index);
        selectedApp.start(null);
    }
}
