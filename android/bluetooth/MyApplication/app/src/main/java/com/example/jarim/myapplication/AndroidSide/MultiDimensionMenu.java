package com.example.jarim.myapplication.AndroidSide;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.jarim.myapplication.Constants;
import com.example.jarim.myapplication.R;
import com.example.jarim.myapplication.TtsService;

import java.util.ArrayList;

/**
 * Created by hochan on 2018-02-04.
 */

public class MultiDimensionMenu implements View.OnClickListener{
    private int vertical_index;
    private int horizontal_index;
    private ArrayList<AppBean> appList;
    private AppBean curItem;
    private TtsService tts;
    private Context mContext;
    private Activity mActivity;
    private AppBean selectedApp;
    public static int MENU_LEVEL = Constants.MAIN_MENU_MODE;
    private Button clickButton;

    public MultiDimensionMenu(TtsService _tts, Context _ctxt) {
        tts = _tts;
        mContext = _ctxt;
        mActivity = (Activity) mContext;
        initialize();
    }

    private void initialize() {
        clickButton = mActivity.findViewById(R.id.click2);
        clickButton.setOnClickListener(this);
        vertical_index = 0;
        horizontal_index = 0;
        appList = new ArrayList<AppBean>();
        AppBean callApp = new AppBean("전화관련",
                "", tts, mContext);
        PhoneCallBean callDialApp = new PhoneCallBean("전화걸기",
                "", tts, mContext);
        PhoneRegisterBean callRegApp = new PhoneRegisterBean("전화번호등록",
                "", tts, mContext);
        PhoneBookBean phoneBook = new PhoneBookBean("전화번호부",
                "", tts, mContext);
        callApp.addSubItem(callDialApp);
        callApp.addSubItem(callRegApp);
        callApp.addSubItem(phoneBook);
        appList.add(callApp);
        AppBean msgApp = new AppBean("메세지", "", tts, mContext);
        MessageBookBean msgReadApp = new MessageBookBean("메시지 읽기",
                                                "", tts, mContext);
        MessageSendBean msgWriteApp = new MessageSendBean("메시지 쓰기",
                                                "", tts, mContext);
        msgApp.addSubItem(msgReadApp);
        msgApp.addSubItem(msgWriteApp);
        appList.add(msgApp);
        MP3Bean mp3App = new MP3Bean("MP3", "", tts, mContext);
        appList.add(mp3App);
    }

    /**
     *  move left.
     */
    public void left() {
        if (MENU_LEVEL == Constants.MAIN_MENU_MODE) {
            Log.e("LHC", "LEFT");
            horizontal_index--;
            if (horizontal_index < 0)
                horizontal_index = appList.size() - 1;
            tts.ispeak(appList.get(horizontal_index).getName());
        } else if (selectedApp != null){
            selectedApp.left();
        }
    }

    /**
     *  move right.
     */
    public void right() {
        if (MENU_LEVEL == Constants.MAIN_MENU_MODE) {
            Log.e("LHC", "RIGHT");
            horizontal_index++;
            if (horizontal_index > appList.size() - 1)
                horizontal_index = 0;
            tts.ispeak(appList.get(horizontal_index).getName());
        } else if (selectedApp != null){
            selectedApp.right();
        }
    }

    /**
     *  move top.
     */
    public void top() {
        if (MENU_LEVEL == Constants.MAIN_MENU_MODE) {
            Log.e("LHC", "TOP");
            curItem = appList.get(horizontal_index);
            if (curItem == null) {
                // EMPTY
                return;
            }
            vertical_index++;
            ArrayList<AppBean> subItems = curItem.getSubItem();
            if (subItems != null && vertical_index > subItems.size() - 1)
                vertical_index = 0;
            if (vertical_index >= 0 && subItems != null &&
                    vertical_index < subItems.size())
                curItem = subItems.get(vertical_index);
            tts.ispeak(curItem.getName());
        } else if (selectedApp != null){
            selectedApp.top();
        }
    }

    /**
     *  move down.
     */
    public void down() {
        if (MENU_LEVEL == Constants.MAIN_MENU_MODE) {
            Log.e("LHC", "DOWN");
            curItem = appList.get(horizontal_index);
            if (curItem == null) {
                // EMPTY
                return;
            }
            vertical_index--;
            ArrayList<AppBean> subItems = curItem.getSubItem();
            if (vertical_index < 0 && subItems != null)
                vertical_index = subItems.size() - 1;
            if (vertical_index >= 0 && subItems != null &&
                    vertical_index < subItems.size())
                curItem = subItems.get(vertical_index);
            tts.ispeak(curItem.getName());
        } else if (selectedApp != null){
            selectedApp.down();
        }
    }

    /**
     *  clicked.
     *  there are two modes: 1) app starts, 2) app input.
     */
    public void click() {
        if (MENU_LEVEL == Constants.MAIN_MENU_MODE) {
            selectedApp = appList.get(horizontal_index);
            if (selectedApp == null) return;
            ArrayList subItems = selectedApp.getSubItem();
            if (vertical_index >= 0 && subItems != null && vertical_index < subItems.size()) {
                // if there are submenus.
                selectedApp = selectedApp.getSubItem().get(vertical_index);
                selectedApp.start(null);
            } else if (vertical_index >= 0) {
                // if there is no subitem, but it can start itself.
                selectedApp.start(null);
            }
        } else if (selectedApp != null){
            selectedApp.click();
        }
    }

    @Override
    public void onClick(View view) {
        if (selectedApp != null) {
            selectedApp.clicked(view);
        }
    }
}
