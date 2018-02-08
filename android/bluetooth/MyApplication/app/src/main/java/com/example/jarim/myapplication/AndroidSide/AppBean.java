package com.example.jarim.myapplication.AndroidSide;

import java.util.ArrayList;

/**
 * Created by hochan on 2018-02-05.
 */

public class AppBean {
    private String name;
    private String intentName;
    private ArrayList<AppBean> subAppList;

    public AppBean() {

    }

    public AppBean(String _name, String _intentName) {
        name = _name;
        intentName = _intentName;
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
    public boolean start() { return true; }
}
