package com.example.jarim.myapplication;

/**
 * Created by hochan on 2018-01-06.
 */

public class Constants {
    public static final int MSG_DEVICE_COUNT = 1;
    public static final int MSG_DEVICD_INFO = 11;
    public static final int MSG_READ_DATA_COUNT = 21;
    public static final int MSG_READ_DATA = 22;
    public static final int MSG_DIALOG_HIDE = 23;
    public static final int MSG_USB_CONN_SUCCESS = 24;
    public static final int MSG_USB_NOTIFY = 25;
    public static final int MSG_SERIAL_ERROR = -1;
    public static final int MSG_FATAL_ERROR_FINISH_APP = -2;
    public static final int MSG_CONN_FAIL = -3;
    public static final String ACTION_USB_PERMISSION ="com.example.jarim.USB_PERMISSION";
    public static String macAddr = "";
    public static final int TIMEOUT = 30000;

    public static final int NAME_REGISTER_STAGE = 0;
    public static final int PHONE_NUM_REG_STAGE = 1;
    public static final int REGISTER_FINAL_STAGE = 2;
    public static final int PHONE_NUMBER_STAGE = 3;
    public static final int CALLING_STAGE = 4;
}
