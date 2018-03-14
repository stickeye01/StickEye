package com.example.jarim.myapplication.AndroidSide;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.jarim.myapplication.BrailleKeyboard.BrailleKeyboard;
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
    private BrailleKeyboard bKey;

    private TextView menu_txt;

    public MultiDimensionMenu(TtsService _tts, Context _ctxt, BrailleKeyboard _bKey) {
        tts = _tts;
        mContext = _ctxt;
        mActivity = (Activity) mContext;
        bKey = _bKey;
        initializeMenu();
        menu_txt = mActivity.findViewById(R.id.md_menu);
    }

    private void initializeMenu() {
        vertical_index = 0;
        horizontal_index = 0;
        appList = new ArrayList<AppBean>();
        AppBean callApp = new AppBean("전화관련",
                "", tts, mContext, bKey);
        PhoneCallBean callDialApp = new PhoneCallBean("전화걸기",
                "", tts, mContext, bKey);
        PhoneRegisterBean callRegApp = new PhoneRegisterBean("전화번호등록",
                "", tts, mContext, bKey);
        PhoneBookBean phoneBook = new PhoneBookBean("전화번호부",
                "", tts, mContext, bKey);
        callApp.addSubItem(callDialApp);
        callApp.addSubItem(callRegApp);
        callApp.addSubItem(phoneBook);
        appList.add(callApp);
        AppBean msgApp = new AppBean("메세지", "", tts, mContext, bKey);
        MessageBookBean msgReadApp = new MessageBookBean("메시지 읽기",
                                                "", tts, mContext, bKey);
        MessageSendBean msgWriteApp = new MessageSendBean("메시지 쓰기",
                                                "", tts, mContext, bKey);
        msgApp.addSubItem(msgReadApp);
        msgApp.addSubItem(msgWriteApp);
        appList.add(msgApp);
        MP3Bean mp3App = new MP3Bean("MP3", "", tts, mContext, bKey);
        appList.add(mp3App);
        NavigationBean naviApp = new NavigationBean("네비게이션", "",
                                    tts, mContext, bKey);
        appList.add(naviApp);
    }

    private void initializeSetting() {
        bKey.turnOffBrailleKB();
        if (Constants.MENU_LEVEL == Constants.BRAILLE_CLICK_MODE)
            Constants.MENU_LEVEL = Constants.MAIN_MENU_MODE;
        if (Constants.MENU_LEVEL == Constants.MAIN_MENU_MODE)
            selectedApp = null;
    }

    /**
     *  move left.
     */
    public void left() {
        initializeSetting();
        if (Constants.MENU_LEVEL == Constants.MAIN_MENU_MODE ||
                Constants.MENU_LEVEL == Constants.BRAILLE_CLICK_MODE) {
            Log.e("LHC", "LEFT");
            horizontal_index--;
            if (horizontal_index < 0)
                horizontal_index = appList.size() - 1;
            tts.ispeak(appList.get(horizontal_index).getName());
            menu_txt.setText(appList.get(horizontal_index).getName());
        } else if (selectedApp != null){
            selectedApp.left();
        }
    }

    /**
     *  move right.
     */
    public void right() {
        initializeSetting();
        if (Constants.MENU_LEVEL == Constants.MAIN_MENU_MODE ||
                Constants.MENU_LEVEL == Constants.BRAILLE_CLICK_MODE) {
            Log.e("LHC", "RIGHT");
            horizontal_index++;
            if (horizontal_index > appList.size() - 1)
                horizontal_index = 0;
            tts.ispeak(appList.get(horizontal_index).getName());
            menu_txt.setText(appList.get(horizontal_index).getName());
        } else if (selectedApp != null){
            selectedApp.right();
        }
    }

    /**
     *  move top.
     */
    public void top() {
        initializeSetting();
        if (Constants.MENU_LEVEL == Constants.MAIN_MENU_MODE ||
                Constants.MENU_LEVEL == Constants.BRAILLE_CLICK_MODE) {
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
            menu_txt.setText(curItem.getName());
        } else if (selectedApp != null){
            selectedApp.top();
        }
    }

    /**
     *  move down.
     */
    public void down() {
        initializeSetting();
        if (Constants.MENU_LEVEL == Constants.MAIN_MENU_MODE ||
                Constants.MENU_LEVEL == Constants.BRAILLE_CLICK_MODE) {
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
            menu_txt.setText(curItem.getName());
        } else if (selectedApp != null){
            selectedApp.down();
        }
    }

    /**
     *  clicked.
     *  there are two modes: 1) app starts, 2) app input.
     */
    public void click() {
        // Main menu mode.
        if (Constants.MENU_LEVEL == Constants.MAIN_MENU_MODE) {
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
        // Sub menu mode.
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
