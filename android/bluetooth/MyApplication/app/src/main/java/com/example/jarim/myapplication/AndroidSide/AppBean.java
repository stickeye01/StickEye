package com.example.jarim.myapplication.AndroidSide;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import com.example.jarim.myapplication.BrailleKeyboard.BrailleKeyboard;
import com.example.jarim.myapplication.R;
import com.example.jarim.myapplication.TtsService;

import java.util.ArrayList;

/**
 * Created by hochan on 2018-2-05.
 */

public class AppBean {
    private String name;
    private String intentName;
    private ArrayList<AppBean> subAppList;
    public BrailleKeyboard bKey;
    public TtsService tts;
    public Context mContext;
    public Activity mActivity;
    public TextView menu_txt;

    public AppBean() {

    }

    public AppBean(String _name, String _intentName, TtsService _tts, Context _ctx,
                    BrailleKeyboard _bKey) {
        name = _name;
        intentName = _intentName;
        tts = _tts;
        mContext = _ctx;
        mActivity = (Activity) mContext;
        bKey = _bKey;
        menu_txt = mActivity.findViewById(R.id.md_menu);
    }

    /**
     * Set a name of application.
     * @param _name
     */
    public void setName(String _name) {
        name = _name;
    }

    /**
     * Get a name of application.
     * @return
     */
    public String getName() {
        if (name == null || name.equals(""))
            return "NULL";
        return name;
    }

    /**
     * Set an intent name of the application.
     * Call the application by using the intent name.
     * @param _name
     */
    public void setIntentName(String _name) {
        intentName = _name;
    }

    /**
     * Get an intent name of the application.
     * @return
     */
    public String getIntentName() {
        if (intentName == null || intentName.equals(""))
            return "NULL";
        return intentName;
    }

    /**
     * Add new sub application onto the sub array list.
     * @param _subItem
     */
    public void addSubItem(AppBean _subItem) {
        if (subAppList == null)
            subAppList = new ArrayList<AppBean>();
        subAppList.add(_subItem);
    }

    /**
     * Get array list that has sub applications.
     * @return
     */
    public ArrayList<AppBean> getSubItem() {
        return subAppList;
    }

    /**
     * Start outer application (need to override).
     * @return
     */
    public boolean start(Object o) { return true; }

    /**
     * Select a menu
     */
    public void clicked(Object o) { }

    /**
     * Direction of menu.
     */
    public void top() {}
    public void down() {}
    public void left() {}
    public void right() {}
    public void click() {}
}
